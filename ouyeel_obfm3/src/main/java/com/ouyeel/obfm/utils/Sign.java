package com.ouyeel.obfm.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baosight.eplat.blockchain.sdk.sm2.Utils;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * 签名样例 参考 com.baosight.eplat.blockchain.sdk.sm2包
 *
 */
public class Sign {
    public String signInfo(JSONObject json) throws UnsupportedEncodingException {
        String privateKey = "44458E6CAAE286E793193DD0BC56D766AB162CC8A515AAF963B7137EDFAAEE32";
        Map<String, Object> param = JSON.parseObject(json.toJSONString(), HashMap.class);
        String content = Utils.sortRequest(param);
        String sign = Utils.signSm2(privateKey,content.getBytes("UTF-8"));
        return sign;
    }
}
