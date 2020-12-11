package com.ouyeel.obfm.fm.service;

import com.alibaba.fastjson.JSONObject;
import com.baosight.iplat4j.core.ei.EiInfo;
import com.baosight.iplat4j.core.service.impl.ServiceBase;
import com.ouyeel.obfm.fm.business.impl.OrderService;
import com.ouyeel.obfm.utils.ResultCode;
import com.ouyeel.obfm.utils.VerifySign;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.UnsupportedEncodingException;

public class ServiceFM01 extends ServiceBase {
    private static final Logger logger = LoggerFactory.getLogger(ServiceFM01.class);

    @Autowired
    private OrderService orderService;

    /**
     * 链上存证 S_FM_01
     *
     * @param
     * @return eiInfo
     */
    public EiInfo upChain(EiInfo inInfo) throws UnsupportedEncodingException {
        logger.info("公文秘钥链上存证 : [{}]", inInfo.toJSONString());
        boolean paramValid = VerifySign.paramValid(inInfo);

        String dataInfo = inInfo.getString("dataInfo");
        JSONObject jsonObject = JSONObject.parseObject(dataInfo);

        if (paramValid) {
            if (StringUtils.isEmpty(dataInfo))
                paramValid = false;
        }
        if (paramValid) {
            JSONObject json = VerifySign.getData(inInfo);
            json.put("callbackUrl", inInfo.getString("callbackUrl"));
            json.put("dataInfo", jsonObject);
            String systemId = inInfo.getString("systemId");
            String sign = inInfo.getString("sign");
            logger.info("验证json : [{}]", json.toJSONString());
            if (VerifySign.valid(systemId, sign, json, dao)) {
                inInfo.set("privateKey","");
                inInfo.set("publicKey","");
                JSONObject inJson = JSONObject.parseObject(inInfo.toJSONString());
                // 调用上链服务
                JSONObject outJson = orderService.upChain(inJson);
                // 返回上链结果
                EiInfo outInfo = EiInfo.parseJSONString(outJson.toJSONString());
                return outInfo;
            } else {
                return ResultCode.buildEiInfo("验签失败");
            }
        } else {
            return ResultCode.buildEiInfo("参数校验失败");
        }
    }

    /**
     * 链上查证 S_FM_02
     *
     * @param
     * @return eiInfo
     */
    public EiInfo queryChain(EiInfo inInfo) throws UnsupportedEncodingException {
        logger.info("公文秘钥链上存证 : [{}]", inInfo.toJSONString());
        boolean paramValid = VerifySign.paramValid(inInfo);

        String dataInfo = inInfo.getString("dataInfo");
        JSONObject jsonObject = JSONObject.parseObject(dataInfo);

        if (paramValid) {
            if (StringUtils.isEmpty(dataInfo))
                paramValid = false;
        }

        if (paramValid) {
            JSONObject json = VerifySign.getData(inInfo);
            json.put("callbackUrl", inInfo.getString("callbackUrl"));
            json.put("dataInfo", jsonObject);

            String systemId = inInfo.getString("systemId");
            String sign = inInfo.getString("sign");
            logger.info("验证json : [{}]", json.toJSONString());
            if (VerifySign.valid(systemId, sign, json, dao)) {
                JSONObject inJson = JSONObject.parseObject(inInfo.toJSONString());
                // 调用查证服务
                JSONObject outJson = orderService.queryChain(inJson);
                // 返回上链结果
                EiInfo outInfo = EiInfo.parseJSONString(outJson.toJSONString());

                return outInfo;
            } else {
                return ResultCode.buildEiInfo("验签失败");
            }
        } else {
            return ResultCode.buildEiInfo("参数校验失败");
        }
    }


    /**
     * 链上验证 S_FM_03
     *
     * @param
     * @return eiInfo
     */
    public EiInfo checkChain(EiInfo inInfo) throws UnsupportedEncodingException {
        logger.info("公文秘钥链上存证 : [{}]", inInfo.toJSONString());
        boolean paramValid = VerifySign.paramValid(inInfo);

        String dataInfo = inInfo.getString("dataInfo");
        JSONObject jsonObject = JSONObject.parseObject(dataInfo);

        if (paramValid) {
            if (StringUtils.isEmpty(dataInfo))
                paramValid = false;
        }

        if (paramValid) {
            JSONObject json = VerifySign.getData(inInfo);
            json.put("callbackUrl", inInfo.getString("callbackUrl"));
            json.put("dataInfo", jsonObject);

            String systemId = inInfo.getString("systemId");
            String sign = inInfo.getString("sign");
            logger.info("验证json : [{}]", json.toJSONString());
            if (VerifySign.valid(systemId, sign, json, dao)) {
                JSONObject inJson = JSONObject.parseObject(inInfo.toJSONString());
                // 调用验证服务
                JSONObject outJson = orderService.checkChain(inJson);
                // 返回上链结果
                EiInfo outInfo = EiInfo.parseJSONString(outJson.toJSONString());
                return outInfo;
            } else {
                return ResultCode.buildEiInfo("验签失败");
            }
        } else {
            return ResultCode.buildEiInfo("参数校验失败");
        }
    }

    /**
     * 链上补偿查询 S_FM_04
     *
     * @param
     * @return eiInfo
     */
    public EiInfo reQueryChain(EiInfo inInfo) throws UnsupportedEncodingException {
        logger.info("公文秘钥链上存证 : [{}]", inInfo.toJSONString());
        boolean paramValid = VerifySign.paramValid(inInfo);

        String dataInfo = inInfo.getString("dataInfo");
        JSONObject jsonObject = JSONObject.parseObject(dataInfo);

        if (paramValid) {
            if (StringUtils.isEmpty(dataInfo))
                paramValid = false;
        }

        if (paramValid) {
            JSONObject json = VerifySign.getData(inInfo);
            json.put("callbackUrl", inInfo.getString("callbackUrl"));
            json.put("dataInfo", jsonObject);

            String systemId = inInfo.getString("systemId");
            String sign = inInfo.getString("sign");
            logger.info("验证json : [{}]", json.toJSONString());
            if (VerifySign.valid(systemId, sign, json, dao)) {
                JSONObject inJson = JSONObject.parseObject(inInfo.toJSONString());
                // 调用验证服务
                JSONObject outJson = orderService.reQueryChain(inJson);
                // 返回上链结果
                EiInfo outInfo = EiInfo.parseJSONString(outJson.toJSONString());
                return outInfo;
            } else {
                return ResultCode.buildEiInfo("验签失败");
            }
        } else {
            return ResultCode.buildEiInfo("参数校验失败");
        }
    }

}
