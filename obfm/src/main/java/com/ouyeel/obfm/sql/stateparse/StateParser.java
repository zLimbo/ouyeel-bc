package com.ouyeel.obfm.sql.stateparse;

import com.alibaba.fastjson.JSONObject;
import com.ouyeel.obfm.sql.config.ChainConfig;
import com.alibaba.fastjson.JSONArray;

import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;


public class StateParser {


    /**
     * * 解析状态信息内容
     *
     * @param message state信息字符串
     * @return
     */
    public JSONObject parseMix(String message){
        String countMessage = message.substring(0, ChainConfig.STATE_COUNT_LEN);
        String timeMessage = "0x" + message.substring(ChainConfig.STATE_COUNT_LEN);
        JSONObject json = new JSONObject();
        json.putAll(parseCount(countMessage));
        json.putAll(parseTime(timeMessage));
        return json;
    }

    /**
     * * 解析上链总数信息内容
     *
     * @param countMessage 信息字符串
     * @return
     */
    public JSONObject parseCount(String countMessage){
        countMessage = countMessage.substring(0, ChainConfig.STATE_COUNT_LEN);
        String message = countMessage.substring(2);
        BigInteger bigInteger = new BigInteger(message, 16);

        String count = bigInteger.toString();
        System.out.println("count: " + count);
        JSONObject json = new JSONObject();
        json.put(ChainConfig.QUERY_STATE_COUNT, count);
        return json;
    }

    /**
     * * 解析最近上链三笔上链时间信息内容
     *
     * @param timeMessage 时间信息字符串
     * @return
     */
    public JSONObject parseTime(String timeMessage){
        timeMessage = timeMessage.substring(0, ChainConfig.STATE_TIME_LEN);
        String message = timeMessage.substring(2);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < 3; ++i) {
            String hexStr = message.substring(ChainConfig.STATE_SINGLE_LEN * i, ChainConfig.STATE_SINGLE_LEN * (i + 1));
            System.out.println("hexStr: " + hexStr);
            BigInteger bigInteger = new BigInteger(hexStr, 16);
            long timestamp = bigInteger.longValue();
            if (timestamp != 0) {
                String date = dateFormat.format(timestamp);
                jsonArray.add(date);
            } else {
                jsonArray.add("-");
            }
        }
        JSONObject json = new JSONObject();
        json.put(ChainConfig.QUERY_STATE_TIME, jsonArray);

        return json;
    }


    public static void main(String[] args) {
        String result = "0x" +
                "0000000000000000000000000000000000000000000000000000000000000007" +
                "00000000000000000000000000000000000000000000000000000178f25eaa79" +
                "00000000000000000000000000000000000000000000000000000178f25bbc52" +
                "00000000000000000000000000000000000000000000000000000178f25e9490^@";

        StateParser stateParser = new StateParser();

        JSONObject stateResult = stateParser.parseMix(result);

        System.out.println(JSONObject.toJSONString(stateResult, true));
    }
}

