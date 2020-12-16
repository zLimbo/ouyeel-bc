package com.zlimbo.bcweb.controller;



import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.zlimbo.bcweb.domain.Invoice;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;


@Controller
@RequestMapping("")
public class PostController {

    final Logger logger = LoggerFactory.getLogger(getClass());
    private static final String tableName = "invoice";

    private static final String LOCAL_URL = "http://127.0.0.1:8082";
    private static final String POST_URL = "http://127.0.0.1:8080";
    private static final String CALLBACK_URL = LOCAL_URL + "/callback";

    private static final String SYSTEM_ID = "000001";
    private static final String PRIVATE_KEY_STRING = "308193020100301306072a8648ce3d020106082a811ccf" +
            "5501822d04793077020101042068769c741bc69dc8dc5dbf2009ef7286905abb7be12353187cef9cc89e564" +
            "023a00a06082a811ccf5501822da144034200040cfea82646c5695da5a4476e3fdcaf3f97ea9cc77fae7860" +
            "8fe12a1969ef8032e3ea6e91d24774445d2744e1c43d4b32845d3022718c06ca7cd4e73317f1e726";

    private static final String PUBLIC_KEY_STRING = "3059301306072a8648ce3d020106082a811ccf5501822d0" +
            "34200040cfea82646c5695da5a4476e3fdcaf3f97ea9cc77fae78608fe12a1969ef8032e3ea6e91d2477444" +
            "5d2744e1c43d4b32845d3022718c06ca7cd4e73317f1e726";
    
    private static long assignBusinessId = 1;

    static String getBusinessId() {
        String returnString = String.format("%020d", assignBusinessId);
        assignBusinessId += 1;
        return returnString;
    }

    private String requestSn;
    private String bussinessId;

    private List<List<String>> onChainTxList = new ArrayList<>();


    /**
     * 签名测试
     * @return
     * @throws Exception
     */
    @RequestMapping("/testSignatrue")
    @ResponseBody
    String testSignatrue() throws Exception {
        final Provider bouncyCastleProvider = new BouncyCastleProvider();
        // 公私钥是16进制情况下解码
        byte[] encodePublicKey = Hex.decode(PUBLIC_KEY_STRING);
        byte[] encodePrivateKey =  Hex.decode(PRIVATE_KEY_STRING);
        // 公私钥是 Base64编码情况下解码
//        byte[] encodePublicKey = Base64.decode(PUBLIC_KEY_STRING);
//        byte[] encodePrivateKey = Base64.decode(PRIVATE_KEY_STRING);

        KeyFactory keyFactory = KeyFactory.getInstance("EC", bouncyCastleProvider);
        // 根据采用的编码结构反序列化公私钥
        PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(encodePublicKey));

        PrivateKey privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(encodePrivateKey));

        Signature signature = Signature.getInstance("SM3withSm2", bouncyCastleProvider);

        /**
         * 签名
         */
        // 签名需要使用私钥，使用私钥 初始化签名实例
        signature.initSign(privateKey);
        // 签名原文
        byte[] plainText = "你好".getBytes(StandardCharsets.UTF_8);
        // 写入签名原文到算法中
        signature.update(plainText);
        // 计算签名值
        byte[] signatureValue = signature.sign();
        System.out.printf("signature[%d]: %s\n", signatureValue.length, Hex.toHexString(signatureValue));


        /**
         * 验签
         */
        // 签名需要使用公钥，使用公钥 初始化签名实例
        signature.initVerify(publicKey);
        // 写入待验签的签名原文到算法中
        signature.update(plainText);
        // 验签
        logger.debug("Signature verify result: " + signature.verify(signatureValue));

        return Hex.toHexString(signatureValue);
    }


    /**
     * 使用国密SM2签名
     * @param plainText
     * @return
     * @throws Exception
     */
    String gmSm2Signature(byte[] plainText) throws Exception {
        final Provider bouncyCastleProvider = new BouncyCastleProvider();
        byte[] encodePrivateKey =  Hex.decode(PRIVATE_KEY_STRING);

        KeyFactory keyFactory = KeyFactory.getInstance("EC", bouncyCastleProvider);
        PrivateKey privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(encodePrivateKey));
        Signature signature = Signature.getInstance("SM3withSm2", bouncyCastleProvider);

        // 签名需要使用私钥，使用私钥 初始化签名实例
        signature.initSign(privateKey);
        // 写入签名原文到算法中
        signature.update(plainText);
        // 计算签名值
        byte[] signatureValue = signature.sign();
        System.out.printf("signature[%d]: %s\n", signatureValue.length, Hex.toHexString(signatureValue));

        return Hex.toHexString(signatureValue);
    }


    @RequestMapping("/onChainTx")
    ModelAndView onChainTx() {
        logger.debug("============> [onChainTx] start");
        ModelAndView modelAndView = new ModelAndView("post/onChainTx");
        for (List<String> onChainTx: onChainTxList) {
            logger.debug(onChainTx.get(0) + " - " + onChainTx.get(1) + " - " + onChainTx.get(2));
        }
        modelAndView.addObject("onChainTxList", onChainTxList);
        logger.debug("============> [onChainTx] end");
        return modelAndView;
    }


    @PostMapping("/callback")
    @ResponseBody
    String callback(@RequestBody JSONObject dataJson) throws IOException {
        logger.debug("============> [callback] start");
        logger.debug("callback data: " + dataJson);
        List<String> onChainTx = Arrays.asList(
                (String)dataJson.get(ChainParam.TX_HASH),
                (String)dataJson.get(ChainParam.BLOCK_TIME),
                (String)dataJson.get(ChainParam.BLOCK_HEIGHT));
        onChainTxList.add(onChainTx);
        JSONObject successJson = new JSONObject();
        successJson.put("success", true);
        successJson.put("msg", "回调接收成功");
        //String resultString = send(LOCAL_URL + "/callbackSuccess", dataJson, "utf-8");
        //logger.debug("callbackSuccess resultString: " + resultString);
        //callbackSuccess(dataJson);
        logger.debug("============> [callback] end");
        return successJson.toJSONString();
    }


    @PostMapping("/callbackSuccess")
    ModelAndView callbackSuccess(@RequestBody JSONObject dataJson) {
        logger.debug("============> [callbackSuccess] start");
        ModelAndView modelAndView = new ModelAndView("post/callbackSuccess");
        logger.debug("============> [callbackSuccess] end");
        return modelAndView;
    }


    @RequestMapping(value = "/randomInvoice", method = RequestMethod.GET)
    @ResponseBody
    public String randomInvoice() {
        logger.debug("--------------------------------------------randomInvoice ok");
        return JSON.toJSONString(Invoice.getRandomInvoice());
    }


//    @GetMapping("/S_ST_01")
//    String upChain(Model model) throws IOException {
//        logger.debug("============> [upChain get] start");
//        requestSn = UUID.randomUUID().toString();
//        bussinessId = getBusinessId();
//        model.addAttribute("systemId", SYSTEM_ID);
//        model.addAttribute("requestSn", requestSn);
//        model.addAttribute("businessId", bussinessId);
//        model.addAttribute("invoice", new Invoice());
//        logger.debug("============> [upChain get] end");
//        return "post/upChain";
//    }

//    @PostMapping("/S_ST_01")
//    ModelAndView upChain(@RequestParam Map<String, Object> params) throws Exception {
//        logger.debug("============> [upChain post] start");
//
//        JSONObject dataInfo = new JSONObject();
//        //dataInfo.putAll(params);
//        dataInfo.put("invoiceno", params.get("invoiceNo"));
//        JSONObject postData = new JSONObject();
//        postData.put("tableName", tableName);
//        postData.put("systemId", SYSTEM_ID);
//        postData.put("requestSn", requestSn);
//        postData.put("dataInfo", dataInfo);
//        postData.put("businessId", bussinessId);
//        postData.put("callbackUrl", CALLBACK_URL);
//        postData.put("invokeTime", String.valueOf(System.currentTimeMillis()));
//
//        // 私钥签名
//        String postDataString = JSONObject.toJSONString(postData, SerializerFeature.PrettyFormat);
//        logger.debug("== postDataString: " + postDataString);
//        String signature = gmSm2Signature(postDataString.getBytes(StandardCharsets.UTF_8));
//        postData.put("sign", signature);
//
//        // post远程请求
//        String response = send(POST_URL + "/obst/service/S_ST_01", postData, "utf-8");
//        JSONObject resultJson = JSONObject.parseObject(response);
//
//        ModelAndView modelAndView = new ModelAndView("post/upChainResult");
//        modelAndView.addObject("postData", postData);
//        modelAndView.addObject("code", resultJson.get("code"));
//        modelAndView.addObject("msg", resultJson.get("msg"));
//        modelAndView.addObject("txHash", resultJson.get("txHash"));
//        modelAndView.addObject("data", dataInfo);
//
//        logger.debug("============> [upChain post] end");
//        return modelAndView;
//    }


    @GetMapping("/S_ST_01")
    ModelAndView testUpChain() throws Exception {
        logger.debug("============> [upChain post] start");

        JSONObject postData = new JSONObject();

        for (String key: ChainParam.UP_CHAIN_PARAM) {
            postData.put(key, key);
        }

        postData.put(ChainParam.TABLE_NAME, "invoice");
        postData.put(ChainParam.REQUEST_SN, UUID.randomUUID().toString());
        postData.put(ChainParam.INVOKE_TIME, "2019-03-05 11:11:11.12");
        postData.put(ChainParam.KEY_ID, "00006");
        postData.put(ChainParam.ACCOUNT_ID, "00002");
        postData.put(ChainParam.CALLBACK_URL, CALLBACK_URL);
        postData.put(ChainParam.SECRET_KEY, "0123456789abcdef");
        postData.put(ChainParam.PRIVATE_KEY, "d6c83aee4bfbeb135a2dcef8c803b186d0678a99002b09d3c60c22aca7105005");
        postData.put(ChainParam.PUBLIC_KEY, "2204404536ab867d9a964bfcc5e6fdaa7d77e509ce5891d38b3ebbb036e5c225994597ea6d0bdff3539fd3062b3943a1c7dd75d173f35101b71298e9f7f08d51");
        String dataInfoStr = "{\"CONSUMER_NAME\":\"数据学院\",\"CONSUMER_TAXES_NO\":\"12100000425006133D\",\"INVOICE_DATE\":\"2020-10-03\",\"INVOICE_NO\":\"3100982170\",\"INVOICE_NUMBER\":\"1\",\"INVOICE_TYPE\":\"增值税\",\"PRICE\":\"1000\",\"PRICE_PLUS_TAXES\":\"1000\",\"SELLER_NAME\":\"华东师大\",\"SELLER_TAXES_NO\":\"913100003245878130\",\"STATEMENT_SHEET\":\"1\",\"STATEMENT_WEIGHT\":\"1kg\",\"TAXES\":\"150\",\"TAXES_POINT\":\"17%\",\"TIMESTAMPS\":\"1601712721\"}";
        JSONObject dataInfo = JSONObject.parseObject(dataInfoStr);
        logger.debug("dataInfo: \n" + dataInfo.getString(JSONObject.DEFFAULT_DATE_FORMAT));
        postData.put(ChainParam.DATA_INFO, dataInfo);


        String postJsonStr = postData.toJSONString();
        logger.debug("postJsonStr: " + postJsonStr);
        // post远程请求
        String response = send(POST_URL + "/obst/service/S_ST_01", postData, "utf-8");
        JSONObject resultJson = JSONObject.parseObject(response);

        ModelAndView modelAndView = new ModelAndView("post/upChainResult");
        modelAndView.addObject("postData", postData);
        modelAndView.addObject("code", resultJson.get("code"));
        modelAndView.addObject("msg", resultJson.get("msg"));
        modelAndView.addObject("txHash", resultJson.get(ChainParam.TX_HASH));
        modelAndView.addObject("data", dataInfo);

        logger.debug("============> [upChain post] end");
        return modelAndView;
    }


    @GetMapping("/S_ST_02")
    String queryByTxHash(Model model) {
        logger.debug("============> [queryByTxHash get] start");
        requestSn = UUID.randomUUID().toString();
        model.addAttribute("systemId", SYSTEM_ID);
        model.addAttribute("requestSn", requestSn);

        logger.debug("============> [queryByTxHash get] end");
        return "post/queryByTxHash";
    }

    @PostMapping("/S_ST_02")
    ModelAndView queryByTxHash(@RequestParam Map<String, Object> params) throws Exception {
        logger.debug("============> [queryByTxHash post] start");
        logger.debug("txHash: " + ((String)params.get("txHash")).trim());
        JSONObject postData = new JSONObject();
        postData.put(ChainParam.TABLE_NAME, tableName);
        postData.put(ChainParam.SYSTEM_ID, SYSTEM_ID);
        postData.put(ChainParam.REQUEST_SN, requestSn);
        postData.put(ChainParam.TX_HASH, ((String)params.get("txHash")).trim());
        postData.put(ChainParam.INVOKE_TIME, String.valueOf(System.currentTimeMillis()));

        // 私钥签名
        String postDataString = JSONObject.toJSONString(postData, SerializerFeature.PrettyFormat);
        logger.debug("== postDataString: " + postDataString);
        String signature = gmSm2Signature(postDataString.getBytes(StandardCharsets.UTF_8));
        postData.put("sign", signature);

        // post远程请求
        String responseString = send(POST_URL + "/obst/service/S_ST_02", postData, "utf-8");
        logger.debug("response: " + responseString);
        JSONObject resultJson = JSONObject.parseObject(responseString);

        ModelAndView modelAndView = new ModelAndView("post/queryByTxHashResult");
        modelAndView.addObject("postData", postData);
        modelAndView.addObject("code", resultJson.get("code"));
        modelAndView.addObject("msg", resultJson.get("msg"));
        modelAndView.addObject("data", resultJson.getJSONObject(ChainParam.DATA_INFO));

        logger.debug("============> [queryByTxHash post] end");
        return modelAndView;
    }


    @GetMapping("/S_ST_03")
    String verifyTxDataInfo(Model model) {
        logger.debug("============> [verifyTxDataInfo get] start");
        requestSn = UUID.randomUUID().toString();
        bussinessId = getBusinessId();
        model.addAttribute("systemId", SYSTEM_ID);
        model.addAttribute("requestSn", requestSn);
        model.addAttribute("businessId", bussinessId);

        logger.debug("============> [verifyTxDataInfo get] end");
        return "post/verifyTxDataInfo";
    }

    @PostMapping("/S_ST_03")
    ModelAndView verifyTxDataInfo(@RequestParam Map<String, Object> params) throws Exception {
        logger.debug("============> [verifyTxDataInfo post] start");
        JSONObject postData = new JSONObject();
        postData.put(ChainParam.TABLE_NAME, tableName);
        postData.put(ChainParam.SYSTEM_ID, SYSTEM_ID);
        postData.put(ChainParam.REQUEST_SN, requestSn);
        postData.put(ChainParam.BUSINESS_ID, bussinessId);
        postData.put(ChainParam.TX_HASH, ((String)params.get("txHash")).trim());
        postData.put(ChainParam.DATA_INFO, params.get("dataInfo"));
        postData.put(ChainParam.INVOKE_TIME, String.valueOf(System.currentTimeMillis()));

        // 私钥签名
        String postDataString = JSONObject.toJSONString(postData, SerializerFeature.PrettyFormat);
        logger.debug("== postDataString: " + postDataString);
        String signature = gmSm2Signature(postDataString.getBytes(StandardCharsets.UTF_8));
        postData.put("sign", signature);

        // post远程请求
        String responseString = send(POST_URL + "/obst/service/S_ST_03", postData, "utf-8");
        logger.debug("response: " + responseString);
        JSONObject resultJson = JSONObject.parseObject(responseString);

        ModelAndView modelAndView = new ModelAndView("post/verifyTxDataInfoResult");
        modelAndView.addObject("postData", postData);
        modelAndView.addObject("code", resultJson.get("code"));
        modelAndView.addObject("msg", resultJson.get("msg"));

        logger.debug("============> [verifyTxDataInfo post] end");
        return modelAndView;
    }


    @GetMapping("/S_ST_04")
    String compensateQuery(Model model) {
        logger.debug("============> [compensateQuery get] start");
        requestSn = UUID.randomUUID().toString();
        bussinessId = getBusinessId();
        model.addAttribute("systemId", SYSTEM_ID);
        model.addAttribute("requestSn", requestSn);
        model.addAttribute("businessId", bussinessId);

        logger.debug("============> [compensateQuery get] end");
        return "post/compensateQuery";
    }

    @PostMapping("/S_ST_04")
    ModelAndView compensateQuery(@RequestParam Map<String, Object> params) throws Exception {
        logger.debug("============> [compensateQuery post] start");

        JSONObject postData = new JSONObject();
        postData.put(ChainParam.TABLE_NAME, tableName);
        postData.put(ChainParam.SYSTEM_ID, SYSTEM_ID);
        postData.put(ChainParam.REQUEST_SN, requestSn);
        postData.put(ChainParam.BUSINESS_ID, bussinessId);
        postData.put(ChainParam.SEARCH_REQUEST_SN, ((String)params.get("searchRequestSn")).trim());
        postData.put(ChainParam.INVOKE_TIME, String.valueOf(System.currentTimeMillis()));




        // 私钥签名
        String postDataString = JSONObject.toJSONString(postData, SerializerFeature.PrettyFormat);
        logger.debug("== postDataString: " + postDataString);
        String signature = gmSm2Signature(postDataString.getBytes(StandardCharsets.UTF_8));
        postData.put("sign", signature);

        // post远程请求
        String responseString = send(POST_URL + "/obst/service/S_ST_04", postData, "utf-8");
        logger.debug("response: " + responseString);
        JSONObject resultJson = JSONObject.parseObject(responseString);

        ModelAndView modelAndView = new ModelAndView("post/compensateQueryResult");
        modelAndView.addObject("postData", postData);
        modelAndView.addObject("code", resultJson.get("code"));
        modelAndView.addObject("msg", resultJson.get("msg"));
        modelAndView.addObject("data", resultJson.getJSONObject(ChainParam.DATA_INFO));

        logger.debug("============> [compensateQuery post] end");
        return modelAndView;
    }
    
    
    public String send(String url, JSONObject jsonObject, String encoding) throws IOException {
        logger.debug("====================> [send] start");
        String body = "";

        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);

        StringEntity stringEntity = new StringEntity(jsonObject.toString(), "utf-8");
        stringEntity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
        httpPost.setEntity(stringEntity);
        logger.debug("== request url: " + url);

        httpPost.setHeader("Content-type", "application/json");
        httpPost.setHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");

        CloseableHttpResponse response = client.execute(httpPost);

        HttpEntity entity = response.getEntity();
        if (entity != null) {
            body = EntityUtils.toString(entity, encoding);
        }
        EntityUtils.consume(entity);
        response.close();
        logger.debug("== body: " + body);
        //JSONObject returnJson = new JSONObject();
        logger.debug("====================> [send] end\n");
        return body;
    }
}
