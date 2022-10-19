package com.contract.modules.contract.service.dto

import com.contract.base.BaseDTO
import io.swagger.annotations.ApiModelProperty
import java.math.BigDecimal
import java.sql.Timestamp

/**
 * Created with IntelliJ IDEA.
 *
 * @author: Ivan
 * @date: 2022/10/12
 * @Version: 1.0
 * @Description:
 */
class LotteryRecordDto(): java.io.Serializable {

    var id: Long? = null

    var markPrice: BigDecimal? = null

    var strikePrice: BigDecimal? = null

    var volume: String? = null

    var drawDate: Timestamp? = null

    var createTime: Timestamp? = null

    var symbol: String? = null

    var status: String? = null

    var type: String? = null

    var second: Long? = null
}