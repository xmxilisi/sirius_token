package com.contract.modules.contract.rest

import com.contract.annotation.rest.AnonymousGetMapping
import com.contract.modules.contract.repository.OrderRepository
import com.contract.modules.contract.service.OrderService
import com.contract.modules.contract.service.bo.OrderBo
import com.contract.modules.contract.utils.log
import com.contract.utils.R
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Created with IntelliJ IDEA.
 *
 * @author: Ivan
 * @date: 2022/10/13
 * @Version: 1.0
 * @Description:
 */
@RestController
@Api(tags = ["app端:订单模块"])
@RequestMapping("/api/order")
class OrderController {
    @Autowired
    private lateinit var orderService: OrderService


    @ApiOperation("下单")
    @PostMapping(value = ["/placeAnOrder"])
    fun placeAnOrder(@RequestBody bo: OrderBo): R<Any> {
        orderService.placeAnOrder(bo)
        return R.ok()
    }

    @ApiOperation("获取订单详情")
    @GetMapping("/getOrderDetail")
    fun getOrderDetail(): R<Map<String, Any>> {
        return R.ok(orderService.getOrderDetail())
    }

}