package com.contract.modules.contract.service.impl

import com.contract.modules.asset.service.UserAssetService
import com.contract.modules.contract.domain.LotteryRecord
import com.contract.modules.contract.domain.Order
import com.contract.modules.contract.repository.LotteryRecordRepository
import com.contract.modules.contract.repository.OrderRepository
import com.contract.modules.contract.service.LotteryRecordService
import com.contract.modules.contract.service.OrderService
import com.contract.modules.contract.service.bo.OrderBo
import com.contract.utils.SecurityUtils
import lombok.RequiredArgsConstructor
import lombok.extern.slf4j.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.lang.Boolean.*
import java.sql.Timestamp
import java.util.Date

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
    private lateinit var orderRepository: OrderRepository
    @Autowired
    private lateinit var userAssetService: UserAssetService


    /**
     * 下单
     *
     * @param bo
     */
    override fun placeAnOrder(bo: OrderBo) {
//        val userId: Long = SecurityUtils.getCurrentUserId()
        val userId = 2L
        val pay: Boolean = userAssetService.pay(userId, bo.betAmount)
        if (TRUE == pay) {
            val recordDto = bo.symbol?.let { lotteryRecordService.getNewVolume(it) }
            val order = Order()
            order.openingTime = Timestamp(Date().time)
            order.second = bo.second
            order.positionType = bo.positionType
            order.userId = userId
            order.status = "0"
            order.volume = recordDto?.volume
            order.betAmount = bo.betAmount
            order.lotteryRecordId = recordDto?.id
            orderRepository.save<Order>(order)
        }
    }
}