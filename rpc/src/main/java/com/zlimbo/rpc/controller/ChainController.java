package com.zlimbo.rpc.controller;

import com.alibaba.fastjson.JSONObject;
import com.citahub.cita.protocol.CITAj;
import com.citahub.cita.protocol.core.DefaultBlockParameter;
import com.citahub.cita.protocol.core.methods.response.*;
import com.citahub.cita.protocol.http.HttpService;
import com.ibatis.common.resources.Resources;
import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapClientBuilder;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.apache.http.protocol.HTTP;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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


@RestController
public class ChainController {

    private static CITAj service = CITAj.build(new HttpService("https://testnet.citahub.com"));
    private static SqlMapClient sqlMapClient = null;
    long[] callbackTimeArray = {3000L, 1000L * 60, 1000L * 60 * 3};
    static {
        try {
            Reader reader = Resources.getResourceAsReader("SqlMapConfig.xml");
            sqlMapClient = SqlMapClientBuilder.buildSqlMapClient(reader);
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public String getHashValue(String data) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update(data.getBytes("UTF-8"));
        byte[] md5Array = md5.digest();
        String hashValue = "0x" + (new BigInteger(1, md5Array)).toString(16);
        return hashValue;
    }

    @RequestMapping("/")
    public String hello() {
        return "hello";
    }


    @RequestMapping("/test")
    public String test() throws SQLException, IOException {
        List<HashMap<String, String>> list = sqlMapClient.queryForList("test");
        String txHash = "1234";
        AppGetTransactionReceipt txReceipt = service.appGetTransactionReceipt(txHash).send();
        System.out.println("TransactionReceipt: " + txReceipt.getTransactionReceipt());
        return "test";
    }

    boolean insertPrivateKey(String systemId) {
        try {
            String privateKey = getHashValue(systemId);
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("systemId", systemId);
            hashMap.put("privateKey", privateKey);
            sqlMapClient.insert("insertPrivateKey", hashMap);
        } catch (Exception e) {
            //e.printStackTrace();
            return false;
        }
        return true;
    }


    public static String send(String url, JSONObject jsonObject, String encoding) throws IOException {
        System.out.println("====================> [send] start");
        String body = "";

        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);

        StringEntity stringEntity = new StringEntity(jsonObject.toString(), "utf-8");
        stringEntity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
        httpPost.setEntity(stringEntity);
        System.out.println("== request url: " + url);

        httpPost.setHeader("Content-type", "application/json");
        httpPost.setHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");

        CloseableHttpResponse response = client.execute(httpPost);

        HttpEntity entity = response.getEntity();
        if (entity != null) {
            body = EntityUtils.toString(entity, encoding);
        }
        EntityUtils.consume(entity);
        response.close();
        System.out.println("== body: " + body);
        //JSONObject returnJson = new JSONObject();
        System.out.println("====================> [send] end\n");
        return body;
    }


    /**
     * 异步回调同时
     * @param callbackUrl
     */
    private void upChainAsyncCallBack(String callbackUrl, String tableName, String txHash) {
        System.out.println("============> [upChainAsyncCallBack] start");
        System.out.println("callbackUrl: " + callbackUrl);
        Thread thread = new Thread(() -> {

            TransactionReceipt transactionReceipt = null;

            for (long time: callbackTimeArray) {
                System.out.println("============> [upChainAsyncCallBack thread] time: " + time);
                try {
                    Thread.sleep(time);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                int exceptionCount = 0;
                while (true) {
                    try {
                        Map<String, String> parameterMap = new HashMap<>();
                        parameterMap.put("txHash", txHash);
                        parameterMap.put("tableName", tableName);
                        List<HashMap<String,Object>> resultList = sqlMapClient.queryForList("queryByTxHash", parameterMap);

                        if (!resultList.isEmpty()) {
                            HashMap<String, Object> resultMap = resultList.get(0);
                            if ("true".equals(resultMap.get("onChain"))) {
                                System.out.println("onChain: " + resultMap.get("onChain"));
                                JSONObject postDataJson = new JSONObject();
                                postDataJson.put("txHash", txHash);
                                postDataJson.put("blockAddTime", resultMap.get("blockAddTime"));
                                postDataJson.put("blockNumer", resultMap.get("blockNumber"));
                                try {
                                    String resultString = send(callbackUrl, postDataJson, "utf-8");
                                    JSONObject resultJson = JSONObject.parseObject(resultString);
                                    if (resultJson.getBoolean("success")) {
                                    return; // 推送成功，不再推送。
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                break; // 一次推送，跳出循环
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        if (++exceptionCount == 10) {
                            break; // 十次异常退出循环
                        }
                    }
                }
            }
        });
        thread.start();
        System.out.println("============> [upChainAsyncCallBack] end");
    }


    void testCallback(String callbackUrl) throws IOException {
        System.out.println("============> [testCallback] start");
        JSONObject data = new JSONObject();
        data.put("ok", 123);
        String res = send(callbackUrl, data, "utf-8");
        System.out.println("result: " + res);
        System.out.println("============> [testCallback] end");
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
            String tableName = (String)dataJson.get("tableName");
            String systemId = (String) dataJson.get("systemId");
            String requestSn = (String) dataJson.get("requestSn");
            String dataInfo = dataJson.getJSONObject("dataInfo").toJSONString();
            String businessId = (String) dataJson.get("businessId");
            String callbackUrl = (String)dataJson.get("callbackUrl");
            String invokeTime = (String) dataJson.get("invokeTime");
            String sign = (String) dataJson.get("sign");
            String attach = (String) dataJson.get("attach");

            System.out.println("dataInfo: " + dataInfo);

            //testCallback(callbackUrl);
//            Map<String, String> parameterMap1 = new HashMap<>();
//            parameterMap1.put("systemId", systemId);
//            List<HashMap<String, String>> resultList1 = sqlMapClient.queryForList("queryForKey", parameterMap1);
//            HashMap<String, String> resultMap1 = resultList1.get(0);
//            String privateKey = resultMap1.get("privateKey");
//            String publicKey = resultMap1.get("publicKey");
            String privateKey = "0x67d7273b1e670ca5b0482381b631cee28a33ac03d8839244ae97df6f74bc027d";
            String publicKey = "0x0379f6feff204503fd71e6ecb16b1f190d70aae14358ac79e2739fcc2779ecc18e" +
                    "c8e25f861839a8607dc941ddc6c75116b89d7a2cbd6f23189d2265ebb4edd7";
            String secretKey = "0123456789abcdef";
            String txHash = getHashValue(dataInfo);
            String onChain = "false";
            //String txHash = "";
            try {
                Map<String, String> dataMap = new HashMap<>();
                dataMap.put("tableName", tableName);
                dataMap.put("systemId", systemId);
                dataMap.put("requestSn", requestSn);
                dataMap.put("dataInfo", dataInfo);
                dataMap.put("secretKey", secretKey);
                dataMap.put("privateKey", privateKey);
                dataMap.put("publicKey", publicKey);
                dataMap.put("txHash", txHash);
                dataMap.put("onChain", onChain);
                sqlMapClient.insert("upChain", dataMap);

                // 查询生成的哈希值
                Map<String, String> parameterMap = new HashMap<>();
                parameterMap.put("tableName", tableName);
                parameterMap.put("requestSn", requestSn);
                List<HashMap<String, String>> resultList = sqlMapClient.queryForList("queryByRequestSn", parameterMap);
                Map<String, String> resultMap = resultList.get(0);
                String resultTxHash = resultMap.get("txHash");

                returnJson.put("code", ResultCode.SUCCESS.getCode());
                returnJson.put("msg", ResultCode.SUCCESS.getMsg());
                JSONObject data = new JSONObject();
                data.put("txHash", resultTxHash);
                returnJson.put("data", data);

                if (callbackUrl != null) {
                    upChainAsyncCallBack(callbackUrl, tableName, txHash);
                }
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
    String queryByTxHash(@RequestBody JSONObject dataJson) {
        System.out.println("====================> [queryByTxHash] start");

        JSONObject returnJson = new JSONObject();
        try {
            System.out.println("== post json: " + dataJson.toJSONString());
            String tableName = (String) dataJson.get("tableName");
            String systemId = (String) dataJson.get("systemId");
            String requestSn = (String) dataJson.get("requestSn");
            String txHash = (String) dataJson.get("txHash");
            String invokeTime = (String) dataJson.get("invokeTime");
            String sign = (String) dataJson.get("sign");

            Map<String, String> parameterMap = new HashMap<>();
            parameterMap.put("tableName", tableName);
            parameterMap.put("systemId", systemId);
            parameterMap.put("txHash", txHash);
            List<HashMap<String,Object>> resultList = sqlMapClient.queryForList("queryByTxHash", parameterMap);

            if (!resultList.isEmpty()) {
                HashMap<String, Object> resultMap = resultList.get(0);
                returnJson.put("code", ResultCode.UP_CHAIN_SUCCESS.getCode());
                returnJson.put("msg", ResultCode.UP_CHAIN_SUCCESS.getMsg());
                JSONObject data = new JSONObject();
                data.putAll(resultMap);
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

        System.out.println("====================> [queryByTxHash] end");
        return returnJson.toJSONString();
    }


    /**
     * 业务数据验证接口, 可验证业务数据hash在链上存证结果。
     * @param dataJson
     * @return
     */
    @RequestMapping(value = "/obst/service/S_ST_03", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    String verifyTxDataInfo(@RequestBody JSONObject dataJson) {
        System.out.println("====================> [verifyTxDataInfo] start");

        JSONObject returnJson = new JSONObject();
        try {
            System.out.println("== post json: " + dataJson.toJSONString());
            String tableName = (String) dataJson.get("tableName");
            String systemId = (String) dataJson.get("systemId");
            String requestSn = (String) dataJson.get("requestSn");
            String txHash = (String) dataJson.get("txHash");
            String invokeTime = (String) dataJson.get("invokeTime");
            String sign = (String) dataJson.get("sign");

            Map<String, String> parameterMap = new HashMap<>();
            parameterMap.put("tableName", tableName);
            parameterMap.put("systemId", systemId);
            parameterMap.put("txHash", txHash);
            List<HashMap<String,Object>> resultList = sqlMapClient.queryForList("queryByTxHash", parameterMap);

            if (!resultList.isEmpty()) {
                Map<String, Object> resultMap = resultList.get(0);
                JSONObject dataInfo = dataJson.getJSONObject("dataInfo");
                // 比较dataInfo内容是否一致
                boolean same = true;
                for (String key: dataInfo.keySet()) {
                    if (!dataInfo.get(key).equals(resultMap.get(key))) {
                        same = false;
                        break;
                    }
                }
                if (same) {
                    returnJson.put("code", ResultCode.UP_CHAIN_SUCCESS.getCode());
                    returnJson.put("msg", ResultCode.UP_CHAIN_SUCCESS.getMsg());
                } else {
                    returnJson.put("code", ResultCode.UP_CHAIN_FAIL.getCode());
                    returnJson.put("msg", ResultCode.UP_CHAIN_FAIL.getMsg());
                }
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

        System.out.println("====================> [verifyTxDataInfo] end");
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
            String tableName = (String) dataJson.get("tableName");
            String systemId = (String) dataJson.get("systemId");
            String requestSn = (String) dataJson.get("requestSn");
            String businessId = (String) dataJson.get("businessId");
            String searchRequestSn = (String) dataJson.get("searchRequestSn");
            String invokeTime = (String) dataJson.get("invokeTime");
            String sign = (String) dataJson.get("sign");

            Map<String, String> parameterMap = new HashMap<>();
            parameterMap.put("tableName", tableName);
            parameterMap.put("searchRequestSn", searchRequestSn);
            List<HashMap<String,Object>> resultList = sqlMapClient.queryForList("compensateQuery", parameterMap);

            if (!resultList.isEmpty()) {
                HashMap<String, Object> resultMap = resultList.get(0);
                returnJson.put("code", ResultCode.UP_CHAIN_SUCCESS.getCode());
                returnJson.put("msg", ResultCode.UP_CHAIN_SUCCESS.getMsg());
                JSONObject data = new JSONObject();
                data.putAll(resultMap);
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



//    private void upChainAsyncCallBack(String callbackUrl, String txHash) {
//        System.out.println("============> [upChainAsyncCallBack] start");
//        System.out.println("callbackUrl: " + callbackUrl);
//        Thread thread = new Thread(() -> {
//
//            TransactionReceipt transactionReceipt = null;
//
//            for (long time: callbackTimeArray) {
//                System.out.println("============> [upChainAsyncCallBack thread] time: " + time);
//                try {
//                    Thread.sleep(time);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                int exceptionCount = 0;
//                while (true) {
//                    try {
//                        AppGetTransactionReceipt txReceipt = service.appGetTransactionReceipt(txHash).send();
//                        transactionReceipt = txReceipt.getTransactionReceipt();
//                        if (transactionReceipt != null) {
//                            JSONObject dataJson = new JSONObject();
//                            dataJson.put("code", ResultCode.UP_CHAIN_SUCCESS.getCode());
//                            dataJson.put("msg", ResultCode.UP_CHAIN_SUCCESS.getMsg());
//
//                            JSONObject data = new JSONObject();
//                            BigInteger blockNumber = transactionReceipt.getBlockNumber();
//                            AppBlock appBlock = service.appGetBlockByNumber(DefaultBlockParameter.valueOf(blockNumber), true).send();
//                            AppBlock.Header header = appBlock.getBlock().getHeader();
//                            Long blockAddTime = header.getTimestamp();
//
//                            data.put("txHash", txHash);
//                            data.put("blockAddTime", blockAddTime);
//                            data.put("blockNumber", blockNumber);
//                            dataJson.put("data", data);
//
//                            try {
//                                String body = send(callbackUrl, dataJson, "utf-8");
//                                System.out.println("body: " + body);
//                                JSONObject bodyJson = JSONObject.parseObject(body);
//                                if (bodyJson.getBoolean("success")) {
//                                    return; // 推送成功，不再推送。
//                                }
//                            } catch (Exception e) {
//
//                            }
//                            break; // 一次推送，跳出循环
//                        }
//                    } catch (IOException e) {
//                        if (++exceptionCount == 10) {
//                            break; // 十次异常退出循环
//                        }
//                        e.printStackTrace();
//                    }
//                }
//            }
//        });
//        thread.start();
//        System.out.println("============> [upChainAsyncCallBack] end");
//    }