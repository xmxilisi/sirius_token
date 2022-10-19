package com.contract.modules.contract.enums

import java.math.BigDecimal

/**
 * Created with IntelliJ IDEA.
 * @author: Ivan
 * @date: 2022/10/17
 * @Version: 1.0
 * @Description:
 */
enum class ContractTypeEnum (val code:String,val second:String,val odds: BigDecimal,val des: String){
    UN_LOCKED("0","30", BigDecimal("0.85"), "未锁仓"),
    LOCKUP("1", "60", BigDecimal("0.90"),"锁仓"),
    LOTTERY("2","180", BigDecimal("0.95"), "开奖");

    companion object {
        fun getByCode(value: String) = values().find { it.code == value }
        fun getBySecond(value: String) = values().find { it.second == value }
        fun getByDes(value: String) = values().find { it.des == value }
        fun getByOdds(value: BigDecimal) = values().find { it.odds.compareTo(value) == 0 }
    }
}