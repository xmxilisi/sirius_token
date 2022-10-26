package com.contract.modules.contract.service.dto

import com.contract.modules.contract.domain.LotteryRecord
import io.swagger.annotations.ApiModelProperty
import java.math.BigDecimal
import java.sql.Timestamp

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
    var betAmount: BigDecimal? = null

    @ApiModelProperty("建仓类型 0 -> 看跌 1 -> 看涨")
    var positionType: String? = null

    @ApiModelProperty("期号")
    var volume: String? = null

    @ApiModelProperty("开仓时间")
    var openingTime: Timestamp? = null

    @ApiModelProperty("用户id")
    var userId: Long? = null

    @ApiModelProperty("状态 0 -> 未开奖 1 -> 输 2 -> 赢")
    var status: String? = null

    var second: String? = null

    @ApiModelProperty("开奖记录")
    var lotteryRecord: LotteryRecord?=null

    @ApiModelProperty(value = "创建时间", hidden = true)
    var createTime :Timestamp?=null
}