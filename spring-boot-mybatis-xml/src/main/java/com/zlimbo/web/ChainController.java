package com.zlimbo.web;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.zlimbo.mapper.ChainMapper;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;


@RestController
public class ChainController {

	@Autowired(required = false)
	private ChainMapper chainMapper;

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

    public String getHashValue(String data) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update(data.getBytes("UTF-8"));
        byte[] md5Array = md5.digest();
        String hashValue = "0x" + (new BigInteger(1, md5Array)).toString(16);
        return hashValue;
    }

    boolean insertPrivateKey(String systemId) {
        try {
            String privateKey = getHashValue(systemId);
            chainMapper.insertPrivateKey(systemId, privateKey);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    /**
     * 业务数据上链存证接口, 将业务信息进行上链存证。不对业务数据进行关联。
     * @param dataJson
     * @return
     */
    @RequestMapping(value = "/obst/service/S_ST_01", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    String upChain(@RequestBody JSONObject dataJson) {
        System.out.println("====================> [upChain] start");
        JSONObject returnJson = new JSONObject();
        try {
            System.out.println("== post json: " + dataJson.toJSONString());
            String tableName = dataJson.get("tableName").toString();
            String systemId = dataJson.get("systemId").toString();
            String requestSn = dataJson.get("requestSn").toString();
            String dataInfo = dataJson.getJSONObject("dataInfo").toJSONString();
            String businessId = dataJson.get("businessId").toString();
            String callbackUrl = dataJson.get("callbackUrl").toString();
            String invokeTime = dataJson.get("invokeTime").toString();
            String sign = dataJson.get("sign").toString();
            String attach = null;
            if (dataJson.containsKey("attach")) {
                attach = dataJson.get("attach").toString();
            }
            System.out.println("dataInfo: " + dataInfo);
            insertPrivateKey(systemId);
            List<HashMap<String,Object>> list = chainMapper.queryPrivateKey("privateKey", systemId);
            String privateKey = (String)list.get(0).get("privateKey");
            //String privateKey = "123456";
            String txHash = getHashValue(dataInfo);
            String dataHash = getHashValue(dataJson.toJSONString());
            try {
                chainMapper.upChain(tableName, privateKey, systemId, requestSn, dataInfo, txHash);
                returnJson.put("code", ResultCode.SUCCESS.getCode());
                returnJson.put("msg", ResultCode.SUCCESS.getMsg());
                returnJson.put("data", dataHash);
                returnJson.put("txHash", txHash);
            } catch (Exception e) {
                e.printStackTrace();
                returnJson.put("code", ResultCode.FAIL.getCode());
                returnJson.put("msg", ResultCode.FAIL.getMsg());
            }
        } catch (Exception e) {
            e.printStackTrace();
            returnJson.clear();
            returnJson.put("code", ResultCode.PARAMETER_ERROR.getCode());
            returnJson.put("msg", ResultCode.PARAMETER_ERROR.getMsg());
        }

        System.out.println("====================> [upChain] end");
        return returnJson.toJSONString();
    }


    /**
     * 根据交易hash查证接口, 可根据存证时交易hash进行查证，返回业务数据上链存证信息。
     * @param dataJson
     * @return
     */
    @RequestMapping(value = "/obst/service/S_ST_02", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    String verifyByTxHash(@RequestBody JSONObject dataJson) {
        System.out.println("====================> [verifyByTxHash] start");

        JSONObject returnJson = new JSONObject();
        try {
            System.out.println("== post json: " + dataJson.toJSONString());
            String tableName = dataJson.get("tableName").toString();
            String systemId = dataJson.get("systemId").toString();
            String requestSn = dataJson.get("requestSn").toString();
            String txHash = dataJson.get("txHash").toString();
            String invokeTime = dataJson.get("invokeTime").toString();
            String sign = dataJson.get("sign").toString();


            List<HashMap<String,Object>> resultList = chainMapper.verifyBxTxHash(tableName, systemId, txHash);

            if (!resultList.isEmpty()) {
                HashMap<String, Object> resultMap = resultList.get(0);
                returnJson.put("code", ResultCode.UP_CHAIN_SUCCESS.getCode());
                returnJson.put("msg", ResultCode.UP_CHAIN_SUCCESS.getMsg());
                JSONObject data = JSONObject.parseObject((String)resultMap.get("dataInfo"));
                returnJson.put("data", data);
            } else {
                returnJson.put("code", ResultCode.UP_CHAIN_FAIL.getCode());
                returnJson.put("msg", ResultCode.UP_CHAIN_FAIL.getMsg());
            }

        } catch (Exception e) {
            e.printStackTrace();
            returnJson.clear();
            returnJson.put("code", ResultCode.PARAMETER_ERROR.getCode());
            returnJson.put("msg", ResultCode.PARAMETER_ERROR.getMsg());
        }

        System.out.println("====================> [verifyByTxHash] end");
        return returnJson.toJSONString();
    }


    /**
     * 业务数据验证接口, 可验证业务数据hash在链上存证结果。
     * @param dataJson
     * @return
     */
    @RequestMapping(value = "/obst/service/S_ST_03", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    String businessDataValidation(@RequestBody JSONObject dataJson) {
        System.out.println("====================> [businessDataValidation] start");

        JSONObject returnJson = new JSONObject();
        try {
            System.out.println("== post json: " + dataJson.toJSONString());
            String tableName = dataJson.get("tableName").toString();
            String systemId = dataJson.get("systemId").toString();
            String requestSn = dataJson.get("requestSn").toString();
            String businessId = dataJson.get("businessId").toString();
            String txHash = dataJson.get("txHash").toString();
            String businessHash = dataJson.get("businessHash").toString();
            String invokeTime = dataJson.get("invokeTime").toString();
            String sign = dataJson.get("sign").toString();

            List<HashMap<String,Object>> resultList = chainMapper.verifyBxTxHash(tableName, systemId, txHash);

            if (!resultList.isEmpty()) {
                HashMap<String, Object> resultMap = resultList.get(0);
                returnJson.put("code", ResultCode.UP_CHAIN_SUCCESS.getCode());
                returnJson.put("msg", ResultCode.UP_CHAIN_SUCCESS.getMsg());
                JSONObject data = JSONObject.parseObject((String)resultMap.get("dataInfo"));
                returnJson.put("data", data);
            } else {
                returnJson.put("code", ResultCode.UP_CHAIN_FAIL.getCode());
                returnJson.put("msg", ResultCode.UP_CHAIN_FAIL.getMsg());
            }

        } catch (Exception e) {
            e.printStackTrace();
            returnJson.clear();
            returnJson.put("code", ResultCode.PARAMETER_ERROR.getCode());
            returnJson.put("msg", ResultCode.PARAMETER_ERROR.getMsg());
        }

        System.out.println("====================> [businessDataValidation] end");
        return returnJson.toJSONString();
    }


    /**
     * 补偿查询接口, 如果异步推送未收到结果，可根据该接口进行主动查询。
     * @param dataJson
     * @return
     */
    @RequestMapping(value = "/obst/service/S_ST_04", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    String compensateQuery(@RequestBody JSONObject dataJson) {
        System.out.println("====================> [compensateQuery] start");

        JSONObject returnJson = new JSONObject();
        try {
            System.out.println("== post json: " + dataJson.toJSONString());
            String tableName = dataJson.get("tableName").toString();
            String systemId = dataJson.get("systemId").toString();
            String requestSn = dataJson.get("requestSn").toString();
            String businessId = dataJson.get("businessId").toString();
            String searchRequestSn = dataJson.get("searchRequestSn").toString();
            String invokeTime = dataJson.get("invokeTime").toString();
            String sign = dataJson.get("sign").toString();

            List<HashMap<String,Object>> resultList = chainMapper.compensateQuery(tableName, searchRequestSn);

            if (!resultList.isEmpty()) {
                HashMap<String, Object> resultMap = resultList.get(0);
                returnJson.put("code", ResultCode.UP_CHAIN_SUCCESS.getCode());
                returnJson.put("msg", ResultCode.UP_CHAIN_SUCCESS.getMsg());
                JSONObject data = JSONObject.parseObject((String)resultMap.get("dataInfo"));
                returnJson.put("data", data);
            } else {
                returnJson.put("code", ResultCode.UP_CHAIN_FAIL.getCode());
                returnJson.put("msg", ResultCode.UP_CHAIN_FAIL.getMsg());
            }

        } catch (Exception e) {
            e.printStackTrace();
            returnJson.clear();
            returnJson.put("code", ResultCode.PARAMETER_ERROR.getCode());
            returnJson.put("msg", ResultCode.PARAMETER_ERROR.getMsg());
        }

        System.out.println("====================> [compensateQuery] end");
        return returnJson.toJSONString();
    }
}