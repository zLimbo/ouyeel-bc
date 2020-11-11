package com.zlimbo.bcweb.controller;

import com.alibaba.fastjson.JSONObject;
import netscape.javascript.JSObject;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;


@Controller
@RequestMapping("")
public class SendController {


    @GetMapping("/send/S_ST_01")
    @ResponseBody
    String upChain(Model model) throws IOException {

        String jsonStr = "{ \"systemId\":\"1000\", \"requestSn\": \"202004071530500\", \"tableName\": \"upChain\", \"dataInfo\": { \"companyName\":\"钢厂1\", \"originalBusinessId\":\"原始业务单据号\", \"originalBusinessTime\":\"原始业务时间\", \"transactionName\":\"入厂信息\", \"licenseNum\":\"123456\", \"driverName\":\"张三\" }, \"businessId\": \"1234\", \"callbackUrl\": \"http://localhost:8082/cfcaCallback\", \"invokeTime\": \"1585805402901\", \"sign\": \"COMbzExK09YU1NaWoNJCAsFRY7GyjUM9HA4yIuQo3CePQchiCX9ICZZLGnbkV1AjJsSmG9hs5gqUlbon2e2PL5Q1rhtCTvqHIS9r9bf5tQGNzqN699UqlJbmLpmAcmcj9CY+D58ec//JwGVT3fIs4xi1eYUbnTRymaM3vdy+AmU=\", \"attach\": \"0\" }";
        JSONObject jsonData = JSONObject.parseObject(jsonStr);

        String result = send("http://127.0.0.1:8082/obst/service/S_ST_01", jsonData, "utf-8");

        return result;
    }

    @PostMapping("/send/S_ST_01")
    String upChain(@RequestBody JSObject json) {

        return null;
    }


    @GetMapping("/send/S_ST_02")
    String verifyByTxHash() {
        return null;
    }

    @PostMapping("/send/S_ST_02")
    String verifyByTxHash(@RequestBody JSObject json) {
        return null;
    }


    @GetMapping("/send/S_ST_03")
    String businessDataValidation() {
        return null;
    }

    @PostMapping("/send/S_ST_03")
    String businessDataValidation(@RequestBody JSObject json) {
        return null;
    }


    @GetMapping("/send/S_ST_04")
    String compensateQuery() {
        return null;
    }

    @PostMapping("/send/S_ST_04")
    String compensateQuery(@RequestBody JSObject json) {
        return null;
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
}
