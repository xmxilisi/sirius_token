package com.contract.modules.contract.service

import com.contract.modules.contract.service.bo.OrderBo

/**
 * Created with IntelliJ IDEA.
 *
 * @author: Ivan
 * @date: 2022/10/13
 * @Version: 1.0
 * @Description:
 */
interface OrderService {
    /**
     * 下单
     * @param bo
     */
    fun placeAnOrder(bo: OrderBo)

    fun getOrderDetail(): Map<String,Any>
}