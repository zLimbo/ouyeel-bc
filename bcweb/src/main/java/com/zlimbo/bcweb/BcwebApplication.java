package com.zlimbo.bcweb;

import com.alibaba.druid.pool.DruidDataSource;
import com.citahub.cita.protobuf.Blockchain;
import com.citahub.cita.protocol.CITAj;
import com.citahub.cita.protocol.core.DefaultBlockParameter;
import com.citahub.cita.protocol.core.methods.response.*;
import com.citahub.cita.protocol.http.HttpService;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import nz.net.ultraq.thymeleaf.LayoutDialect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.thymeleaf.TemplateEngine;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;

@SpringBootApplication
public class BcwebApplication {


    public static void main(String[] args) throws IOException {

        TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.addDialect(new LayoutDialect());

        SpringApplication.run(BcwebApplication.class, args);

       // CITAj service = CITAj.build(new HttpService("http://139.196.208.146:1337"));
//        CITAj service = CITAj.build(new HttpService("https://testnet.citahub.com"));
//        testCitaService(service);
        //testProtobuf();
    }

    @Autowired
    private Environment env;

    //destroy-method="close"的作用是当数据库连接不使用的时候,就把该连接重新放到数据池中,方便下次使用调用.
    @Bean(destroyMethod =  "")
    public DataSource dataSource() {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setUrl(env.getProperty("spring.datasource.url"));
        dataSource.setUsername(env.getProperty("spring.datasource.username"));//用户名
        dataSource.setPassword(env.getProperty("spring.datasource.password"));//密码
        dataSource.setDriverClassName(env.getProperty("spring.datasource.driver-class-name"));
        dataSource.setInitialSize(2);//初始化时建立物理连接的个数。初始化发生在显示调用init方法，或者第一次getConnection时
        dataSource.setMaxActive(20);//最大连接池数量
        dataSource.setMinIdle(0);//最小连接池数量
        dataSource.setMaxWait(60000);//获取连接时最大等待时间，单位毫秒。配置了maxWait之后，缺省启用公平锁，并发效率会有所下降，如果需要可以通过配置useUnfairLock属性为true使用非公平锁。
        dataSource.setValidationQuery("SELECT 1");//用来检测连接是否有效的sql，要求是一个查询语句，常用select 'x'。如果validationQuery为null，testOnBorrow、testOnReturn、testWhileIdle都不会其作用。
        dataSource.setTestOnBorrow(false);//申请连接时执行validationQuery检测连接是否有效，做了这个配置会降低性能。
        dataSource.setTestWhileIdle(true);//建议配置为true，不影响性能，并且保证安全性。申请连接的时候检测，如果空闲时间大于timeBetweenEvictionRunsMillis，执行validationQuery检测连接是否有效。
        dataSource.setPoolPreparedStatements(false);//是否缓存preparedStatement，也就是PSCache。PSCache对支持游标的数据库性能提升巨大，比如说oracle。在mysql下建议关闭。
        return dataSource;
    }


    public static void testCitaService(CITAj service) throws IOException {
        System.out.println("\n");
        NetPeerCount netPeerCount = service.netPeerCount().send();
        BigInteger peerCount = netPeerCount.getQuantity();
        System.out.print("peerCount: ");
        System.out.println(peerCount);

        String addr1 = "0x034f4fcd88b349edc5e30860b088736b82e9ccfc";
        DefaultBlockParameter defaultBlockParameter1 = DefaultBlockParameter.valueOf("latest");
        AppGetBalance getBalance = service.appGetBalance(addr1, defaultBlockParameter1).send();
        BigInteger balance = getBalance.getBalance();
        System.out.print("balance: ");
        System.out.println(balance);

        String addr2 = "0x73dd1d91001cce116cce33ca75f2af5f96898ed2e9c83d5cf0045e99e0f2e5e0";
        DefaultBlockParameter defaultBlockParameter2 = DefaultBlockParameter.valueOf("latest");
        AppGetAbi getAbi = service.appGetAbi(addr2, defaultBlockParameter2).send();
        String abi = getAbi.getAbi();
        System.out.print("abi: ");
        System.out.println(abi);

        DefaultBlockParameter defaultParam = DefaultBlockParameter.valueOf("latest");
        AppMetaData appMetaData = service.appMetaData(defaultParam).send();
        AppMetaData.AppMetaDataResult result = appMetaData.getAppMetaDataResult();
        BigInteger chainId = result.getChainId();
        String chainName = result.getChainName();
        String genesisTS = result.getGenesisTimestamp();
        System.out.print("chainId: ");
        System.out.println(chainId);
        System.out.print("chainName: ");
        System.out.println(chainName);
        System.out.print("genesisTS: ");
        System.out.println(genesisTS);

        BigInteger preBlockNumber = new BigInteger("-1");
        while (true) {
            AppBlockNumber result2 = service.appBlockNumber().send();
            BigInteger blockNumber = result2.getBlockNumber();

            if (blockNumber.compareTo(preBlockNumber) == 0) {
                continue;
            }
            preBlockNumber = blockNumber;

            System.out.print("\n\n--blockNumber: ");
            System.out.println(blockNumber);

            AppBlock appBlock = service.appGetBlockByNumber(DefaultBlockParameter.valueOf(blockNumber), true).send();
            System.out.println("----appBlock");
            System.out.print("id: ");
            System.out.println(appBlock.getId());
            System.out.print("jsonrpc: ");
            System.out.println(appBlock.getJsonrpc());
            System.out.print("rawResponse: ");
            System.out.println(appBlock.getRawResponse());

//            if (appBlock.isEmpty()) {
//                System.out.println("This no block!");
//            } else {
                AppBlock.Block block = appBlock.getBlock();
                System.out.println("--------Block");
                System.out.print("version: ");
                System.out.println(block.getVersion());
                System.out.print("hash: ");
                System.out.println(block.getHash());

                AppBlock.Header header = block.getHeader();
                System.out.println("------------header");
                System.out.print("timestamp: ");
                System.out.println(header.getTimestamp());
                System.out.print("prevHash ");
                System.out.println(header.getPrevHash());
                System.out.print("number: ");
                System.out.println(header.getNumber());
                System.out.print("stateRoot ");
                System.out.println(header.getStateRoot());
                System.out.print("transactionsRoot: ");
                System.out.println(header.getTransactionsRoot());
                System.out.print("receiptsRoot: ");
                System.out.println(header.getReceiptsRoot());
                System.out.print("quotaUsed: ");
                System.out.println(header.getQuotaUsed());
                System.out.print("proposer: ");
                System.out.println(header.getProposer());

                AppBlock.Body body = block.getBody();
                List<AppBlock.TransactionObject> transactionObjects = body.getTransactions();
                System.out.println("------------body(txs): ");
                System.out.println("This is " + transactionObjects.size() + " transactions.");

                for (AppBlock.TransactionObject transactionObject : transactionObjects) {
                    Transaction transaction = transactionObject.get();
                    System.out.print("hash: ");
                    System.out.println(transaction.getHash());
                    System.out.print("blockHash: ");
                    System.out.println(transaction.getBlockHash());
//                    System.out.print("blockNumber: ");
//                    System.out.println(transaction.getBlockNumber());
                    System.out.print("content: ");
                    System.out.println(transaction.getContent());
                    System.out.print("index: ");
                    System.out.println(transaction.getIndex());
                    System.out.print("from: ");
                    System.out.println(transaction.getFrom());
                    System.out.println();


                    // byte[] data = transaction.getContent().getBytes();
                    String content = transaction.getContent().substring(2);
                    System.out.println("content: " + content);
                    BigInteger bigInteger = new BigInteger(content, 16);
                    System.out.println("bigInteger: " + bigInteger);
                    byte[] data = bigInteger.toByteArray();
                    Blockchain.UnverifiedTransaction unverifiedTransaction = Blockchain.UnverifiedTransaction.parseFrom(data);
                    Blockchain.Transaction txContent = unverifiedTransaction.getTransaction();

                    System.out.println("----------------UnverifiedTransaction: ");
                    System.out.println("crypto: " + unverifiedTransaction.getCryptoValue());
                    System.out.println("signature: " + byteToString(unverifiedTransaction.getSignature()));
                    System.out.println("--------------------txContent: ");
                    System.out.println("to: " + txContent.getTo());
                    System.out.println("nonce: " + txContent.getNonce());
                    System.out.println("quota: " + txContent.getQuota());
                    System.out.println("valid_until_block: " + txContent.getValidUntilBlock());
                    System.out.println("data: " +  byteToString(txContent.getData()));
                    System.out.println("value: " + byteToString(txContent.getValue()));
                    System.out.println("chain_id: " + txContent.getChainId());
                    System.out.println("version: " + txContent.getVersion());
                    System.out.println("to_v1: " + byteToString(txContent.getToV1()));
                    System.out.println("chain_id_v1: " + byteToString(txContent.getChainIdV1()));
                    System.out.println();
//                }
            }
        }
    }


    public static void testProtobuf() throws InvalidProtocolBufferException {

        String content = "0x0af002122038306335313266393861343134663732383065616131333437353763356134351880ade20420662afe01608060405234801561001057600080fd5b5060df8061001f6000396000f3006080604052600436106049576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff16806360fe47b114604e5780636d4ce63c146078575b600080fd5b348015605957600080fd5b5060766004803603810190808035906020019092919050505060a0565b005b348015608357600080fd5b50608a60aa565b6040518082815260200191505060405180910390f35b8060008190555050565b600080549050905600a165627a7a723058205aed214856a5c433292a354261c9eb88eed1396c83dabbe105bde142e49838ac0029322000000000000000000000000000000000000000000000000000000000000000004002522000000000000000000000000000000000000000000000000000000000000000011241b42d8e68fb9897dc14cca3ca1ee525305c21c14beb7378d25dda0761e1c69ea56a03327702e98c4701b1345550e5f49bb7945cde6dacf4bb8c643a460350e8d001";        content = content.substring(2);
        System.out.println("content: " + content);
        BigInteger bigInteger = new BigInteger(content, 16);
        System.out.println("bigInteger: " + bigInteger);
        byte[] data = bigInteger.toByteArray();

        System.out.println("content length: " + content.length());
        System.out.println("data length: " + data.length);
        System.out.println();
        Blockchain.UnverifiedTransaction unverifiedTransaction = Blockchain.UnverifiedTransaction.parseFrom(data);
        Blockchain.Transaction txContent = unverifiedTransaction.getTransaction();

        System.out.println("----------------UnverifiedTransaction: ");
        System.out.println("crypto: " + unverifiedTransaction.getCryptoValue());
        System.out.println("signature: " + byteToString(unverifiedTransaction.getSignature()));
        System.out.println("--------------------txContent: ");
        System.out.println("to: " + txContent.getTo());
        System.out.println("nonce: " + txContent.getNonce());
        System.out.println("quota: " + txContent.getQuota());
        System.out.println("valid_until_block: " + txContent.getValidUntilBlock());
        System.out.println("data: " +  byteToString(txContent.getData()));
        System.out.println("value: " + byteToString(txContent.getValue()));
        System.out.println("chain_id: " + txContent.getChainId());
        System.out.println("version: " + txContent.getVersion());
        System.out.println("to_v1: " + byteToString(txContent.getToV1()));
        System.out.println("chain_id_v1: " + byteToString(txContent.getChainIdV1()));
        System.out.println();
    }


    public static String byteToString(ByteString byteString) {
        if (byteString.isEmpty()) {
            return "0x0";
        }
        StringBuffer stringBuffer = new StringBuffer("0x");
        for (byte b: byteString) {
            int high = (int)(b & 0xF0) >>> 4, low = (int)(b & 0x0F);
            if (high < 10) {
                stringBuffer.append((char)('0' + high));
            } else {
                stringBuffer.append((char)('a' + high - 10));
            }
            if (low < 10) {
                stringBuffer.append((char)('0' + low));
            } else {
                stringBuffer.append((char)('a' + low - 10));
            }
        }
        return stringBuffer.toString();
    }
}