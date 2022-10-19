package com.contract.modules.contract.service.impl

import cn.hutool.core.util.IdUtil
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSON.parseArray
import com.alibaba.fastjson.JSONArray
import com.contract.constants.Constants
import com.contract.modules.asset.service.UserAssetService
import com.contract.modules.contract.domain.KLineDataQueryParam
import com.contract.modules.contract.domain.LotteryRecord
import com.contract.modules.contract.domain.Order
import com.contract.modules.contract.enums.ContractTypeEnum
import com.contract.modules.contract.enums.SymbolsEnum
import com.contract.modules.contract.repository.LotteryRecordRepository
import com.contract.modules.contract.repository.OrderRepository
import com.contract.modules.contract.service.LotteryRecordService
import com.contract.modules.contract.service.dto.LotteryRecordDto
import com.contract.modules.contract.service.mapstruct.LotteryRecordMapper
import com.contract.modules.contract.utils.CryptoUtils.getKLineData
import com.contract.modules.contract.utils.CryptoUtils.getSymbolTickers
import com.contract.modules.system.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.sql.Timestamp
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created with IntelliJ IDEA.
 *
 * @author: Ivan
 * @date: 2022/10/12
 * @Version: 1.0
 * @Description:
 */
@Service
class LotteryRecordServiceImpl : LotteryRecordService {
    @Autowired
    private lateinit var lotteryRecordRepository: LotteryRecordRepository
    @Autowired
    private lateinit var lotteryRecordMapper: LotteryRecordMapper
    @Autowired
    private lateinit var redisTemplate: RedisTemplate<String,String>
    @Autowired
    private lateinit var orderRepository: OrderRepository
    @Autowired
    private lateinit var userAssetService: UserAssetService

    private val list = listOf<String>("30","60","180")

    override fun createContract(price : BigDecimal) {
        val contractList = mutableListOf<LotteryRecord>()
        for (second in list) {
            SymbolsEnum.values().forEach {
                val volume = IdUtil.getSnowflake().nextIdStr()
                val lotteryRecord = LotteryRecord()
                lotteryRecord.markPrice= price
                lotteryRecord.status="0"
                lotteryRecord.volume=volume
                lotteryRecord.type=second
                lotteryRecord.symbol=it.name
                contractList.add(lotteryRecord)
                redisTemplate.opsForValue().set("contract-$volume", JSON.toJSONString(lotteryRecord),30,TimeUnit.SECONDS)
            }
        }
        lotteryRecordRepository.saveAllAndFlush(contractList)
    }

    override fun lottery( second: String) {
        val orders: List<Order> = orderRepository.findListBySecond(second)
        var lotteryRecords = lotteryRecordRepository.findAll()
        val contractTypeEnum = ContractTypeEnum.getBySecond(second)
        lotteryRecords = lotteryRecords.filter { it.type.equals(second) && it.status.equals(contractTypeEnum?.code)}
        val lotteryRecordMap = lotteryRecords.associateBy { it.id }

        orders.forEach {
            val kLineData = getKLineData(
                KLineDataQueryParam(
                    Constants.Cypto.BTC_USDT,
                    "1s",
                    it.openingTime?.time?.plus(3000),
                    it.openingTime?.time?.plus(3000),
                    30
                )
            )
            val settlementPrice = BigDecimal(parseArray(parseArray(kLineData)[0].toString())[1].toString())
            lotteryRecordRepository.lottery(Timestamp.from(Date().toInstant()),second,
                settlementPrice,"1",it.userId,"2")
            val lotteryRecord = lotteryRecordMap[it.lotteryRecordId]
            var flag : Boolean = false
            if (settlementPrice > (lotteryRecord?.markPrice ?: BigDecimal.ZERO) && it.positionType == "0"){
                flag = true
            }
            it.status = "1"
            if (flag){
                it.status = "2"
                userAssetService.addBalance(it.userId,it.betAmount?.multiply(contractTypeEnum?.odds))
            }
        }
        orderRepository.saveAll(orders)
    }

    override fun lockUp() {
        lotteryRecordRepository.lockUp()
    }

    override fun deleteForNotUsed() {
        lotteryRecordRepository.deleteForNotUsed()
    }

    /**
     * 获取指定交易对的期号和倒计时
     */
    override fun getNewVolume(symbol: String): LotteryRecordDto? {
        val recordDto = lotteryRecordMapper.toDto(lotteryRecordRepository.findFirstBySymbolAndStatus(symbol, "0"))
        recordDto.second = redisTemplate.opsForValue().operations.getExpire("contract-${recordDto.volume}")
        return recordDto
    }
}