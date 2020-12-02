package com.zlimbo.zcat.controller;

import com.citahub.cita.protocol.CITAj;
import com.citahub.cita.protocol.core.DefaultBlockParameter;
import com.citahub.cita.protocol.core.methods.response.AppBlock;
import com.citahub.cita.protocol.core.methods.response.AppBlockNumber;
import com.citahub.cita.protocol.core.methods.response.AppMetaData;
import com.citahub.cita.protocol.core.methods.response.NetPeerCount;
import com.citahub.cita.protocol.http.HttpService;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

public class ChainControl {

    final Logger logger = LoggerFactory.getLogger(getClass());

    private final String CITA_URL = "https://testnet.citahub.com";
    private final String CITA_URL2 = "http://139.196.208.146:1337";

    private int txAllNumber = 0;
    CITAj service;
    Timer timer;
    private boolean connectSuccess;

    private StringProperty peerCount = new SimpleStringProperty();
    private StringProperty blockNumber = new SimpleStringProperty();
    private StringProperty chainId = new SimpleStringProperty();
    private StringProperty chainName = new SimpleStringProperty();
    private StringProperty genesisTS = new SimpleStringProperty();
    private StringProperty blockId = new SimpleStringProperty();
    private StringProperty blockJsonrpc = new SimpleStringProperty();
    private StringProperty blockVersion = new SimpleStringProperty();
    private StringProperty blockHash = new SimpleStringProperty();
    private StringProperty headerTimestamp = new SimpleStringProperty();
    private StringProperty headerPrevHash = new SimpleStringProperty();
    private StringProperty headerNumber = new SimpleStringProperty();
    private StringProperty headerStateRoot = new SimpleStringProperty();
    private StringProperty headerTransactionsRoot = new SimpleStringProperty();
    private StringProperty headerReceiptsRoot = new SimpleStringProperty();
    private StringProperty headerProposer = new SimpleStringProperty();
    private StringProperty blockTxNumber = new SimpleStringProperty();


    public ChainControl(String citaUrl) {
        logger.debug("[ChainControl] start");
        logger.debug("cita url: " + citaUrl);
        service = CITAj.build(new HttpService(citaUrl));
        try {
            service.netPeerCount().send();
            connectSuccess = true;
        } catch (Exception e) {
            e.printStackTrace();
            connectSuccess = false;
        }
        logger.debug("[ChainControl] end");
    }


    public void updateStart() {
        logger.debug("[updateStart] start");
        updateBcinfo();
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                updateBcinfo();
            }
        }, 3000, 3000);
        logger.debug("[updateStart] end");
    }


    public void updateStop() {
        logger.debug("[updateStop] start");
        timer.cancel();
        logger.debug("[updateStop] end");
    }


    public void updateBcinfo() {
        logger.debug("[updateBcinfo] start");

        try {
            NetPeerCount netPeerCount = service.netPeerCount().send();
            BigInteger peerCount = netPeerCount.getQuantity();
            this.setPeerCount(peerCount.toString());

            AppBlockNumber appBlockNumber = service.appBlockNumber().send();
            BigInteger blockNumber = appBlockNumber.getBlockNumber();
            this.setBlockNumber(blockNumber.toString());

            DefaultBlockParameter defaultParam = DefaultBlockParameter.valueOf("latest");
            AppMetaData appMetaData = service.appMetaData(defaultParam).send();
            AppMetaData.AppMetaDataResult result = appMetaData.getAppMetaDataResult();
            BigInteger chainId = result.getChainId();
            String chainName = result.getChainName();
            String genesisTS = result.getGenesisTimestamp();
            this.setChainId(chainId.toString());
            this.setChainName(chainName);
            this.setGenesisTS(genesisTS);

            AppBlock appBlock = service.appGetBlockByNumber(DefaultBlockParameter.valueOf(blockNumber), true).send();
            this.setBlockId( String.valueOf(appBlock.getId()));
            this.setBlockJsonrpc(appBlock.getJsonrpc());

            AppBlock.Block block = appBlock.getBlock();
            this.setBlockVersion(block.getVersion());
            this.setBlockHash(block.getHash());

            AppBlock.Header header = block.getHeader();
            this.setHeaderTimestamp(header.getTimestamp().toString());
            this.setHeaderPrevHash(header.getPrevHash());
            this.setHeaderNumber(header.getNumber());
            this.setHeaderStateRoot(header.getStateRoot());
            this.setHeaderTransactionsRoot(header.getTransactionsRoot());
            this.setHeaderReceiptsRoot(header.getReceiptsRoot());
            this.setHeaderProposer(header.getProposer());

            AppBlock.Body body = block.getBody();
            List<AppBlock.TransactionObject> transactionObjects = body.getTransactions();
            int blockTxNumber = transactionObjects.size();
            this.setBlockTxNumber(String.valueOf(blockTxNumber));
            //this.settxAllNumber( String.valueOf(txAllNumber));
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        logger.debug("[updateBcinfo] end");
    }


    List<List<StringProperty>> getBcInfo() {
        logger.debug("[getBcInfo] start");
        List<List<StringProperty>> bcInfo = new ArrayList<>();
        bcInfo.add(Arrays.asList(new SimpleStringProperty("peer count"), peerCount));
        bcInfo.add(Arrays.asList(new SimpleStringProperty("block number"), blockNumber));
        bcInfo.add(Arrays.asList(new SimpleStringProperty("chain id"), chainId));
        bcInfo.add(Arrays.asList(new SimpleStringProperty("chain name"), chainName));
        bcInfo.add(Arrays.asList(new SimpleStringProperty("genesis timestamp"), genesisTS));
        bcInfo.add(Arrays.asList(new SimpleStringProperty("block id"), blockId));
        bcInfo.add(Arrays.asList(new SimpleStringProperty("block jsonrpc"), blockJsonrpc));
        bcInfo.add(Arrays.asList(new SimpleStringProperty("block version"), blockVersion));
        bcInfo.add(Arrays.asList(new SimpleStringProperty("block hash"), blockHash));
        bcInfo.add(Arrays.asList(new SimpleStringProperty("header timestamp"), headerTimestamp));
        bcInfo.add(Arrays.asList(new SimpleStringProperty("header prev hash"), headerPrevHash));
        bcInfo.add(Arrays.asList(new SimpleStringProperty("header number"), headerNumber));
        bcInfo.add(Arrays.asList(new SimpleStringProperty("header state root"), headerStateRoot));
        bcInfo.add(Arrays.asList(new SimpleStringProperty("header transactions root"), headerTransactionsRoot));
        bcInfo.add(Arrays.asList(new SimpleStringProperty("header receipts root"), headerReceiptsRoot));
        bcInfo.add(Arrays.asList(new SimpleStringProperty("header proposer"), headerProposer));
        bcInfo.add(Arrays.asList(new SimpleStringProperty("block transaction number"), blockTxNumber));
        logger.debug("[getBcInfo] end");
        return bcInfo;
    }

    public boolean isConnectSuccess() {
        return connectSuccess;
    }

    public String getPeerCount() {
        return peerCount.get();
    }

    public StringProperty peerCountProperty() {
        return peerCount;
    }

    public void setPeerCount(String peerCount) {
        this.peerCount.set(peerCount);
    }

    public String getBlockNumber() {
        return blockNumber.get();
    }

    public StringProperty blockNumberProperty() {
        return blockNumber;
    }

    public void setBlockNumber(String blockNumber) {
        this.blockNumber.set(blockNumber);
    }

    public String getChainId() {
        return chainId.get();
    }

    public StringProperty chainIdProperty() {
        return chainId;
    }

    public void setChainId(String chainId) {
        this.chainId.set(chainId);
    }

    public String getChainName() {
        return chainName.get();
    }

    public StringProperty chainNameProperty() {
        return chainName;
    }

    public void setChainName(String chainName) {
        this.chainName.set(chainName);
    }

    public String getGenesisTS() {
        return genesisTS.get();
    }

    public StringProperty genesisTSProperty() {
        return genesisTS;
    }

    public void setGenesisTS(String genesisTS) {
        this.genesisTS.set(genesisTS);
    }

    public String getBlockId() {
        return blockId.get();
    }

    public StringProperty blockIdProperty() {
        return blockId;
    }

    public void setBlockId(String blockId) {
        this.blockId.set(blockId);
    }

    public String getBlockJsonrpc() {
        return blockJsonrpc.get();
    }

    public StringProperty blockJsonrpcProperty() {
        return blockJsonrpc;
    }

    public void setBlockJsonrpc(String blockJsonrpc) {
        this.blockJsonrpc.set(blockJsonrpc);
    }

    public String getBlockVersion() {
        return blockVersion.get();
    }

    public StringProperty blockVersionProperty() {
        return blockVersion;
    }

    public void setBlockVersion(String blockVersion) {
        this.blockVersion.set(blockVersion);
    }

    public String getBlockHash() {
        return blockHash.get();
    }

    public StringProperty blockHashProperty() {
        return blockHash;
    }

    public void setBlockHash(String blockHash) {
        this.blockHash.set(blockHash);
    }

    public String getHeaderTimestamp() {
        return headerTimestamp.get();
    }

    public StringProperty headerTimestampProperty() {
        return headerTimestamp;
    }

    public void setHeaderTimestamp(String headerTimestamp) {
        this.headerTimestamp.set(headerTimestamp);
    }

    public String getHeaderPrevHash() {
        return headerPrevHash.get();
    }

    public StringProperty headerPrevHashProperty() {
        return headerPrevHash;
    }

    public void setHeaderPrevHash(String headerPrevHash) {
        this.headerPrevHash.set(headerPrevHash);
    }

    public String getHeaderNumber() {
        return headerNumber.get();
    }

    public StringProperty headerNumberProperty() {
        return headerNumber;
    }

    public void setHeaderNumber(String headerNumber) {
        this.headerNumber.set(headerNumber);
    }

    public String getHeaderStateRoot() {
        return headerStateRoot.get();
    }

    public StringProperty headerStateRootProperty() {
        return headerStateRoot;
    }

    public void setHeaderStateRoot(String headerStateRoot) {
        this.headerStateRoot.set(headerStateRoot);
    }

    public String getHeaderTransactionsRoot() {
        return headerTransactionsRoot.get();
    }

    public StringProperty headerTransactionsRootProperty() {
        return headerTransactionsRoot;
    }

    public void setHeaderTransactionsRoot(String headerTransactionsRoot) {
        this.headerTransactionsRoot.set(headerTransactionsRoot);
    }

    public String getHeaderReceiptsRoot() {
        return headerReceiptsRoot.get();
    }

    public StringProperty headerReceiptsRootProperty() {
        return headerReceiptsRoot;
    }

    public void setHeaderReceiptsRoot(String headerReceiptsRoot) {
        this.headerReceiptsRoot.set(headerReceiptsRoot);
    }

    public String getHeaderProposer() {
        return headerProposer.get();
    }

    public StringProperty headerProposerProperty() {
        return headerProposer;
    }

    public void setHeaderProposer(String headerProposer) {
        this.headerProposer.set(headerProposer);
    }

    public String getBlockTxNumber() {
        return blockTxNumber.get();
    }

    public StringProperty blockTxNumberProperty() {
        return blockTxNumber;
    }

    public void setBlockTxNumber(String blockTxNumber) {
        this.blockTxNumber.set(blockTxNumber);
    }
}
