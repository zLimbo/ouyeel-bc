package com.zlimbo.bc.controller;

import com.citahub.cita.protocol.CITAj;
import com.citahub.cita.protocol.core.DefaultBlockParameter;
import com.citahub.cita.protocol.core.methods.response.AppBlock;
import com.citahub.cita.protocol.core.methods.response.AppBlockNumber;
import com.citahub.cita.protocol.core.methods.response.AppMetaData;
import com.citahub.cita.protocol.core.methods.response.NetPeerCount;
import com.citahub.cita.protocol.http.HttpService;

import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChainControl {

    private final String CITA_URL = "https://testnet.citahub.com";
    private final String CITA_URL2 = "http://139.196.208.146:1337";

    private int txAllNumber;
    CITAj service;


    public ChainControl() {
        txAllNumber = 0;
        service = CITAj.build(new HttpService(CITA_URL));
    }


    Map<String, String> getBcinfo() throws IOException {
        System.out.println("============> [getBcinfo] start");
        Map<String, String> hashMap = new HashMap<>();

        NetPeerCount netPeerCount = service.netPeerCount().send();
        BigInteger peerCount = netPeerCount.getQuantity();
        hashMap.put("peerCount", peerCount.toString());

        AppBlockNumber appBlockNumber = service.appBlockNumber().send();
        BigInteger blockNumber = appBlockNumber.getBlockNumber();
        hashMap.put("blockNumber", blockNumber.toString());


        DefaultBlockParameter defaultParam = DefaultBlockParameter.valueOf("latest");
        AppMetaData appMetaData = service.appMetaData(defaultParam).send();
        AppMetaData.AppMetaDataResult result = appMetaData.getAppMetaDataResult();
        BigInteger chainId = result.getChainId();
        String chainName = result.getChainName();
        String genesisTS = result.getGenesisTimestamp();
        hashMap.put("chainId", chainId.toString());
        hashMap.put("chainName", chainName);
        hashMap.put("genesisTS", genesisTS);


        AppBlock appBlock = service.appGetBlockByNumber(DefaultBlockParameter.valueOf(blockNumber), true).send();
        hashMap.put("blockId", String.valueOf(appBlock.getId()));
        hashMap.put("blockJsonrpc", appBlock.getJsonrpc());

        AppBlock.Block block = appBlock.getBlock();
        hashMap.put("blockVersion", block.getVersion());
        hashMap.put("blockHash", block.getHash());

        AppBlock.Header header = block.getHeader();
        hashMap.put("headerTimestamp", header.getTimestamp().toString());
        hashMap.put("headerPrevHash", header.getPrevHash());
        hashMap.put("headerNumber", header.getNumber());
        hashMap.put("headerStateRoot", header.getStateRoot());
        hashMap.put("headerTransactionsRoot", header.getTransactionsRoot());
        hashMap.put("headerReceiptsRoot", header.getReceiptsRoot());
        hashMap.put("headerProposer", header.getProposer());

        AppBlock.Body body = block.getBody();
        List<AppBlock.TransactionObject> transactionObjects = body.getTransactions();
        int blockTxNumber = transactionObjects.size();
        hashMap.put("blockTxNumber", String.valueOf(blockTxNumber));
        txAllNumber += blockTxNumber;
        hashMap.put("txAllNumber", String.valueOf(txAllNumber));

        System.out.println("============> [getBcinfo] end\n");
        return hashMap;
    }
}
