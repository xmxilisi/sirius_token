package com.contract.modules.contract.domain

import com.contract.base.BaseEntity
import io.swagger.annotations.ApiModelProperty
import lombok.Getter
import lombok.Setter
import java.io.Serializable
import java.math.BigDecimal
import java.sql.Timestamp
import javax.persistence.*

/**
 * Created with IntelliJ IDEA.
 * 投注记录
 * @author: Ivan
 * @date: 2022/10/11
 * @Version: 1.0
 * @Description:
 */
@Entity
@Getter
@Setter
@Table(name = "sc_order")
class Order : BaseEntity(), Serializable {
    @Id
    @Column(name = "order_id")
    @ApiModelProperty(value = "ID", hidden = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    @ApiModelProperty("下注金额")
    var betAmount: BigDecimal? = null

    @Column(columnDefinition = "CHAR(1)")
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

    @ApiModelProperty("开奖id")
    var lotteryRecordId: Long? = null

    @ApiModelProperty("周期")
    var second: String? = null
}