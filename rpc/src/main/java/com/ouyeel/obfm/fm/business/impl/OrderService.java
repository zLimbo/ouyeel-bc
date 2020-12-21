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
     * @param systemId
     * @return
     */
    private static Map<String, String> obtainKey(String systemId) {
        Map<String, String> outMap = new HashMap<>();

        Map<String, String> sm4KeyMap = SecurityUtil.getSm4Key(systemId);
        String sm4Key = sm4KeyMap.get(ChainConfig.SM4_KEY);
        String sm4Iv = sm4KeyMap.get(ChainConfig.SM4_IV);
        String publicKey = SecurityUtil.getPublicKey(systemId);
        String privateKey = SecurityUtil.getPrivateKey(systemId);
        logger.debug("systemId: [{}]", systemId);
        logger.debug("sm4Key: [{}]", sm4Key);
        logger.debug("sm4Iv: [{}]", sm4Iv);
        logger.debug("publicKey: [{}]", publicKey);
        logger.debug("privateKey: [{}]", privateKey);
        outMap.put(ChainConfig.SM4_KEY, ChainConfig.lowerCase(sm4Key));
        outMap.put(ChainConfig.SM4_IV, ChainConfig.lowerCase(sm4Iv));
        outMap.put(ChainConfig.PUBLIC_KEY, ChainConfig.lowerCase(publicKey));
        outMap.put(ChainConfig.PRIVATE_KEY, ChainConfig.lowerCase(privateKey));

        return outMap;
    }


    /**
     * 业务数据上链存证接口, 将业务信息进行上链存证。不对业务数据进行关联。
     * @param inJson JSONObject类型包含requestSn,systemId,invokeTime,businessId,callbackUrl,keyId,accountId...dataInfo
     * @return JSONObject类型包含 txHash systemId requestSn businessId
     */
    public JSONObject upChain(JSONObject inJson) {
        logger.debug("[upChain] start");

        JSONObject outJson = new JSONObject();
        try {
            inJson = ChainConfig.smallHumpToUpperUnderline(JSONObject.parseObject(inJson.toJSONString()));
        } catch (Exception e) {
            logger.error("PARAMETER_ERROR!");
            ResponseCode.putCodeAndMsg(outJson, ResponseCode.PARAMETER_ERROR);
            return ChainConfig.upperUnderlineToSmallHump(outJson);
        }
        logger.debug("inJson: [{}]", inJson.toJSONString());

        try {
            Map<String, String> keys = obtainKey(inJson.getString(ChainConfig.SYSTEM_ID));
            inJson.putAll(keys);
        } catch (Exception e) {
            logger.error("OBTAIN_KEY_FAIL!");
            ResponseCode.putCodeAndMsg(outJson, ResponseCode.OBTAIN_KEY_FAIL);
            return ChainConfig.upperUnderlineToSmallHump(outJson);
        }


        /**
         * dataInfo单独获取，但字段参数统一检查，为空则返回参数为空错误码
         */
        Map<String, String> paramMap = new HashMap<>();
        for (String key: ChainConfig.UP_CHAIN_PARAM) {
            if (inJson.get(key) == null) {
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
        boolean insertResult = ChainConfig.CHAIN_DAO.insertTx(paramMap);
        if (!insertResult) {
            logger.debug("upChain fail");
            ResponseCode.putCodeAndMsg(outJson, ResponseCode.NO_TX);
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

        /**
         * 查询hash值
         */
        String txHash = ChainConfig.CHAIN_DAO.queryTxHashByRequestSn(
                paramMap.get(ChainConfig.TABLE_NAME), paramMap.get(ChainConfig.REQUEST_SN));
        if (txHash != null) {
            logger.debug("txHash: [{}]", txHash);
            outJson.put(ChainConfig.TX_HASH, txHash);
        } else {
            logger.debug("query txHash fail");
        }

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
            inJson = ChainConfig.smallHumpToUpperUnderline(JSONObject.parseObject(inJson.toJSONString()));
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
        List<Map<String, String>> queryResulList = ChainConfig.CHAIN_DAO.queryAllByTxHash(tableName, txHash);
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
            inJson = ChainConfig.smallHumpToUpperUnderline(JSONObject.parseObject(inJson.toJSONString()));
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
        List<Map<String, String>> queryResulList = ChainConfig.CHAIN_DAO.queryAllByTxHash(tableName, txHash);
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
            inJson = ChainConfig.smallHumpToUpperUnderline(JSONObject.parseObject(inJson.toJSONString()));
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
        List<Map<String, String>> queryResulList = ChainConfig.CHAIN_DAO.queryAllByRequestSn(tableName, searchRequestSn);
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
