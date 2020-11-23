package com.zlimbo.bcweb.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zlimbo.bcweb.domain.Invoice;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;


@Controller
@RequestMapping("")
public class PostController {

    private static String systemId = "000001";
    private static String postUrl = "http://127.0.0.1:8080";
    private static String callbackUrl = "http://127.0.0.1:8082/callback";

    private String requestSn;

    @PostMapping("/callback")
    @ResponseBody
    String callback(@RequestBody JSONObject dataJson) throws IOException {
        System.out.println("============> [callback] start");
        System.out.println("data: " + dataJson);
        JSONObject successJson = new JSONObject();
        successJson.put("success", true);
        successJson.put("msg", "回调接收成功");
        System.out.println("============> [callback] end");
        return successJson.toJSONString();
    }

    @RequestMapping(value = "/randomInvoice", method = RequestMethod.GET)
    @ResponseBody
    public String randomInvoice() {
        System.out.println("--------------------------------------------randomInvoice ok");
        return JSON.toJSONString(Invoice.getRandomInvoice());
    }


    @GetMapping("/S_ST_01")
    String upChain(Model model) throws IOException {
        System.out.println("============> [upChain get] start");
        requestSn = UUID.randomUUID().toString();
        model.addAttribute("systemId", systemId);
        model.addAttribute("requestSn", requestSn);
        model.addAttribute("invoice", new Invoice());
        System.out.println("============> [upChain get] end");
        return "post/upChain";
    }

    @PostMapping("/S_ST_01")
    @ResponseBody
    String upChain(@ModelAttribute Invoice invoice) throws IOException {
        System.out.println("============> [upChain post] start");
        JSONObject dataInfo = JSONObject.parseObject(JSON.toJSONString(invoice));
        JSONObject postData = new JSONObject();
        postData.put("tableName", "invoice");
        postData.put("systemId", systemId);
        postData.put("requestSn", requestSn);
        postData.put("dataInfo", dataInfo);
        postData.put("businessId", invoice.getInvoiceNo());
        postData.put("callbackUrl", callbackUrl);
        postData.put("invokeTime", String.valueOf(System.currentTimeMillis()));
        postData.put("sign", "0x123456789abcdef");
        String response = send(postUrl + "/obst/service/S_ST_01", postData, "utf-8");
        JSONObject resJson = JSONObject.parseObject(response);
        System.out.println("============> [upChain post] end");
        return response;
    }


    @GetMapping("/S_ST_02")
    String verifyByTxHash() {
        return null;
    }

    @PostMapping("/S_ST_02")
    String verifyByTxHash(@RequestBody JSObject json) {
        return null;
    }


    @GetMapping("/S_ST_03")
    String businessDataValidation() {
        return null;
    }

    @PostMapping("/S_ST_03")
    String businessDataValidation(@RequestBody JSObject json) {
        return null;
    }


    @GetMapping("/S_ST_04")
    String compensateQuery() {
        return null;
    }

    @PostMapping("/S_ST_04")
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
