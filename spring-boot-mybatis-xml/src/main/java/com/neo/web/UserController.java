package com.neo.web;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.neo.mapper.UserMapper;

@RestController
public class UserController {
	
	@Autowired
	private UserMapper userMapper;

	@RequestMapping("sc")
    public String insertSC() {
	    int res = userMapper.insertSC();
	    System.out.println("res = " + res);
	    return "ok";
    }


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


    @RequestMapping(value = "/obst/service/S_ST_01", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    String upChain(@RequestBody JSONObject jsonData) {
        System.out.println("====================> [upChain] start");

        JSONObject result = new JSONObject();
        try {
            System.out.println("== post json: " + jsonData.toJSONString());
            String tableName = jsonData.get("tableName").toString();
            String systemId = jsonData.get("systemId").toString();
            String requestSn = jsonData.get("requestSn").toString();
            String dataInfo = jsonData.get("dataInfo").toString();

            String privateKey = "123";
            int res = userMapper.upChain(tableName, privateKey, systemId, requestSn, dataInfo);
            if (res == 1) {
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
}