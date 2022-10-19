package com.contract.modules.contract.service.bo

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
class OrderBo {
    @ApiModelProperty("交易对")
    var symbol: String? = null

    @ApiModelProperty("下注金额")
    var betAmount: BigDecimal? = null

    @ApiModelProperty("开仓时间")
    var second: String? = null

    @ApiModelProperty("建仓类型 0 -> 看跌 1 -> 看涨")
    var positionType: String? = null
}