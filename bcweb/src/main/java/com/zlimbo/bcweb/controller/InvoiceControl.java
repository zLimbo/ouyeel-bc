package com.zlimbo.bcweb.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zlimbo.bcweb.domain.Invoice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.*;

@Controller
@RequestMapping("")
public class InvoiceControl {

    final static Logger logger = LoggerFactory.getLogger(InvoiceControl.class);


    @RequestMapping(value = {"/"})
    public ModelAndView invoiceShow() {

        if (InvoiceData.curPos + InvoiceData.ITEM_SIZE >= InvoiceData.invoicesBuffer.size()) {
            if (!InvoiceData.readMoreInvoice()) {

            }
        }
        List<Invoice> invoices = new ArrayList<Invoice>();

        for (int i = 0; i < InvoiceData.ITEM_SIZE && InvoiceData.curPos + i < InvoiceData.invoicesBuffer.size(); ++i) {
            Invoice invoice = InvoiceData.invoicesBuffer.get(InvoiceData.curPos + i);
            invoices.add(invoice);
            InvoiceData.updateGraphInfo(invoice);
        }

        ModelAndView modelAndView = new ModelAndView("index");
        modelAndView.addObject("invoices", invoices);

        return modelAndView;
    }


    @RequestMapping(value = "/invoiceUpdate", method = RequestMethod.GET)
    @ResponseBody
    public synchronized String invoiceUpdate() {
        logger.debug("curPos: {}", InvoiceData.curPos);
        int pos = InvoiceData.curPos + InvoiceData.ITEM_SIZE;
        if (pos >= InvoiceData.invoicesBuffer.size()) {
            InvoiceData.readMoreInvoice();
        }
        if (pos >= InvoiceData.invoicesBuffer.size()) {
            pos = InvoiceData.curPos + InvoiceData.auxPos;
            ++InvoiceData.auxPos;
        } else {
            InvoiceData.auxPos = 0;
            ++InvoiceData.curPos;
        }
        Invoice invoice = InvoiceData.invoicesBuffer.get(pos);
        InvoiceData.updateGraphInfo(invoice);
        return getInvoiceItem(invoice);
    }


    @RequestMapping(value = "/graphUpdate", method = RequestMethod.GET)
    @ResponseBody
    public String graphUpdate() throws IOException {
        logger.debug("graphUpdate ok");

        JSONObject jsonObject = new JSONObject();
        JSONArray pieData = new JSONArray();

        JSONObject vatData = new JSONObject();
        JSONObject normalData = new JSONObject();
        JSONObject professionalData = new JSONObject();
        vatData.put("value", InvoiceData.vatNumber);
        vatData.put("name", "增值税发票");
        normalData.put("value", InvoiceData.normalNumber);
        normalData.put("name", "普通发票");
        professionalData.put("value", InvoiceData.professionalNumber);
        professionalData.put("name", "专业发票");
        pieData.add(vatData);
        pieData.add(normalData);
        pieData.add(professionalData);

        jsonObject.put("pieData", pieData);

        JSONArray xAxisJson = new JSONArray();
        JSONArray priceJson = new JSONArray();
        JSONArray taxesJson = new JSONArray();

        for (int i = 0; i < xAxisJson.size(); ++i) {
            xAxisJson.add(InvoiceData.xAxisData.get(i));
            priceJson.add(InvoiceData.priceData.get(i));
            taxesJson.add(InvoiceData.taxesData.get(i));
        }
        jsonObject.put("xAxisData", InvoiceData.xAxisData);
        jsonObject.put("priceData", InvoiceData.priceData);
        jsonObject.put("taxesData", InvoiceData.taxesData);

        String jsonObj = jsonObject.toJSONString();
        logger.debug(jsonObj);
        return jsonObj;
    }


    @GetMapping("/invoiceInsert")
    public String invoiceForm(Model model) {
        logger.debug("----------------------------------invoiceForm ok");
        model.addAttribute("invoice", new Invoice());
        return "invoiceInsert";
    }


    @RequestMapping(value = "/randomInvoice", method = RequestMethod.GET)
    @ResponseBody
    public String randomInvoice() {
        logger.debug("randomInvoice ok");
        return JSON.toJSONString(Invoice.getRandomInvoice());
    }


    String getInvoiceItem(Invoice invoice) {
        return "<li>\n<div>" +
                invoice.getInvoiceDate() + "</div>\n<div>" +
                invoice.getConsumerName() + "</div>\n<div>" +
                invoice.getSellerName() + "</div>\n<div>" +
                invoice.getInvoiceType() + "</div>\n<div>" +
                invoice.getTaxesPoint() + "</div>\n<div>" +
                invoice.getPrice() + "</div>\n<div>" +
                invoice.getTaxes() + "</div>\n<div>" +
                invoice.getPricePlusTaxes() + "</div></li>";
    }



}


