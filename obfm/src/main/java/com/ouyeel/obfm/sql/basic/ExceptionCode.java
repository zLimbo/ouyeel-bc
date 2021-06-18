package com.ouyeel.obfm.sql.basic;

/**
 * 业务异常码
 */
public enum ExceptionCode {

    UP_TX_FAIL(5001, "上传交易失败！"),
    OBTAIN_KEY_FAIL(5002, "获取账户，密钥失败！"),
    REQUEST_SN_EXISTS(5003, "requestSn 已存在！"),
    CALLBAKC_SERVICE_ID_NOT_FOUND(5004, "上链结果通知回调serviceId为空！");


    private final Integer code;
    private final String msg;

    ExceptionCode(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    @Override
    public String toString() {
        return "BusinessErrorCode{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                '}';
    }
}
