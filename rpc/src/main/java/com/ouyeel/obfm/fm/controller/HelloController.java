package com.ouyeel.obfm.fm.controller;

import com.alibaba.fastjson.JSONObject;
import com.ouyeel.obfm.fm.business.IOrderService;
import com.ouyeel.obfm.fm.business.impl.AsyncCallback;
import com.ouyeel.obfm.fm.business.impl.ChainConfig;
import com.ouyeel.obfm.fm.business.impl.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
public class HelloController {

    static long time = 0;
    static Integer count = 0;

    /**
     * 日志
     */
    final static Logger logger = LoggerFactory.getLogger(HelloController.class);

    @Autowired
    private IOrderService orderService;

    /**
     * index
     * @return
     */
    @RequestMapping("/")
    public String index() throws SQLException {
        logger.debug("index start");
        List<Map<String, String>> list =
                AsyncCallback.CHAIN_DAO.getSqlMapClient().queryForList("test");
        logger.debug("index end");
        return String.valueOf(list.size());
    }


    /**
     * 业务数据上链存证接口, 将业务信息进行上链存证。不对业务数据进行关联。
     * @param inJson
     * @return
     */
    @RequestMapping(value = "/obst/service/S_ST_01",
            method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    String upChain(@RequestBody JSONObject inJson) {
        logger.debug("[upChain] start");

        inJson.put("requestSn", UUID.randomUUID().toString());

        long start = System.currentTimeMillis();
        JSONObject outJson = orderService.upChain(inJson);
//        JSONObject outJson = new JSONObject();
        long end = System.currentTimeMillis();
        synchronized (count) {
            time += end - start;
            System.out.println("upChain count: " + (count++) +
                    " requestSn: " + inJson.getString("requestSn") +
                    " txHash: " + outJson.getString("txHash"));
//            System.out.println("HelloController: " + count + ": " + (end - start) / 1000.0 + " s" +
//                    ", cur:" + 1 / ((end - start) / 1000.0) + " tps" +
//                    ", sum:" + count / (time / 1000.0) + " tps ThreadName: " + Thread.currentThread().getName() +
//                    ", time: " + time);
        }


        logger.debug("[upChain] end");
        return outJson.toJSONString();
    }


    /**
     * 根据交易hash查证接口, 可根据存证时交易hash进行查证，返回业务数据上链存证信息。
     * @param inJson
     * @return
     */
    @RequestMapping(value = "/obst/service/S_ST_02",
            method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    String queryChain(@RequestBody JSONObject inJson) {
        logger.debug("[queryByTxHash] start");

        JSONObject outJson = orderService.queryChain(inJson);
//        JSONObject outJson = new JSONObject();
        logger.debug("[queryByTxHash] end");
        return outJson.toJSONString();
    }


    /**
     * 业务数据验证接口, 可验证业务数据hash在链上存证结果。
     * @param inJson
     * @return
     */
    @RequestMapping(value = "/obst/service/S_ST_03",
            method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    String checkChain(@RequestBody JSONObject inJson) {
        logger.debug("[verifyTxDataInfo] start");

        JSONObject outJson = orderService.checkChain(inJson);

        logger.debug("[verifyTxDataInfo] end");
        return outJson.toJSONString();
    }


    /**
     * 补偿查询接口, 如果异步推送未收到结果，可根据该接口进行主动查询。
     * @param inJson
     * @return
     */
    @RequestMapping(value = "/obst/service/S_ST_04",
            method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    String compensateQuery(@RequestBody JSONObject inJson) {
        logger.debug("[compensateQuery] start");

        JSONObject outJson = orderService.reQueryChain(inJson);

        logger.debug("[compensateQuery] end");
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
