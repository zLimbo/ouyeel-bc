package com.zlimbo.bcweb.controller;

import com.alibaba.fastjson.JSONObject;
import netscape.javascript.JSObject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;

@Controller
@RequestMapping("")
public class VerifyController {

    SqlController sqlController = new SqlController(ConnectInfo.DATABASE, ConnectInfo.USER, ConnectInfo.PASSWORD);

    enum ResultCode {
        SUCCESS(1, "成功"),
        PARAM_IS_INVALID(1001, "参数无效");

        private Integer code;
        private String msg;

        ResultCode(Integer code, String msg) {
            this.code = code;
            this.msg = msg;
        }

        public Integer getCode() {
            return code;
        }

        public String getMsg() {
            return msg;
        }
    }

    class Result implements Serializable {
        private Integer code;
        private String msg;
        private Object data;

        public Result(ResultCode resultCode, Object data) {
            this.code = resultCode.getCode();
            this.msg = resultCode.getMsg();
            this.data = data;
        }
    }

    class AsyncResult implements Serializable {
        private Integer code;
        private String msg;
        private Object data;
        private String requestSn;
        private String sign;

        AsyncResult(ResultCode resultCode, Object data, String requestSn, String sign) {
            this.code = resultCode.getCode();
            this.msg = resultCode.getMsg();
            this.data = data;
            this.requestSn = requestSn;
            this.sign = sign;
        }
    }

    @GetMapping("/obst/service/S_ST_01")
    String upChain(Model model) {

        return "upChain";
    }

    @PostMapping("/obst/service/S_ST_01")
    @ResponseBody
    String upChain(@RequestBody JSONObject json) {
        System.out.println("====================> [upChain] start");

        String systemId = json.get("systemId").toString();
        String requestSn = json.get("requestSn").toString();
        String tableName = json.get("tableName").toString();
        String dataInfo = json.get("dataInfo").toString();
        String businessId = json.get("businessId").toString();
        String callbackUrl = json.get("callbackUrl").toString();
        String invokeTime = json.get("invokeTime").toString();
        String sign = json.get("sign").toString();
        String attach = json.get("attach").toString();

        String sql = "INSERT INTO " + tableName + " VALUES(" +
                "'" + systemId + "', " +
                "'" + requestSn + "', " +
                "'" + tableName + "', " +
                "'" + dataInfo + "', " +
                "'" + businessId + "', " +
                "'" + callbackUrl + "', " +
                "'" + invokeTime + "', " +
                "'" + sign + "', " +
                "'" + attach + "');";

        String errorMessage = sqlController.sqlInsert(sql);
        JSONObject result = new JSONObject();

        if (errorMessage == null) {
            result.put("code", 1);
            result.put("msg", "上链成功！");
        } else {
            result.put("code", -1);
            result.put("msg", "上链失败！");
        }

        System.out.println("====================> [upChain] end");
        return result.toJSONString();
    }


    @GetMapping("/obst/service/S_ST_02")
    String verifyByTxHash() {
        return null;
    }

    @PostMapping("/obst/service/S_ST_02")
    Result verifyByTxHash(@RequestBody JSObject json) {
        return null;
    }


    @GetMapping("/obst/service/S_ST_03")
    String businessDataValidation() {
        return null;
    }

    @PostMapping("/obst/service/S_ST_03")
    Result businessDataValidation(@RequestBody JSObject json) {
        return null;
    }


    @GetMapping("/obst/service/S_ST_04")
    String compensateQuery() {
        return null;
    }

    @PostMapping("/obst/service/S_ST_04")
    Result compensateQuery(@RequestBody JSObject json) {
        return null;
    }



}
