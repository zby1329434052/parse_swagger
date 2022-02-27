package com.auto.base.swagger.generate.xls;

import com.auto.base.swagger.generate.ExcelMata;
import com.google.common.collect.Lists;
import javafx.util.Pair;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class XlsGenerateTest {

    @Test
    void generateMata() throws IOException {
        ExcelMata excelMata = new ExcelMata();
        ExcelMata.SwaggerMata swaggerMata = new ExcelMata.SwaggerMata();
        swaggerMata.setSwaggerLabel("a");
        swaggerMata.setSwaggerJsonUrl("/swagger/api.json");
        swaggerMata.setBaseUrl("http://camel.dev.yoohoor.com:82");
        List<Pair<String, String>> dbList = new ArrayList<>();
        Pair<String, String> pair = new Pair<>("test", "camel,tender,oms,yto_trans");
        dbList.add(pair);
        swaggerMata.setDbList(dbList);

        ExcelMata.SwaggerMata swaggerMata2 = new ExcelMata.SwaggerMata();
        swaggerMata2.setSwaggerLabel("a");
        swaggerMata2.setSwaggerJsonUrl("/swagger/api.json");
        swaggerMata2.setBaseUrl("http://camel.dev.yoohoor.com:82");
        List<Pair<String, String>> dbList2 = new ArrayList<>();
        Pair<String, String> pair2 = new Pair<>("test", "camel,tender,oms,yto_trans");
        dbList2.add(pair2);
        swaggerMata2.setDbList(dbList2);

        ExcelMata.SwaggerMata swaggerMata3 = new ExcelMata.SwaggerMata();
        swaggerMata3.setSwaggerLabel("a");
        swaggerMata3.setSwaggerJsonUrl("/swagger/api.json");
        swaggerMata3.setBaseUrl("http://camel.dev.yoohoor.com:82");
        List<Pair<String, String>> dbList3 = new ArrayList<>();
        Pair<String, String> pair3 = new Pair<>("test", "camel,tender,oms,yto_trans");
        dbList3.add(pair3);
        swaggerMata3.setDbList(dbList3);

        ExcelMata.DbMata dbMata = new ExcelMata.DbMata();
        dbMata.setDbLabel("test");
        dbMata.setDbHost("127.0.0.1");
        dbMata.setDbPort("3340");
        dbMata.setDbUsername("root");
        dbMata.setDbPassword("ptdAChu+byhzq2dCc0");


        ExcelMata.RedisMata redisMata = new ExcelMata.RedisMata();
        redisMata.setRedisLabel("test");
        redisMata.setRedisHost("127.0.0.1");
        redisMata.setRedisPort("6390");
        redisMata.setRedisPassword("");
        redisMata.setRedisNo("15");

        ExcelMata.RedisMata redisMata2 = new ExcelMata.RedisMata();
        redisMata2.setRedisLabel("test1");
        redisMata2.setRedisHost("127.0.0.1");
        redisMata2.setRedisPort("6390");
        redisMata2.setRedisPassword("");
        redisMata2.setRedisNo("1");

        ExcelMata.SetMata setMata = new ExcelMata.SetMata();
        setMata.setSetKey("sso_token");
        setMata.setSetValue("11111111111");

        ExcelMata.SetMata setMata2 = new ExcelMata.SetMata();
        setMata2.setSetKey("sso_token2");
        setMata2.setSetValue("22222222222");


        excelMata.setSwaggerMata(Lists.newArrayList());
        excelMata.getSwaggerMata().add(swaggerMata);
        excelMata.getSwaggerMata().add(swaggerMata2);
        excelMata.getSwaggerMata().add(swaggerMata3);

        excelMata.setDbMata(Lists.newArrayList());
        excelMata.getDbMata().add(dbMata);

        excelMata.setRedisMata(Lists.newArrayList());
        excelMata.getRedisMata().add(redisMata);
        excelMata.getRedisMata().add(redisMata2);

        excelMata.setSetMata(Lists.newArrayList());
        excelMata.getSetMata().add(setMata);
        excelMata.getSetMata().add(setMata2);

        Workbook sheets = new XlsGenerate().GenerateMata(excelMata);
        File file = new File("C:\\Users\\13294\\Desktop\\yto\\swagger\\template.xls");
        sheets.write(new FileOutputStream(file));
        sheets.close();
    }

    @Test
    void generateSheet() {
    }
}
