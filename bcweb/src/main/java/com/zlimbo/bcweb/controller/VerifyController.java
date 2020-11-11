package com.zlimbo.bcweb.controller;

import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.io.Serializable;

@Controller
@RequestMapping("")
public class VerifyController {

    SqlController sqlController = new SqlController(ConnectInfo.DATABASE, ConnectInfo.USER, ConnectInfo.PASSWORD);

    enum ResultCode {
        SUCCESS(1, "成功"),
        FAIL(-1, "失败"),
        PARAMETER_ERROR(105, "业务参数错误"),
        UP_CHAIN_SUCCESS(104, "数据已上链，请检查参数"),
        UP_CHAIN_FAIL(101, "上链失败"),
        UP_CHAIN_WAIT(102, "上链中"),
        SIGN_VERIFY_FAIL(106, "签名验证失败"),
        NO_REQUEST(107, "请求不存在");

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


    @RequestMapping(value = "/obst/service/S_ST_01", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @ResponseBody
    String upChain(@RequestBody JSONObject jsonData) {
        System.out.println("====================> [upChain] start");

        JSONObject result = new JSONObject();
        try {
            System.out.println("== post json: " + jsonData.toJSONString());
            String tableName = jsonData.get("tableName").toString();
            String systemId = jsonData.get("systemId").toString();
            String requestSn = jsonData.get("requestSn").toString();
            String dataInfo = jsonData.get("dataInfo").toString();
            String businessId = jsonData.get("businessId").toString();
            String callbackUrl = jsonData.get("callbackUrl").toString();
            String invokeTime = jsonData.get("invokeTime").toString();
            String sign = jsonData.get("sign").toString();
            String attach = null;
            if (jsonData.containsKey("attach")) {
                attach = jsonData.get("attach").toString();
            }
            String sql = "INSERT INTO " + tableName + " VALUES(" +
                    "'" + systemId + "', " +
                    "'" + requestSn + "', " +
                    "'" + dataInfo + "', " +
                    "'" + businessId + "', " +
                    "'" + callbackUrl + "', " +
                    "'" + invokeTime + "', " +
                    "'" + sign + "', " +
                    (attach == null ? "null" : "'" + attach + "'") + ")";

            String errorMessage = sqlController.sqlInsert(sql);

            if (errorMessage == null) {
                result.put("code", ResultCode.SUCCESS.getCode());
                result.put("msg", ResultCode.SUCCESS.getMsg());
                result.put("data", "0x7171e32cfa127937800a6d275b1288d0");
                result.put("transactionHash", "0xfc091cd49cd6edf3dad2d3f80e2bf0b4");
            } else {
                result.put("code", ResultCode.FAIL.getCode());
                result.put("msg", ResultCode.FAIL.getMsg());
            }

        } catch (Exception e) {
            e.printStackTrace();
            result.clear();
            result.put("code", ResultCode.PARAMETER_ERROR.getCode());
            result.put("msg", ResultCode.PARAMETER_ERROR.getMsg());
        }

        System.out.println("====================> [upChain] end");
        return result.toJSONString();
    }


    @PostMapping("/obst/service/S_ST_02")
    @ResponseBody
    String verifyByTxHash(@RequestBody JSONObject jsonData) {
        System.out.println("====================> [verifyByTxHash] start");

        JSONObject result = new JSONObject();
        try {
            System.out.println("== post json: " + jsonData.toJSONString());
            String tableName = jsonData.get("tableName").toString();
            String systemId = jsonData.get("systemId").toString();
            String requestSn = jsonData.get("requestSn").toString();
            String txHash = jsonData.get("txHash").toString();
            String invokeTime = jsonData.get("invokeTime").toString();
            String sign = jsonData.get("sign").toString();

            String sql = "INSERT INTO " + tableName + " VALUES(" +
                    "'" + systemId + "', " +
                    "'" + requestSn + "', " +
                    "'" + txHash + "', " +
                    "'" + invokeTime + "', " +
                    "'" + sign + "')";

            String errorMessage = sqlController.sqlInsert(sql);

            if (errorMessage == null) {
                result.put("code", ResultCode.UP_CHAIN_SUCCESS.getCode());
                result.put("msg", ResultCode.UP_CHAIN_SUCCESS.getMsg());
                result.put("data", "0x7171e32cfa127937800a6d275b1288d0");
                result.put("transactionHash", "0xfc091cd49cd6edf3dad2d3f80e2bf0b4");
            } else {
                result.put("code", ResultCode.UP_CHAIN_FAIL.getCode());
                result.put("msg", ResultCode.UP_CHAIN_FAIL.getMsg());
            }

        } catch (Exception e) {
            e.printStackTrace();
            result.clear();
            result.put("code", ResultCode.PARAMETER_ERROR.getCode());
            result.put("msg", ResultCode.PARAMETER_ERROR.getMsg());
        }

        System.out.println("====================> [verifyByTxHash] end");
        return result.toJSONString();
    }


    @PostMapping("/obst/service/S_ST_03")
    @ResponseBody
    String businessDataValidation(@RequestBody JSONObject jsonData) {
        System.out.println("====================> [businessDataValidation] start");

        JSONObject result = new JSONObject();
        try {
            System.out.println("== post json: " + jsonData.toJSONString());
            String tableName = jsonData.get("tableName").toString();
            String systemId = jsonData.get("systemId").toString();
            String requestSn = jsonData.get("requestSn").toString();
            String businessId = jsonData.get("businessId").toString();
            String txHash = jsonData.get("txHash").toString();
            String businessHash = jsonData.get("businessHash").toString();
            String invokeTime = jsonData.get("invokeTime").toString();
            String sign = jsonData.get("sign").toString();

            String sql = "INSERT INTO " + tableName + " VALUES(" +
                    "'" + systemId + "', " +
                    "'" + requestSn + "', " +
                    "'" + businessId + "', " +
                    "'" + txHash + "', " +
                    "'" + businessHash + "', " +
                    "'" + invokeTime + "', " +
                    "'" + sign + "')";

            String errorMessage = sqlController.sqlInsert(sql);

            if (errorMessage == null) {
                result.put("code", ResultCode.UP_CHAIN_SUCCESS.getCode());
                result.put("msg", ResultCode.UP_CHAIN_SUCCESS.getMsg());
                result.put("data", "0x7171e32cfa127937800a6d275b1288d0");
                result.put("transactionHash", "0xfc091cd49cd6edf3dad2d3f80e2bf0b4");
            } else {
                result.put("code", ResultCode.UP_CHAIN_FAIL.getCode());
                result.put("msg", ResultCode.UP_CHAIN_FAIL.getMsg());
            }

        } catch (Exception e) {
            e.printStackTrace();
            result.clear();
            result.put("code", ResultCode.PARAMETER_ERROR.getCode());
            result.put("msg", ResultCode.PARAMETER_ERROR.getMsg());
        }

        System.out.println("====================> [businessDataValidation] end");
        return result.toJSONString();
    }


    @PostMapping("/obst/service/S_ST_04")
    @ResponseBody
    String compensateQuery(@RequestBody JSONObject jsonData) {
        System.out.println("====================> [compensateQuery] start");

        JSONObject result = new JSONObject();
        try {
            System.out.println("== post json: " + jsonData.toJSONString());
            String tableName = jsonData.get("tableName").toString();
            String systemId = jsonData.get("systemId").toString();
            String requestSn = jsonData.get("requestSn").toString();
            String businessId = jsonData.get("businessId").toString();
            String searchRequestSn = jsonData.get("searchRequestSn").toString();
            String invokeTime = jsonData.get("invokeTime").toString();
            String sign = jsonData.get("sign").toString();

            String sql = "INSERT INTO " + tableName + " VALUES(" +
                    "'" + systemId + "', " +
                    "'" + requestSn + "', " +
                    "'" + businessId + "', " +
                    "'" + searchRequestSn + "', " +
                    "'" + invokeTime + "', " +
                    "'" + sign + "')";

            String errorMessage = sqlController.sqlInsert(sql);

            if (errorMessage == null) {
                result.put("code", ResultCode.UP_CHAIN_SUCCESS.getCode());
                result.put("msg", ResultCode.UP_CHAIN_SUCCESS.getMsg());
                result.put("data", "0x7171e32cfa127937800a6d275b1288d0");
                result.put("transactionHash", "0xfc091cd49cd6edf3dad2d3f80e2bf0b4");
            } else {
                result.put("code", ResultCode.UP_CHAIN_FAIL.getCode());
                result.put("msg", ResultCode.UP_CHAIN_FAIL.getMsg());
            }

        } catch (Exception e) {
            e.printStackTrace();
            result.clear();
            result.put("code", ResultCode.PARAMETER_ERROR.getCode());
            result.put("msg", ResultCode.PARAMETER_ERROR.getMsg());
        }

        System.out.println("====================> [compensateQuery] end");
        return result.toJSONString();
    }



}
