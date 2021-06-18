package com.ouyeel.obfm.controller;

import com.alibaba.fastjson.JSONObject;
import com.ouyeel.obfm.sql.ChainManager;
import com.ouyeel.obfm.sql.InvoiceChainManager;
import com.ouyeel.obfm.sql.config.ChainConfig;
import com.ouyeel.obfm.sql.util.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.UUID;

@RestController
public class InvoiceController {

    static Logger logger = LoggerFactory.getLogger(InvoiceController.class);

    private ChainManager chainManager = new InvoiceChainManager();

//    public InvoiceController() {
//        super();
//        chainManager.init();
//    }

    @RequestMapping("/")
    public String hello() {
        chainManager.test();
        return "hello world!";
    }

    /**
     * 业务数据上链存证接口, 将业务信息进行上链存证。不对业务数据进行关联。
     * @param inJson
     * @return
     */
    @RequestMapping(value = "/obst/service/S_ST_01",
            method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    String upChain(@RequestBody JSONObject inJson) {
//        logger.debug("inJson: \n" + JSONObject.toJSONString(inJson, true));

        inJson.put(ChainConfig.LOWERCASE_INVOKE_TIME, String.valueOf(System.currentTimeMillis()));
        inJson.put(ChainConfig.LOWERCASE_REQUEST_SN, UUID.randomUUID().toString());

        String txHash = chainManager.executor(inJson);
        JSONObject outJson = new JSONObject();
        outJson.put(ChainConfig.TX_HASH, txHash);
        logger.debug("txHash: [{}]", txHash);
        return outJson.toJSONString();
    }


    /**
     * 状态查询之交易数目查询
     * @param inJson
     * @return
     */
    @RequestMapping(value = "/obst/service/S_ST_06",
            method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    String queryState(@RequestBody JSONObject inJson) {
        inJson = CommonUtils.smallHumpToUpperUnderline(inJson);
        logger.debug("inJson: \n" + JSONObject.toJSONString(inJson, true));

        JSONObject outJson = chainManager.queryState(inJson);
        return outJson.toJSONString();
    }


    /**
     * 回调发送响应处
     * @param inJson
     * @return
     * @throws IOException
     */
    @PostMapping("/callback")
    @ResponseBody
    String callback(@RequestBody JSONObject inJson) throws IOException {
        logger.debug("============> [callback] start");
        logger.debug("callback data:\n" + JSONObject.toJSONString(inJson, true));
//        List<String> onChainTx = Arrays.asList(
//                (String)inJson.get(ChainConfig.TX_HASH),
//                (String)inJson.get(ChainConfig.BLOCK_TIME),
//                (String)inJson.get(ChainConfig.BLOCK_HEIGHT));
//        onChainTxList.add(onChainTx);
        JSONObject successJson = new JSONObject();
        successJson.put("success", true);
        successJson.put("msg", "回调接收成功");
        logger.debug("============> [callback] end");
        return successJson.toJSONString();
    }

}
