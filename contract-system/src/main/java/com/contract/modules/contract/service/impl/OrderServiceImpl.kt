package com.contract.modules.contract.service.impl

import com.alibaba.fastjson.JSON
import com.contract.modules.asset.service.UserAssetService
import com.contract.modules.contract.domain.Order
import com.contract.modules.contract.repository.LotteryRecordRepository
import com.contract.modules.contract.repository.OrderRepository
import com.contract.modules.contract.service.LotteryRecordService
import com.contract.modules.contract.service.OrderService
import com.contract.modules.contract.service.bo.OrderBo
import com.contract.modules.contract.service.mapstruct.LotteryRecordMapper
import com.contract.modules.contract.service.mapstruct.OrderMapper
import com.contract.utils.SecurityUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.lang.Boolean.*
import java.util.concurrent.TimeUnit
import kotlin.streams.toList

/**
 * Created with IntelliJ IDEA.
 *
 * @author: Ivan
 * @date: 2022/10/13
 * @Version: 1.0
 * @Description:
 */
@Service
class OrderServiceImpl : OrderService {
    @Autowired
    private lateinit var lotteryRecordRepository: LotteryRecordRepository
    @Autowired
    private lateinit var lotteryRecordService: LotteryRecordService
    @Autowired
    private lateinit var lotteryRecordMapper: LotteryRecordMapper
    @Autowired
    private lateinit var orderRepository: OrderRepository
    @Autowired
    private lateinit var orderMapper: OrderMapper
    @Autowired
    private lateinit var userAssetService: UserAssetService
    @Autowired
    private lateinit var redisTemplate: RedisTemplate<String,String>


    /**
     * 下单
     *
     * @param bo
     */
    override fun placeAnOrder(bo: OrderBo) {
        val userId: Long = SecurityUtils.getCurrentUserId()
        val pay: Boolean = userAssetService.pay(userId, bo.betAmount)
        if (TRUE == pay) {
            val recordDto = bo.symbol?.let { lotteryRecordService.getNewVolume(it,bo.second.let { "30" }) }
            val order = Order()
            order.second = bo.second
            order.positionType = bo.positionType
            order.userId = userId
            order.status = "0"
            order.volume = recordDto?.volume
            order.betAmount = bo.betAmount
            order.lotteryRecord = lotteryRecordMapper.toEntity(recordDto)
            orderRepository.save<Order>(order)
            redisTemplate.opsForValue().set("contract-${order.volume}-countdown", JSON.toJSONString(order), (order.second?.toLong() ?: 0L) + redisTemplate.getExpire("contract-${order.volume}"), TimeUnit.SECONDS)
        }
    }

    override fun getOrderDetail(volume: String): Map<String, Any> {
        val map = mutableMapOf<String,Any>()
        val order:List<Order> = orderRepository.findListByVolume(volume)
        val orderDtoList = order.stream().map { orderMapper.toDto(it) }.toList()
        map["second"] = redisTemplate.getExpire("contract-${volume}-countdown")
        map["type"] = orderDtoList[0].second.toString()
        map["order"] = orderDtoList
        return map;
    }
}