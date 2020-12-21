package com.ouyeel.obfm.fm.business.impl;

import com.alibaba.fastjson.JSONObject;

/**
 * 响应码
 */
public enum ResponseCode {
    SUCCESS(1, "成功"),
    FAIL(-1, "失败"),
    PARAMETER_LOSS(100, "缺少相应参数"),
    PARAMETER_ERROR(200, "参数错误"),
    UP_TX_SUCCESS(101, "上传交易成功"),
    UP_TX_FAIL(201, "上传交易失败"),
    UP_CHAIN_SUCCESS(102, "数据已上链，请检查参数"),
    NO_TX(202, "链上无此交易"),
    UP_CHAIN_WAITTING(302, "数据上链中，请稍等"),
    VERIFY_TX_SUCCESS(103, "数据验证成功"),
    VERIFY_TX_FAIL(203, "数据验证失败"),
    SIGN_VERIFY_FAIL(204, "签名验证失败"),
    NO_REQUEST(105, "请求不存在"),
    QUERY_FAIL(106, "查询失败，请重试"),
    OBTAIN_KEY_FAIL(207, "获取密钥失败"),
    DATA_INFO_ERROR(208, "dataInfo参数错误");

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
        json.put(ChainConfig.CODE, responseCode.getCode());
        json.put(ChainConfig.MSG, responseCode.getMsg());
    }
}
