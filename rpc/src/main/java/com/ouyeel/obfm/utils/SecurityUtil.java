package com.ouyeel.obfm.utils;

import com.ouyeel.obfm.fm.business.impl.ChainParam;

import java.util.HashMap;
import java.util.Map;

public  class SecurityUtil {

    public static final Map<String, Map<String,String>> SecretMap;
    public static final Map<String, Map<String,String>> SM4KeyMap;
    static
    {
        SecretMap = new HashMap<String, Map<String, String>>();
        Map<String,String>  keyMap = new HashMap<String,String>();
        keyMap.put(ChainParam.PUBLIC_KEY, "0489A82A1A82CEE4AAEAE2B8910584F7ACC561192B09C8613650347EF57ABC46F377B682F0B572EEEA55CC1E74DECD7596C3E39121570FF5D646ABD5EDC5AFB09E");
        keyMap.put(ChainParam.PRIVATE_KEY, "00CA258FB2D851E94C82D5CD93FBE8398F90F0EB5AEB1E0BD90E996A9BF99CA79E");
        SecretMap.put("A_BE_BS_03",keyMap); // keyId

        SM4KeyMap = new HashMap<String, Map<String, String>>();
        Map<String,String>  smkeyMap = new HashMap<String,String>();
        smkeyMap.put(ChainParam.SM4_KEY, "6DAA9BF10B97AB19F983EEDAC9D70A53");
        smkeyMap.put(ChainParam.SM4_IV, "06A19559FDB5B1274CA0F4C5265C9D88");
        SecretMap.put("QXLD",keyMap);   // accountId
    }

    public static String getPrivateKey(String accountId){
       return SecretMap.get(accountId).get(ChainParam.PRIVATE_KEY);
    }
    public static String getPublicKey(String accountId){
        return SecretMap.get(accountId).get(ChainParam.PUBLIC_KEY);
    }
    public static Map<String,String> getKeys(String accountId){
        return SecretMap.get(accountId);
    }

    public static Map<String, String> getSm4Key(String keyId){
        return SM4KeyMap.get(keyId);
    }
}
