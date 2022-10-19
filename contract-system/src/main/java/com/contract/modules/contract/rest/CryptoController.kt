package com.contract.modules.contract.rest

import com.contract.modules.contract.domain.KLineDataQueryParam
import com.contract.modules.contract.enums.SymbolsEnum
import com.contract.modules.contract.utils.CryptoUtils
import com.contract.utils.R
import io.swagger.annotations.Api
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Created with IntelliJ IDEA.
 *
 * @author: Ivan
 * @date: 2022/10/14
 * @Version: 1.0
 * @Description:
 */
@RestController
@Api(tags = ["app端：加密货币相关接口"])
@RequestMapping("/api/crypto")
class CryptoController {

    @GetMapping("/getKLineData")
    fun getKLineData(queryParam: KLineDataQueryParam) : R<String> {
        return R.ok(CryptoUtils.getKLineData(queryParam))
    }

    @GetMapping("/getSymbols")
    fun getSymbols():R<Array<SymbolsEnum>>{
        return R.ok(SymbolsEnum.values())
    }

}