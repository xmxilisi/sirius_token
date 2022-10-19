package com.contract.modules.contract.utils

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.contract.modules.contract.domain.KLineDataQueryParam
import com.contract.utils.StringUtils
import com.sun.istack.NotNull
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Map

/**
 * Created with IntelliJ IDEA.
 * 加密货币工具类
 * @author: Ivan
 * @date: 2022/10/12
 * @Version: 1.0
 * @Description:
 */
object CryptoUtils {
    private const val BASE_URL = "https://api.binance.com"

    /**
     *
     * @param symbol 交易对
     */
    @JvmStatic
    fun getSymbolTickers(@NotNull vararg symbol: String?): BigDecimal {
        val httpParam = "symbol"
        val response = HttpClient["$BASE_URL/api/v3/ticker/price", Map.of( if (symbol.size > 1) httpParam + "s" else httpParam, symbol)]
        response?.body().let { return if (symbol.size <= 1) BigDecimal(JSON.parseObject(it)["price"].toString()) else BigDecimal(JSON.parseObject(JSON.parseArray(it)[0].toString())["price"].toString()); }
    }

    /**
     * 获取k线图
     */
    @JvmStatic
    fun getKLineData(query: KLineDataQueryParam): String{
        HttpClient["$BASE_URL/api/v3/klines", JSON.parseObject(JSON.toJSONString(query), mutableMapOf<String,Any>()::class.java)].apply { return this?.body().toString() }
    }

    /**
     * 获取市值排行
     */
    @JvmStatic
    fun getExchangeInfo(): String{
        HttpClient["https://api.coingecko.com/api/v3/coins/markets?vs_currency=usd&order=market_cap_desc"].apply {
            return this?.body().toString();
        }
    }
}