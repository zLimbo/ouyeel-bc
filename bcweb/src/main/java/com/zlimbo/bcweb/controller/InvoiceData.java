package com.zlimbo.bcweb.controller;

import com.zlimbo.bcweb.domain.Invoice;
import com.zlimbo.bcweb.utils.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class InvoiceData {

    final static Logger logger = LoggerFactory.getLogger(InvoiceData.class);

    static final String HOST = "192.168.192.136";
    static final String PORT = "3306";
    static final String DATABASE = "ouyeel_cita";
    static final String USER = "root";
    static final String PASSWORD = "admin";

    static final String DB_URL =
            "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE +
                    "?useSSL=false" +
                    "&useUnicode=true" +
                    "&characterEncoding=UTF8" +
                    "&serverTimezone=GMT" +
                    "&allowPublicKeyRetrieval=true";

    static final int ITEM_SIZE = 6;


    static LinkedList<String> xAxisData = new LinkedList<>();
    static LinkedList<String> priceData = new LinkedList<>();
    static LinkedList<String> taxesData = new LinkedList<>();
    static List<Invoice> invoicesBuffer = new ArrayList<>();

    static int curPos = 0;
    static int auxPos = 0;
    static int vatNumber = 0;
    static int normalNumber = 0;
    static int professionalNumber = 0;

    static Connection conn = null;

    static {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            logger.debug("Connecting to a selected database...");
            conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
            logger.debug("Connected database successfully...");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }


    static boolean readMoreInvoice() {
        int offset = invoicesBuffer.size();
        Statement stmt = null;
        try {

            logger.debug("Creating statement...");
            stmt = conn.createStatement();

            String sql = "SELECT * FROM invoice ORDER BY timestamps LIMIT " + offset + ", " + ITEM_SIZE * 10;
            ResultSet resultSet = stmt.executeQuery(sql);

            while (resultSet.next()) {
                Invoice invoice = new Invoice(
                        resultSet.getString(CommonUtils.smallHumpToUpperUnderline("systemId")),
                        resultSet.getString(CommonUtils.smallHumpToUpperUnderline("requestSn")),
                        resultSet.getString(CommonUtils.smallHumpToUpperUnderline("invokeTime")),
                        resultSet.getString(CommonUtils.smallHumpToUpperUnderline("businessId")),
                        resultSet.getString(CommonUtils.smallHumpToUpperUnderline("callbackUrl")),
                        resultSet.getString(CommonUtils.smallHumpToUpperUnderline("keyId")),
                        resultSet.getString(CommonUtils.smallHumpToUpperUnderline("accountId")),
                        resultSet.getString(CommonUtils.smallHumpToUpperUnderline("sellerName")),
                        resultSet.getString(CommonUtils.smallHumpToUpperUnderline("sellerTaxesNo")),
                        resultSet.getString(CommonUtils.smallHumpToUpperUnderline("statementSheet")),
                        resultSet.getString(CommonUtils.smallHumpToUpperUnderline("statementWeight")),
                        resultSet.getString(CommonUtils.smallHumpToUpperUnderline("taxes")),
                        resultSet.getString(CommonUtils.smallHumpToUpperUnderline("taxesPoint")),
                        resultSet.getString(CommonUtils.smallHumpToUpperUnderline("timestamps")),
                        resultSet.getString(CommonUtils.smallHumpToUpperUnderline("consumerName")),
                        resultSet.getString(CommonUtils.smallHumpToUpperUnderline("consumerTaxesNo")),
                        resultSet.getString(CommonUtils.smallHumpToUpperUnderline("invoiceDate")),
                        resultSet.getString(CommonUtils.smallHumpToUpperUnderline("invoiceNo")),
                        resultSet.getString(CommonUtils.smallHumpToUpperUnderline("invoiceNumber")),
                        resultSet.getString(CommonUtils.smallHumpToUpperUnderline("invoiceType")),
                        resultSet.getString(CommonUtils.smallHumpToUpperUnderline("price")),
                        resultSet.getString(CommonUtils.smallHumpToUpperUnderline("pricePlusTaxes"))
                );
                invoice.setContractAddress(resultSet.getString(CommonUtils.smallHumpToUpperUnderline("contractAddress")));
                invoice.setOnChain(resultSet.getString(CommonUtils.smallHumpToUpperUnderline("onChain")));
                invoice.setBlockHeight(resultSet.getString(CommonUtils.smallHumpToUpperUnderline("blockHeight")));
                invoice.setBlockTime(resultSet.getString(CommonUtils.smallHumpToUpperUnderline("blockTime")));
                invoicesBuffer.add(invoice);
            }
            resultSet.close();
        } catch (Exception se) {
            se.printStackTrace();
        } finally {
            try {
                if (stmt != null)
                    stmt.close();
            } catch (SQLException ignored) {
            }
        }

        return invoicesBuffer.size() == offset;
    }


    static void updateGraphInfo(Invoice invoice) {
        xAxisData.add(invoice.getInvoiceDate() + "\n买:" +
                invoice.getConsumerName() + "\n卖:" +
                invoice.getSellerName());
        priceData.add(invoice.getPrice());
        taxesData.add(invoice.getTaxes());
        if (xAxisData.size() > ITEM_SIZE) {
            xAxisData.pollFirst();
            priceData.pollFirst();
            taxesData.pollFirst();
        }
        vatNumber = normalNumber = professionalNumber = 0;
        for (int i = 0; i < curPos + ITEM_SIZE && i < invoicesBuffer.size(); ++i) {
            String invoiceType = invoicesBuffer.get(i).getInvoiceType();
            if ("增值税发票".equals(invoiceType)) {
                vatNumber += 1;
            } else if ("普通发票".equals(invoiceType)) {
                normalNumber += 1;
            } else if ("专业发票".equals(invoiceType)) {
                professionalNumber += 1;
            }
        }
    }
}
