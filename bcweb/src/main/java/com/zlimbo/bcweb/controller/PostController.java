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
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.*;


@Controller
@RequestMapping("")
public class PostController {

    private static String localUrl = "http://127.0.0.1:8082";
    private static String postUrl = "http://127.0.0.1:8080";
    private static String callbackUrl = localUrl + "/callback";

    private static String systemId = "000001";
    private static long assignBusinessId = 1;

    static String getBusinessId() {
        String returnString = String.format("%020d", assignBusinessId);
        assignBusinessId += 1;
        return returnString;
    }

    private String requestSn;
    private String bussinessId;

    private List<List<String>> onChainTxList = new ArrayList<>();

    @RequestMapping("/onChainTx")
    ModelAndView onChainTx() {
        System.out.println("============> [onChainTx] start");
        ModelAndView modelAndView = new ModelAndView("post/onChainTx");
        for (List<String> onChainTx: onChainTxList) {
            System.out.println(onChainTx.get(0) + " - " + onChainTx.get(1) + " - " + onChainTx.get(2));
        }
        modelAndView.addObject("onChainTxList", onChainTxList);
        System.out.println("============> [onChainTx] end");
        return modelAndView;
    }

    @PostMapping("/callback")
    @ResponseBody
    String callback(@RequestBody JSONObject dataJson) throws IOException {
        System.out.println("============> [callback] start");
        System.out.println("callback data: " + dataJson);
        List<String> onChainTx = Arrays.asList(
                (String)dataJson.get("txHash"),
                (String)dataJson.get("blockAddTime"),
                (String)dataJson.get("blockNumber"));
        onChainTxList.add(onChainTx);
        JSONObject successJson = new JSONObject();
        successJson.put("success", true);
        successJson.put("msg", "回调接收成功");
        //String resultString = send(localUrl + "/callbackSuccess", dataJson, "utf-8");
        //System.out.println("callbackSuccess resultString: " + resultString);
        //callbackSuccess(dataJson);
        System.out.println("============> [callback] end");
        return successJson.toJSONString();
    }


    @PostMapping("/callbackSuccess")
    ModelAndView callbackSuccess(@RequestBody JSONObject dataJson) {
        System.out.println("============> [callbackSuccess] start");
        ModelAndView modelAndView = new ModelAndView("post/callbackSuccess");
        System.out.println("============> [callbackSuccess] end");
        return modelAndView;
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
        bussinessId = getBusinessId();
        model.addAttribute("systemId", systemId);
        model.addAttribute("requestSn", requestSn);
        model.addAttribute("businessId", bussinessId);
        model.addAttribute("invoice", new Invoice());
        System.out.println("============> [upChain get] end");
        return "post/upChain";
    }

    @PostMapping("/S_ST_01")
    ModelAndView upChain(@ModelAttribute Invoice invoice) throws IOException {
        System.out.println("============> [upChain post] start");
        JSONObject dataInfo = JSONObject.parseObject(JSON.toJSONString(invoice));
        JSONObject postData = new JSONObject();
        postData.put("tableName", "tx");
        postData.put("systemId", systemId);
        postData.put("requestSn", requestSn);
        postData.put("dataInfo", dataInfo);
        postData.put("businessId", bussinessId);
        postData.put("callbackUrl", callbackUrl);
        postData.put("invokeTime", String.valueOf(System.currentTimeMillis()));
        postData.put("sign", "0x0123456789abcdef");
        String response = send(postUrl + "/obst/service/S_ST_01", postData, "utf-8");
        JSONObject resultJson = JSONObject.parseObject(response);

        ModelAndView modelAndView = new ModelAndView("post/upChainResult");
        modelAndView.addObject("postData", postData);
        modelAndView.addObject("code", resultJson.get("code"));
        modelAndView.addObject("msg", resultJson.get("msg"));
        modelAndView.addObject("txHash", resultJson.get("txHash"));
        modelAndView.addObject("data", dataInfo);

        System.out.println("============> [upChain post] end");
        return modelAndView;
    }


    @GetMapping("/S_ST_02")
    String queryByTxHash(Model model) {
        System.out.println("============> [queryByTxHash get] start");
        requestSn = UUID.randomUUID().toString();
        model.addAttribute("systemId", systemId);
        model.addAttribute("requestSn", requestSn);

        System.out.println("============> [queryByTxHash get] end");
        return "post/queryByTxHash";
    }

    @PostMapping("/S_ST_02")
    ModelAndView queryByTxHash(@RequestParam Map<String, Object> params) throws IOException {
        System.out.println("============> [queryByTxHash post] start");
        System.out.println("txHash: " + ((String)params.get("txHash")).trim());
        JSONObject postData = new JSONObject();
        postData.put("tableName", "tx");
        postData.put("systemId", systemId);
        postData.put("requestSn", requestSn);
        postData.put("txHash", ((String)params.get("txHash")).trim());
        postData.put("invokeTime", String.valueOf(System.currentTimeMillis()));

        String responseString = send(postUrl + "/obst/service/S_ST_02", postData, "utf-8");
        System.out.println("response: " + responseString);
        JSONObject resultJson = JSONObject.parseObject(responseString);

        ModelAndView modelAndView = new ModelAndView("post/queryByTxHashResult");
        modelAndView.addObject("postData", postData);
        modelAndView.addObject("code", resultJson.get("code"));
        modelAndView.addObject("msg", resultJson.get("msg"));
        modelAndView.addObject("data", resultJson.getJSONObject("data"));

        System.out.println("============> [queryByTxHash post] end");
        return modelAndView;
    }


    @GetMapping("/S_ST_03")
    String verifyTxDataInfo(Model model) {
        System.out.println("============> [verifyTxDataInfo get] start");
        requestSn = UUID.randomUUID().toString();
        bussinessId = getBusinessId();
        model.addAttribute("systemId", systemId);
        model.addAttribute("requestSn", requestSn);
        model.addAttribute("businessId", bussinessId);

        System.out.println("============> [verifyTxDataInfo get] end");
        return "post/verifyTxDataInfo";
    }

    @PostMapping("/S_ST_03")
    ModelAndView verifyTxDataInfo(@RequestParam Map<String, Object> params) throws IOException {
        System.out.println("============> [verifyTxDataInfo post] start");
        JSONObject postData = new JSONObject();
        postData.put("tableName", "tx");
        postData.put("systemId", systemId);
        postData.put("requestSn", requestSn);
        postData.put("businessId", bussinessId);
        postData.put("txHash", ((String)params.get("txHash")).trim());
        postData.put("dataInfo", params.get("dataInfo"));
        postData.put("invokeTime", String.valueOf(System.currentTimeMillis()));

        String responseString = send(postUrl + "/obst/service/S_ST_03", postData, "utf-8");
        System.out.println("response: " + responseString);
        JSONObject resultJson = JSONObject.parseObject(responseString);

        ModelAndView modelAndView = new ModelAndView("post/verifyTxDataInfoResult");
        modelAndView.addObject("postData", postData);
        modelAndView.addObject("code", resultJson.get("code"));
        modelAndView.addObject("msg", resultJson.get("msg"));

        System.out.println("============> [verifyTxDataInfo post] end");
        return modelAndView;
    }


    @GetMapping("/S_ST_04")
    String compensateQuery(Model model) {
        System.out.println("============> [compensateQuery get] start");
        requestSn = UUID.randomUUID().toString();
        bussinessId = getBusinessId();
        model.addAttribute("systemId", systemId);
        model.addAttribute("requestSn", requestSn);
        model.addAttribute("businessId", bussinessId);

        System.out.println("============> [compensateQuery get] end");
        return "post/compensateQuery";
    }

    @PostMapping("/S_ST_04")
    ModelAndView compensateQuery(@RequestParam Map<String, Object> params) throws IOException {
        System.out.println("============> [compensateQuery post] start");

        JSONObject postData = new JSONObject();
        postData.put("tableName", "tx");
        postData.put("systemId", systemId);
        postData.put("requestSn", requestSn);
        postData.put("businessId", bussinessId);
        postData.put("searchRequestSn", ((String)params.get("searchRequestSn")).trim());
        postData.put("invokeTime", String.valueOf(System.currentTimeMillis()));
        postData.put("sign", "0x0123456789abcdef");
        String responseString = send(postUrl + "/obst/service/S_ST_04", postData, "utf-8");
        System.out.println("response: " + responseString);
        JSONObject resultJson = JSONObject.parseObject(responseString);

        ModelAndView modelAndView = new ModelAndView("post/compensateQueryResult");
        modelAndView.addObject("postData", postData);
        modelAndView.addObject("code", resultJson.get("code"));
        modelAndView.addObject("msg", resultJson.get("msg"));
        modelAndView.addObject("data", resultJson.getJSONObject("data"));

        System.out.println("============> [compensateQuery post] end");
        return modelAndView;
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
