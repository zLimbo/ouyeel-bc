package com.ouyeel.obfm.fm.business.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.ibatis.sqlmap.client.SqlMapClient;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.apache.http.protocol.HTTP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ouyeel.obfm.fm.business.OrderService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
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

public class ChainOrderService implements OrderService {

    /**
     * 日志
     */
    final Logger logger = LoggerFactory.getLogger(getClass());


    /**
     * 回调时间（3个）
     */
    private final long[] CALLBACK_TIMES = {3000L, 1000L * 60, 1000L * 60 * 3};


    /**
     * ibatis 连接
     */
    private static SqlMapClient sqlMapClient = null;


    public JSONObject upChain(JSONObject inJson) {
        logger.debug("[upChain] start");
        JSONObject outJson = new JSONObject();
        try {
            logger.debug("request json: " + inJson.toJSONString());
            
            // 获取json数据
            String tableName = (String)inJson.get("tableName");
            String systemId = (String) inJson.get("systemId");
            String requestSn = (String) inJson.get("requestSn");
            String dataInfo = inJson.getJSONObject("dataInfo").toJSONString();
            String businessId = (String) inJson.get("businessId");
            String callbackUrl = (String)inJson.get("callbackUrl");
            String invokeTime = (String) inJson.get("invokeTime");
            String attach = (String) inJson.get("attach");

            // 根据systemId查找公私钥
            Map<String, String> parameterMap1 = new HashMap<>();
            parameterMap1.put("systemId", systemId);
            List<HashMap<String, String>> resultList1 = sqlMapClient.queryForList("queryForKey", parameterMap1);
            HashMap<String, String> resultMap1 = resultList1.get(0);
            String privateKey = resultMap1.get("privateKey");
            String publicKey = resultMap1.get("publicKey");
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
                sqlMapClient.insert("upChain", dataMap);

                // 查询生成的哈希值
                Map<String, String> parameterMap = new HashMap<>();
                parameterMap.put("tableName", tableName);
                parameterMap.put("requestSn", requestSn);
                List<HashMap<String, String>> resultList =
                        sqlMapClient.queryForList("queryByRequestSn", parameterMap);
                Map<String, String> resultMap = resultList.get(0);
                String resultTxHash = resultMap.get("txHash");

                // 返回响应
                outJson.put("code", ResultCode.UP_TX_SUCCESS.getCode());
                outJson.put("msg", ResultCode.UP_TX_SUCCESS.getMsg());
                outJson.put("txHash", resultTxHash);

                // 如果有回调地址，则进行异步回调
                if (callbackUrl != null) {
                    upChainAsyncCallBack(callbackUrl, tableName, resultTxHash);
                }
            } catch (Exception e) {
                e.printStackTrace();
                outJson.put("code", ResultCode.UP_TX_FAIL.getCode());
                outJson.put("msg", ResultCode.UP_TX_FAIL.getMsg());
            }
        } catch (Exception e) {
            e.printStackTrace();
            outJson.clear();
            outJson.put("code", ResultCode.PARAMETER_ERROR.getCode());
            outJson.put("msg", ResultCode.PARAMETER_ERROR.getMsg());
        }

        logger.debug("[upChain] end");
        return outJson;
    }

    public JSONObject queryChain(JSONObject inJson) {
        return null;
    }

    public JSONObject checkChain(JSONObject inJson) {
        return null;
    }

    public JSONObject reQueryChain(JSONObject inJson) {
        return null;
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
                        List<HashMap<String,Object>> resultList = sqlMapClient.queryForList("queryByTxHash", parameterMap);

                        if (!resultList.isEmpty()) {
                            HashMap<String, Object> resultMap = resultList.get(0);
                            if ("1".equals(resultMap.get("onChain"))) {
                                logger.debug("onChain: " + resultMap.get("onChain"));
                                JSONObject postDataJson = new JSONObject();
                                postDataJson.put("txHash", txHash);
                                postDataJson.put("blockAddTime", resultMap.get("blockAddTime"));
                                postDataJson.put("blockNumber", resultMap.get("blockNumber"));
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
}
