package com.contract.modules.contract.rest

import com.contract.modules.contract.service.LotteryRecordService
import com.contract.modules.contract.service.dto.LotteryRecordDto
import com.contract.utils.R
import io.swagger.annotations.Api
import io.swagger.annotations.ApiModelProperty
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Created with IntelliJ IDEA.
 *
 * @author: Ivan
 * @date: 2022/10/12
 * @Version: 1.0
 * @Description:
 */
@RestController
@Api(tags = ["app端:合约接口"])
@RequestMapping("/api/contract")
class LotteryRecordController {

    @Autowired
    private lateinit var lotteryRecordService: LotteryRecordService

    @ApiModelProperty("获取最新期号")
    @GetMapping("/getNewVolume")
    fun getNewVolume(symbol: String,second : String) : R<LotteryRecordDto?> {
        return R.ok(lotteryRecordService.getNewVolume(symbol,second))
    }

}
