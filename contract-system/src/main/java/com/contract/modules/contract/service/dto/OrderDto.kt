package com.contract.modules.contract.service.dto

import io.swagger.annotations.ApiModelProperty
import java.math.BigDecimal

/**
 * Created with IntelliJ IDEA.
 *
 * @author: Ivan
 * @date: 2022/10/13
 * @Version: 1.0
 * @Description:
 */
class OrderDto {
    private val id: Long? = null

    @ApiModelProperty("下注金额")
    private val betAmount: BigDecimal? = null

    @ApiModelProperty("建仓类型 0 -> 看跌 1 -> 看涨")
    private val positionType: String? = null

    @ApiModelProperty("期号")
    private val volume: String? = null

    @ApiModelProperty("开仓时间")
    private val openingTime: String? = null

    @ApiModelProperty("用户id")
    private val userId: Long? = null

    @ApiModelProperty("状态 0 -> 未开奖 1 -> 输 2 -> 赢")
    private val status: String? = null

    @ApiModelProperty("开奖记录id")
    private val lotteryRecordId: Long? = null
}