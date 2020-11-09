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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;


@Controller
@RequestMapping("")
public class SendController {


    @GetMapping("/send/S_ST_01")
    String upChain(Model model) {

        return "upChain";
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
