package com.ouyeel.obfm.fm.business.impl;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;


public class AsyncCallback {
    
    final static Logger logger = LoggerFactory.getLogger(AsyncCallback.class);


    static class CallbackThreadTask implements Runnable, Comparable<CallbackThreadTask> {
        private String callbackUrl;
        private String tableName;
        private String requestSn;
        private int callBackTimeIndex;
        private Long callbackTime;

        public CallbackThreadTask(String callbackUrl,
                                  String tableName,
                                  String requestSn,
                                  int callBackTimeIndex) {
            this.callbackUrl = callbackUrl;
            this.tableName = tableName;
            this.requestSn = requestSn;
            this.callBackTimeIndex = callBackTimeIndex;
            this.callbackTime = System.currentTimeMillis() + ChainParam.CALL_BACK_TIMES[callBackTimeIndex];
        }

        @Override
        public int compareTo(CallbackThreadTask rhs) {
            return rhs.callbackTime.compareTo(this.callbackTime);
        }

        @Override
        public void run() {
            /**
             * 上链需要时间，直接回调通常没有结果，浪费资源
             */
            long sleepTime = callbackTime - System.currentTimeMillis();
            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.error(e.getMessage());
                }
            }

            /**
             * 回调成功则返回，否则根据是否需要继续回调继续加入延时的回调任务
             */
            if (AsyncCallback.callback(callbackUrl, tableName, requestSn)) {
                return;
            }
            if (callBackTimeIndex + 1 < ChainParam.CALL_BACK_TIMES.length) {
                CallbackThreadTask callbackTask = new CallbackThreadTask(
                        callbackUrl, tableName, requestSn, callBackTimeIndex + 1);
                threadPoolExecutor.execute(callbackTask);
            }
        }
    }


    /**
     * 线程池，优先队列，回调时间早的先回调
     */
    private static ExecutorService threadPoolExecutor = new ThreadPoolExecutor(
            ChainParam.CORE_POOL_SIZE,
            ChainParam.MAXIMUM_POOL_SIZE,
            ChainParam.KEEP_ALIVET_TIME,
            TimeUnit.MILLISECONDS,
            new PriorityBlockingQueue<Runnable>(),
            Executors.defaultThreadFactory(),
            new ThreadPoolExecutor.AbortPolicy());


    /**
     * 添加回调线程任务
     * @param callbackUrl
     * @param tableName
     * @param requestSn
     */
    public static void addTask(String callbackUrl, String tableName, String requestSn) {
        logger.debug("[addTask] start");
        logger.debug("callbackUrl: [{}] tableName: [{}] requestSn: [{}]", callbackUrl, tableName, requestSn);
        CallbackThreadTask callbackTask = new CallbackThreadTask(callbackUrl, tableName, requestSn, 0);
        threadPoolExecutor.execute(callbackTask);
        logger.debug("[addTask] end");
    }


    /**
     * 查询上链信息，然后回调
     * @return 布尔值：回调是否成功
     */
    private static boolean callback(String callbackUrl, String tableName, String requestSn) {
        logger.debug("[send] start");
        List<Map<String, String>> queryResulList = SqlService.queryCallbackByRequestSn(tableName, requestSn);
        if (queryResulList == null) {
            return false;
        }
        Map<String, String> queryResultMap = queryResulList.get(0);
        String onChain = queryResultMap.get(ChainParam.ON_CHAIN);
        if (ChainParam.ON_CHAIN_SUCCESS.equals(onChain)) {
            JSONObject postJson = new JSONObject();
            postJson.put(ChainParam.TX_HASH, queryResultMap.get(ChainParam.TX_HASH));
            postJson.put(ChainParam.BLOCK_TIME, queryResultMap.get(ChainParam.BLOCK_TIME));
            postJson.put(ChainParam.BLOCK_HEIGHT, queryResultMap.get(ChainParam.BLOCK_HEIGHT));
            String response = null;
            try {
                response = AsyncCallback.send(callbackUrl, postJson, "UTF-8");
            } catch (IOException e) {
                logger.debug("callback send fail");
                logger.debug(e.getMessage());
                e.printStackTrace();
            }
            if (response != null) {
                JSONObject responseJson = JSONObject.parseObject(response);
                if (responseJson.getBoolean(ChainParam.SUCCESS)) {
                    logger.debug("callback success");
                    return true;
                }
            }
        }
        logger.debug("[send] end");
        return false;
    }


    /**
     * http post send
     * @param url
     * @param jsonObject
     * @param encoding
     * @return
     * @throws IOException
     */
    public static String send(String url, JSONObject jsonObject, String encoding) throws IOException {
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

        //logger.debug("header: [{}]", httpPost.getAllHeaders());
        logger.debug("body: [{}]", body);
        logger.debug("[send] end");

        return body;
    }
}
