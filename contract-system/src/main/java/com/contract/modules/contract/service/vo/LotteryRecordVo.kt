package com.contract.modules.contract.service.vo

import com.contract.modules.contract.service.dto.LotteryRecordDto
import io.swagger.annotations.ApiModelProperty
import org.hibernate.annotations.CreationTimestamp
import java.math.BigDecimal
import java.sql.Timestamp

/**
 * Created with IntelliJ IDEA.
 * @author: Ivan
 * @date: 2022/10/19
 * @Version: 1.0
 * @Description:
 */
class LotteryRecordVo {
    @ApiModelProperty("标记价格")
    var markPrice: BigDecimal? = null
    @ApiModelProperty("行权价格")
    var strikePrice: BigDecimal? = null
    @ApiModelProperty("期号")
    var volume: String? = null
    @ApiModelProperty("建仓类型 0 -> 看跌 1 -> 看涨")
    var positionType: String? = null
    @ApiModelProperty("下注金额")
    var betAmount: BigDecimal? = null
    @ApiModelProperty("状态 0 -> 未开奖 1 -> 输 2 -> 赢")
    var status: String? = null
    @ApiModelProperty("周期")
    var second: String? = null
    @ApiModelProperty("创建时间")
    var createTime: Timestamp ?= null
}