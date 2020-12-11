package com.ouyeel.obfm.utils;

import com.baosight.iplat4j.core.ei.EiInfo;

public enum ResultCode {

    SUCCESS("1", "交易成功"),
    FAIL("-1", "交易失败"),
    BLOCK_REUSLT_FAIL("0001", "交易hash对应交易结果不存在！");

    private String code;
    private String msg;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    ResultCode(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }


    public static EiInfo buildSuccess() {
        EiInfo eiInfo = new EiInfo();

        return eiInfo;
    }

    public static EiInfo buildEiInfo(String msg) {
        EiInfo eiInfo = new EiInfo();
        eiInfo.set("msg", msg);
        return eiInfo;
    }



    public static EiInfo buildEiInfo(int code, String msg) {
        EiInfo eiInfo = new EiInfo();
        eiInfo.set("code", code);
        eiInfo.set("msg", msg);
        return eiInfo;
    }
}
