package com.ouyeel.obfm.fm.business.impl;

import com.alibaba.fastjson.JSONObject;
import com.ouyeel.obfm.fm.business.IOrderService;
import com.ouyeel.obfm.utils.SecurityUtil;

import java.util.Map;

public class OrderService implements IOrderService {


    public JSONObject upChain(JSONObject inJson) {
        // sm4 的秘钥
        Map<String,String> keys = SecurityUtil.getSm4Key(inJson.getString("keyId"));
        // publickey
        String publickey = SecurityUtil.getPublicKey(inJson.getString("accountId"));
        // privatekey
        String privatekey = SecurityUtil.getPrivateKey(inJson.getString("accountId"));

        /**
         * your code
         */

        return null;
    }

    public JSONObject queryChain(JSONObject inJson) {
        return null;
    }

    public JSONObject checkChain(JSONObject inJson) {
        return null;
    }

    public JSONObject reQueryChain(JSONObject inJson) {
        return null;
    }
}
