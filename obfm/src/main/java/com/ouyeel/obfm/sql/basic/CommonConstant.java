package com.ouyeel.obfm.sql.basic;

//import com.baosight.iplat4j.core.ioc.spring.PlatApplicationContext;

public class CommonConstant {

    //根据账户id查询区块链账户信息
    public static final String BLOCK_CHAIN_ACCOUNT_ID = "S_BE_BS_12";

    //根据秘钥id查询密钥信息
    public static final String BLOCK_CHAIN_SECRET_KEY_ID = "S_BE_BS_13";

    //上链回调队列
//    public static final String AGENT_STORAGE_QUEUE = PlatApplicationContext.getProperty("eplat.obmp.dr.sc.agentStore.queue");

    public static final String AGENT_STORAGE_QUEUE = "TestDirectQueue";





}
