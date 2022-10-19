package com.contract.utils

import com.contract.utils.enums.ResponseCodeEnum
import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.annotations.ApiModel
import java.io.Serializable

@ApiModel("响应体")
@JsonInclude(JsonInclude.Include.NON_NULL)
class R<T> private constructor() : Serializable {
    var code: Int? = null
    var msg: String? = null
    var data: T? = null

    companion object {

        fun <T> ok(data: T): R<T> {
            return R<T>().apply {
                code = ResponseCodeEnum.OK.code
                this.msg = ResponseCodeEnum.OK.desc
                this.data = data
            }
        }

        fun <T> ok(): R<T> {
            return R<T>().apply {
                code = ResponseCodeEnum.OK.code
                this.msg = ResponseCodeEnum.OK.desc
                this.data = null
            }
        }


        fun <T> fail(): R<T> {
            return R<T>().apply {
                code = ResponseCodeEnum.ERROR.code
                this.msg = ResponseCodeEnum.ERROR.desc
                this.data = null
            }
        }

        fun <T> fail(message: String): R<T> {
            return R<T>().apply {
                code = ResponseCodeEnum.ERROR.code
                this.msg = message
                this.data = null
            }
        }

        fun <T> getInstance(codeEnum: ResponseCodeEnum, message: String = codeEnum.desc, data: T): R<T> {
            return R<T>().apply {
                code = codeEnum.code
                this.msg = message
                this.data = data
            }
        }

        fun <T> getSuccessInstance(message: String = ResponseCodeEnum.OK.desc, data: T? = null): R<T> {
            return R<T>().apply {
                code = ResponseCodeEnum.OK.code
                this.msg = message
                this.data = data
            }
        }
    }
}