package com.ouyeel.obfm.fm.business.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.ibatis.common.resources.Resources;
import com.ibatis.sqlmap.client.SqlMapClient;
import com.ibatis.sqlmap.client.SqlMapClientBuilder;
import com.ouyeel.obfm.fm.business.IOrderService;
import com.ouyeel.obfm.fm.service.ServiceFM01;
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

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


enum ResultCode {
    SUCCESS(100, "成功"),
    FAIL(200, "失败"),
    PARAMETER_ERROR(201, "参数错误"),
    UP_TX_SUCCESS(103, "上传交易成功"),
    UP_TX_FAIL(203, "上传交易失败"),
    UP_CHAIN_SUCCESS(104, "数据已上链，请检查参数"),
    UP_CHAIN_WAITTING(105, "数据上链中，请稍等"),
    UP_CHAIN_FAIL(204, "上链失败"),
    VERIFY_TX_SUCCESS(106, "数据验证成功"),
    VERIFY_TX_FAIL(206, "数据验证失败"),
    SIGN_VERIFY_FAIL(207, "签名验证失败"),
    NO_REQUEST(208, "请求不存在");

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


public class OrderService implements IOrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    private static final long[] CALLBACK_TIMES = {3000L, 1000L * 60, 1000L * 60 * 3};

    private static SqlMapClient sqlMapClient = null;

    static {
        Logger staticLogger = LoggerFactory.getLogger(OrderService.class);
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

    @Override
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
            String callbackUrl = (String)inJson.get("callbackUrl");
            String privateKey = (String)inJson.get("privateKey");
            String publicKey = (String)inJson.get("publicKey");
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
                Object obj = sqlMapClient.insert("upChain", dataMap);
                logger.debug("upChain success");
                // 查询生成的哈希值
                Map<String, String> parameterMap = new HashMap<>();
                parameterMap.put("tableName", tableName);
                parameterMap.put("requestSn", requestSn);
                logger.debug("queryByRequestSn...");
                Thread.sleep(8000);
                List<HashMap<String, String>> resultList =
                        sqlMapClient.queryForList("queryByRequestSn", parameterMap);
                logger.debug("queryByRequestSnl success");
                Map<String, String> resultMap = resultList.get(0);
                String resultTxHash = resultMap.get("txhash");

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

    
    @Override
    public JSONObject queryChain(JSONObject inJson) {
        logger.debug("[queryByTxHash] start");

        JSONObject outJson = new JSONObject();
        try {
            logger.debug("request json: " + inJson.toJSONString());

            // 获取json数据
            String tableName = (String) inJson.get("tableName");
            String systemId = (String) inJson.get("systemId");
            String txHash = (String) inJson.get("txHash");

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
                    outJson.put("code", ResultCode.UP_CHAIN_SUCCESS.getCode());
                    outJson.put("msg", ResultCode.UP_CHAIN_SUCCESS.getMsg());
                    outJson.put("data", resultMap.get("dataInfo"));
                } else {
                    logger.debug("UP_CHAIN_WAITTING");
                    outJson.put("code", ResultCode.UP_CHAIN_WAITTING.getCode());
                    outJson.put("msg", ResultCode.UP_CHAIN_WAITTING.getMsg());
                    outJson.put("data", resultMap.get("dataInfo"));
                }
            } else {
                logger.debug("UP_CHAIN_FAIL");
                outJson.put("code", ResultCode.UP_CHAIN_FAIL.getCode());
                outJson.put("msg", ResultCode.UP_CHAIN_FAIL.getMsg());
            }
        } catch (Exception e) {
            logger.debug("PARAMETER_ERROR: " + e.getMessage());
            e.printStackTrace();
            outJson.clear();
            outJson.put("code", ResultCode.PARAMETER_ERROR.getCode());
            outJson.put("msg", ResultCode.PARAMETER_ERROR.getMsg());
        }

        logger.debug("[queryByTxHash] end");
        return outJson;
    }

    @Override
    public JSONObject checkChain(JSONObject inJson) {
        logger.debug("[verifyTxDataInfo] start");

        JSONObject outJson = new JSONObject();
        try {
            logger.debug("request json: " + inJson.toJSONString());

            // 获取json数据
            String tableName = (String) inJson.get("tableName");
            String systemId = (String) inJson.get("systemId");
            String txHash = (String) inJson.get("txHash");

            // 根据 txHash 查询上链交易数据
            Map<String, String> parameterMap = new HashMap<>();
            parameterMap.put("tableName", tableName);
            parameterMap.put("systemId", systemId);
            parameterMap.put("txHash", txHash);
            List<HashMap<String,Object>> resultList = sqlMapClient.queryForList("queryByTxHash", parameterMap);

            if (!resultList.isEmpty()) {
                Map<String, Object> resultMap = resultList.get(0);
//                JSONObject resultMap = JSONObject.parseObject((String) resultList.get(0).get("dataInfo"));
                JSONObject dataInfo = inJson.getJSONObject("dataInfo");

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
                    outJson.put("code", ResultCode.VERIFY_TX_SUCCESS.getCode());
                    outJson.put("msg", ResultCode.VERIFY_TX_SUCCESS.getMsg());
                } else {
                    logger.debug("VERIFY_TX_FAIL");
                    outJson.put("code", ResultCode.VERIFY_TX_FAIL.getCode());
                    outJson.put("msg", ResultCode.VERIFY_TX_FAIL.getMsg());
                }
            } else {
                logger.debug("UP_CHAIN_FAIL");
                outJson.put("code", ResultCode.UP_CHAIN_FAIL.getCode());
                outJson.put("msg", ResultCode.UP_CHAIN_FAIL.getMsg());
            }
        } catch (Exception e) {
            logger.debug(e.getMessage());
            e.printStackTrace();
            outJson.clear();
            logger.debug("PARAMETER_ERROR");
            outJson.put("code", ResultCode.PARAMETER_ERROR.getCode());
            outJson.put("msg", ResultCode.PARAMETER_ERROR.getMsg());
        }

        logger.debug("[verifyTxDataInfo] end");
        return outJson;
    }

    @Override
    public JSONObject reQueryChain(JSONObject inJson) {
        logger.debug("[compensateQuery] start");

        JSONObject outJson = new JSONObject();
        try {
            logger.debug("request json: " + inJson.toJSONString());

            // 获取json数据
            String tableName = (String) inJson.get("tableName");
            String searchRequestSn = (String) inJson.get("searchRequestSn");

            // 根据 searchRequestSn 补偿查询
            Map<String, String> parameterMap = new HashMap<>();
            parameterMap.put("tableName", tableName);
            parameterMap.put("searchRequestSn", searchRequestSn);
            List<HashMap<String,Object>> resultList = sqlMapClient.queryForList("queryByRequestSn", parameterMap);

            if (!resultList.isEmpty()) {
                logger.debug("UP_CHAIN_SUCCESS");
                HashMap<String, Object> resultMap = resultList.get(0);
                if ("1".equals(resultMap.get("onchain"))) {
                    logger.debug("sql query empty");
                    outJson.put("code", ResultCode.UP_CHAIN_SUCCESS.getCode());
                    outJson.put("msg", ResultCode.UP_CHAIN_SUCCESS.getMsg());
                    outJson.put("data", resultMap.get("dataInfo"));
                    JSONObject data = new JSONObject();
                    data.put("txHash", resultMap.get("txHash"));
                    data.put("blockAddTime", resultMap.get("timestamp"));
                    data.put("blockNumber", resultMap.get("blockNumber"));
                    outJson.put("data", data);
                } else {
                    logger.debug("UP_CHAIN_WAITTING");
                    outJson.put("code", ResultCode.UP_CHAIN_WAITTING.getCode());
                    outJson.put("msg", ResultCode.UP_CHAIN_WAITTING.getMsg());
                }
            } else {
                logger.debug("UP_CHAIN_FAIL");
                outJson.put("code", ResultCode.UP_CHAIN_FAIL.getCode());
                outJson.put("msg", ResultCode.UP_CHAIN_FAIL.getMsg());
            }

        } catch (Exception e) {
            logger.debug(e.getMessage());
            e.printStackTrace();
            outJson.clear();
            logger.debug("PARAMETER_ERROR");
            outJson.put("code", ResultCode.PARAMETER_ERROR.getCode());
            outJson.put("msg", ResultCode.PARAMETER_ERROR.getMsg());
        }

        logger.debug("[compensateQuery] end");
        return outJson;
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
                            if ("1".equals(resultMap.get("onchain"))) {
                                logger.debug("onChain: " + resultMap.get("onchain"));
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
}
