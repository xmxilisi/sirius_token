package com.contract.modules.contract.service.impl

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSON.parseArray
import com.contract.constants.Constants.Cypto.BTC_USDT
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
import com.contract.modules.contract.service.dto.OrderDto
import com.contract.modules.contract.service.mapstruct.LotteryRecordMapper
import com.contract.modules.contract.service.mapstruct.OrderMapper
import com.contract.modules.contract.service.vo.LotteryRecordVo
import com.contract.modules.contract.utils.CryptoUtils.getKLineData
import com.contract.modules.contract.utils.CryptoUtils.getSymbolTickers
import com.contract.modules.contract.utils.CryptoUtils.getSymbolsPrice
import com.contract.utils.DateUtil
import com.contract.utils.SecurityUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.sql.Timestamp
import java.time.LocalDateTime
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
    private lateinit var redisTemplate: RedisTemplate<String, String>

    @Autowired
    private lateinit var orderRepository: OrderRepository

    @Autowired
    private lateinit var userAssetService: UserAssetService

    @Autowired
    private lateinit var orderMapper: OrderMapper

    private val list = listOf("30", "60", "180")

    override fun createContract() {
        val contractList = mutableListOf<LotteryRecord>()
        val symbolsPrice = getSymbolsPrice()
        for (second in list) {
            var i : Int = 0
            val startTime:Timestamp = Timestamp.from(Date().toInstant().plusSeconds(30L))
            val endTime = Timestamp(startTime.time + second.toLong() * 1000)
            SymbolsEnum.values().forEach {
                val volume = DateUtil.localDateTimeFormat(LocalDateTime.now(),DateUtil.DFY_MD_HMS_SP) + second + if (i < 10) "0$i" else i
                val lotteryRecord = LotteryRecord()
                lotteryRecord.markPrice = symbolsPrice[it.name]
                lotteryRecord.status = "0"
                lotteryRecord.volume = volume
                lotteryRecord.type = second
                lotteryRecord.symbol = it.name
                lotteryRecord.startTime = startTime
                lotteryRecord.endTime = endTime
                contractList.add(lotteryRecord)
                redisTemplate.opsForValue().set("contract-$volume", JSON.toJSONString(lotteryRecord), 30, TimeUnit.SECONDS)
                i++
            }
        }
        lotteryRecordRepository.saveAllAndFlush(contractList)
    }

    override fun lottery(second: String) {
        val lotteryRecords = lotteryRecordRepository.findAll()
        var mutableList = lotteryRecordMapper.toDto(lotteryRecords)
        val contractTypeEnum = ContractTypeEnum.getBySecond(second)
        mutableList = mutableList.filter { it.type.equals(second) && it.status.equals("1") && Date().time - (it.createTime?.time ?: Date().time) >= (second.toLong() * 1000L + redisTemplate.getExpire("contract-$it.volume")) }
        mutableList.forEach {
            val orderList = orderRepository.findListByVolume(it.volume)
            val dtoMutableList = orderMapper.toDto(orderList)
            val orderListByUpdate = mutableListOf<OrderDto>()
            val time =  it.startTime?.time?.plus((second.toLong()+redisTemplate.getExpire("contract-$it.volume"))*1000);
            val kLineData = getKLineData(KLineDataQueryParam(BTC_USDT, "1s", time, time, 30))
            val settlementPrice = try {
                BigDecimal(parseArray(parseArray(kLineData)[0].toString())[1].toString())
            } catch (e: Exception) {
                getSymbolTickers(BTC_USDT)
            }
            lotteryRecordRepository.lottery(Timestamp(Date().time), settlementPrice, "2", it.id, "1",second)
            dtoMutableList.forEach { order ->
                order.status = "1"
                if ((settlementPrice > (it?.markPrice ?: BigDecimal.ZERO) && order.positionType == "1") || (settlementPrice < (it?.markPrice ?: BigDecimal.ZERO) && order.positionType == "0")) {
                    order.status = "2"
                    userAssetService.addBalance(order.userId, order.betAmount?.multiply(contractTypeEnum?.odds))
                }
                orderListByUpdate.add(order)
            }
            orderRepository.saveAllAndFlush(orderMapper.toEntity(orderListByUpdate))
        }
    }

    override fun lockUp() {
        val recordList = lotteryRecordRepository.lockUp()
        val mutableList = lotteryRecordMapper.toDto(recordList)
        val time = Timestamp(Date().time)
        mutableList.forEach {
            it.status = "1"
        }
        lotteryRecordRepository.saveAllAndFlush(lotteryRecordMapper.toEntity(mutableList))
    }

    override fun deleteForNotUsed() {
        lotteryRecordRepository.deleteForNotUsed()
    }

    /**
     * 获取指定交易对的期号和倒计时
     */
    override fun getNewVolume(symbol: String, second: String): LotteryRecordDto? {
        val recordDto = lotteryRecordMapper.toDto(lotteryRecordRepository.findFirstBySymbolAndStatusAndTypeOrderByCreateTime(symbol, "0",second))
        recordDto.second = redisTemplate.getExpire("contract-${recordDto.volume}")
        return recordDto
    }

    override fun getTheLotteryRecord(second: String, symbol: String): List<LotteryRecordVo> {
        val userId = SecurityUtils.getCurrentUserId()
        val order: List<Order> = orderRepository.findListByUserIdAndSecondOrderByCreateTimeDesc(userId,second);
        var mutableList = orderMapper.toDto(order)
        val list = mutableListOf<LotteryRecordVo>()
        mutableList = mutableList.filter { it.lotteryRecord?.symbol.equals(symbol) && !it.status.equals("0")}
        mutableList.forEach {
            val vo = LotteryRecordVo()
            vo.markPrice = it.lotteryRecord?.markPrice
            vo.strikePrice = it.lotteryRecord?.strikePrice
            vo.volume = it.volume
            vo.positionType = it.positionType
            vo.second = it.second
            vo.betAmount = it.betAmount
            vo.status = it.status
            vo.createTime = it.createTime
            list.add(vo)
        }
        return list
    }

    override fun getBettingHistory(second: String): Any {
        val userId = SecurityUtils.getCurrentUserId()
        val order: List<Order> = orderRepository.findListByUserIdAndSecond(userId,second);
        val mutableList = orderMapper.toDto(order)
        val list = mutableListOf<LotteryRecordVo>()
        mutableList.stream().map {
            val vo = LotteryRecordVo()
            vo.markPrice = it.lotteryRecord?.markPrice
            vo.strikePrice = it.lotteryRecord?.strikePrice
            vo.volume = it.volume
            vo.positionType = it.positionType
            list.add(vo)
        }
        return list
    }
}