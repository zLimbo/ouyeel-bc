package com.ouyeel.obfm.test;

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

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Random;
import java.util.UUID;

public class TestPost {

    static public void main(String[] args) throws IOException, InterruptedException {


        for (int i = 0; i < 10000; ++i) {
            Thread.sleep(3000);
            JSONObject json = randomInvoiceJson();
            System.out.println(JSONObject.toJSONString(json, true));

            String response = send("http://127.0.0.1:8080/obst/service/S_ST_01", json, "UTF-8");
            System.out.println("hash: " + response);
        }
    }

    static JSONObject randomInvoiceJson() {
        Random random = new Random();

        String systemId = String.format("%012d", Math.abs(random.nextInt()));
        String requestSn = UUID.randomUUID().toString();
        String invokeTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis());
        String businessId = String.format("%012d", Math.abs(random.nextInt()));
        String callbackUrl = "https://127.0.0.1/callback";
        String keyId = String.format("%06d", Math.abs(random.nextInt() % (int)1e7));
        String accountId = String.format("%06d", Math.abs(random.nextInt() % (int)1e7));
        String sm4Key = "0123456789abcdef0123456789abcdef";
        String sm4Iv = "0123456789abcdef0123456789abcdef";
        String priKey = "d6c83aee4bfbeb135a2dcef8c803b186d0678a99002b09d3c60c22aca7105005";
        String pubKey = "2204404536ab867d9a964bfcc5e6fdaa7d77e509ce5891d38b3ebbb036e5c225994597ea6d0bdff3539fd3062b3943a1c7dd75d173f35101b71298e9f7f08d51";
        JSONObject dataInfo = new InvoiceInfo().genDataInfo();

        JSONObject json = new JSONObject();
        json.put("systemId", systemId);
        json.put("requestSn", requestSn);
        json.put("invokeTime", invokeTime);
        json.put("businessId", businessId);
        json.put("callbackUrl", callbackUrl);
        json.put("callbackServiceId", callbackUrl);
        json.put("keyId", keyId);
        json.put("accountId", accountId);
        json.put("dataInfo", dataInfo);
        json.put("tableName", "invoice_s");

        return json;
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
//        logger.debug("[send] start");
//        System.out.println("send " + Thread.currentThread().getName());

        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);

        StringEntity stringEntity = new StringEntity(jsonObject.toString(), "utf-8");
        stringEntity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
        httpPost.setEntity(stringEntity);
//        logger.debug("request url: " + url);

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
//        logger.debug("body: [{}]", body);
//        logger.debug("[send] end");

        return body;
    }

}
