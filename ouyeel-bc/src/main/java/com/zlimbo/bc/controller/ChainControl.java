package com.zlimbo.bc.controller;

import com.citahub.cita.protocol.CITAj;
import com.citahub.cita.protocol.core.DefaultBlockParameter;
import com.citahub.cita.protocol.core.methods.response.AppBlock;
import com.citahub.cita.protocol.core.methods.response.AppBlockNumber;
import com.citahub.cita.protocol.core.methods.response.AppMetaData;
import com.citahub.cita.protocol.core.methods.response.NetPeerCount;
import com.citahub.cita.protocol.http.HttpService;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

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

    private StringProperty peerCount;
    private StringProperty blockNumber;
    private StringProperty chainId;
    private StringProperty chainName;
    private StringProperty genesisTS;
    private StringProperty blockId;
    private StringProperty blockJsonrpc;
    private StringProperty blockVersion;
    private StringProperty blockHash;
    private StringProperty headerTimestamp;
    private StringProperty headerPrevHash;
    private StringProperty headerNumber;
    private StringProperty headerStateRoot;
    private StringProperty headerTransactionsRoot;
    private StringProperty headerReceiptsRoot;
    private StringProperty headerProposer;
    private StringProperty blockTxNumber;

    public String getPeerCount() {
        return peerCount.get();
    }

    public StringProperty peerCountProperty() {
        return peerCount;
    }

    public String getBlockNumber() {
        return blockNumber.get();
    }

    public StringProperty blockNumberProperty() {
        return blockNumber;
    }

    public String getChainId() {
        return chainId.get();
    }

    public StringProperty chainIdProperty() {
        return chainId;
    }

    public String getChainName() {
        return chainName.get();
    }

    public StringProperty chainNameProperty() {
        return chainName;
    }

    public String getGenesisTS() {
        return genesisTS.get();
    }

    public StringProperty genesisTSProperty() {
        return genesisTS;
    }

    public String getBlockId() {
        return blockId.get();
    }

    public StringProperty blockIdProperty() {
        return blockId;
    }

    public String getBlockJsonrpc() {
        return blockJsonrpc.get();
    }

    public StringProperty blockJsonrpcProperty() {
        return blockJsonrpc;
    }

    public String getBlockVersion() {
        return blockVersion.get();
    }

    public StringProperty blockVersionProperty() {
        return blockVersion;
    }

    public String getBlockHash() {
        return blockHash.get();
    }

    public StringProperty blockHashProperty() {
        return blockHash;
    }

    public String getHeaderTimestamp() {
        return headerTimestamp.get();
    }

    public StringProperty headerTimestampProperty() {
        return headerTimestamp;
    }

    public String getHeaderPrevHash() {
        return headerPrevHash.get();
    }

    public StringProperty headerPrevHashProperty() {
        return headerPrevHash;
    }

    public String getHeaderNumber() {
        return headerNumber.get();
    }

    public StringProperty headerNumberProperty() {
        return headerNumber;
    }

    public String getHeaderStateRoot() {
        return headerStateRoot.get();
    }

    public StringProperty headerStateRootProperty() {
        return headerStateRoot;
    }

    public String getHeaderTransactionsRoot() {
        return headerTransactionsRoot.get();
    }

    public StringProperty headerTransactionsRootProperty() {
        return headerTransactionsRoot;
    }

    public String getHeaderProposer() {
        return headerProposer.get();
    }

    public StringProperty headerProposerProperty() {
        return headerProposer;
    }

    public ChainControl() {
        txAllNumber = 0;
        service = CITAj.build(new HttpService(CITA_URL));
    }


    Map<String, String> getBcinfo() {
        System.out.println("============> [getBcinfo] start");
        Map<String, String> hashMap = new HashMap<>();

        try {
            NetPeerCount netPeerCount = service.netPeerCount().send();
            BigInteger peerCount = netPeerCount.getQuantity();
            hashMap.put("peerCount", peerCount.toString());
            this.peerCount = new SimpleStringProperty(peerCount.toString());

            AppBlockNumber appBlockNumber = service.appBlockNumber().send();
            BigInteger blockNumber = appBlockNumber.getBlockNumber();
            hashMap.put("blockNumber", blockNumber.toString());
            this.blockNumber = new SimpleStringProperty(blockNumber.toString());


            DefaultBlockParameter defaultParam = DefaultBlockParameter.valueOf("latest");
            AppMetaData appMetaData = service.appMetaData(defaultParam).send();
            AppMetaData.AppMetaDataResult result = appMetaData.getAppMetaDataResult();
            BigInteger chainId = result.getChainId();
            String chainName = result.getChainName();
            String genesisTS = result.getGenesisTimestamp();
            hashMap.put("chainId", chainId.toString());
            this.chainId = new SimpleStringProperty(chainId.toString());
            hashMap.put("chainName", chainName);
            this.chainName = new SimpleStringProperty(chainName);
            hashMap.put("genesisTS", genesisTS);
            this.genesisTS = new SimpleStringProperty(genesisTS);


            AppBlock appBlock = service.appGetBlockByNumber(DefaultBlockParameter.valueOf(blockNumber), true).send();
            hashMap.put("blockId", String.valueOf(appBlock.getId()));
            this.blockId = new SimpleStringProperty(String.valueOf(appBlock.getId()));
            hashMap.put("blockJsonrpc", appBlock.getJsonrpc());
            this.blockJsonrpc = new SimpleStringProperty(appBlock.getJsonrpc());

            AppBlock.Block block = appBlock.getBlock();
            hashMap.put("blockVersion", block.getVersion());
            this.blockVersion = new SimpleStringProperty(block.getVersion());
            hashMap.put("blockHash", block.getHash());
            this.blockHash = new SimpleStringProperty(block.getHash());

            AppBlock.Header header = block.getHeader();
            hashMap.put("headerTimestamp", header.getTimestamp().toString());
            this.headerTimestamp = new SimpleStringProperty(header.getTimestamp().toString());
            hashMap.put("headerPrevHash", header.getPrevHash());
            this.headerPrevHash = new SimpleStringProperty(header.getPrevHash());
            hashMap.put("headerNumber", header.getNumber());
            this.headerNumber = new SimpleStringProperty(header.getNumber());
            hashMap.put("headerStateRoot", header.getStateRoot());
            this.headerStateRoot = new SimpleStringProperty(header.getStateRoot());
            hashMap.put("headerTransactionsRoot", header.getTransactionsRoot());
            this.headerTransactionsRoot = new SimpleStringProperty(header.getTransactionsRoot());
            hashMap.put("headerReceiptsRoot", header.getReceiptsRoot());
            this.headerReceiptsRoot = new SimpleStringProperty(header.getReceiptsRoot());
            hashMap.put("headerProposer", header.getProposer());
            this.headerProposer = new SimpleStringProperty(header.getProposer());

            AppBlock.Body body = block.getBody();
            List<AppBlock.TransactionObject> transactionObjects = body.getTransactions();
            int blockTxNumber = transactionObjects.size();
            hashMap.put("blockTxNumber", String.valueOf(blockTxNumber));
            this.blockTxNumber = new SimpleStringProperty(String.valueOf(blockTxNumber));
            hashMap.put("txAllNumber", String.valueOf(txAllNumber));
//            this.txAllNumber = new SimpleStringProperty(String.valueOf(txAllNumber));
        } catch (IOException ioe) {
            ioe.printStackTrace();
            hashMap = null;
        }

        System.out.println("============> [getBcinfo] end\n");
        return hashMap;
    }
}
