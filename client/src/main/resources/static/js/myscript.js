//获取当前时间
function getNowFormatDate() {
    var date = new Date();
    var year = date.getFullYear();
    var month = date.getMonth() + 1;
    var strDate = date.getDate();
    var Hour = date.getHours();       // 获取当前小时数(0-23)
    var Minute = date.getMinutes();     // 获取当前分钟数(0-59)
    var Second = date.getSeconds();     // 获取当前秒数(0-59)
    var show_day = new Array('星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六');
    var day = date.getDay();
    if (Hour < 10) {
        Hour = "0" + Hour;
    }
    if (Minute < 10) {
        Minute = "0" + Minute;
    }
    if (Second < 10) {
        Second = "0" + Second;
    }
    if (month >= 1 && month <= 9) {
        month = "0" + month;
    }
    if (strDate >= 0 && strDate <= 9) {
        strDate = "0" + strDate;
    }
    var currentdate = '<div><p>' + year + '年' + month + '月' + strDate + '号</p><p>' + show_day[day] + '</p></div>';
    var HMS = Hour + ':' + Minute + ':' + Second;
    var temp_time = year + '-' + month + '-' + strDate + ' ' + HMS;
    $('.nowTime li:nth-child(1)').html(HMS);
    $('.nowTime li:nth-child(2)').html(currentdate);
    //$('.topRec_List li div:nth-child(3)').html(temp_time);
    setTimeout(getNowFormatDate, 1000);//每隔1秒重新调用一次该函数
}



// 实施更新链信息
setInterval(function(){$.ajax(bcinfoUpdate)},3000);
var bcinfoUpdate = {
    type: "get",
    url: "/bcinfoUpdate",
    dataType: "json",
    success: function (jsonObj) {
        $("#chainId").html(jsonObj.chainId);
        $("#chainName").html(jsonObj.chainName);
        $("#genesisTS").html(jsonObj.genesisTS);
        $("#peerCount").html(jsonObj.peerCount);
        $("#blockNumber").html(jsonObj.blockNumber);
        $("#txAllNumber").html(jsonObj.txAllNumber);

        $("#blockId").html(jsonObj.blockId);
        $("#blockJsonrpc").html(jsonObj.blockJsonrpc);
        $("#blockVersion").html(jsonObj.blockVersion);
        $("#headerTimestamp").html(jsonObj.headerTimestamp);
        $("#blockTxNumber").html(jsonObj.blockTxNumber);
        //alert(data["peerCount"]);
        //var jsonObj = eval("(" + data + ")");
    }
}


// 时间
getNowFormatDate();



// 随机生成发票数据
$("#randomInvoice").click(function (){
    $.ajax({
        type: "GET",
        url: "/randomInvoice",
        dataType: "json",
        success: function (jsonObj) {
            //var jsonObj = eval("(" + data + ")");
            $("#hashValue").val(jsonObj.hashValue);
            $("#invoiceNo").val(jsonObj.invoiceNo);
            $("#buyerName").val(jsonObj.buyerName);
            $("#buyerTaxesNo").val(jsonObj.buyerTaxesNo);
            $("#sellerName").val(jsonObj.sellerName);
            $("#sellerTaxesNo").val(jsonObj.sellerTaxesNo);
            $("#invoiceDate").val(jsonObj.invoiceDate);
            $("#invoiceType").val(jsonObj.invoiceType);
            $("#taxesPoint").val(jsonObj.taxesPoint);
            $("#taxes").val(jsonObj.taxes);
            $("#price").val(jsonObj.price);
            $("#pricePlusTaxes").val(jsonObj.pricePlusTaxes);
            $("#invoiceNumber").val(jsonObj.invoiceNumber);
            $("#statementSheet").val(jsonObj.statementSheet);
            $("#statementWeight").val(jsonObj.statementWeight);
            $("#timestamp").val(jsonObj.timestamp);
            $("#contractAddress").val(jsonObj.contractAddress);
        }
    })
})


$("#btn-invoiceQueryStart").click(function (){
    $("#hashValueForQuery").val("");
})

// 查询数据
$("#btn-invoiceQuery").click(function (){
    $("#query-hashValue").val("");
    $("#query-invoiceNo").val("");
    $("#query-buyerName").val("");
    $("#query-buyerTaxesNo").val("");
    $("#query-sellerName").val("");
    $("#query-sellerTaxesNo").val("");
    $("#query-invoiceDate").val("");
    $("#query-invoiceType").val("");
    $("#query-taxesPoint").val("");
    $("#query-taxes").val("");
    $("#query-price").val("");
    $("#query-pricePlusTaxes").val("");
    $("#query-invoiceNumber").val("");
    $("#query-statementSheet").val("");
    $("#query-statementWeight").val("");
    $("#query-timestamp").val("");
    $("#query-contractAddress").val("");
// $(document).on("click", "#btn-invoiceQuery", function () {
    //var jsonString = JSON.stringify(json);
    //var data = $('#invoiceForm').serialize()
    // alert(jsonString)
    var data = {"hashValue": $("#hashValueForQuery").val()}
    //alert(JSON.stringify(data));
    $.ajax({
        type: 'GET',
        url: '/invoiceQuery',
        dataType: 'json',
        data: data,
        error: function (request) {
            //alert("提交错误");
            //$("#queryShowModel").modal('hide');
            $("#myModalLabel-querqyShow").html("查询结果为空，不存在该交易！")
        },
        success: function (jsonObj) {
            //var jsonObj = eval("(" + data + ")");
            $("#query-hashValue").val(jsonObj.hashValue);
            $("#query-invoiceNo").val(jsonObj.invoiceNo);
            $("#query-buyerName").val(jsonObj.buyerName);
            $("#query-buyerTaxesNo").val(jsonObj.buyerTaxesNo);
            $("#query-sellerName").val(jsonObj.sellerName);
            $("#query-sellerTaxesNo").val(jsonObj.sellerTaxesNo);
            $("#query-invoiceDate").val(jsonObj.invoiceDate);
            $("#query-invoiceType").val(jsonObj.invoiceType);
            $("#query-taxesPoint").val(jsonObj.taxesPoint);
            $("#query-taxes").val(jsonObj.taxes);
            $("#query-price").val(jsonObj.price);
            $("#query-pricePlusTaxes").val(jsonObj.pricePlusTaxes);
            $("#query-invoiceNumber").val(jsonObj.invoiceNumber);
            $("#query-statementSheet").val(jsonObj.statementSheet);
            $("#query-statementWeight").val(jsonObj.statementWeight);
            $("#query-timestamp").val(jsonObj.timestamp);
            $("#query-contractAddress").val(jsonObj.contractAddress);
        }
    })
})


$("#btn-insertModal").click(function () {
    $("#hashValue").val("");
    $("#invoiceNo").val("");
    $("#buyerName").val("");
    $("#buyerTaxesNo").val("");
    $("#sellerName").val("");
    $("#sellerTaxesNo").val("");
    $("#invoiceDate").val("");
    $("#invoiceType").val("");
    $("#taxesPoint").val("");
    $("#taxes").val("");
    $("#price").val("");
    $("#pricePlusTaxes").val("");
    $("#invoiceNumber").val("");
    $("#statementSheet").val("");
    $("#statementWeight").val("");
    $("#timestamp").val("");
    $("#contractAddress").val("");
})


// 插入数据
$("#btn-invoiceInsertSubmit").click(function () {
    var json=$("#form-invoiceInsert").serializeJSON();
    alert(json);
    var jsonString = JSON.stringify(json);
    alert(jsonString);

    $.ajax({
        contentType: "application/json; charset=utf-8",
        type: 'POST',
        url: '/invoiceInsertSubmit',
        data: jsonString,
        error: function (request) {
            alert("插入失败！");
        },
        success: function (liItem) {
            if (liItem === "哈希值重复！") {
                alert(liItem);
            } else if (liItem === "系统错误！") {

            } else if (liItem) {
                $(".maquee").find("ul").append(liItem);
            }
            $("#myModal-invoiceInsert").modal('hide');
        }
    })
})


$("#btn-deployContract").click(function (){
    $("#tableName").val("");
    $("#bytecode").val("");
})

// 部署合约
$("#btn-deployContract-submit").click(function (){
    var data = {
        "tableName": $("#tableName").val(),
        "bytecode": $("#bytecode").val()
    };
    //alert(data);
    $.ajax({
        type: 'POST',
        url: '/deployContract',
        contentType: "application/json",
        data: JSON.stringify(data),
        success: function (data) {
            alert(data);
            $("#myModal-deployContract").modal('hide');
        }
    })
})

// 数据实时更新 + 滚动
setInterval(function(){$.ajax(invoiceUpdate)},3000);
// 实时更新滚动表格
var invoiceUpdate = {
    type: "get",
    url: "/invoiceUpdate",    //向后端请求数据的url
    success: function (liItem) {
        if (liItem) {
            $(".maquee").find("ul").animate({
                marginTop: "-37px"
            }, 500, function () {
                $(this).append(liItem);
                $(this).find("li:first").remove();
                $(this).css({marginTop: "0px"});
            })
        }
    }
};

// 基于准备好的dom，初始化echarts实例
var myChart = echarts.init(document.getElementById('main'));
var posList = [
    'left', 'right', 'top', 'bottom',
    'inside',
    'insideTop', 'insideLeft', 'insideRight', 'insideBottom',
    'insideTopLeft', 'insideTopRight', 'insideBottomLeft', 'insideBottomRight'
];


var app = {};
app.configParameters = {
    rotate: {
        min: -90,
        max: 90
    },
    align: {
        options: {
            left: 'left',
            center: 'center',
            right: 'right'
        }
    },
    verticalAlign: {
        options: {
            top: 'top',
            middle: 'middle',
            bottom: 'bottom'
        }
    },
    position: {
        options: echarts.util.reduce(posList, function (map, pos) {
            map[pos] = pos;
            return map;
        }, {})
    },
    distance: {
        min: 0,
        max: 100
    }
};

app.config = {
    rotate: 30, // 柱形数字倾斜角度
    align: 'left',
    verticalAlign: 'middle',
    position: 'top',
    distance: 10,
    onChange: function () {
        var labelOption = {
            normal: {
                rotate: app.config.rotate,
                align: app.config.align,
                verticalAlign: app.config.verticalAlign,
                position: app.config.position,
                distance: app.config.distance
            }
        };
        myChart.setOption({
            series: [{
                label: labelOption
            }, {
                label: labelOption
            }]
        });
    }
};
var labelOption = {
    show: true,
    position: app.config.position,
    distance: app.config.distance,
    align: app.config.align,
    verticalAlign: app.config.verticalAlign,
    rotate: app.config.rotate,
    formatter: '{c}',
    fontSize: 9,
    rich: {
        name: {
            textBorderColor: '#fff'
        }
    }
};




// 基于准备好的dom，初始化echarts实例
var myChart2 = echarts.init(document.getElementById('main2'), 'wonderland');
 //myChart2.showLoading();  // 开启 loading 效果
setInterval(function () {
    $.ajax({
        type: "GET",
        url: "/graphUpdate",
        dataType: "json",
        success: function (jsonObj) {
            // 指定图表的配置项和数据
            var option = {
                series : [
                    {
                        name: '访问来源',
                        type: 'pie',
                        radius: '40%',
                        center: ['40%', '40%'],
                        data:jsonObj.pieData,
                        roseType: 'angle',
                        itemStyle: {
                            normal: {
                                shadowBlur: 200,
                                shadowColor: 'rgba(0, 0, 0, 0.5)',
                            }
                        },
                        label: {
                            normal: {
                                show: true,
                                position: 'outter',
                                textStyle: {
                                    fontWeight: 200,
                                    fontSize: 12
                                },
                                formatter: '{b} : {c} ({d}%)'
                            }
                        }
                    }
                ]
            };
            // 使用刚指定的配置项和数据显示图表。
            myChart2.setOption(option);


            // 指定图表的配置项和数据
            option = {
                color: ['#4cabce', '#e5323e'],
                tooltip: {
                    trigger: 'axis',
                    axisPointer: {
                        type: 'shadow'
                    }
                },
                legend: {
                    data: ['价费', '税费'],
                    textStyle: {
                        color: '#76dbd1'
                    }
                },
                toolbox: {
                    show: true,
                    orient: 'vertical',
                    left: 'right',
                    top: 'center',
                    showTitle: false,
                    feature: {
                        mark: {show: true},
                        // dataView: {show: true, readOnly: false},
                        // magicType: {show: true, type: ['line', 'bar', 'stack', 'tiled']},
                        // restore: {show: true},
                        // saveAsImage: {show: true}
                    }
                },
                xAxis: [
                    {
                        type: 'category',
                        axisLine: {
                            lineStyle: {
                                color: '#76dbd1'
                            }
                        },
                        // data: ['2012', '2013', '2014', '2015', '2016', '2017'],
                        data: jsonObj.xAxisData,
                        axisLabel: {
                            // show:true,
                            // interval: 0, //强制所有标签显示
                            // align:'left',
                            // margin: 115, //标签向右移动 如果yAxis 跟 xAxis换了,这个margin应该调为0才能看见标签显示
                            // textStyle: {
                            //     color: "#000",
                            // },
                            formatter: function (params){   //标签输出形式 ---请开始你的表演
                                var strs = params.split("\n");
                                var ret = strs[0];
                                var len = 7;
                                if (strs[1].length <= 6) {
                                    ret += "\n" + strs[1].substring(0, len);
                                } else {
                                    ret += "\n" + strs[1].substring(0, len - 1) + "...";
                                }
                                if (strs[2].length <= 6) {
                                    ret += "\n" + strs[2].substring(0, len);
                                } else {
                                    ret += "\n" + strs[2].substring(0, len - 1) + "...";
                                }
                                return ret;
                            },
                        },
                    }
                ],
                yAxis: [
                    {
                        type: 'value',
                        axisLine: {
                            lineStyle: {
                                color: '#76dbd1'
                            }
                        }
                    }
                ],
                series: [
                    {
                        name: '价费',
                        type: 'line',
                        smooth: true,
                        barGap: 0,
                        label: labelOption,
                        //data: [32000, 33200, 30100, 33400, 39000, 30000],
                        data: jsonObj.priceData,
                    },
                    {
                        name: '税费',
                        type: 'line',
                        smooth: true,
                        label: labelOption,
                        //data: [220, 182, 191, 234, 290, 300]
                        data: jsonObj.taxesData
                    }
                ]
            };

            // 使用刚指定的配置项和数据显示图表。
            myChart.setOption(option);

        }
    })
}, 1000);


