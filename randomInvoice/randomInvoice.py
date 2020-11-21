#_*_ coding:utf-8 _*_

import random
import time
import re
from data2 import *

assign_systemId = 1
assign_requestSn = 1

def getHexString(n):
        """返回指定长度的随机十六进制字符串"""
        return '0x' + ''.join([str(hex(random.randint(0, 15)))[2:] for _ in range(n)])


def getOctString(n):
        """返回指定长度的随机十进制字符串"""
        return '0x' + ''.join([str(random.randint(0, 9)) for _ in range(n)])


def randomInvoice():
        """获取一个随机的发票数据"""
        global assign_systemId
        global assign_requestSn
        systemId = str(assign_systemId).rjust(12, '0')
        if assign_requestSn % 5 == 0:
                assign_systemId += 1
        requestSn = str(assign_requestSn).rjust(18, '0')
        assign_requestSn += 1

        invoiceNo = getOctString(10)
        (consumerName, consumerTaxesNo), (sellerName, sellerTaxesNo) = random.sample(COMPANY_TAXESNO, 2) # 随机采样
        invoiceDate = time.strftime("%Y-%m-%d %H:%M:%S")
        invoiceType = random.choice(INVOICE_KIND)
        taxes_n = 100 + random.randint(0, 1000)
        price_n = 10000 + random.randint(0, 100000)
        taxesPoint = str(round(10 + random.random() * 10, 2)) + "%"
        taxes = str(taxes_n)
        price = str(price_n)
        pricePlusTaxes = str(taxes_n + price_n)
        invoiceNumber = str(1 + random.randint(0, 3))
        statementSheet = str(1 + random.randint(0, 3))
        statementWeight = str(1 + random.randint(0, 10)) + "kg"
        contractAddress = getHexString(16)
        secretKey = getHexString(16)
        privateKey = getHexString(16)
        publicKey = getHexString(32)
        blockNumber = getHexString(16)
        txHash = getHexString(32)
        timestamp = str(int(time.time() * 10**9))
        invoice = [ 
                systemId,
                requestSn,
                invoiceNo,
                consumerName, 
                consumerTaxesNo, 
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
                contractAddress,
                blockNumber,
                txHash,
                timestamp,
        ]
        return invoice


def writeIntoFile(n):
        """写入文件"""
        with open("data_" + str(n) + ".txt", "w") as f:
                for _ in range(n):
                        invoice = randomInvoice()
                        result=',\n\t'.join(map("'{0}'".format, invoice))
                        result = 'insert into invoice values(\n\t'+result
                        result = result+')'
                        print(result)
                        f.write(result + ";\n")


# 获取指定数目的随机发票
num = [20]
for n in num:
        writeIntoFile(n)
