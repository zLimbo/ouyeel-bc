#_*_ coding:utf-8 _*_

import random
import time
import re
from data import *

assign_systemId = 1
assign_requestSn = 1
block_num = 24000

def getHexString(n):
        """返回指定长度的随机十六进制字符串"""
        return '0x' + ''.join([str(hex(random.randint(0, 15)))[2:] for _ in range(n)])


def getOctString(n):
        """返回指定长度的随机十进制字符串"""
        return '0x' + ''.join([str(random.randint(0, 9)) for _ in range(n)])


def randomInvoice():
        """获取一个随机的发票数据"""
        # global assign_systemId
        # global assign_requestSn
        global block_num
        # systemId = str(assign_systemId).rjust(12, '0')
        systemId = getHexString(12)
        
        

        # if assign_requestSn % 5 == 0:
        #         assign_systemId += 1
        # requestSn = str(assign_requestSn).rjust(18, '0')
        requestSn = getHexString(18)
        # assign_requestSn += 1

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
        block_num += random.randint(10, 20)
        blockNumber = str(hex(block_num))
        txHash = getHexString(64)
        timestamp = str(time.time_ns() // 10**6)
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
                #contractAddress,
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
                        value =', '.join(map("'{0}'".format, invoice))
                        sql = 'insert into invoice values('+ value + ');\n'
                        #print(sql)
                        f.write(sql)




# 获取指定数目的随机发票
num = [200000]
for n in num:
        writeIntoFile(n)



# def writeIntoFile(n):
#         """写入文件"""
#         with open("data_" + str(n) + ".txt", "w") as f:
#                 for _ in range(n):
#                         invoice = randomInvoice()
#                         result=',\n\t'.join(map("'{0}'".format, invoice))
#                         result = 'insert into invoice values(\n\t'+result
#                         result = result+')'
#                         #print(result)
#                         f.write(result + ";\n")


# def writeIntoFile(n):
#         """写入文件"""
#         with open("data_" + str(n) + ".txt", "w") as f:
#                 sql = 'insert into invoice values\n'
#                 for _ in range(n):
#                         invoice = randomInvoice()
#                         value = ', '.join(map("'{0}'".format, invoice))
#                         sql += '(' + value + ')'
#                         if _ != n - 1:
#                                 sql += ',\n'
#                 #print(sql)
#                 f.write(sql + ";\n")