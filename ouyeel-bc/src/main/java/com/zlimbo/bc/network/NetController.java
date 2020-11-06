package com.zlimbo.bc.network;

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

public class NetController implements NetService {

    //public static Object upChain(String systemId, String requestSn, )

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


    @Override
    public Result send(String systemId, String requestSn, Object dataInfo, String businessId,
                       String callbackUrl, String invokeTime, String sign, String attach) {
        return null;
    }

    @Override
    public Result verifyByTxHash(String systemId, String requestSn, String txHash,
                                 String invokeTime, String sign) {
        return null;
    }

    @Override
    public Result businessDataValidation(String systemId, String requestSn, String businessId,
                                         String txHash, String businessHash, String invokeTime, String sign) {
        return null;
    }

    @Override
    public Result compensateQuery(String systemId, String requestSn, String businessId,
                                  String searchRequestSn, String invokeTime, String sign) {
        return null;
    }
}
