package com.ouyeel.obfm.fm.business.impl;

import com.alibaba.fastjson.JSONObject;

/**
 * 响应码
 */
public enum ResponseCode {
    SUCCESS(1, "成功"),
    FAIL(-1, "失败"),
    NULL_PARAMETER(105, "空参数错误"),
    UP_TX_SUCCESS(1, "上传交易成功"),
    UP_TX_FAIL(-1, "上传交易失败"),
    UP_CHAIN_SUCCESS(104, "数据已上链，请检查参数"),
    UP_CHAIN_WAITTING(105, "数据上链中，请稍等"),
    UP_CHAIN_FAIL(101, "上链失败"),
    VERIFY_TX_SUCCESS(1, "数据验证成功"),
    VERIFY_TX_FAIL(-1, "数据验证失败"),
    UP_CHAIN_WAIT(102, "上链中"),
    SIGN_VERIFY_FAIL(106, "签名验证失败"),
    NO_REQUEST(107, "请求不存在"),
    QUERY_FAIL(108, "查询失败，请重试");

    private final Integer code;
    private final String msg;

    ResponseCode(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public static void putCodeAndMsg(JSONObject json, ResponseCode responseCode) {
        json.put("code", responseCode.getCode());
        json.put("msg", responseCode.getMsg());
    }
}
