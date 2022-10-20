package com.contract.modules.contract.rest

import com.contract.modules.contract.service.LotteryRecordService
import com.contract.modules.contract.service.vo.LotteryRecordVo
import com.contract.utils.R
import io.swagger.annotations.Api
import io.swagger.annotations.ApiModelProperty
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.lang.model.element.Element

/**
 * Created with IntelliJ IDEA.
 * @author: Ivan
 * @date: 2022/10/19
 * @Version: 1.0
 * @Description:
 */
@RestController
@Api(tags = ["app端:鉴权合约接口"])
@RequestMapping("/api/contract-user")
class LotteryRecordUserController {
    @Autowired
    private lateinit var lotteryRecordService: LotteryRecordService

    @ApiModelProperty("获取开奖记录")
    @GetMapping("/getTheLotteryRecord")
    fun getTheLotteryRecord(second: String,symbol: String): R<List<LotteryRecordVo>> {
        return R.ok(lotteryRecordService.getTheLotteryRecord(second,symbol))
    }

    @ApiModelProperty("获取投注记录")
    @GetMapping("/getBettingHistory")
    fun getBettingHistory(second: String): R<Any> {
        return R.ok(lotteryRecordService.getBettingHistory(second))
    }

}