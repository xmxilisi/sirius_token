package com.contract.modules.quartz.task

import com.contract.constants.Constants
import lombok.extern.slf4j.Slf4j
import lombok.RequiredArgsConstructor
import com.contract.modules.contract.service.LotteryRecordService
import com.contract.modules.contract.utils.CryptoUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 * Created with IntelliJ IDEA.
 *
 * @author: Ivan
 * @date: 2022/10/14
 * @Version: 1.0
 * @Description:
 */
@Service
class ContractTask {
    @Autowired
    private lateinit var lotteryRecordService: LotteryRecordService

    private val list = listOf<String>("30","60","180")

    fun createContract() {
        lotteryRecordService.createContract(CryptoUtils.getSymbolTickers(Constants.Cypto.BTC_USDT))
        lotteryRecordService.lockUp()
        lotteryRecordService.deleteForNotUsed()
    }
    fun lotteryThirty(){
        lotteryRecordService.lottery(list[0])
    }
    fun lotterySixty(){
        lotteryRecordService.lottery(list[1])
    }
    fun lotteryOneHundredAndEighty(){
        lotteryRecordService.lottery(list[2])
    }
}