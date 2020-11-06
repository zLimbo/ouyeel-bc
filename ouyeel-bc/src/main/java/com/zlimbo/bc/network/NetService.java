package com.zlimbo.bc.network;

import com.alibaba.fastjson.JSONObject;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.Serializable;

@FeignClient(name = "NetService", url = "https://chain-test.ouyeel.com")
public interface NetService {

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


    @PostMapping("/obst/service/S_ST_01")
    Result send(@RequestParam("systemId") String systemId,
                @RequestParam("requestSn") String requestSn,
                @RequestParam("dataInfo") Object dataInfo,
                @RequestParam("businessId") String businessId,
                @RequestParam("callbackUrl") String callbackUrl,
                @RequestParam("invokeTime") String invokeTime,
                @RequestParam("sign") String sign,
                @RequestParam(value = "attach", required = false) String attach);


    @PostMapping("/obst/service/S_ST_02")
    Result verifyByTxHash(@RequestParam("systemId") String systemId,
                          @RequestParam("requestSn") String requestSn,
                          @RequestParam("txHash") String txHash,
                          @RequestParam("invokeTime") String invokeTime,
                          @RequestParam("sign") String sign);


    @PostMapping("/obst/service/S_ST_03")
    Result businessDataValidation(@RequestParam("systemId") String systemId,
                                  @RequestParam("requestSn") String requestSn,
                                  @RequestParam("businessId") String businessId,
                                  @RequestParam("txHash") String txHash,
                                  @RequestParam("businessHash") String businessHash,
                                  @RequestParam("invokeTime") String invokeTime,
                                  @RequestParam("sign") String sign);


    @PostMapping("/obst/service/S_ST_04")
    Result compensateQuery(@RequestParam("systemId") String systemId,
                           @RequestParam("requestSn") String requestSn,
                           @RequestParam("businessId") String businessId,
                           @RequestParam("searchRequestSn") String searchRequestSn,
                           @RequestParam("invokeTime") String invokeTime,
                           @RequestParam("sign") String sign);
}
