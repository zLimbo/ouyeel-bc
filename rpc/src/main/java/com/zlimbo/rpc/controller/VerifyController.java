package com.zlimbo.rpc.controller;

import com.alibaba.fastjson.JSONObject;
import netscape.javascript.JSObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.InputStreamReader;
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

    public JSONObject getJSONParam(HttpServletRequest request){
        JSONObject jsonParam = null;
        try {
            // 获取输入流
            BufferedReader streamReader = new BufferedReader(new InputStreamReader(request.getInputStream(), "UTF-8"));

            // 写入数据到Stringbuilder
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = streamReader.readLine()) != null) {
                sb.append(line);
            }
            jsonParam = JSONObject.parseObject(sb.toString());
            // 直接将json信息打印出来
            System.out.println(jsonParam.toJSONString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonParam;
    }

    @RequestMapping(value = "/obst/service/S_ST_01", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @ResponseBody
    String upChain(@RequestBody JSONObject jsonData) {

        System.out.println("====================> [upChain] start");
        //request.get
        //JSONObject jsonData = this.getJSONParam(request);
        System.out.println("== json: " + jsonData.toJSONString());
        String systemId = jsonData.get("systemId").toString();
        String requestSn = jsonData.get("requestSn").toString();
        String tableName = jsonData.get("tableName").toString();
        String dataInfo = jsonData.get("dataInfo").toString();
        String businessId = jsonData.get("businessId").toString();
        String callbackUrl = jsonData.get("callbackUrl").toString();
        String invokeTime = jsonData.get("invokeTime").toString();
        String sign = jsonData.get("sign").toString();
        String attach = jsonData.get("attach").toString();

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
