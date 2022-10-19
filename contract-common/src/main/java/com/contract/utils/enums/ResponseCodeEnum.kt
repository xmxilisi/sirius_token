package com.contract.utils.enums

import com.contract.constants.PromptConstant

enum class ResponseCodeEnum(val code: Int, val desc: String) {

    OK(200, "成功"),

    /**
     * 错误请求
     * 服务器不理解请求的语法
     */
    ERROR(400, "错误请求"),

    /**
     * 未授权
     * 请求要求身份验证。 对于需要登录的网页，服务器可能返回此响应。
     */
    UNAUTHORIZED(401, PromptConstant.UNAUTHORIZED),

    /**
     * 参数错误
     * 前端提交表单参数验证失败
     */
    PARAM_ERROR(402, "参数错误"),

    /**
     * 未满足前提条件
     * 服务器未满足请求者在请求中设置的其中一个前提条件。
     */
    PRECONDITION_FAILED(412, "请求错误啦"),

    /**
     * 服务器内部错误
     * 服务器遇到错误，无法完成请求。
     */
    INTERNAL_SERVER_ERROR(500, "抱歉，哒配生病啦"),

    /**
     * 尚未实施
     * 服务器不具备完成请求的功能。 例如，服务器无法识别请求方法时可能会返回此代码。
     */
    NOT_IMPLEMENTED(501, "抱歉，哒配还没完成此功能"),

    // =========== 自定义状态 =============

    CANNOT_FIND_ORDER(10001, "抱歉，未找到该笔订单")
}