package com.contract.modules.contract.domain

import io.swagger.annotations.ApiModelProperty
import java.math.BigDecimal
import lombok.ToString
import org.hibernate.annotations.CreationTimestamp
import lombok.Getter
import lombok.Setter
import java.sql.Timestamp
import javax.persistence.*

/**
 * Created with IntelliJ IDEA.
 * 开奖记录
 * @author: Ivan
 * @date: 2022/10/11
 * @Version: 1.0
 * @Description:
 */
@Getter
@Setter
@Entity
@ToString
@Table(name = "sc_lottery_record")
class LotteryRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lottery_record_id")
    @ApiModelProperty(value = "ID", hidden = true)
    var id: Long? = null

    @ApiModelProperty("标记价格")
    var markPrice: BigDecimal? = null

    @ApiModelProperty("行权价格")
    var strikePrice: BigDecimal? = null

    @ApiModelProperty("期号")
    var volume: String? = null

    @ApiModelProperty(value = "开奖时间")
    var drawDate: Timestamp? = null

    @CreationTimestamp
    @ApiModelProperty(value = "开奖时间")
    var createTime: Timestamp? = null

    @ApiModelProperty("交易对")
    var symbol: String? = null

    @ApiModelProperty("状态 0 -> 未锁仓 1 -> 锁仓 2 -> 已开奖")
    var status: String? = null

    @ApiModelProperty("类型")
    var type: String? = null

    @ApiModelProperty(value = "开始时间")
    var startTime: Timestamp? = null

    @ApiModelProperty(value = "结束时间")
    var endTime: Timestamp? = null
}