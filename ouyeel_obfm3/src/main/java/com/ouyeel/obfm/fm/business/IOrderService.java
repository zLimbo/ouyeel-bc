package com.ouyeel.obfm.fm.business;

import com.alibaba.fastjson.JSONObject;

/***
 * 上链接口定义
 * 注： 异步回调方法实现，在impl包中的类实现
 */
public interface IOrderService {
    /**
     *      * 上链接口
     * 描述：JSONObject类型包含tableName,systemId,...dataInfo内部业务字段实现上链
     *
     * @param inJson
     * @return JSONObject类型包含 txHash systemId requestSn
     */
    public JSONObject upChain(JSONObject inJson);

    /**
     * 链上查询
     * 描述： JSONObject类型包含 tableName systemId txHash
     * @param inJson
     * @return JSONObject类型包含 tableName txHash blockHeight blockTime dataInfo
     */
    public JSONObject queryChain(JSONObject inJson);

    /**
     * 验证接口
     * 描述： JSONObject类型包含 tableName txHash systemId,...dataInfo内部业务字段实现上链
     * @param inJson
     * @return JSONObject类型包含 success message
     */
    public JSONObject checkChain(JSONObject inJson);

    /**
     * 补偿查询
     * 描述： JSONObject类型包含 tableName systemId requestSn
     * @param inJson
     * @return  JSONObject类型包含 tableName txHash blockHeight blockTime dataInfo
     */
    public JSONObject reQueryChain(JSONObject inJson);
}
