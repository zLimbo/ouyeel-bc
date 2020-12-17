package com.ouyeel.obfm.fm.business.impl;

import com.alibaba.fastjson.JSONObject;
import com.ouyeel.obfm.fm.business.IOrderService;
import com.ouyeel.obfm.utils.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderService implements IOrderService {

    final static Logger logger = LoggerFactory.getLogger(OrderService.class);

    /**
     * 获取相关的密钥
     * @param keyId
     * @param accountId
     * @return
     */
    private static Map<String, String> obtainKey(String keyId, String accountId) {
        if (keyId == null || accountId == null) {
            return null;
        }
        Map<String, String> outMap = new HashMap<>();
        Map<String, String> sm4KeyMap = SecurityUtil.getSm4Key(keyId);
        outMap.put(ChainParam.SM4_KEY, ChainParam.lowerCase(sm4KeyMap.get("SM4key")));
        outMap.put(ChainParam.SM4_IV, ChainParam.lowerCase(sm4KeyMap.get("SM4iv")));
        outMap.put(ChainParam.PUBLIC_KEY, ChainParam.lowerCase(SecurityUtil.getPublicKey(accountId)));
        outMap.put(ChainParam.PRIVATE_KEY, ChainParam.lowerCase(SecurityUtil.getPrivateKey(accountId)));
        return outMap;
    }


    /**
     * 业务数据上链存证接口, 将业务信息进行上链存证。不对业务数据进行关联。
     * @param inJson JSONObject类型包含requestSn,systemId,invokeTime,businessId,callbackUrl,keyId,accountId...dataInfo
     * @return JSONObject类型包含 txHash systemId requestSn businessId
     */
    public JSONObject upChain(JSONObject inJson) {
        logger.debug("[upChain] start");

        inJson = ChainParam.smallHumpToUpperUnderline(inJson);
        logger.debug("inJson: [{}]", inJson.toJSONString());

        Map<String, String> keys =
                obtainKey(inJson.getString(ChainParam.KEY_ID), inJson.getString(ChainParam.ACCOUNT_ID));
        inJson.putAll(keys);

        JSONObject outJson = new JSONObject();

        /**
         * dataInfo单独获取，但字段参数统一检查，为空则返回参数为空错误码
         */
        Map<String, String> paramMap = new HashMap<>();
        for (String key: ChainParam.UP_CHAIN_PARAM) {
            if (inJson.get(key) == null) {
                ResponseCode.putCodeAndMsg(outJson, ResponseCode.NULL_PARAMETER);
                return outJson;
            }
            if (!ChainParam.DATA_INFO.equals(key)) {
                paramMap.put(key, String.valueOf(inJson.get(key)));
            }
        }

        paramMap.put(ChainParam.DATA_INFO, inJson.getJSONObject(ChainParam.DATA_INFO).toJSONString());

        /**
         * 上链
         */
        logger.debug("upChain...");
        boolean insertResult = SqlService.insertTx(paramMap);
        if (!insertResult) {
            logger.debug("upChain fail");
            ResponseCode.putCodeAndMsg(outJson, ResponseCode.UP_CHAIN_FAIL);
            return outJson;
        }
        logger.debug("upChain success");
        ResponseCode.putCodeAndMsg(outJson, ResponseCode.UP_TX_SUCCESS);

        /**
         * 添加异步回调任务到线程池，回调发送上链结果（异步线程池，线程池大小在 ChainParam 类内定义）
         */
        if (paramMap.get(ChainParam.CALLBACK_URL) != null) {
            AsyncCallback.addTask(
                    paramMap.get(ChainParam.CALLBACK_URL),
                    paramMap.get(ChainParam.TABLE_NAME),
                    paramMap.get(ChainParam.REQUEST_SN));
        }

        /**
         * 查询hash值
         */
        String txHash = SqlService.queryTxHashByRequestSn(
                paramMap.get(ChainParam.TABLE_NAME), paramMap.get(ChainParam.REQUEST_SN));
        if (txHash != null) {
            logger.debug("txHash: [{}]", txHash);
            outJson.put(ChainParam.TX_HASH, txHash);
        } else {
            logger.debug("query txHash fail");
        }

        outJson.put(ChainParam.SYSTEM_ID, paramMap.get(ChainParam.SYSTEM_ID));
        outJson.put(ChainParam.REQUEST_SN, paramMap.get(ChainParam.REQUEST_SN));
        outJson.put(ChainParam.BUSINESS_ID, paramMap.get(ChainParam.BUSINESS_ID));

        outJson = ChainParam.upperUnderlineToSmallHump(outJson);
        logger.debug("outJson: [{}]", outJson.toJSONString());
        logger.debug("[upChain] end");
        return outJson;
    }


    /**
     * 根据交易hash查证接口, 可根据存证时交易hash进行查证，返回业务数据上链存证信息。
     * @param inJson JSONObject类型包含 requestSn systemId txHash
     * @return JSONObject类型包含 txHash invokeTime blockTime blockHeight dataInfo
     */
    public JSONObject queryChain(JSONObject inJson) {
        logger.debug("[queryByTxHash] start");

        inJson = ChainParam.smallHumpToUpperUnderline(inJson);
        logger.debug("inJson: [{}]", inJson.toJSONString());

        JSONObject outJson = new JSONObject();

        /**
         * 检查参数
         */
        if (inJson.get(ChainParam.TABLE_NAME) == null || inJson.get(ChainParam.TX_HASH) == null) {
            ResponseCode.putCodeAndMsg(outJson, ResponseCode.NULL_PARAMETER);
            return outJson;
        }
        String tableName = String.valueOf(inJson.get(ChainParam.TABLE_NAME));
        String txHash = String.valueOf(inJson.get(ChainParam.TX_HASH));

        /**
         * 根据 txHash 查询上链交易数据
         */
        List<Map<String, String>> queryResulList = SqlService.queryAllByTxHash(tableName, txHash);
        if (queryResulList == null) {
            ResponseCode.putCodeAndMsg(outJson, ResponseCode.QUERY_FAIL);
            return outJson;
        }

        if (!queryResulList.isEmpty()) {
            Map<String, String> queryResultMap = queryResulList.get(0);
            logger.debug("onChain: [{}]", queryResultMap.get(ChainParam.ON_CHAIN));
            if (ChainParam.ON_CHAIN_SUCCESS.equals(queryResultMap.get(ChainParam.ON_CHAIN))) {
                logger.debug("UP_CHAIN_SUCCESS");
                ResponseCode.putCodeAndMsg(outJson, ResponseCode.UP_CHAIN_SUCCESS);
                String[] keys = {ChainParam.TX_HASH, ChainParam.INVOKE_TIME,
                        ChainParam.BLOCK_TIME, ChainParam.BLOCK_HEIGHT};
                for (String key: keys) {
                    outJson.put(key, queryResultMap.get(key));
                }
                JSONObject dataInfo = getDataInfo(queryResultMap);
                outJson.put("DATA_INFO", dataInfo);
            } else {
                logger.debug("UP_CHAIN_WAITTING");
                ResponseCode.putCodeAndMsg(outJson, ResponseCode.UP_CHAIN_WAITTING);
            }
        } else {
            logger.debug("UP_CHAIN_FAIL");
            ResponseCode.putCodeAndMsg(outJson, ResponseCode.UP_CHAIN_FAIL);
        }

        outJson = ChainParam.upperUnderlineToSmallHump(outJson);
        logger.debug("outJson: [{}]", outJson.toJSONString());
        logger.debug("[queryByTxHash] end");
        return outJson;
    }


    /**
     * 业务数据验证接口, 可验证业务数据hash在链上存证结果。
     * @param inJson JSONObject类型包含  txHash systemId,businessId,requestSn...dataInfo
     * @return JSONObject类型包含 success message
     */
    public JSONObject checkChain(JSONObject inJson) {
        logger.debug("[verifyTxDataInfo] start");

        inJson = ChainParam.smallHumpToUpperUnderline(inJson);
        logger.debug("inJson: [{}]", inJson.toJSONString());

        JSONObject outJson = new JSONObject();

        /**
         * 检查参数
         */
        if (inJson.get(ChainParam.TABLE_NAME) == null ||
                inJson.get(ChainParam.TX_HASH) == null ||
                inJson.getJSONObject(ChainParam.DATA_INFO) == null) {
            ResponseCode.putCodeAndMsg(outJson, ResponseCode.NULL_PARAMETER);
            return outJson;
        }
        String tableName = String.valueOf(inJson.get(ChainParam.TABLE_NAME));
        String txHash = String.valueOf(inJson.get(ChainParam.TX_HASH));
        
        /**
         * 根据 txHash 查询上链交易数据
         */
        List<Map<String, String>> queryResulList = SqlService.queryAllByTxHash(tableName, txHash);
        if (queryResulList == null) {
            ResponseCode.putCodeAndMsg(outJson, ResponseCode.QUERY_FAIL);
            return outJson;
        }

        if (!queryResulList.isEmpty()) {
            Map<String, String> queryResultMap = queryResulList.get(0);
            JSONObject dataInfo = inJson.getJSONObject(ChainParam.DATA_INFO);
            JSONObject chainDataInfo = getDataInfo(queryResultMap);
            logger.debug("dataInfo:         [{}]", dataInfo);
            logger.debug("chainDataInfo:    [{}]", chainDataInfo);
            logger.debug("dataInfo =? chainDataInfo [{}]", dataInfo.equals(chainDataInfo));
            if (!dataInfo.equals(chainDataInfo)) {
                logger.debug("VERIFY_TX_FAIL");
                ResponseCode.putCodeAndMsg(outJson, ResponseCode.VERIFY_TX_FAIL);
                return outJson;
            }
            logger.debug("VERIFY_TX_SUCCESS");
            ResponseCode.putCodeAndMsg(outJson, ResponseCode.VERIFY_TX_SUCCESS);
        } else {
            logger.debug("UP_CHAIN_FAIL");
            ResponseCode.putCodeAndMsg(outJson, ResponseCode.UP_CHAIN_FAIL);
        }

        outJson = ChainParam.upperUnderlineToSmallHump(outJson);
        logger.debug("outJson: [{}]", outJson.toJSONString());
        logger.debug("[verifyTxDataInfo] end");
        return outJson;
    }


    /**
     * 补偿查询接口, 如果异步推送未收到结果，可根据该接口进行主动查询。
     * @param inJson JSONObject类型包含  systemId requestSn businessId
     * @return JSONObject类型包含  txHash blockHeight blockTime dataInfo
     */
    public JSONObject reQueryChain(JSONObject inJson) {
        logger.debug("[compensateQuery] start");

        inJson = ChainParam.smallHumpToUpperUnderline(inJson);
        logger.debug("inJson: [{}]", inJson.toJSONString());

        JSONObject outJson = new JSONObject();

        /**
         * 检查参数
         */
        if (inJson.get(ChainParam.TABLE_NAME) == null || inJson.get(ChainParam.SEARCH_REQUEST_SN) == null) {
            ResponseCode.putCodeAndMsg(outJson, ResponseCode.NULL_PARAMETER);
            return outJson;
        }

        String tableName = String.valueOf(inJson.get(ChainParam.TABLE_NAME));
        String searchRequestSn = String.valueOf(inJson.get(ChainParam.SEARCH_REQUEST_SN));


        /**
         * 根据 searchRequestSn 补偿查询
         */
        List<Map<String, String>> queryResulList = SqlService.queryAllByRequestSn(tableName, searchRequestSn);
        if (queryResulList == null) {
            ResponseCode.putCodeAndMsg(outJson, ResponseCode.QUERY_FAIL);
            return outJson;
        }

        if (!queryResulList.isEmpty()) {
            Map<String, String> queryResultMap = queryResulList.get(0);
            logger.debug("onChain: [{}]", queryResultMap.get(ChainParam.ON_CHAIN));
            if (ChainParam.ON_CHAIN_SUCCESS.equals(queryResultMap.get(ChainParam.ON_CHAIN))) {
                logger.debug("UP_CHAIN_SUCCESS");
                ResponseCode.putCodeAndMsg(outJson, ResponseCode.UP_CHAIN_SUCCESS);
                String[] keys = {ChainParam.TX_HASH, ChainParam.BLOCK_HEIGHT, ChainParam.BLOCK_TIME};
                for (String key: keys) {
                    outJson.put(key, queryResultMap.get(key));
                }
                JSONObject dataInfo = getDataInfo(queryResultMap);
                outJson.put("DATA_INFO", dataInfo);
            } else {
                logger.debug("UP_CHAIN_WAITTING");
                ResponseCode.putCodeAndMsg(outJson, ResponseCode.UP_CHAIN_WAITTING);
                outJson.put("data", queryResultMap.get(ChainParam.TX_HASH));
            }
        } else {
            logger.debug("UP_CHAIN_FAIL");
            ResponseCode.putCodeAndMsg(outJson, ResponseCode.UP_CHAIN_FAIL);
        }

        outJson = ChainParam.upperUnderlineToSmallHump(outJson);
        logger.debug("outJson: [{}]", outJson.toJSONString());
        logger.debug("[compensateQuery] end");
        return outJson;
    }


    static public JSONObject getDataInfo(Map<String, String> queryResultMap) {
        JSONObject dataInfo = new JSONObject();
        for (String key: queryResultMap.keySet()) {
            if (!ChainParam.SYSTEM_PARAM.contains(key)) {
                dataInfo.put(key, queryResultMap.get(key));
            }
        }
        return dataInfo;
    }
}
