package com.auto.base.swagger.commons.utils;

import com.alibaba.fastjson.JSON;
import com.auto.base.swagger.generate.ExcelMata;
import com.auto.base.swagger.generate.base.Mata;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zby
 * @date 2022/2/27 2:57
 */
public class MataUtil {

    public static List<String> mataToList(Mata mata) {
        List<String> list = new ArrayList<>();
        if (mata instanceof ExcelMata.SwaggerMata) {
            ExcelMata.SwaggerMata swaggerMata = (ExcelMata.SwaggerMata) mata;
            list.add(swaggerMata.getSwaggerLabel());
            list.add(swaggerMata.getSwaggerJsonUrl());
            list.add(swaggerMata.getBaseUrl());
            List<Pair<String, String>> dbList = swaggerMata.getDbList();
            list.add(JSON.toJSONString(dbList));
        } else if (mata instanceof ExcelMata.DbMata) {
            ExcelMata.DbMata dbMata = (ExcelMata.DbMata) mata;
            list.add(dbMata.getDbLabel());
            list.add(dbMata.getDbHost());
            list.add(dbMata.getDbPort());
            list.add(dbMata.getDbUsername());
            list.add(dbMata.getDbPassword());
        } else if(mata instanceof ExcelMata.RedisMata) {
            ExcelMata.RedisMata dbMata = (ExcelMata.RedisMata) mata;
            list.add(dbMata.getRedisLabel());
            list.add(dbMata.getRedisHost());
            list.add(dbMata.getRedisPort());
            list.add(dbMata.getRedisPassword());
            list.add(dbMata.getRedisNo());
        } else if (mata instanceof ExcelMata.SetMata) {
            ExcelMata.SetMata setMata = (ExcelMata.SetMata) mata;
            list.add(setMata.getSetKey());
            list.add(setMata.getSetValue());
        }
        return list;
    }
}
