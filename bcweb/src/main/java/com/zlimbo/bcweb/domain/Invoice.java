package com.zlimbo.bcweb.domain;

import com.zlimbo.bcweb.utils.InvoiceUtil;

import java.text.SimpleDateFormat;
import java.util.*;

public class Invoice {
    

    private String systemId;
    private String requestSn;
    private String invokeTime;
    private String businessId;
    private String callbackUrl;
    private String keyId;
    private String accountId;

    final private String sm4Key = "0123456789abcdef0123456789abcdef";
    final private String sm4Iv = "0123456789abcdef0123456789abcdef";
    final private String priKey = "d6c83aee4bfbeb135a2dcef8c803b186d0678a99002b09d3c60c22aca7105005";
    final private String pubKey = "2204404536ab867d9a964bfcc5e6fdaa7d77e509ce5891d38b3ebbb036e5c225994597ea6d0bdff3539fd3062b3943a1c7dd75d173f35101b71298e9f7f08d51";

    // dataInfo
    private String sellerName;
    private String sellerTaxesNo;
    private String statementSheet;
    private String statementWeight;
    private String taxes;
    private String taxesPoint;
    private String timestamps;
    
    // pubInfo
    private String consumerName;
    private String consumerTaxesNo;
    private String invoiceDate;
    private String invoiceNo;
    private String invoiceNumber;
    private String invoiceType;
    private String price;
    private String pricePlusTaxes;

    private String contractAddress;
    private String onChain;
    private String blockHeight;
    private String blockTime;

    public Invoice() { }

    public Invoice(String systemId,
                   String requestSn,
                   String invokeTime,
                   String businessId,
                   String callbackUrl,
                   String keyId,
                   String accountId,
                   String sellerName,
                   String sellerTaxesNo,
                   String statementSheet,
                   String statementWeight,
                   String taxes,
                   String taxesPoint,
                   String timestamps,
                   String consumerName,
                   String consumerTaxesNo,
                   String invoiceDate,
                   String invoiceNo,
                   String invoiceNumber,
                   String invoiceType,
                   String price,
                   String pricePlusTaxes) {
        this.systemId = systemId;
        this.requestSn = requestSn;
        this.invokeTime = invokeTime;
        this.businessId = businessId;
        this.callbackUrl = callbackUrl;
        this.keyId = keyId;
        this.accountId = accountId;
        this.sellerName = sellerName;
        this.sellerTaxesNo = sellerTaxesNo;
        this.statementSheet = statementSheet;
        this.statementWeight = statementWeight;
        this.taxes = taxes;
        this.taxesPoint = taxesPoint;
        this.timestamps = timestamps;
        this.consumerName = consumerName;
        this.consumerTaxesNo = consumerTaxesNo;
        this.invoiceDate = invoiceDate;
        this.invoiceNo = invoiceNo;
        this.invoiceNumber = invoiceNumber;
        this.invoiceType = invoiceType;
        this.price = price;
        this.pricePlusTaxes = pricePlusTaxes;
    }

    
    public static Invoice getRandomInvoice() {

        Random random = new Random();

        String systemId = String.format("%012d", Math.abs(random.nextInt()));
        String requestSn = UUID.randomUUID().toString();
        String invokeTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis());
        String businessId = String.format("%012d", Math.abs(random.nextInt()));
        String callbackUrl = "https://127.0.0.1/callback";
        String keyId = String.format("%06d", Math.abs(random.nextInt() % (int)1e7));
        String accountId = String.format("%06d", Math.abs(random.nextInt() % (int)1e7));
        
        // dataInfo
        String[] seller = InvoiceUtil.COMPANY_TAXESNO[random.nextInt(InvoiceUtil.COMPANY_TAXESNO.length)];
        String sellerName = seller[0];
        String sellerTaxesNo = seller[1];
        String statementSheet = "" + (1 + random.nextInt(3));
        String statementWeight = (1 + random.nextInt(10)) + "kg";
        int taxesRaw = 100 + random.nextInt(1000);
        String taxes = "" + taxesRaw;
        String taxesPoint = (10 + random.nextInt(10)) + "%";
        String timestamps = "" + System.currentTimeMillis();

        // pubInfo
        String[] consumer = InvoiceUtil.COMPANY_TAXESNO[random.nextInt(InvoiceUtil.COMPANY_TAXESNO.length)];
        String consumerName = consumer[0];
        String consumerTaxesNo = consumer[1];
        String invoiceDate = new SimpleDateFormat("yyyy-MM-dd" ).format(new Date());
        String invoiceNo = InvoiceUtil.octString(10);
        String invoiceNumber = "" + (1 + random.nextInt(3));
        String invoiceType = InvoiceUtil.INVOICE_KIND[random.nextInt(InvoiceUtil.INVOICE_KIND.length)];
        int priceRaw = 10000 + random.nextInt(100000);
        String price = "" + priceRaw;
        String pricePlusTaxes = "" + (taxesRaw + priceRaw);

        return new Invoice(
                systemId,
                requestSn,
                invokeTime,
                businessId,
                callbackUrl,
                keyId,
                accountId,
                sellerName,
                sellerTaxesNo,
                statementSheet,
                statementWeight,
                taxes,
                taxesPoint,
                timestamps,
                consumerName,
                consumerTaxesNo,
                invoiceDate,
                invoiceNo,
                invoiceNumber,
                invoiceType,
                price,
                pricePlusTaxes);
    }


    public String getSystemId() {
        return systemId;
    }

    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    public String getRequestSn() {
        return requestSn;
    }

    public void setRequestSn(String requestSn) {
        this.requestSn = requestSn;
    }

    public String getInvokeTime() {
        return invokeTime;
    }

    public void setInvokeTime(String invokeTime) {
        this.invokeTime = invokeTime;
    }

    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public String getKeyId() {
        return keyId;
    }

    public void setKeyId(String keyId) {
        this.keyId = keyId;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getSm4Key() {
        return sm4Key;
    }

    public String getSm4Iv() {
        return sm4Iv;
    }

    public String getPriKey() {
        return priKey;
    }

    public String getPubKey() {
        return pubKey;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public String getSellerTaxesNo() {
        return sellerTaxesNo;
    }

    public void setSellerTaxesNo(String sellerTaxesNo) {
        this.sellerTaxesNo = sellerTaxesNo;
    }

    public String getStatementSheet() {
        return statementSheet;
    }

    public void setStatementSheet(String statementSheet) {
        this.statementSheet = statementSheet;
    }

    public String getStatementWeight() {
        return statementWeight;
    }

    public void setStatementWeight(String statementWeight) {
        this.statementWeight = statementWeight;
    }

    public String getTaxes() {
        return taxes;
    }

    public void setTaxes(String taxes) {
        this.taxes = taxes;
    }

    public String getTaxesPoint() {
        return taxesPoint;
    }

    public void setTaxesPoint(String taxesPoint) {
        this.taxesPoint = taxesPoint;
    }

    public String getTimestamps() {
        return timestamps;
    }

    public void setTimestamps(String timestamps) {
        this.timestamps = timestamps;
    }

    public String getConsumerName() {
        return consumerName;
    }

    public void setConsumerName(String consumerName) {
        this.consumerName = consumerName;
    }

    public String getConsumerTaxesNo() {
        return consumerTaxesNo;
    }

    public void setConsumerTaxesNo(String consumerTaxesNo) {
        this.consumerTaxesNo = consumerTaxesNo;
    }

    public String getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(String invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public String getInvoiceNo() {
        return invoiceNo;
    }

    public void setInvoiceNo(String invoiceNo) {
        this.invoiceNo = invoiceNo;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getInvoiceType() {
        return invoiceType;
    }

    public void setInvoiceType(String invoiceType) {
        this.invoiceType = invoiceType;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getPricePlusTaxes() {
        return pricePlusTaxes;
    }

    public void setPricePlusTaxes(String pricePlusTaxes) {
        this.pricePlusTaxes = pricePlusTaxes;
    }

    public String getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }

    public String getOnChain() {
        return onChain;
    }

    public void setOnChain(String onChain) {
        this.onChain = onChain;
    }

    public String getBlockHeight() {
        return blockHeight;
    }

    public void setBlockHeight(String blockHeight) {
        this.blockHeight = blockHeight;
    }

    public String getBlockTime() {
        return blockTime;
    }

    public void setBlockTime(String blockTime) {
        this.blockTime = blockTime;
    }
}
