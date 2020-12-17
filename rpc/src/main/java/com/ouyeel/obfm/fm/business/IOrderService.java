package com.ouyeel.obfm.fm.business;

import com.alibaba.fastjson.JSONObject;

/***
 * 上链接口定义
 * 注： 异步回调方法实现，在impl包中的类实现
 */
public interface IOrderService {
    /**
     * 上链接口
     * 描述：JSONObject类型包含requestSn,systemId,invokeTime,businessId,callbackUrl,keyId,accountId...dataInfo内部业务字段实现上链
     *  通过secretkey 获得sm4的秘钥
     *  通过accountId 获得上链的公钥私钥
     * @param inJson
     * @return JSONObject类型包含 txHash systemId requestSn businessId
     */
    JSONObject upChain(JSONObject inJson);

    /**
     * 链上查询
     * 描述： JSONObject类型包含 requestSn systemId txHash
     * @param inJson
     * @return JSONObject类型包含  txHash invokeTime blockTime blockHeight  dataInfo
     */
    JSONObject queryChain(JSONObject inJson);

    /**
     * 验证接口
     * 描述： JSONObject类型包含  txHash systemId,businessId,requestSn...dataInfo内部业务字段实现上链
     * @param inJson
     * @return JSONObject类型包含 success message
     */
    JSONObject checkChain(JSONObject inJson);

    /**
     * 补偿查询
     * 描述： JSONObject类型包含  systemId requestSn businessId
     * @param inJson
     * @return  JSONObject类型包含  txHash blockHeight blockTime dataInfo
     */
    JSONObject reQueryChain(JSONObject inJson);
}
