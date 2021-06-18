package com.ouyeel.obfm.sql.util;

import com.alibaba.fastjson.JSONObject;
//import com.baosight.iplat4j.core.ei.EiConstant;
//import com.baosight.iplat4j.core.ei.EiInfo;
//import com.baosight.iplat4j.core.service.soa.XServiceManager;
//import com.baosight.obmp.chain.sql.basic.CommonConstant;
//import com.baosight.obmp.chain.sql.basic.ExceptionCode;
//import com.baosight.obmp.chain.sql.config.ChainConfig;
//import com.baosight.obmp.chain.sql.exception.BusinessException;
import com.google.common.collect.Maps;
import com.ouyeel.obfm.sql.basic.CommonConstant;
import com.ouyeel.obfm.sql.basic.ExceptionCode;
import com.ouyeel.obfm.sql.config.ChainConfig;
import com.ouyeel.obfm.sql.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * 区块链账户，秘钥工具类
 */
@Service
public class BlockChainBasicService {

    private static final Logger logger = LoggerFactory.getLogger(BlockChainBasicService.class);

    static class  Test implements Runnable{

        @Override
        public void run() {
            System.out.println(111);
        }
    }

    /**
     * * 获取相关的密钥
     * * S_BE_BS_12 查询账户信息
     * * S_BE_BS_13查询加密密钥信息
     * * 请求参数
     * * info.setCell(EiConstant.queryBlock,0,"accountId","[账户ID]");  //S_BE_BS_12 查询账户
     * * info.setCell(EiConstant.queryBlock,0,"encrypKeyId","[密钥ID]");//S_BE_BS_13 查询加密密钥
     *
     * @param accountId 账户ID
     * @param keyId     密钥ID
     * @return
     */
//    public Map<String, String> obtainKey(String accountId, String keyId) {
//        BlockAccountKeyParam account = BlockAccountKeyParam.build("accountId",
//                                        accountId,
//                                        CommonConstant.BLOCK_CHAIN_ACCOUNT_ID);
//        BlockAccountKeyParam encrypKey = BlockAccountKeyParam.build("encrypKeyId",
//                                        keyId,
//                                        CommonConstant.BLOCK_CHAIN_SECRET_KEY_ID);
//        Future<EiInfo> accountTask = BusinessExecutors.getThreadPool().submit(account);
//        Future<EiInfo> encrypTask = BusinessExecutors.getThreadPool().submit(encrypKey);
//
//        Map<String, String> map = Maps.newHashMap();
//        try {
//            EiInfo accountEiInfo = accountTask.get();
//            logger.info("区块链账户查询结果：[{}] ", accountEiInfo.toJSONString());
//            String privateKey = accountEiInfo.getCellStr(EiConstant.resultBlock, 0, "privateKey");
//            String publicKey = accountEiInfo.getCellStr(EiConstant.resultBlock, 0, "publicKey");
//
//            EiInfo encrypEiInfo = encrypTask.get();
//            logger.info("区块链秘钥查询结果：[{}] ", accountEiInfo.toJSONString());
//            String encryp = encrypEiInfo.getCellStr(EiConstant.resultBlock, 0, "encrypKey");
//            JSONObject object = JSONObject.parseObject(encryp);
//
//            map.put(ChainConfig.PRIVATE_KEY, privateKey);
//            map.put(ChainConfig.PUBLIC_KEY, publicKey);
//            map.put(ChainConfig.SM4_KEY, object.getString("SM4key"));
//            map.put(ChainConfig.SM4_IV, object.getString("SM4iv"));
//        } catch (Exception e) {
//            logger.error("区块链账户，秘钥获取失败");
//            e.printStackTrace();
//            throw new BusinessException(ExceptionCode.OBTAIN_KEY_FAIL);
//        }
//        return map;
//    }

    /**
     * 获取相关的密钥(测试）
     * @param
     * @return
     */
    public Map<String, String> obtainKey(String keyId, String accountId) {
        Map<String, String> map = new HashMap<>();

        String publicKey = "2204404536ab867d9a964bfcc5e6fdaa7d77e509ce5891d38b3ebbb036e5c225994597ea6d0bdff3539fd3062b3943a1c7dd75d173f35101b71298e9f7f08d51";
        String privateKey = "d6c83aee4bfbeb135a2dcef8c803b186d0678a99002b09d3c60c22aca7105005";
        String sm4Key = "0123456789abcdef0123456789abcdef";
        String sm4Iv = "0123456789abcdef0123456789abcdef";

        map.put(ChainConfig.PRIVATE_KEY, privateKey);
        map.put(ChainConfig.PUBLIC_KEY, publicKey);
        map.put(ChainConfig.SM4_KEY, sm4Key);
        map.put(ChainConfig.SM4_IV, sm4Iv);

        return map;
    }

//    static class BlockAccountKeyParam implements Callable<EiInfo> {
//        private String accountType;
//        private String accountId;
//        private String serviceId;
//
//        public BlockAccountKeyParam(String accountType, String accountId, String serviceId) {
//            this.accountType = accountType;
//            this.accountId = accountId;
//            this.serviceId = serviceId;
//        }
//
//        public static BlockAccountKeyParam build(String accountType, String accountId, String serviceId) {
//            return new BlockAccountKeyParam(accountType, accountId, serviceId);
//        }
//
//        @Override
//        public EiInfo call() {
//            EiInfo eiInfo = new EiInfo();
//            eiInfo.set(EiConstant.serviceId, serviceId);
//            eiInfo.setCell(EiConstant.queryBlock, 0, accountType, accountId);
//            EiInfo account = XServiceManager.call(eiInfo);
//            return account;
//        }
//    }

}
