package com.ouyeel.obfm.fm.service;

/***
 * 类说明：
 * 该类是宝武集团内部iplat4j框架的服务，华东师大不需要实现此服务
 * 由于很多内部类和防范无法提供，该类注释，请华师大参考代码逻辑，有助于实现OrderService类
 */

//public class ServiceFM01 {
//    private static final Logger logger = LoggerFactory.getLogger(ServiceFM01.class);
//    private Dao dao = new Dao();
//
//    @Autowired
//    private OrderService orderService;
//
//    /**
//     * 链上存证 S_FM_01
//     *
//     * @param
//     * @return eiInfo
//     */
//    public EiInfo upChain(EiInfo inInfo) throws UnsupportedEncodingException {
//        logger.info("公文秘钥链上存证 : [{}]", inInfo.toJSONString());
//        boolean paramValid = VerifySign.paramValid(inInfo);
//
//        String dataInfo = inInfo.getString("dataInfo");
//        JSONObject jsonObject = JSONObject.parseObject(dataInfo);
//
//        if (paramValid) {
//            JSONObject json = VerifySign.getData(inInfo);
//            json.put("callbackUrl", inInfo.getString("callbackUrl"));
//            json.put("dataInfo", jsonObject);
//            String systemId = inInfo.getString("systemId");
//            String sign = inInfo.getString("sign");
//            logger.info("验证json : [{}]", json.toJSONString());
//            if (VerifySign.valid(systemId, sign, json, dao)) { // 验签方法
//                JSONObject inJson = JSONObject.parseObject(inInfo.toJSONString());
//                // 调用上链服务
//                JSONObject outJson = orderService.upChain(inJson);
//                // 返回上链结果
//                EiInfo outInfo = EiInfo.parseJSONString(outJson.toJSONString());
//                return outInfo;
//            } else {
//                return ResultCode.buildEiInfo("验签失败");
//            }
//        } else {
//            return ResultCode.buildEiInfo("参数校验失败");
//        }
//    }
//
//    /**
//     * 链上查证 S_FM_02
//     *
//     * @param
//     * @return eiInfo
//     */
//    public EiInfo queryChain(EiInfo inInfo) throws UnsupportedEncodingException {
//        logger.info("公文秘钥链上存证 : [{}]", inInfo.toJSONString());
//        boolean paramValid = VerifySign.paramValid(inInfo);
//
//        String dataInfo = inInfo.getString("dataInfo");
//        JSONObject jsonObject = JSONObject.parseObject(dataInfo);
//
//        if (paramValid) {
//            JSONObject json = VerifySign.getData(inInfo);
//            json.put("callbackUrl", inInfo.getString("callbackUrl"));
//            json.put("dataInfo", jsonObject);
//
//            String systemId = inInfo.getString("systemId");
//            String sign = inInfo.getString("sign");
//            logger.info("验证json : [{}]", json.toJSONString());
//            if (VerifySign.valid(systemId, sign, json, dao)) {
//                JSONObject inJson = JSONObject.parseObject(inInfo.toJSONString());
//                // 调用查证服务
//                JSONObject outJson = orderService.queryChain(inJson);
//                // 返回上链结果
//                EiInfo outInfo = EiInfo.parseJSONString(outJson.toJSONString());
//
//                return outInfo;
//            } else {
//                return ResultCode.buildEiInfo("验签失败");
//            }
//        } else {
//            return ResultCode.buildEiInfo("参数校验失败");
//        }
//    }
//
//
//    /**
//     * 链上验证 S_FM_03
//     *
//     * @param
//     * @return eiInfo
//     */
//    public EiInfo checkChain(EiInfo inInfo) throws UnsupportedEncodingException {
//        logger.info("公文秘钥链上存证 : [{}]", inInfo.toJSONString());
//        boolean paramValid = VerifySign.paramValid(inInfo);
//
//        String dataInfo = inInfo.getString("dataInfo");
//        JSONObject jsonObject = JSONObject.parseObject(dataInfo);
//
//
//
//        if (paramValid) {
//            JSONObject json = VerifySign.getData(inInfo);
//            json.put("callbackUrl", inInfo.getString("callbackUrl"));
//            json.put("dataInfo", jsonObject);
//
//            String systemId = inInfo.getString("systemId");
//            String sign = inInfo.getString("sign");
//            logger.info("验证json : [{}]", json.toJSONString());
//            if (VerifySign.valid(systemId, sign, json, dao)) {
//                JSONObject inJson = JSONObject.parseObject(inInfo.toJSONString());
//                // 调用验证服务
//                JSONObject outJson = orderService.checkChain(inJson);
//                // 返回上链结果
//                EiInfo outInfo = EiInfo.parseJSONString(outJson.toJSONString());
//                return outInfo;
//            } else {
//                return ResultCode.buildEiInfo("验签失败");
//            }
//        } else {
//            return ResultCode.buildEiInfo("参数校验失败");
//        }
//    }
//
//    /**
//     * 链上补偿查询 S_FM_04
//     *
//     * @param
//     * @return eiInfo
//     */
//    public EiInfo reQueryChain(EiInfo inInfo) throws UnsupportedEncodingException {
//        logger.info("公文秘钥链上存证 : [{}]", inInfo.toJSONString());
//        boolean paramValid = VerifySign.paramValid(inInfo);
//
//        String dataInfo = inInfo.getString("dataInfo");
//        JSONObject jsonObject = JSONObject.parseObject(dataInfo);
//
//        if (paramValid) {
//            JSONObject json = VerifySign.getData(inInfo);
//            json.put("callbackUrl", inInfo.getString("callbackUrl"));
//            json.put("dataInfo", jsonObject);
//
//            String systemId = inInfo.getString("systemId");
//            String sign = inInfo.getString("sign");
//            logger.info("验证json : [{}]", json.toJSONString());
//            if (VerifySign.valid(systemId, sign, json, dao)) {
//                JSONObject inJson = JSONObject.parseObject(inInfo.toJSONString());
//                // 调用验证服务
//                JSONObject outJson = orderService.reQueryChain(inJson);
//                // 返回上链结果
//                EiInfo outInfo = EiInfo.parseJSONString(outJson.toJSONString());
//                return outInfo;
//            } else {
//                return ResultCode.buildEiInfo("验签失败");
//            }
//        } else {
//            return ResultCode.buildEiInfo("参数校验失败");
//        }
//    }
//
//}
