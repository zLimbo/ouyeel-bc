package com.zlimbo.rpc.controller;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.citahub.cita.protocol.CITAj;
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
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

enum ResultCode {
    SUCCESS(1, "成功"),
    FAIL(-1, "失败"),
    PARAMETER_ERROR(105, "参数错误"),
    UP_TX_SUCCESS(1, "上传交易成功"),
    UP_TX_FAIL(-1, "上传交易失败"),
    UP_CHAIN_SUCCESS(104, "数据已上链，请检查参数"),
    UP_CHAIN_WAITTING(105, "数据上链中，请稍等"),
    UP_CHAIN_FAIL(101, "上链失败"),
    VERIFY_TX_SUCCESS(1, "数据验证成功"),
    VERIFY_TX_FAIL(-1, "数据验证失败"),
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


    /**
     * 日志
     */
    final Logger logger = LoggerFactory.getLogger(getClass());


    /**
     * 测试公钥
     */
    private static final String PUBLIC_KEY_STRING = "3059301306072a8648ce3d020106082a811ccf5501822d0" +
            "34200040cfea82646c5695da5a4476e3fdcaf3f97ea9cc77fae78608fe12a1969ef8032e3ea6e91d2477444" +
            "5d2744e1c43d4b32845d3022718c06ca7cd4e73317f1e726";


    /**
     * 回调时间（3个）
     */
    private final long[] CALLBACK_TIMES = {3000L, 1000L * 60, 1000L * 60 * 3};


    /**
     * cita
     */
    private static CITAj service = CITAj.build(new HttpService("https://testnet.citahub.com"));


    /**
     * ibatis 连接
     */
    private static SqlMapClient sqlMapClient = null;

    static {
        Logger staticLogger = LoggerFactory.getLogger(ChainController.class);
        staticLogger.debug("ibatis SqlMapConfig ...");
        try {
            Reader reader = Resources.getResourceAsReader("SqlMapConfig.xml");
            sqlMapClient = SqlMapClientBuilder.buildSqlMapClient(reader);
            reader.close();
            staticLogger.debug("ibatis SqlMapConfig success");
        } catch (IOException e) {
            staticLogger.error(e.getMessage());
            e.printStackTrace();
            staticLogger.debug("ibatis SqlMapConfig error");
            staticLogger.error(e.getMessage());
        }
    }


    /**
     * sm2 签名验证
     * @param plainText
     * @param signatureValue
     * @return
     * @throws Exception
     */
    boolean gmSm2VerifySignature(byte[] plainText, byte[] signatureValue) throws Exception {
        logger.debug("[gmSm2VerifySignature] start");
        
        final Provider bouncyCastleProvider = new BouncyCastleProvider();
        // 公私钥是16进制情况下解码
        byte[] encodePublicKey = Hex.decode(PUBLIC_KEY_STRING);

        KeyFactory keyFactory = KeyFactory.getInstance("EC", bouncyCastleProvider);
        // 根据采用的编码结构反序列化公钥
        PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(encodePublicKey));
        Signature signature = Signature.getInstance("SM3withSm2", bouncyCastleProvider);
        // 签名需要使用公钥，使用公钥 初始化签名实例
        signature.initVerify(publicKey);
        // 写入待验签的签名原文到算法中
        signature.update(plainText);
        // 验签
        boolean result = signature.verify(signatureValue);

        logger.debug("signature[%d]: %s", signatureValue.length, Hex.toHexString(signatureValue));
        logger.debug("Signature verify result: " + result);
        logger.debug("[gmSm2VerifySignature] end");

        return result;
    }


    /**
     * 哈希摘要
     * @param data
     * @return
     * @throws UnsupportedEncodingException
     * @throws NoSuchAlgorithmException
     */
    public String getHashValue(String data) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update(data.getBytes("UTF-8"));
        byte[] md5Array = md5.digest();
        String hashValue = "0x" + (new BigInteger(1, md5Array)).toString(16);
        return hashValue;
    }


    /**
     * index
     * @return
     */
    @RequestMapping("/")
    public String index() {

        return "hello";
    }


    /**
     * test ibatis
     * @return
     * @throws SQLException
     * @throws IOException
     */
    @RequestMapping("/test")
    public String test() throws SQLException {
        logger.debug("[test] start");
        Map<String, String> map = new HashMap<>();
        map.put("tableName", "tx6");
        map.put("requestSn", "0xff5869b44fb2a389e755304ec3b0015b70206ca4494ab5ff61c0931d39245218");
        List<HashMap<String, String>> list = sqlMapClient.queryForList("queryByTxHash", map);
        String txHash = "1234";
        logger.debug("[test] end");
        return String.valueOf(list.size());
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


    /**
     * http post send
     * @param url
     * @param jsonObject
     * @param encoding
     * @return
     * @throws IOException
     */
    public String send(String url, JSONObject jsonObject, String encoding) throws IOException {
        logger.debug("[send] start");

        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);

        StringEntity stringEntity = new StringEntity(jsonObject.toString(), "utf-8");
        stringEntity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
        httpPost.setEntity(stringEntity);
        logger.debug("request url: " + url);

        httpPost.setHeader("Content-type", "application/json");
        httpPost.setHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");

        CloseableHttpResponse response = client.execute(httpPost);

        HttpEntity entity = response.getEntity();
        String body = "";
        if (entity != null) {
            body = EntityUtils.toString(entity, encoding);
        }
        EntityUtils.consume(entity);
        response.close();

        //logger.debug("header: " + httpPost.getAllHeaders());
        logger.debug("body: " + body);
        logger.debug("[send] end");

        return body;
    }


    /**
     * 异步回调同时
     * @param callbackUrl
     */
    private void upChainAsyncCallBack(String callbackUrl, String tableName, String txHash) {
        logger.debug("[upChainAsyncCallBack] start");
        logger.debug("callbackUrl: " + callbackUrl);
        Thread thread = new Thread(() -> {

            for (long time: CALLBACK_TIMES) {
                logger.debug("[upChainAsyncCallBack thread] time: " + time);
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
                        logger.debug("queryByTxHash...");
                        logger.debug("tableName: " + tableName);
                        logger.debug("txHash: [{}]", txHash);
                        List<HashMap<String, String>> resultList = sqlMapClient.queryForList("queryByTxHash", parameterMap);
                        logger.debug("queryByTxHash success");
                        logger.debug("result num: " + resultList.size());
                        if (!resultList.isEmpty()) {
                            HashMap<String, String> resultMap = resultList.get(0);
                            logger.debug("onChain: " + resultMap.get("onchain"));
                            if ("1".equals(resultMap.get("onchain"))) {

                                JSONObject postDataJson = new JSONObject();
                                postDataJson.put("txHash", txHash);
                                postDataJson.put("blockAddTime", resultMap.get("blockaddtime"));
                                postDataJson.put("blockNumber", resultMap.get("blockheight"));
                                try {
                                    String resultString = send(callbackUrl, postDataJson, "utf-8");
                                    JSONObject resultJson = JSONObject.parseObject(resultString);
                                    if (resultJson.getBoolean("success")) {
                                        logger.debug("callback send success");
                                        return; // 推送成功，不再推送。
                                    }
                                } catch (Exception e) {
                                    logger.error("send fail: " + e.getMessage());
                                    e.printStackTrace();
                                }
                                break; // 一次推送，跳出循环
                            }
                        }
                    } catch (Exception e) {
                        logger.error(e.getMessage());
                        e.printStackTrace();
                        if (++exceptionCount == 10) {
                            logger.error("An exception exits the loop ten times");
                            break; // 十次异常退出循环
                        }
                    }
                }
            }
        });
        thread.start();
        logger.debug("[upChainAsyncCallBack] end");
    }


    void testCallback(String callbackUrl) throws IOException {
        logger.debug("[testCallback] start");
        JSONObject data = new JSONObject();
        data.put("ok", 123);
        String res = send(callbackUrl, data, "utf-8");
        logger.debug("result: " + res);
        logger.debug("[testCallback] end");
    }

    @RequestMapping(
            value = "/address",
            method = RequestMethod.POST,
            produces = "application/json;charset=UTF-8")
    String rpcInterface(@RequestBody JSONObject dataJson) {
        // 处理输入的 json 数据
        // 返回响应消息
        return "reponseDataJsonString";
    }

//    /**
//     * 业务数据上链存证接口, 将业务信息进行上链存证。不对业务数据进行关联。
//     * @param dataJson
//     * @return
//     */
//    @RequestMapping(value = "/obst/service/S_ST_01", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
//    String upChain(@RequestBody JSONObject dataJson) {
//        logger.debug("[upChain] start");
//        JSONObject returnJson = new JSONObject();
//        try {
//            logger.debug("request json: " + dataJson.toJSONString());
//
//            // 从json中获取签名用公钥进行验证
//            String sign = (String) dataJson.get("sign");
//            dataJson.remove("sign");
//            String dataString = JSONObject.toJSONString(dataJson, SerializerFeature.PrettyFormat);
//            // 验签失败，返回
//            if (!gmSm2VerifySignature(dataString.getBytes(StandardCharsets.UTF_8), Hex.decode(sign))) {
//                logger.debug("gmSm2VerifySignature fail");
//                returnJson.put("code", ResultCode.SIGN_VERIFY_FAIL.getCode());
//                returnJson.put("msg", ResultCode.SIGN_VERIFY_FAIL.getMsg());
//                return returnJson.toJSONString();
//            }
//
//            String tableName = (String)dataJson.get("tableName");
//            String systemId = (String) dataJson.get("systemId");
//            String requestSn = (String) dataJson.get("requestSn");
//            String dataInfo = dataJson.getJSONObject("dataInfo").toJSONString();
//            String businessId = (String) dataJson.get("businessId");
//            String callbackUrl = (String)dataJson.get("callbackUrl");
//            String invokeTime = (String) dataJson.get("invokeTime");
//            String attach = (String) dataJson.get("attach");
//
//            String privateKey = "0x67d7273b1e670ca5b0482381b631cee28a33ac03d8839244ae97df6f74bc027d";
//            String publicKey = "0x0379f6feff204503fd71e6ecb16b1f190d70aae14358ac79e2739fcc2779ecc18e" +
//                    "c8e25f861839a8607dc941ddc6c75116b89d7a2cbd6f23189d2265ebb4edd7";
//            String secretKey = "0123456789abcdef";
//            String txHash = getHashValue(dataInfo);
//            String onChain = "1";
//            try {
//                Map<String, String> dataMap = new HashMap<>();
//                dataMap.put("tableName", tableName);
//                dataMap.put("systemId", systemId);
//                dataMap.put("requestSn", requestSn);
//                dataMap.put("dataInfo", dataInfo);
//                dataMap.put("secretKey", secretKey);
//                dataMap.put("privateKey", privateKey);
//                dataMap.put("publicKey", publicKey);
//                dataMap.put("txHash", txHash);
//                dataMap.put("onchain", onChain);
//                sqlMapClient.insert("upChain2", dataMap);
//
//                // 查询生成的哈希值
//                Map<String, String> parameterMap = new HashMap<>();
//                parameterMap.put("tableName", tableName);
//                parameterMap.put("requestSn", requestSn);
//                List<HashMap<String, String>> resultList = sqlMapClient.queryForList("queryByRequestSn", parameterMap);
//                Map<String, String> resultMap = resultList.get(0);
//                String resultTxHash = resultMap.get("txHash");
//
//                logger.debug("UP_TX_SUCCESS");
//                returnJson.put("code", ResultCode.UP_TX_SUCCESS.getCode());
//                returnJson.put("msg", ResultCode.UP_TX_SUCCESS.getMsg());
//                returnJson.put("txHash", resultTxHash);
//
//                if (callbackUrl != null) {
//                    upChainAsyncCallBack(callbackUrl, tableName, txHash);
//                }
//            } catch (Exception e) {
//                logger.error(e.getMessage());
//                e.printStackTrace();
//                returnJson.put("code", ResultCode.UP_TX_FAIL.getCode());
//                returnJson.put("msg", ResultCode.UP_TX_FAIL.getMsg());
//            }
//        } catch (Exception e) {
//            logger.error(e.getMessage());
//            e.printStackTrace();
//            returnJson.clear();
//            returnJson.put("code", ResultCode.PARAMETER_ERROR.getCode());
//            returnJson.put("msg", ResultCode.PARAMETER_ERROR.getMsg());
//        }
//        logger.debug(returnJson.toJSONString());
//        logger.debug("[upChain] end");
//        return returnJson.toJSONString();
//    }




    /**
     * 业务数据上链存证接口, 将业务信息进行上链存证。不对业务数据进行关联。
     * @param dataJson
     * @return
     */
    @RequestMapping(value = "/obst/service/S_ST_01",
            method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    String upChain(@RequestBody JSONObject dataJson) {
        logger.debug("[upChain] start");
        JSONObject returnJson = new JSONObject();
        try {
            logger.debug("request json: " + dataJson.toJSONString());
            // 从json中获取签名用公钥进行验证
            String sign = (String) dataJson.get("sign");
            dataJson.remove("sign");
            String dataString = JSONObject.toJSONString(dataJson, SerializerFeature.PrettyFormat);
            // 验签失败，返回
            if (!gmSm2VerifySignature(dataString.getBytes(StandardCharsets.UTF_8), Hex.decode(sign))) {
                logger.debug("gmSm2VerifySignature fail");
                returnJson.put("code", ResultCode.SIGN_VERIFY_FAIL.getCode());
                returnJson.put("msg", ResultCode.SIGN_VERIFY_FAIL.getMsg());
                return returnJson.toJSONString();
            }
            // 获取json数据
            String tableName = (String)dataJson.get("tableName");
            String systemId = (String) dataJson.get("systemId");
            String requestSn = (String) dataJson.get("requestSn");
            String dataInfo = dataJson.getJSONObject("dataInfo").toJSONString();
            String businessId = (String) dataJson.get("businessId");
            String callbackUrl = (String)dataJson.get("callbackUrl");
            String invokeTime = (String) dataJson.get("invokeTime");
            String attach = (String) dataJson.get("attach");

            // 根据systemId查找公私钥
//            Map<String, String> parameterMap1 = new HashMap<>();
//            parameterMap1.put("systemId", systemId);
//            List<HashMap<String, String>> resultList1 = sqlMapClient.queryForList("queryForKey", parameterMap1);
//            HashMap<String, String> resultMap1 = resultList1.get(0);
//            String privateKey = resultMap1.get("privateKey");
//            String publicKey = resultMap1.get("publicKey");
            String privateKey = "d6c83aee4bfbeb135a2dcef8c803b186d0678a99002b09d3c60c22aca7105005";
            String publicKey = "2204404536ab867d9a964bfcc5e6fdaa7d77e509ce5891d38b3ebbb036e5c225994" +
                    "597ea6d0bdff3539fd3062b3943a1c7dd75d173f35101b71298e9f7f08d51";
            String secretKey = "0123456789abcdef";

            try {
                // 交易上链
                Map<String, String> dataMap = new HashMap<>();
                dataMap.put("tableName", tableName);
                dataMap.put("systemId", systemId);
                dataMap.put("requestSn", requestSn);
                dataMap.put("dataInfo", dataInfo);
                dataMap.put("secretKey", secretKey);
                dataMap.put("privateKey", privateKey);
                dataMap.put("publicKey", publicKey);
                logger.debug("upChain...");
                String sql = "INSERT INTO " + dataMap.get("tableName") + " VALUES (" +
                        "\'" + dataMap.get("systemId") + "\', " +
                        "\'" + dataMap.get("requestSn") + "\', " +
                        "\'" + dataMap.get("dataInfo") + "\', " +
                        "\'" + dataMap.get("secretKey") + "\', " +
                        "\'" + dataMap.get("privateKey") + "\', " +
                        "\'" + dataMap.get("publicKey") + "\')";
                logger.debug("sql: " + sql);
                Object obj = sqlMapClient.insert("upChain", dataMap);
                //logger.debug(obj.toString());
                logger.debug("upChain success");
                // 查询生成的哈希值
                Map<String, String> parameterMap = new HashMap<>();
                parameterMap.put("tableName", tableName);
                parameterMap.put("requestSn", requestSn);
                logger.debug("queryByRequestSn...");
                String sql2 = "SELECT * FROM " + parameterMap.get("tableName") + " " +
                        "WHERE requestsn=\'" + parameterMap.get("requestSn") + "\'";
                //
                 Thread.sleep(10000);
                logger.debug("sql2: " + sql2);
                List<HashMap<String, String>> resultList =
                        sqlMapClient.queryForList("queryByRequestSn", parameterMap);
                logger.debug("queryByRequestSnl success");
                Map<String, String> resultMap = resultList.get(0);
                String resultTxHash = resultMap.get("txhash");
                logger.debug("txHash: " + resultTxHash);

                // 返回响应
                returnJson.put("code", ResultCode.UP_TX_SUCCESS.getCode());
                returnJson.put("msg", ResultCode.UP_TX_SUCCESS.getMsg());
                returnJson.put("txHash", resultTxHash);

                // 如果有回调地址，则进行异步回调
                if (callbackUrl != null) {
                    upChainAsyncCallBack(callbackUrl, tableName, resultTxHash);
                }
            } catch (Exception e) {
                e.printStackTrace();
                returnJson.put("code", ResultCode.UP_TX_FAIL.getCode());
                returnJson.put("msg", ResultCode.UP_TX_FAIL.getMsg());
            }
        } catch (Exception e) {
            e.printStackTrace();
            returnJson.clear();
            returnJson.put("code", ResultCode.PARAMETER_ERROR.getCode());
            returnJson.put("msg", ResultCode.PARAMETER_ERROR.getMsg());
        }

        logger.debug("[upChain] end");
        return returnJson.toJSONString();
    }


    /**
     * 根据交易hash查证接口, 可根据存证时交易hash进行查证，返回业务数据上链存证信息。
     * @param dataJson
     * @return
     */
    @RequestMapping(value = "/obst/service/S_ST_02", 
            method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    String queryByTxHash(@RequestBody JSONObject dataJson) {
        logger.debug("[queryByTxHash] start");

        JSONObject returnJson = new JSONObject();
        try {
            logger.debug("request json: " + dataJson.toJSONString());

            // 从json中获取签名用公钥进行验证
            String sign = (String) dataJson.get("sign");
            dataJson.remove("sign");
            String dataString = JSONObject.toJSONString(dataJson, SerializerFeature.PrettyFormat);
            // 验签失败，返回
            if (!gmSm2VerifySignature(dataString.getBytes(StandardCharsets.UTF_8), Hex.decode(sign))) {
                logger.debug("gmSm2VerifySignature fail");
                returnJson.put("code", ResultCode.SIGN_VERIFY_FAIL.getCode());
                returnJson.put("msg", ResultCode.SIGN_VERIFY_FAIL.getMsg());
                return returnJson.toJSONString();
            }

            // 获取json数据
            String tableName = (String) dataJson.get("tableName");
            String systemId = (String) dataJson.get("systemId");
            String requestSn = (String) dataJson.get("requestSn");
            String txHash = (String) dataJson.get("txHash");
            String invokeTime = (String) dataJson.get("invokeTime");

            // 根据 txHash 查询上链交易数据
            Map<String, String> parameterMap = new HashMap<>();
            parameterMap.put("tableName", tableName);
            parameterMap.put("systemId", systemId);
            parameterMap.put("txHash", txHash);
            List<HashMap<String, String>> resultList = sqlMapClient.queryForList("queryByTxHash", parameterMap);

            if (!resultList.isEmpty()) {
                HashMap<String, String> resultMap = resultList.get(0);
                logger.debug("onChain: " + resultMap.get("onchain"));
                if ("1".equals(resultMap.get("onchain"))) {
                    logger.debug("UP_CHAIN_SUCCESS");
                    returnJson.put("code", ResultCode.UP_CHAIN_SUCCESS.getCode());
                    returnJson.put("msg", ResultCode.UP_CHAIN_SUCCESS.getMsg());
                    returnJson.put("data", resultMap.get("dataInfo"));
                } else {
                    logger.debug("UP_CHAIN_WAITTING");
                    returnJson.put("code", ResultCode.UP_CHAIN_WAITTING.getCode());
                    returnJson.put("msg", ResultCode.UP_CHAIN_WAITTING.getMsg());
                    returnJson.put("data", resultMap.get("dataInfo"));
                }
            } else {
                logger.debug("UP_CHAIN_FAIL");
                returnJson.put("code", ResultCode.UP_CHAIN_FAIL.getCode());
                returnJson.put("msg", ResultCode.UP_CHAIN_FAIL.getMsg());
            }
        } catch (Exception e) {
            logger.debug("PARAMETER_ERROR: " + e.getMessage());
            e.printStackTrace();
            returnJson.clear();
            returnJson.put("code", ResultCode.PARAMETER_ERROR.getCode());
            returnJson.put("msg", ResultCode.PARAMETER_ERROR.getMsg());
        }

        logger.debug("[queryByTxHash] end");
        return returnJson.toJSONString();
    }


    /**
     * 业务数据验证接口, 可验证业务数据hash在链上存证结果。
     * @param dataJson
     * @return
     */
    @RequestMapping(value = "/obst/service/S_ST_03",
            method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    String verifyTxDataInfo(@RequestBody JSONObject dataJson) {
        logger.debug("[verifyTxDataInfo] start");

        JSONObject returnJson = new JSONObject();
        try {
            logger.debug("request json: " + dataJson.toJSONString());

            // 从json中获取签名用公钥进行验证
            String sign = (String) dataJson.get("sign");
            dataJson.remove("sign");
            String dataString = JSONObject.toJSONString(dataJson, SerializerFeature.PrettyFormat);
            // 验签失败，返回
            if (!gmSm2VerifySignature(dataString.getBytes(StandardCharsets.UTF_8), Hex.decode(sign))) {
                logger.debug("gmSm2VerifySignature fail");
                returnJson.put("code", ResultCode.SIGN_VERIFY_FAIL.getCode());
                returnJson.put("msg", ResultCode.SIGN_VERIFY_FAIL.getMsg());
                return returnJson.toJSONString();
            }

            // 获取json数据
            String tableName = (String) dataJson.get("tableName");
            String systemId = (String) dataJson.get("systemId");
            String requestSn = (String) dataJson.get("requestSn");
            String txHash = (String) dataJson.get("txHash");
            String invokeTime = (String) dataJson.get("invokeTime");

            // 根据 txHash 查询上链交易数据
            Map<String, String> parameterMap = new HashMap<>();
            parameterMap.put("tableName", tableName);
            parameterMap.put("systemId", systemId);
            parameterMap.put("txHash", txHash);
            List<HashMap<String,Object>> resultList = sqlMapClient.queryForList("queryByTxHash", parameterMap);

            if (!resultList.isEmpty()) {
                Map<String, Object> resultMap = resultList.get(0);
//                JSONObject resultMap = JSONObject.parseObject((String) resultList.get(0).get("dataInfo"));
                JSONObject dataInfo = dataJson.getJSONObject("dataInfo");

                // 对数据进行验证，比较dataInfo内容是否一致
                boolean same = true;
                for (String key: dataInfo.keySet()) {
                    logger.debug("compare: " + dataInfo.get(key) + " ~ " + resultMap.get(key));
                    if (!dataInfo.get(key).equals(resultMap.get(key))) {
                        logger.debug(dataInfo.get(key) + " != " + resultMap.get(key));
                        same = false;
                        break;
                    }
                }
                if (same) {
                    logger.debug("VERIFY_TX_SUCCESS");
                    returnJson.put("code", ResultCode.VERIFY_TX_SUCCESS.getCode());
                    returnJson.put("msg", ResultCode.VERIFY_TX_SUCCESS.getMsg());
                } else {
                    logger.debug("VERIFY_TX_FAIL");
                    returnJson.put("code", ResultCode.VERIFY_TX_FAIL.getCode());
                    returnJson.put("msg", ResultCode.VERIFY_TX_FAIL.getMsg());
                }
            } else {
                logger.debug("UP_CHAIN_FAIL");
                returnJson.put("code", ResultCode.UP_CHAIN_FAIL.getCode());
                returnJson.put("msg", ResultCode.UP_CHAIN_FAIL.getMsg());
            }
        } catch (Exception e) {
            logger.debug(e.getMessage());
            e.printStackTrace();
            returnJson.clear();
            logger.debug("PARAMETER_ERROR");
            returnJson.put("code", ResultCode.PARAMETER_ERROR.getCode());
            returnJson.put("msg", ResultCode.PARAMETER_ERROR.getMsg());
        }

        logger.debug("[verifyTxDataInfo] end");
        return returnJson.toJSONString();
    }


    /**
     * 补偿查询接口, 如果异步推送未收到结果，可根据该接口进行主动查询。
     * @param dataJson
     * @return
     */
    @RequestMapping(value = "/obst/service/S_ST_04",
            method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    String compensateQuery(@RequestBody JSONObject dataJson) {
        logger.debug("[compensateQuery] start");

        JSONObject returnJson = new JSONObject();
        try {
            logger.debug("request json: " + dataJson.toJSONString());

            // 从json中获取签名用公钥进行验证
            String sign = (String) dataJson.get("sign");
            dataJson.remove("sign");
            String dataString = JSONObject.toJSONString(dataJson, SerializerFeature.PrettyFormat);
            //logger.debug("dataString: " + dataString);
            // 验签失败，返回
            if (!gmSm2VerifySignature(dataString.getBytes(StandardCharsets.UTF_8), Hex.decode(sign))) {
                logger.debug("gmSm2VerifySignature fail");
                returnJson.put("code", ResultCode.SIGN_VERIFY_FAIL.getCode());
                returnJson.put("msg", ResultCode.SIGN_VERIFY_FAIL.getMsg());
                return returnJson.toJSONString();
            }

            // 获取json数据
            String tableName = (String) dataJson.get("tableName");
            String systemId = (String) dataJson.get("systemId");
            String requestSn = (String) dataJson.get("requestSn");
            String businessId = (String) dataJson.get("businessId");
            String searchRequestSn = (String) dataJson.get("searchRequestSn");
            String invokeTime = (String) dataJson.get("invokeTime");

            // 根据 searchRequestSn 补偿查询
            Map<String, String> parameterMap = new HashMap<>();
            parameterMap.put("tableName", tableName);
            parameterMap.put("searchRequestSn", searchRequestSn);
            List<HashMap<String,String>> resultList = sqlMapClient.queryForList("compensateQuery", parameterMap);
            logger.debug("result num: " + resultList.size());
            if (!resultList.isEmpty()) {
                logger.debug("UP_CHAIN_SUCCESS");
                HashMap<String, String> resultMap = resultList.get(0);
                String onChain = resultMap.get("onchain");
                logger.debug("onChain: [{}] [{}]", onChain, "1".equals(onChain));
                if ("1".equals(onChain)) {
                    logger.debug("sql query empty");
                    returnJson.put("code", ResultCode.UP_CHAIN_SUCCESS.getCode());
                    returnJson.put("msg", ResultCode.UP_CHAIN_SUCCESS.getMsg());
                    returnJson.put("data", resultMap.get("dataInfo"));
                    JSONObject data = new JSONObject();
                    data.put("txHash", resultMap.get("txHash"));
                    data.put("blockAddTime", resultMap.get("timestamp"));
                    data.put("blockNumber", resultMap.get("blockNumber"));
                    returnJson.put("data", data);
                } else {
                    logger.debug("UP_CHAIN_WAITTING");
                    returnJson.put("code", ResultCode.UP_CHAIN_WAITTING.getCode());
                    returnJson.put("msg", ResultCode.UP_CHAIN_WAITTING.getMsg());
                }
            } else {
                logger.debug("UP_CHAIN_FAIL");
                returnJson.put("code", ResultCode.UP_CHAIN_FAIL.getCode());
                returnJson.put("msg", ResultCode.UP_CHAIN_FAIL.getMsg());
            }

        } catch (Exception e) {
            logger.debug(e.getMessage());
            e.printStackTrace();
            returnJson.clear();
            logger.debug("PARAMETER_ERROR");
            returnJson.put("code", ResultCode.PARAMETER_ERROR.getCode());
            returnJson.put("msg", ResultCode.PARAMETER_ERROR.getMsg());
        }

        logger.debug("[compensateQuery] end");
        return returnJson.toJSONString();
    }

}


//    private void upChainAsyncCallBack(String callbackUrl, String txHash) {
//        logger.debug("[upChainAsyncCallBack] start");
//        logger.debug("callbackUrl: " + callbackUrl);
//        Thread thread = new Thread(() -> {
//
//            TransactionReceipt transactionReceipt = null;
//
//            for (long time: CALLBACK_TIMES) {
//                logger.debug("[upChainAsyncCallBack thread] time: " + time);
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
//                                logger.debug("body: " + body);
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
//        logger.debug("[upChainAsyncCallBack] end");
//    }