package com.zlimbo.bcweb.domain;

import jnr.ffi.annotations.In;

import java.text.SimpleDateFormat;
import java.util.*;

public class Invoice {

    private String hashValue;
    private String invoiceNo;
    private String buyerName;
    private String buyerTaxesNo;
    private String sellerName;
    private String sellerTaxesNo;
    private String invoiceDate;
    private String invoiceType;
    private String taxesPoint;
    private String taxes;
    private String price;
    private String pricePlusTaxes;
    private String invoiceNumber;
    private String statementSheet;
    private String statementWeight;
    private String timestamp;
    private String contractAddress;

    public Invoice() { }

    public Invoice(String hashValue,
                   String invoiceNo,
                   String buyerName,
                   String buyerTaxesNo,
                   String sellerName,
                   String sellerTaxesNo,
                   String invoiceDate,
                   String invoiceType,
                   String taxesPoint,
                   String taxes,
                   String price,
                   String pricePlusTaxes,
                   String invoiceNumber,
                   String statementSheet,
                   String statementWeight,
                   String timestamp,
                   String contractAddress) {
        this.hashValue = hashValue;
        this.invoiceNo = invoiceNo;
        this.buyerName = buyerName;
        this.buyerTaxesNo = buyerTaxesNo;
        this.sellerName = sellerName;
        this.sellerTaxesNo = sellerTaxesNo;
        this.invoiceDate = invoiceDate;
        this.invoiceType = invoiceType;
        this.taxesPoint = taxesPoint;
        this.taxes = taxes;
        this.price = price;
        this.pricePlusTaxes = pricePlusTaxes;
        this.invoiceNumber = invoiceNumber;
        this.statementSheet = statementSheet;
        this.statementWeight = statementWeight;
        this.timestamp = timestamp;
        this.contractAddress = contractAddress;
    }

    public String getHashValue() {
        return hashValue;
    }

    public void setHashValue(String hashValue) {
        this.hashValue = hashValue;
    }

    public String getInvoiceNo() {
        return invoiceNo;
    }

    public void setInvoiceNo(String invoiceNo) {
        this.invoiceNo = invoiceNo;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public void setBuyerName(String buyerName) {
        this.buyerName = buyerName;
    }

    public String getBuyerTaxesNo() {
        return buyerTaxesNo;
    }

    public void setBuyerTaxesNo(String buyerTaxesNo) {
        this.buyerTaxesNo = buyerTaxesNo;
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

    public String getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(String invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public String getInvoiceType() {
        return invoiceType;
    }

    public void setInvoiceType(String invoiceType) {
        this.invoiceType = invoiceType;
    }

    public String getTaxesPoint() {
        return taxesPoint;
    }

    public void setTaxesPoint(String taxesPoint) {
        this.taxesPoint = taxesPoint;
    }

    public String getTaxes() {
        return taxes;
    }

    public void setTaxes(String taxes) {
        this.taxes = taxes;
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

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
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

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }


    public static Invoice getRandomInvoice() {

        Random random = new Random();
        Invoice invoice = new Invoice();

        String hashValue = getHexString(32);
        String invoiceNo = getOctString(10);
        String[] buyer = COMPANY_TAXESNO[random.nextInt(COMPANY_TAXESNO.length)];
        String[] seller = COMPANY_TAXESNO[random.nextInt(COMPANY_TAXESNO.length)];
        String buyerName = buyer[0];
        String buyerTaxesNo = buyer[1];
        String sellerName = seller[0];
        String sellerTaxesNo = seller[1];
        String invoiceDate = new SimpleDateFormat("yyyy-MM-dd" ).format(new Date());
        String invoiceType = INVOICE_KIND[random.nextInt(INVOICE_KIND.length)];
        String taxesPoint = (10 + random.nextInt(10)) + "%";
        int taxesRaw = 100 + random.nextInt(1000);
        int priceRaw = 10000 + random.nextInt(100000);
        String taxes = "" + taxesRaw;
        String price = "" + priceRaw;
        String pricePlusTaxes = "" + (taxesRaw + priceRaw);
        String invoiceNumber = "" + (1 + random.nextInt(3));
        String statementSheet = "" + (1 + random.nextInt(3));
        String statementWeight = (1 + random.nextInt(10)) + "kg";
        String timestamp = "" + System.currentTimeMillis();
        String contractAddress = getHexString(16);
        return new Invoice(
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
                timestamp,
                contractAddress);
    }


    final static String[] INVOICE_KIND = {"增值税发票", "普通发票", "专业发票"};

    public static List<Character> Hex =
            Arrays.asList('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f');

    // 返回指定的十六进制字符串
    public static String getHexString(int length) {
        StringBuilder stringBuilder = new StringBuilder("0x");
        Random random = new Random();
        while (length-- != 0) {
            stringBuilder.append(Hex.get(random.nextInt(16)));
        }
        return stringBuilder.toString();
    }

    public static String getOctString(int length) {
        StringBuilder stringBuilder = new StringBuilder("0x");
        Random random = new Random();
        while (length-- != 0) {
            stringBuilder.append(Hex.get(random.nextInt(10)));
        }
        return stringBuilder.toString();
    }


    final static String[][] COMPANY_TAXESNO = {
            {"证券简称", "736763JRERB3H54"},
            {"荣丰控股", "737462HU9S9DTV8"},
            {"三湘印象", "7387169WWD894R7"},
            {"科华生物", "734551HGHYFPFA1"},
            {"思源电气", "73994732QUV1XE0"},
            {"威尔泰", "739185FCFOYGHBX"},
            {"中国海诚", "739272L15GREYJ8"},
            {"汉钟精机", "731305KG24YXJG9"},
            {"悦心健康", "7342506SXUSOGI0"},
            {"延华智能", "737055XVKT065P9"},
            {"海得控制", "736059BSIW8FX25"},
            {"二三四五", "733586OLXQYVX56"},
            {"宏达新材", "731090NISOL3SV0"},
            {"上海莱士", "738228RGSDL42H5"},
            {"美邦服饰", "734878VQ2382VF6"},
            {"神开股份", "736219HEXL3WNW6"},
            {"普利特", "733135P67MTGUG0"},
            {"新朋股份", "735120MK0AYVQP2"},
            {"柘中股份", "737417TS3T23JK9"},
            {"中远海科", "7308634W22HY8N6"},
            {"摩恩电气", "735324TP9IO6X53"},
            {"松芝股份", "733206X24M35MA7"},
            {"嘉麟杰", "733979FK8NVYNT3"},
            {"协鑫集成", "73665460YTH5UA1"},
            {"新时达", "739237HU7DLFS01"},
            {"徐家汇", "7365211MU3P3P5X"},
            {"顺灏股份", "738962FEXP7C5E0"},
            {"百润股份", "735114V6OYDWAJX"},
            {"姚记科技", "734450L20PON6EX"},
            {"金安国纪", "738111YSESAJP22"},
            {"康达新材", "7391739NQT6ROD9"},
            {"良信电器", "731974JGB72L8C0"},
            {"纳尔股份", "7369379YTKT4QD1"},
            {"力盛赛车", "7319402V7M3FSN4"},
            {"天海防务", "735895GTW8V03F9"},
            {"网宿科技", "7337180P2X6PUS6"},
            {"上海凯宝", "739536F7QW2K8R9"},
            {"东方财富", "7373367NFM873UX"},
            {"旗天科技", "739917AU6DQ2Y25"},
            {"安诺其", "7398825GAOGR6R5"},
            {"华平股份", "7348944KPNEPFY7"},
            {"锐奇股份", "739343RH42B0QG4"},
            {"泰胜风能", "7329333SBDVNC8X"},
            {"科泰电源", "7334375PVF7SOV9"},
            {"万达信息", "7324609HBLXQ8M9"},
            {"汉得信息", "735830YQTWOXGO3"},
            {"东富龙", "731887ICQKDTBR1"},
            {"华峰超纤", "7312692PXGMUYX9"},
            {"科大智能", "732830VVL5VSYY1"},
            {"金力泰", "7317969P4JTFW20"},
            {"上海钢联", "7399005G79UCVY8"},
            {"永利股份", "738082QB080WVA3"},
            {"上海新阳", "730561WBXHCUJ4X"},
            {"天玑科技", "739093RODNBA4U7"},
            {"卫宁健康", "735980FGEJ72SC0"},
            {"巴安水务", "734156KOPSU5JQ5"},
            {"开能健康", "732671A9RD43Q7X"},
            {"安科瑞", "738566C559JDY68"},
            {"凯利泰", "739016XI5KO3BU8"},
            {"中颖电子", "739439P60C8CSO6"},
            {"华虹计通", "731091EL2JQF4AX"},
            {"新文化", "732516K86BH9EY0"},
            {"鼎捷软件", "738245L775DFQR4"},
            {"安硕信息", "739758Q85U0J5T6"},
            {"飞凯材料", "734989THUOVL9R4"},
            {"普丽盛", "734644GG33N5XB2"},
            {"华铭智能", "732727TKKEQGR27"},
            {"信息发展", "732897N308J4BK4"},
            {"沃施股份", "739108USVBJMV83"},
            {"润欣科技", "739937L6YA0SRD0"},
            {"海顺新材", "734748UAO3E7SC3"},
            {"维宏股份", "734905HMDA7PDF9"},
            {"雪榕生物", "739077CX5TMVOF3"},
            {"古鳌科技", "735089TDWX0TW69"},
            {"会畅通讯", "736280SI9J3UEJ6"},
            {"移为通信", "739030WXYQWLBF3"},
            {"汇纳科技", "739027AR27XJQ5X"},
            {"富瀚微", "731294241FWESA8"},
            {"华测导航", "738555MC5WN8P17"},
            {"透景生命", "736446KTH55TKQ3"},
            {"上海瀚讯", "7388580KH1G8XC2"},
            {"矩子科技", "731234LM4MKOAQ1"},
            {"浦发银行", "735682D7BKUIP00"},
            {"上海机场", "736805HWF9QNNC5"},
            {"上港集团", "730093PMES3ICQ8"},
            {"宝钢股份", "734934Y73NDRUN3"},
            {"上海电力", "738335WE7B8HYJ9"},
            {"中远海能", "738928DPR7U5658"},
            {"国投资本", "733019TIKK775I8"},
            {"中船科技", "7379237HDX52G01"},
            {"上海梅林", "730115CTYLL1220"},
            {"东风科技", "732092XSIJJUCW6"},
            {"中视传媒", "7332881UDWYQ6G5"},
            {"大名城", "731979HS4YE90X5"},
            {"开创国际", "730359CMXQXO4D9"},
            {"上汽集团", "739487RMLNASKP9"},
            {"东方航空", "7385243VRE8KYG2"},
            {"ST长投", "733374B9KLS6S06"},
            {"中国船舶", "7399465MHRXA5K5"},
            {"航天机电", "733046K6PK2SFB4"},
            {"上海建工", "732912DBHM76VWX"},
            {"上海贝岭", "73815746CLN9IC5"},
            {"ST创兴", "7355809WB7KEIF3"},
            {"复星医药", "731896L35F0EW76"},
            {"紫江企业", "735474V8WSKU8M0"},
            {"开开实业", "7311720HCR3HQT4"},
            {"东方创业", "735674C2NN28XK4"},
            {"浦东建设", "738720DNQRDAFL6"},
            {"上海家化", "737825I51MLRB77"},
            {"振华重工", "7304037HH6A8XM6"},
            {"现代制药", "736977KDKUD1AG5"},
            {"鹏欣资源", "737111PQHNJHRC9"},
            {"中化国际", "739874L8BDK4J56"},
            {"华丽家族", "733240U6SJXIQA5"},
            {"上海能源", "7301500XLP6F144"},
            {"置信电气", "7334654QCYJT384"},
            {"交大昂立", "738418NJ1TLXDE7"},
            {"宏达矿业", "734030770NJEHB8"},
            {"光明乳业", "733392XGO2IXJC1"},
            {"方正科技", "7395081WWPAFYX7"},
            {"云赛智联", "733225S2NFVR7W6"},
            {"市北高新", "734297CCHAGU0A2"},
            {"汇通能源", "738253SK8AWND94"},
            {"绿地控股", "733334NWC4DL569"},
            {"ST沪科", "737385Q2XESW4N3"},
            {"ST毅达", "736531RDEPB2K05"},
            {"大众交通", "733492JTWEWF2Y1"},
            {"老凤祥", "731697K29NY5VX7"},
            {"神奇制药", "738595P09I2CIP8"},
            {"丰华股份", "739994PMBILTC41"},
            {"金枫酒业", "735047TV94IS5I8"},
            {"氯碱化工", "733610M2DUADG00"},
            {"海立股份", "734075SM23UXDT4"},
            {"天宸股份", "73679221H4MFHJ8"},
            {"华鑫股份", "739063KTAA7OHO7"},
            {"光大嘉宝", "730415FUU5METM0"},
            {"华谊集团", "734111DBL850569"},
            {"复旦复华", "736407SMITD17U7"},
            {"申达股份", "737110Q6DR3E6J8"},
            {"新世界", "735041XLVYVCLT7"},
            {"华建集团", "735719NU4PPSSG9"},
            {"龙头股份", "7385455PA15XTF1"},
            {"ST富控", "73114966NG61T88"},
            {"大众公用", "739273VWKQNT3VX"},
            {"三爱富", "731154VB73S8NY0"},
            {"东方明珠", "737278RMY1Y6001"},
            {"新黄浦", "733850EXAHMGYJ1"},
            {"浦东金桥", "733574U9OE1OFX4"},
            {"号百控股", "730460TU01YHP88"},
            {"万业企业", "7388015IALW219X"},
            {"申能股份", "7348453LGJDBTX0"},
            {"爱建集团", "731914A97V77CU6"},
            {"同达创业", "7387993UQY50UAX"},
            {"外高桥", "731535U96US9ER7"},
            {"城投控股", "730487GDYC4U1C5"},
            {"锦江投资", "733051IJCDIHO36"},
            {"飞乐音响", "7301833GPSWEYF3"},
            {"ST游久", "734494GFE9MASR1"},
            {"申华控股", "736375XKYS179X2"},
            {"ST中安", "737455LFIPCUEE4"},
            {"豫园股份", "731065Y3GK86NS1"},
            {"昂立教育", "7314705HCSPI4H6"},
            {"强生控股", "737120FLLHCKL70"},
            {"陆家嘴", "734250N6QT428U7"},
            {"中华企业", "73915701H5QLXT6"},
            {"交运股份", "7360014M44J7WJ2"},
            {"上海凤凰", "731126D8LAHXH10"},
            {"上海石化", "73810463XNW9XX5"},
            {"上海三毛", "735105XB25JAT55"},
            {"亚通股份", "737707HEPL9AF52"},
            {"绿庭投资", "732243JAO5UMVO0"},
            {"ST岩石", "730457JP1BB2KG5"},
            {"光明地产", "734710V7233GV56"},
            {"ST爱旭", "734249EH6KSNYS4"},
            {"华域汽车", "736195P9OS45392"},
            {"上实发展", "735615U0QXPTMP6"},
            {"锦江酒店", "730688D1J3XCAC0"},
            {"ST运盛", "73292259D8HIEYX"},
            {"安信信托", "730117JGDPQ0IC8"},
            {"中路股份", "731720LJNC238Y3"},
            {"耀皮玻璃", "73145750J4TIA14"},
            {"隧道股份", "7332763RDE6X229"},
            {"上海物贸", "733037YEJG1PBI9"},
            {"世茂股份", "739529HJRYA7T44"},
            {"益民集团", "732763O7Y67XYV6"},
            {"新华传媒", "7396682JSLTBNH8"},
            {"兰生股份", "736554N381OWVN8"},
            {"百联股份", "733459OUGAJ3EC1"},
            {"第一医药", "737108YSYJGYHQX"},
            {"申通地铁", "730357EIH33KLVX"},
            {"上海机电", "739017PGDOY03Y9"},
            {"界龙实业", "737656V70L17144"},
            {"海通证券", "734316Y2KXBNEDX"},
            {"上海九百", "731115X7YEW5BL0"},
            {"上柴股份", "730422AT9W1L2MX"},
            {"上工申贝", "734630V36POYMC2"},
            {"宝信软件", "735899P8TVGFAUX"},
            {"同济科技", "738966V2M5LEBF3"},
            {"上海临港", "733845U5RN6RSK5"},
            {"华东电脑", "736559JEEJJU3B7"},
            {"海欣股份", "735730MIQ0ALLJ4"},
            {"妙可蓝多", "731558VRTFVT7F6"},
            {"张江高科", "735909FYLDU4NM4"},
            {"东方证券", "735129ND823XM16"},
            {"春秋航空", "7378857IBHDI7R8"},
            {"上海环境", "738233PBCS3DBL3"},
            {"国泰君安", "732937RPHHXXOC5"},
            {"上海银行", "738717X6KKTAO3X"},
            {"环旭电子", "736512YSS67YA6X"},
            {"交通银行", "736776U6SW94297"},
            {"大智慧", "737166PKJ1ISGX7"},
            {"上海电影", "731929JNI82P4C5"},
            {"中国太保", "7328842Q69Q0QJ1"},
            {"上海医药", "733219HT9S541Y4"},
            {"中国核建", "7336417CGJ9SL35"},
            {"广电电气", "732351UJVO5BYI7"},
            {"中银证券", "739207FMHXVL3C4"},
            {"上海电气", "732454E9VPUAL76"},
            {"光大证券", "73112946NHRQ5S8"},
            {"美凯龙", "7366025TH7FY4I7"},
            {"中远海发", "734338ULCG7TM33"},
            {"招商轮船", "736591EPX2ELQE9"},
            {"宝钢包装", "732110OOWW63K24"},
            {"龙宇燃油", "7306245UDIIST56"},
            {"联明股份", "7357429TSAEJXD6"},
            {"北特科技", "7331264G7YJI7B9"},
            {"创力集团", "735457QC618NR54"},
            {"爱普股份", "7385691CXW3L5B6"},
            {"新通联", "739024LJUDT83G9"},
            {"全筑股份", "733576F5SD6NC45"},
            {"凯众股份", "737771HBR6DJ2N2"},
            {"泛微网络", "7364413YLO78W85"},
            {"德邦股份", "737059GJPYWCH83"},
            {"博通集成", "738070D71LJHAB5"},
            {"剑桥科技", "730572JDQHTPX05"},
            {"润达医疗", "736876ODGJOQIS4"},
            {"华培动力", "7350244DDXJMB98"},
            {"华贸物流", "733646088UMTNG8"},
            {"上海沪工", "735445NUVO6HK44"},
            {"拉夏贝尔", "73292660BSX7AIX"},
            {"上海亚虹", "730749V2MF4FUD7"},
            {"网达软件", "737066QDWMRRYM2"},
            {"汇得科技", "733768RX5OM53Q3"},
            {"日播时尚", "737677E8S3MAXTX"},
            {"保隆科技", "731114ANPJPBH71"},
            {"上海洗霸", "730623QPOXS2QF0"},
            {"爱婴室", "7341497UIITXW01"},
            {"菲林格尔", "7313040BPF80YMX"},
            {"格尔软件", "7356204QBST0040"},
            {"移远通信", "739295PWHU3CYB2"},
            {"宏和科技", "733827SG9M9USLX"},
            {"上海雅仕", "730489Y87PHI0G5"},
            {"上海天洋", "733441WIOMYM9S4"},
            {"水星家纺", "737158JDC38B944"},
            {"亚士创能", "7383016GDI3XSU2"},
            {"风语筑", "7361005EPY6I8A9"},
            {"恒为科技", "7329568NWIBM1B3"},
            {"翔港科技", "737570R8NXR99FX"},
            {"韦尔股份", "736468S7WQUKDC0"},
            {"欧普照明", "732181O2IWRQ7P1"},
            {"荣泰健康", "738436M2RVGKFIX"},
            {"艾艾精工", "737554D5T4139J6"},
            {"地素时尚", "738550Q2Q5VUEH8"},
            {"中曼石油", "738369IQBCPQ8Y3"},
            {"徕木股份", "7373156T606ROC8"},
            {"畅联股份", "735690Q7MIUXB64"},
            {"彤程新材", "731981CVVT1FH75"},
            {"璞泰来", "7347026SMUGEPV1"},
            {"永冠新材", "732606QA6Y4MCP0"},
            {"晶华新材", "735238QU5746C17"},
            {"至纯科技", "730833CX5DIKC40"},
            {"密尔克卫", "7397406M1NT7QR0"},
            {"海利生物", "736779TB2F4YC93"},
            {"鸣志电器", "734506A7QUSWDN4"},
            {"龙韵股份", "7376418DYMUPCKX"},
            {"岱美股份", "7377832LFXY42B8"},
            {"来伊份", "731694UVDB318EX"},
            {"科博达", "734084BYDM98AE7"},
            {"雅运股份", "7320064G5K6KY09"},
            {"华荣股份", "7372401S01HNR08"},
            {"飞科电器", "734492OBVOW2BE8"},
            {"数据港", "73940845KXTQ7M8"},
            {"吉祥航空", "733723T0KTPB7L7"},
            {"元祖股份", "737965JLDF6SY40"},
            {"城地股份", "737145NMSSW1CK1"},
            {"天永智能", "735398PI8IM35V2"},
            {"晨光文具", "732267ECFHFF431"},
            {"金桥信息", "734241B9GRK34C8"},
            {"威派格", "732934IEPT3VCR2"},
            {"克来机电", "731077BFO5WFVE0"},
            {"康德莱", "736309QM2GOYVD5"},
            {"至正股份", "7336385JAOIHBB0"},
            {"澜起科技", "7365513E7BVS5P8"},
            {"中微公司", "731893EMEDQ7502"},
            {"心脉医疗", "737353FBNQSLS11"},
            {"乐鑫科技", "7377029EIXPJ8A0"},
            {"安集科技", "737989QKUG99X21"},
            {"申联生物", "730146LCBBRPNT6"},
            {"晶晨股份", "737962HN9EDUP17"},
            {"普元信息", "7359276QNU45K35"},
            {"聚辰股份", "7319047HA2DM8U0"},
            {"优刻得", "734884BPHUJYY37"},
            {"柏楚电子", "7388021KSTN0P29"},
            {"美迪西", "73565890PDDO093"},
            {"昊海生科", "736326ROHSMAYW6"},
            {"晶丰明源", "7347372A9BX0IL4"}
    };

    //生成企业组织机构代码
    public static String getORGANIZATION_CODE(){
        int [] in = { 3, 7, 9, 10, 5, 8, 4, 2 };
        String data = "";
        String yz = "";
        int a = 0;
        //随机生成英文字母和数字
        for (int i = 0; i < in.length; i++){
            String word = getCharAndNumr(1,0).toUpperCase();
            if (word.matches("[A-Z]")) {
                a += in[i] * getAsc(word);
            }else{
                a += in[i] * Integer.parseInt(word);
            }
            data += word;
        }
        //确定序列
        int c9 = 11 - a % 11;
        //判断c9大小，安装 X 0 或者C9
        if (c9 == 10) {
            yz = "X";
        } else if (c9 == 11) {
            yz = "0";
        } else {
            yz = c9 + "";
        }
        data += "-"+yz;
        return data.toUpperCase();
    }

    //生成税务登记号码
    public static String getTAX_REGISTRATION_CODE(){
        String data = "";
        String first = "73"+getCharAndNumr(4,2);
        String end = getORGANIZATION_CODE();
        data= first+end;
        data =data.toUpperCase().replaceAll("-","");
        if (!test5(data.toUpperCase())) getTAX_REGISTRATION_CODE();
        return data;
    }

    public static int getAsc(String st) {
        byte[] gc = st.getBytes();
        int ascNum = (int) gc[0] - 55;
        return ascNum;
    }

    public static boolean test5(String data){
        String regex = "[1-8][1-6]\\d{4}[a-zA-Z0-9]{9}$";
        if (!data.matches(regex)) {
            return false;
        }else
            return true;
    }

    public static String getCharAndNumr(int length,int status) {
        Random random = new Random();
        StringBuffer valSb = new StringBuffer();
        String charStr = "0123456789abcdefghijklmnopqrstuvwxy";
        if (status == 1) charStr = "0123456789";
        if (status == 2) charStr = "0123456789";
        if (status == 3) charStr = "0123456789ABCDEFGHJKLMNPQRTUWXY";
        int charLength = charStr.length();
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(charLength);
            if (status==1&&index==0){ index =3;}
            valSb.append(charStr.charAt(index));
        }
        return valSb.toString();
    }
}
