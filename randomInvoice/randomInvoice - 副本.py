#_*_ coding:utf-8 _*_

import random
import time
import re
from data import *

def getHexString(n):
        """返回指定长度的随机十六进制字符串"""
        return '0x' + ''.join([str(hex(random.randint(0, 15)))[2:] for _ in range(n)])


def getOctString(n):
        """返回指定长度的随机十进制字符串"""
        return '0x' + ''.join([str(random.randint(0, 9)) for _ in range(n)])


def randomInvoice():
        """获取一个随机的发票数据"""
        hashValue = getHexString(32);
        invoiceNo = getOctString(10);
        (buyerName, buyerTaxesNo), (sellerName, sellerTaxesNo) = random.sample(COMPANY_TAXESNO, 2) # 随机采样
        invoiceDate = time.strftime("%Y-%m-%d %H:%M:%S")
        invoiceType = random.choice(INVOICE_KIND);
        taxesPoint = str(round(10 + random.random() * 10, 2)) + "%";
        taxes = str(100 + random.randint(0, 1000));
        price = str(10000 + random.randint(0, 100000));
        pricePlusTaxes = str(taxes + price);
        invoiceNumber = str(1 + random.randint(0, 3));
        statementSheet = str(1 + random.randint(0, 3));
        statementWeight = str(1 + random.randint(0, 10)) + "kg";
        timestamp = str(int(time.time() * 10**9))
        contractAddress = getHexString(16);
        invoice = [
                hashValue, 
                invoiceNo,
                buyerName, 
                buyerTaxesNo, 
                sellerName, 
                sellerTaxesNo, 
                invoiceDate,
                invoiceType,
                taxesPoint,
                taxes,
                price,
                pricePlusTaxes,
                invoiceNumber,
                statementSheet,
                statementWeight,
                timestamp
                #contractAddress
        ]
        return invoice


def writeIntoFile(n):
        """写入文件"""
        with open("data_" + str(n) + ".txt", "w") as f:
                for _ in range(n):
                        invoice = randomInvoice()
                        result=','.join(map("'{0}'".format, invoice))
                        result = 'insert into invoice values('+result
                        result = result+')'
                        print(result)
                        f.write(str(result) + "\n")


# 获取指定数目的随机发票
num = [5]
for n in num:
        writeIntoFile(n)
