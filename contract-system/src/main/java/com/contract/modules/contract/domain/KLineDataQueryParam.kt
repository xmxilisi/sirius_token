package com.contract.modules.contract.domain

/**
 * Created with IntelliJ IDEA.
 * @author: Ivan
 * @date: 2022/10/17
 * @Version: 1.0
 * @Description:
 */
class KLineDataQueryParam {
    var symbol:	String?= null

    /**
     * s -> 秒; m -> 分钟; h -> 小时; d -> 天; w -> 周; M -> 月
     */
    var interval:String?= null
    var startTime:Long?=null
    var endTime:Long?= null
    var limit:Int?= null

    constructor(symbol: String?, interval: String?, startTime: Long?, endTime: Long?, limit: Int?) {
        this.symbol = symbol
        this.interval = interval
        this.startTime = startTime
        this.endTime = endTime
        this.limit = limit
    }
    constructor(symbol: String?, interval: String?, limit: Int?) {
        this.symbol = symbol
        this.interval = interval
        this.limit = limit
    }

    constructor()
}
