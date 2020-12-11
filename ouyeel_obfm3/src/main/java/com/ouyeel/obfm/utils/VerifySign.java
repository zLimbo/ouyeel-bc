package com.ouyeel.obfm.utils;

import com.alibaba.fastjson.JSONObject;
import com.baosight.eplat.blockchain.sdk.sm2.Utils;
import com.baosight.iplat4j.core.data.ibatis.dao.Dao;
import com.baosight.iplat4j.core.ei.EiInfo;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VerifySign {

    private static final Logger logger = LoggerFactory.getLogger(com.ouyeel.obfm.utils.VerifySign.class);

    /**
     * 验签
     */
    public static boolean valid(String systemId, String sign, JSONObject json, Dao dao) throws UnsupportedEncodingException {
        logger.info("valid param : [{}]", json);
        Map param = new HashMap();
        param.put("systemid", systemId);
        List query = dao.query("OA03.queryPubkeyBySystemId", param);
        if (query != null && !query.isEmpty()) {
//            ST01 rs01 = (ST01) query.get(0);
//            String pubkey = rs01.getPubkey();
            String pubkey = "";
            logger.info("pubkey :[{}]",pubkey);
            Map map = JSONObject.parseObject(json.toJSONString(), HashMap.class);
            String content = Utils.sortRequest(map);
            byte[] data = content.getBytes("UTF-8");
            boolean verify = Utils.verifySm2(pubkey, data, sign);
            return verify;
        }
        return false;
    }

    /**
     * 通用参数校验
     */
    public static boolean paramValid(EiInfo eiInfo) {
        logger.info("report request param : [{}]", eiInfo.toJSONString());
        if (StringUtils.isEmpty(eiInfo.getString("systemId")))
            return false;
        if (StringUtils.isEmpty(eiInfo.getString("requestSn")))
            return false;
        if (StringUtils.isEmpty(eiInfo.getString("invokeTime")))
            return false;
        if (StringUtils.isEmpty(eiInfo.getString("businessId")))
            return false;
        return !StringUtils.isEmpty(eiInfo.getString("sign"));
    }

    /**
     * 组织通用验签数据
     */
    public static JSONObject getData(EiInfo eiInfo) {
        JSONObject json = new JSONObject();
        json.put("systemId", eiInfo.getString("systemId"));
        json.put("requestSn", eiInfo.getString("requestSn"));
        json.put("invokeTime", eiInfo.getString("invokeTime"));
        json.put("businessId", eiInfo.getString("businessId"));
        return json;
    }
}
