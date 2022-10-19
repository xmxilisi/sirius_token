package com.contract.modules.contract.service

import java.math.BigDecimal
import com.contract.modules.contract.service.dto.LotteryRecordDto

/**
 * Created with IntelliJ IDEA.
 *
 * @author: Ivan
 * @date: 2022/10/12
 * @Version: 1.0
 * @Description:
 */
interface LotteryRecordService {
    fun createContract(price: BigDecimal)
    fun lottery(second: String)
    fun lockUp()
    fun deleteForNotUsed()

    /**
     * 获取指定交易对的期号和倒计时
     */
    fun getNewVolume(symbol: String): LotteryRecordDto?
}