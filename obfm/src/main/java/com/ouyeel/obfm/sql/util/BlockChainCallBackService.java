package com.ouyeel.obfm.sql.util;

//import com.baosight.iplat4j.core.ei.EiConstant;
//import com.baosight.iplat4j.core.ei.EiInfo;
//import com.baosight.iplat4j.core.ioc.spring.PlatApplicationContext;
//import com.baosight.iplat4j.core.service.soa.XServiceManager;
//import com.baosight.obmp.chain.sql.config.ChainConfig;
//import com.baosight.obmp.chain.sql.dao.impl.ChainDaoImpl;
//import com.baosight.obmp.util.ResultCode;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.ouyeel.obfm.sql.config.ChainConfig;
import com.ouyeel.obfm.sql.dao.impl.ChainDaoImpl;
import com.ouyeel.obfm.sql.stateparse.StateParser;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 上链结果通知
 */
@Service
public class BlockChainCallBackService {

    private static final Logger logger = LoggerFactory.getLogger(BlockChainCallBackService.class);

    @Autowired
    private ChainDaoImpl commonDao;

//    @Autowired
//    private StateParse stateParse;




//    //上链结果通知次数
//    private static final String NOTICE_NUM = PlatApplicationContext.getProperty("eplat.obmp.dr.sc.notice.num");
//    //上链结果查询次数
//    private static final String QUERY_NUM = PlatApplicationContext.getProperty("eplat.obmp.dr.sc.query.num");
//    //上链结果通知失败间隔
//    private static final String CALLBACK_INTERVAL = PlatApplicationContext.getProperty("eplat.obmp.dr.sc.callback.interval");
//    //上链结果查询间隔
//    private static final String QUERY_INTERVAL = PlatApplicationContext.getProperty("eplat.obmp.dr.sc.query.interval");

    //上链结果通知次数
    private static final String NOTICE_NUM = "3";
    //上链结果查询次数
    private static final String QUERY_NUM = "3";
    //上链结果通知失败间隔
    private static final String CALLBACK_INTERVAL = "3000";
    //上链结果查询间隔
    private static final String QUERY_INTERVAL = "5000";

    public void callback(String callbackServiceId, String tableName, String requestSn) {
        logger.info("上链结果通知参数：callbackServiceId : [" + callbackServiceId + "],tableName:[" + tableName + "],requestSn:[" + requestSn + "]");

        Map<String, String> map = null;
        boolean isResult = false;
        //上链结果查询
        for (int i = 1; i <= Integer.parseInt(QUERY_NUM); i++) {
            List<Map<String, String>> list = commonDao.queryCallbackByRequestSn(tableName, requestSn);
            logger.info("上链结果查询第[" + i + "]次，结果：[{}]", list);

//            if (CollectionUtils.isNotEmpty(list)) {
//                isResult = true;
//                map = list.get(0);
//                break;
//            }
            if (CollectionUtils.isNotEmpty(list)) {
                map = list.get(0);
                String onChain = String.valueOf(map.get(ChainConfig.ON_CHAIN));
                if (ChainConfig.ON_CHAIN_SUCCESS.equals(onChain)) {
                    isResult = true;
                    break;
                }
            }

            //
            try {
                logger.warn("上链结果查询，第[" + i + "]次未查到结果。");
                TimeUnit.MILLISECONDS.sleep(Long.valueOf(QUERY_INTERVAL));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (isResult) {
            Map<String, String> callBackMap = buildNoticeParam(map);
            for (int i = 1; i <= Integer.valueOf(NOTICE_NUM); i++) {
//                EiInfo eiInfo = ResultCode.buildSuccess();
////                eiInfo.set(EiConstant.serviceId, callbackServiceId);
////                eiInfo.set(ChainConfig.LOWERCASE_CALLBACK_URL, map.get(ChainConfig.CALLBACK_URL));
////                eiInfo.set("data", callBackMap);
////
////                logger.info("上链结果通知，参数：[{}]", eiInfo.toJSONString());
////                EiInfo respInfo = XServiceManager.call(eiInfo);
////                logger.info("上链结果通知，第[" + i + "]次，通知结果：[{}]", respInfo.toJSONString());
////                if (respInfo.getStatus() == Integer.valueOf(ResultCode.SUCCESS.getCode())) {
////                    logger.info("callback success ...");
////                    break;
////                }
                JSONObject callBackJson = new JSONObject();
                callBackJson.putAll(callBackMap);
                String response = null;
                try {
                    response = HttpUtil.send(callbackServiceId, callBackJson, "UTF-8");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (StringUtils.isNotEmpty(response)) {
                    JSONObject json = JSONObject.parseObject(response);
                    if (json.getBoolean(ChainConfig.SUCCESS)) {
                        logger.debug("callback success ...");
                        break;
                    }
                }

                //
                logger.warn("结果通知失败，重试。");
                try {
                    TimeUnit.SECONDS.sleep(Long.valueOf(CALLBACK_INTERVAL));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } else {
            logger.warn("上链结果通知,查询无结果，tableName:[" + tableName + "],requestSn:[" + requestSn + "],不存在.");
        }
    }

    private Map<String, String> buildNoticeParam(Map<String, String> map) {
        Map<String, String> callBackMap = Maps.newHashMap();
        callBackMap.put(ChainConfig.LOWERCASE_TX_HASH, map.get(ChainConfig.TX_HASH));
        callBackMap.put(ChainConfig.LOWERCASE_BLOCK_TIME, map.get(ChainConfig.BLOCK_TIME));
        callBackMap.put(ChainConfig.LOWERCASE_BLOCK_HEIGHT, map.get(ChainConfig.BLOCK_HEIGHT));
        return callBackMap;
    }
}
