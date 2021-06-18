package com.ouyeel.obfm.fm.business.impl;

import com.alibaba.fastjson.JSONObject;
import com.ouyeel.obfm.fm.business.IOrderService;
import com.ouyeel.obfm.fm.dao.ChainDao;
import com.ouyeel.obfm.fm.dao.Dao;
import com.ouyeel.obfm.utils.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.sql.SQLSyntaxErrorException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderService implements IOrderService {

    final static Logger logger = LoggerFactory.getLogger(OrderService.class);

//    static int count = 0;
//    static long time = 0;
    Dao CHAIN_DAO = new ChainDao();


    public static Map<String, String> obtainKey(String keyId, String accountId) {
        Map<String, String> map = new HashMap<>();

        String publicKey = "2204404536ab867d9a964bfcc5e6fdaa7d77e509ce5891d38b3ebbb036e5c225994597ea6d0bdff3539fd3062b3943a1c7dd75d173f35101b71298e9f7f08d51";
        String privateKey = "d6c83aee4bfbeb135a2dcef8c803b186d0678a99002b09d3c60c22aca7105005";
        String sm4Key = "0123456789abcdef0123456789abcdef";
        String sm4Iv = "0123456789abcdef0123456789abcdef";

        map.put(ChainConfig.PRIVATE_KEY, privateKey);
        map.put(ChainConfig.PUBLIC_KEY, publicKey);
        map.put(ChainConfig.SM4_KEY, sm4Key);
        map.put(ChainConfig.SM4_IV, sm4Iv);

        return map;
    }


    /**
     * 业务数据上链存证接口, 将业务信息进行上链存证。不对业务数据进行关联。
     * @param inJson JSONObject类型包含requestSn,systemId,invokeTime,businessId,callbackUrl,keyId,accountId...dataInfo
     * @return JSONObject类型包含 txHash systemId requestSn businessId
     */
    @Async
    public JSONObject upChain(JSONObject inJson) {
        logger.debug("[upChain] start");

        JSONObject outJson = new JSONObject();
        try {
            inJson = ChainConfig.smallHumpToUpperUnderline(inJson);
        } catch (Exception e) {
            logger.error("PARAMETER_ERROR!");
            e.printStackTrace();
            ResponseCode.putCodeAndMsg(outJson, ResponseCode.PARAMETER_ERROR);
            return ChainConfig.upperUnderlineToSmallHump(outJson);
        }
        logger.debug("inJson: [{}]", inJson.toJSONString());
        //System.out.println("inJson: " + inJson.toJSONString());

        try {
            Map<String, String> keys = obtainKey(
                    inJson.getString(ChainConfig.KEY_ID), inJson.getString(ChainConfig.ACCOUNT_ID));
            inJson.putAll(keys);
        } catch (Exception e) {
            logger.debug("OBTAIN_KEY_FAIL!");
            ResponseCode.putCodeAndMsg(outJson, ResponseCode.OBTAIN_KEY_FAIL);
            return ChainConfig.upperUnderlineToSmallHump(outJson);
        }


        /**
         * dataInfo单独获取，但字段参数统一检查，为空则返回参数为空错误码
         */
        Map<String, String> paramMap = new HashMap<>();
        for (String key: ChainConfig.UP_CHAIN_PARAM) {
            if (inJson.get(key) == null) {
                logger.debug("inJson get key " + key + " failed!");
                ResponseCode.putCodeAndMsg(outJson, ResponseCode.PARAMETER_LOSS);
                return ChainConfig.upperUnderlineToSmallHump(outJson);
            }
            if (!ChainConfig.DATA_INFO.equals(key)) {
                paramMap.put(key, String.valueOf(inJson.get(key)));
            }
        }
        /**
         * 检查dataInfo
         */
        try {
            paramMap.put(ChainConfig.DATA_INFO, inJson.getJSONObject(ChainConfig.DATA_INFO).toJSONString());
        } catch (Exception e) {
            logger.error("DATA_INFO_ERROR");
            logger.error(e.getMessage());
            e.printStackTrace();
            ResponseCode.putCodeAndMsg(outJson, ResponseCode.DATA_INFO_ERROR);
            return ChainConfig.upperUnderlineToSmallHump(outJson);
        }

        /**
         * 上链
         */
        logger.debug("upChain...");
        boolean insertResult = CHAIN_DAO.insertTx(paramMap);

        if (!insertResult) {
            logger.debug("upChain fail");
            ResponseCode.putCodeAndMsg(outJson, ResponseCode.UP_TX_FAIL);
            return ChainConfig.upperUnderlineToSmallHump(outJson);
        }
        logger.debug("upChain success");
        ResponseCode.putCodeAndMsg(outJson, ResponseCode.UP_TX_SUCCESS);

        /**
         * 添加异步回调任务到线程池，回调发送上链结果（异步线程池，线程池大小在 ChainConfig 类内定义）
         */
        if (paramMap.get(ChainConfig.CALLBACK_URL) != null) {
            AsyncCallback.addTask(
                    paramMap.get(ChainConfig.CALLBACK_URL),
                    paramMap.get(ChainConfig.TABLE_NAME),
                    paramMap.get(ChainConfig.REQUEST_SN));
        }

//        ++count;
//        long start = System.currentTimeMillis();
        /**y
         * 查询hash值
         */
        String txHash = CHAIN_DAO.queryTxHashByRequestSn(
                paramMap.get(ChainConfig.TABLE_NAME), paramMap.get(ChainConfig.REQUEST_SN));
        if (txHash != null) {
            logger.debug("txHash: [{}]", txHash);
            outJson.put(ChainConfig.TX_HASH, txHash);
        } else {
            logger.debug("query txHash fail");
        }

//        long end = System.currentTimeMillis();
//        time += end - start;
//        System.out.println("OrderService query txHash: " + count + ": " + (end - start) / 1000.0 + " s" +
//                ", cur:" + 1 / ((end - start) / 1000.0) + " tps" +
//                ", sum:" + count / (time / 1000.0) + " tps ThreadName: " + Thread.currentThread().getName());

        outJson.put(ChainConfig.SYSTEM_ID, paramMap.get(ChainConfig.SYSTEM_ID));
        outJson.put(ChainConfig.REQUEST_SN, paramMap.get(ChainConfig.REQUEST_SN));
        outJson.put(ChainConfig.BUSINESS_ID, paramMap.get(ChainConfig.BUSINESS_ID));

        logger.debug("outJson: [{}]", outJson.toJSONString());
        logger.debug("[upChain] end");
        return ChainConfig.upperUnderlineToSmallHump(outJson);
    }


    /**
     * 根据交易hash查证接口, 可根据存证时交易hash进行查证，返回业务数据上链存证信息。
     * @param inJson JSONObject类型包含 requestSn systemId txHash
     * @return JSONObject类型包含 txHash invokeTime blockTime blockHeight dataInfo
     */
    public JSONObject queryChain(JSONObject inJson) {
        logger.debug("[queryByTxHash] start");

        JSONObject outJson = new JSONObject();
        try {
            inJson = ChainConfig.smallHumpToUpperUnderline(inJson);
        } catch (Exception e) {
            logger.error("PARAMETER_ERROR!");
            ResponseCode.putCodeAndMsg(outJson, ResponseCode.PARAMETER_ERROR);
            return ChainConfig.upperUnderlineToSmallHump(outJson);
        }
        logger.debug("inJson: [{}]", inJson.toJSONString());

        /**
         * 检查参数
         */
        if (inJson.get(ChainConfig.TABLE_NAME) == null || inJson.get(ChainConfig.TX_HASH) == null) {
            ResponseCode.putCodeAndMsg(outJson, ResponseCode.PARAMETER_LOSS);
            return ChainConfig.upperUnderlineToSmallHump(outJson);
        }
        String tableName = String.valueOf(inJson.get(ChainConfig.TABLE_NAME));
        String txHash = String.valueOf(inJson.get(ChainConfig.TX_HASH));

        /**
         * 根据 txHash 查询上链交易数据
         */
        List<Map<String, String>> queryResulList = CHAIN_DAO.queryAllByTxHash(tableName, txHash);
        if (queryResulList == null) {
            ResponseCode.putCodeAndMsg(outJson, ResponseCode.QUERY_FAIL);
            return ChainConfig.upperUnderlineToSmallHump(outJson);
        }

        if (!queryResulList.isEmpty()) {
            Map<String, String> queryResultMap = queryResulList.get(0);
            logger.debug("onChain: [{}]", queryResultMap.get(ChainConfig.ON_CHAIN));
            if (ChainConfig.ON_CHAIN_SUCCESS.equals(queryResultMap.get(ChainConfig.ON_CHAIN))) {
                logger.debug("UP_CHAIN_SUCCESS");
                ResponseCode.putCodeAndMsg(outJson, ResponseCode.UP_CHAIN_SUCCESS);
                String[] keys = {ChainConfig.TX_HASH, ChainConfig.INVOKE_TIME,
                        ChainConfig.BLOCK_TIME, ChainConfig.BLOCK_HEIGHT};
                for (String key: keys) {
                    outJson.put(key, queryResultMap.get(key));
                }
                JSONObject dataInfo = getDataInfo(queryResultMap);
                outJson.put(ChainConfig.DATA_INFO, dataInfo);
            } else {
                logger.debug("UP_CHAIN_WAITTING");
                ResponseCode.putCodeAndMsg(outJson, ResponseCode.UP_CHAIN_WAITTING);
            }
        } else {
            logger.debug("NO_TX");
            ResponseCode.putCodeAndMsg(outJson, ResponseCode.NO_TX);
        }

        logger.debug("outJson: [{}]", outJson.toJSONString());
        logger.debug("[queryByTxHash] end");
        return ChainConfig.upperUnderlineToSmallHump(outJson);
    }


    /**
     * 业务数据验证接口, 可验证业务数据hash在链上存证结果。
     * @param inJson JSONObject类型包含  txHash systemId,businessId,requestSn...dataInfo
     * @return JSONObject类型包含 success message
     */
    public JSONObject checkChain(JSONObject inJson) {
        logger.debug("[verifyTxDataInfo] start");

        JSONObject outJson = new JSONObject();
        try {
            inJson = ChainConfig.smallHumpToUpperUnderline(inJson);
        } catch (Exception e) {
            logger.error("PARAMETER_ERROR!");
            ResponseCode.putCodeAndMsg(outJson, ResponseCode.PARAMETER_ERROR);
            return ChainConfig.upperUnderlineToSmallHump(outJson);
        }
        logger.debug("inJson: [{}]", inJson.toJSONString());

        /**
         * 检查参数
         */
        if (inJson.get(ChainConfig.TABLE_NAME) == null ||
                inJson.get(ChainConfig.TX_HASH) == null ||
                inJson.getJSONObject(ChainConfig.DATA_INFO) == null) {
            ResponseCode.putCodeAndMsg(outJson, ResponseCode.PARAMETER_LOSS);
            return ChainConfig.upperUnderlineToSmallHump(outJson);
        }
        String tableName = String.valueOf(inJson.get(ChainConfig.TABLE_NAME));
        String txHash = String.valueOf(inJson.get(ChainConfig.TX_HASH));
        
        /**
         * 根据 txHash 查询上链交易数据
         */
        List<Map<String, String>> queryResulList = CHAIN_DAO.queryAllByTxHash(tableName, txHash);
        if (queryResulList == null) {
            ResponseCode.putCodeAndMsg(outJson, ResponseCode.QUERY_FAIL);
            return ChainConfig.upperUnderlineToSmallHump(outJson);
        }

        if (!queryResulList.isEmpty()) {
            Map<String, String> queryResultMap = queryResulList.get(0);
            JSONObject dataInfo = inJson.getJSONObject(ChainConfig.DATA_INFO);
            JSONObject chainDataInfo = getDataInfo(queryResultMap);
            logger.debug("dataInfo:         [{}]", dataInfo);
            logger.debug("chainDataInfo:    [{}]", chainDataInfo);
            logger.debug("dataInfo =? chainDataInfo [{}]", dataInfo.equals(chainDataInfo));
            if (!dataInfo.equals(chainDataInfo)) {
                logger.debug("VERIFY_TX_FAIL");
                ResponseCode.putCodeAndMsg(outJson, ResponseCode.VERIFY_TX_FAIL);
                return ChainConfig.upperUnderlineToSmallHump(outJson);
            }
            logger.debug("VERIFY_TX_SUCCESS");
            ResponseCode.putCodeAndMsg(outJson, ResponseCode.VERIFY_TX_SUCCESS);
        } else {
            logger.debug("NO_TX");
            ResponseCode.putCodeAndMsg(outJson, ResponseCode.NO_TX);
        }

        logger.debug("outJson: [{}]", outJson.toJSONString());
        logger.debug("[verifyTxDataInfo] end");
        return ChainConfig.upperUnderlineToSmallHump(outJson);
    }


    /**
     * 补偿查询接口, 如果异步推送未收到结果，可根据该接口进行主动查询。
     * @param inJson JSONObject类型包含  systemId requestSn businessId
     * @return JSONObject类型包含  txHash blockHeight blockTime dataInfo
     */
    public JSONObject reQueryChain(JSONObject inJson) {
        logger.debug("[compensateQuery] start");

        JSONObject outJson = new JSONObject();
        try {
            inJson = ChainConfig.smallHumpToUpperUnderline(inJson);
        } catch (Exception e) {
            logger.error("PARAMETER_ERROR!");
            ResponseCode.putCodeAndMsg(outJson, ResponseCode.PARAMETER_ERROR);
            return ChainConfig.upperUnderlineToSmallHump(outJson);
        }
        logger.debug("inJson: [{}]", inJson.toJSONString());

        /**
         * 检查参数
         */
        if (inJson.get(ChainConfig.TABLE_NAME) == null || inJson.get(ChainConfig.SEARCH_REQUEST_SN) == null) {
            ResponseCode.putCodeAndMsg(outJson, ResponseCode.PARAMETER_LOSS);
            return ChainConfig.upperUnderlineToSmallHump(outJson);
        }

        String tableName = String.valueOf(inJson.get(ChainConfig.TABLE_NAME));
        String searchRequestSn = String.valueOf(inJson.get(ChainConfig.SEARCH_REQUEST_SN));


        /**
         * 根据 searchRequestSn 补偿查询
         */
        List<Map<String, String>> queryResulList = CHAIN_DAO.queryAllByRequestSn(tableName, searchRequestSn);
        if (queryResulList == null) {
            ResponseCode.putCodeAndMsg(outJson, ResponseCode.QUERY_FAIL);
            return ChainConfig.upperUnderlineToSmallHump(outJson);
        }

        if (!queryResulList.isEmpty()) {
            Map<String, String> queryResultMap = queryResulList.get(0);
            logger.debug("onChain: [{}]", queryResultMap.get(ChainConfig.ON_CHAIN));
            if (ChainConfig.ON_CHAIN_SUCCESS.equals(queryResultMap.get(ChainConfig.ON_CHAIN))) {
                logger.debug("UP_CHAIN_SUCCESS");
                ResponseCode.putCodeAndMsg(outJson, ResponseCode.UP_CHAIN_SUCCESS);
                String[] keys = {ChainConfig.TX_HASH, ChainConfig.BLOCK_HEIGHT, ChainConfig.BLOCK_TIME};
                for (String key: keys) {
                    outJson.put(key, queryResultMap.get(key));
                }
                JSONObject dataInfo = getDataInfo(queryResultMap);
                outJson.put(ChainConfig.DATA_INFO, dataInfo);
            } else {
                logger.debug("UP_CHAIN_WAITTING");
                ResponseCode.putCodeAndMsg(outJson, ResponseCode.UP_CHAIN_WAITTING);
                outJson.put(ChainConfig.TX_HASH, queryResultMap.get(ChainConfig.TX_HASH));
            }
        } else {
            logger.debug("NO_TX");
            ResponseCode.putCodeAndMsg(outJson, ResponseCode.NO_TX);
        }

        logger.debug("outJson: [{}]", outJson.toJSONString());
        logger.debug("[compensateQuery] end");
        return ChainConfig.upperUnderlineToSmallHump(outJson);
    }


    static public JSONObject getDataInfo(Map<String, String> queryResultMap) {
        logger.debug("[getDataInfo] start");
        JSONObject dataInfo = new JSONObject();
        for (String key: queryResultMap.keySet()) {
            if (!ChainConfig.SYSTEM_PARAM.contains(key)) {
                logger.debug(key + " : " + queryResultMap.get(key));
                dataInfo.put(key, queryResultMap.get(key));
            }
        }
        logger.debug("[getDataInfo] end");
        return dataInfo;
    }
}
