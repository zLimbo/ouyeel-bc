package com.zlimbo.rpc.fm.business.impl;

import com.alibaba.fastjson.JSONObject;
import com.zlimbo.rpc.fm.business.IOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderService implements IOrderService {

    final static Logger logger = LoggerFactory.getLogger(OrderService.class);


    /**
     * 业务数据上链存证接口, 将业务信息进行上链存证。不对业务数据进行关联。
     * @param inJson JSONObject类型包含requestSn,systemId,invokeTime,businessId,callbackUrl,keyId,accountId...dataInfo
     * @return JSONObject类型包含 txHash systemId requestSn businessId
     */
    public JSONObject upChain(JSONObject inJson) {
        logger.debug("[upChain] start");
        logger.debug("inJson: [{}]", inJson.toJSONString());

        /**
         * 如果传入的inJson字段里没有密钥，须从这里获取
         */
//        // sm4 的秘钥
//        inJson.put(ChainParam.SECRET_KEY, SecurityUtil.getSm4Key(inJson.getString(ChainParam.KEY_ID)));
//        // publickey
//        inJson.put(ChainParam.PUBLIC_KEY, SecurityUtil.getPublicKey(inJson.getString(ChainParam.ACCOUNT_ID)));
//        // privatekey
//        inJson.put(ChainParam.PRIVATE_KEY, SecurityUtil.getPrivateKey(inJson.getString(ChainParam.ACCOUNT_ID)));

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
            logger.debug("dataInfo =? chainDataInfo [{}]", dataInfo.equals(chainDataInfo));
            logger.debug("dataInfo:         [{}]", dataInfo);
            logger.debug("chainDataInfo:    [{}]", chainDataInfo);
            for (String key: dataInfo.keySet()) {
                logger.debug("compare [{}]: [{}] =? [{}]", key, dataInfo.get(key), queryResultMap.get(key));
                if (!dataInfo.get(key).equals(queryResultMap.get(key))) {
                    logger.debug(dataInfo.get(key) + " != " + queryResultMap.get(key));
                    logger.debug("VERIFY_TX_FAIL");
                    ResponseCode.putCodeAndMsg(outJson, ResponseCode.VERIFY_TX_FAIL);
                    return outJson;
                }
            }
            logger.debug("VERIFY_TX_SUCCESS");
            ResponseCode.putCodeAndMsg(outJson, ResponseCode.VERIFY_TX_SUCCESS);
        } else {
            logger.debug("UP_CHAIN_FAIL");
            ResponseCode.putCodeAndMsg(outJson, ResponseCode.UP_CHAIN_FAIL);
        }

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

        logger.debug("[compensateQuery] end");
        return outJson;
    }


    JSONObject getDataInfo(Map<String, String> queryResultMap) {
        JSONObject dataInfo = new JSONObject();
        for (String key: queryResultMap.keySet()) {
            if (!ChainParam.SYSTEM_PARAM.contains(key)) {
                dataInfo.put(key, queryResultMap.get(key));
            }
        }
        return dataInfo;
    }
}
