package com.ouyeel.obfm.utils;

import com.ouyeel.obfm.fm.business.impl.ChainConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public  class SecurityUtil {

    public static final Map<String, Map<String,String>> SecretMap;
    public static final Map<String, Map<String,String>> SM4KeyMap;
    static
    {
        SecretMap = new HashMap<String, Map<String, String>>();
        Map<String,String>  keyMap = new HashMap<String,String>();
        keyMap.put(ChainConfig.PUBLIC_KEY, "2204404536ab867d9a964bfcc5e6fdaa7d77e509ce5891d38b3ebbb036e5c225994597ea6d0bdff3539fd3062b3943a1c7dd75d173f35101b71298e9f7f08d51");
        keyMap.put(ChainConfig.PRIVATE_KEY, "d6c83aee4bfbeb135a2dcef8c803b186d0678a99002b09d3c60c22aca7105005");
        SecretMap.put("002",keyMap); // accountId

        SM4KeyMap = new HashMap<String, Map<String, String>>();
        Map<String,String>  smkeyMap = new HashMap<String,String>();
        smkeyMap.put(ChainConfig.SM4_KEY, "0123456789abcdef0123456789abcdef");
        smkeyMap.put(ChainConfig.SM4_IV, "0123456789abcdef0123456789abcdef");

        SM4KeyMap.put("0001", smkeyMap);   // keyId

    }

    public static String getPrivateKey(String accountId){
        accountId = "002"; // 测试
       return SecretMap.get(accountId).get(ChainConfig.PRIVATE_KEY);
    }
    public static String getPublicKey(String accountId){
        accountId = "002"; // 测试
        return SecretMap.get(accountId).get(ChainConfig.PUBLIC_KEY);
    }
    public static Map<String,String> getKeys(String accountId){
        return SecretMap.get(accountId);
    }

    public static Map<String, String> getSm4Key(String keyId){
        if (!SM4KeyMap.containsKey(keyId)) {
            Map<String,String>  smkeyMap = new HashMap<String,String>();
            smkeyMap.put(ChainConfig.SM4_KEY, randomHexString(32));
            smkeyMap.put(ChainConfig.SM4_IV, randomHexString(32));
            SM4KeyMap.put(keyId, smkeyMap);   // akeyId
        }
        return SM4KeyMap.get(keyId);
    }


    static String randomHexString(int n) {
        StringBuilder stringBuilder = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < n; ++i) {
            Integer integer = random.nextInt(16);
            stringBuilder.append(Integer.toHexString(integer));
        }
        System.out.println(stringBuilder.substring(0, n));
        return stringBuilder.substring(0, n);
    }
}
