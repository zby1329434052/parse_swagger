package com.auto.base.swagger.generate.xls;

import com.alibaba.fastjson.JSONObject;
import com.auto.base.swagger.commons.constants.ApiSwaggerStore;
import com.auto.base.swagger.commons.utils.ExcelUtil;
import com.auto.base.swagger.commons.utils.MataUtil;
import com.auto.base.swagger.domain.ApiDefinitionWithBLOBs;
import com.auto.base.swagger.generate.ExcelGenerate;
import com.auto.base.swagger.generate.ExcelMata;
import com.auto.base.swagger.generate.base.XlsExcel;
import com.auto.base.swagger.mock.MockApiUtils;
import com.auto.base.swagger.parse.ApiDefinitionImport;
import com.auto.base.swagger.request.MsHTTPSamplerProxy;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * xls模板类
 * @author zby
 * @date 2022/2/27 1:42
 */
public class XlsGenerate extends XlsExcel implements ExcelGenerate {

    @Override
    public Workbook GenerateMata(ExcelMata mata) {
        Workbook workbook = null;
        try {
            workbook = new HSSFWorkbook();
            ExcelUtil excelUtil = new ExcelUtil(workbook);
            List<ExcelMata.SwaggerMata> swaggerMata = mata.getSwaggerMata();
            List<ExcelMata.DbMata> dbMata = mata.getDbMata();
            List<ExcelMata.RedisMata> redisMata = mata.getRedisMata();
            List<ExcelMata.SetMata> setMata = mata.getSetMata();
            excelUtil.createSheet();
            excelUtil.setSheetName(0, "mata");
            int lastRowIndex = 0;

            if (swaggerMata.size() > 0) {
                for (int i = 0; i < swaggerMata.size() + 1; i++) {
                    if (i == 0) {
                        excelUtil.createRow(i, i);
                        excelUtil.createCell(i, i, i);
                        excelUtil.setValueAt(i, i, i, "**");
                        continue;
                    }
                    excelUtil.createRow(0, i);
                    excelUtil.setRowValue(0, MataUtil.mataToList(swaggerMata.get(i-1)), i);
                }
            }

            //空行
            lastRowIndex += swaggerMata.size() == 0 ? 0 : swaggerMata.size() + 1;

            if (dbMata.size() > 0) {
                for (int i = 0; i < dbMata.size() + 1; i++) {
                    if (i == 0) {
                        excelUtil.createRow(i, i + 1 + lastRowIndex);
                        excelUtil.createCell(i, i + 1 + lastRowIndex, i);
                        excelUtil.setValueAt(i, i + 1 + lastRowIndex, i, "db");
                        continue;
                    }
                    excelUtil.createRow(0, i + 1 + lastRowIndex);
                    excelUtil.setRowValue(0, MataUtil.mataToList(dbMata.get(i-1)), i + 1 + lastRowIndex);
                }
            }

            //空行
            lastRowIndex += dbMata.size() == 0 ? 0 : dbMata.size() + 2;

            if (redisMata.size() > 0) {
                for (int i = 0; i < redisMata.size() + 1; i++) {
                    if (i == 0) {
                        excelUtil.createRow(i, i + 1 + lastRowIndex);
                        excelUtil.createCell(i, i + 1 + lastRowIndex, i);
                        excelUtil.setValueAt(i, i + 1 + lastRowIndex, i, "redis");
                        continue;
                    }
                    excelUtil.createRow(0, i  + 1 + lastRowIndex);
                    excelUtil.setRowValue(0, MataUtil.mataToList(redisMata.get(i-1)), i + 1 + lastRowIndex);
                }
            }

            //空行
            lastRowIndex += redisMata.size() == 0 ? 0 : redisMata.size() + 2;

            if (setMata.size() > 0) {
                for (int i = 0; i < setMata.size() + 1; i++) {
                    if (i == 0) {
                        excelUtil.createRow(i, i + 1 + lastRowIndex);
                        excelUtil.createCell(i, i + 1 + lastRowIndex, i);
                        excelUtil.setValueAt(i, i + 1 + lastRowIndex, i, "set");
                        continue;
                    }
                    excelUtil.createRow(0, i  + 1 + lastRowIndex);
                    excelUtil.setRowValue(0, MataUtil.mataToList(setMata.get(i-1)), i + 1 + lastRowIndex);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


        return workbook;
    }

    @Override
    public Workbook GenerateSheet() {
        try {
            if (workbook == null) {
                workbook = new HSSFWorkbook();
            }
            ExcelUtil excelUtil = new ExcelUtil(workbook);
            ApiDefinitionImport apiDefinitionImport = ApiSwaggerStore.apiDefinitionImport;
            doGenerateSheet(apiDefinitionImport, excelUtil);


        } catch (IOException e) {
            e.printStackTrace();
        }

        return workbook;
    }

    public void doGenerateSheet(ApiDefinitionImport apiDefinitionImport, ExcelUtil excelUtil) throws IOException {
        excelUtil.createSheet();
        excelUtil.setSheetName(1, apiDefinitionImport.getSwaggerApi().getInfo().getTitle());
        //todo 接着写 目前写完获取系统名
        MsHTTPSamplerProxy proxy;
        Map<String, String> returnMap;
        String currentTagName = "nohave";
        int sheetIndex = 0;
        for (ApiDefinitionWithBLOBs item : apiDefinitionImport.getData()) {
            proxy = JSONObject.parseObject(item.getRequest(), MsHTTPSamplerProxy.class);
            returnMap = MockApiUtils.getApiResponse(item.getResponse());

            int column = 0;
            boolean isGroupStart = false;
            //先归类 一个tag对应一个group
            List<MsHTTPSamplerProxy> xxa = new ArrayList<>();
            List<Map<String, String>> xxb = new ArrayList<>();
            String tag = "";
            doGenerateGroup(tag, xxa, xxb);
            workbook.write(new FileOutputStream("ss"));
            workbook.close();
        }
    }

    private void doGenerateGroup(String tag, List<MsHTTPSamplerProxy> proxy, List<Map<String, String>> returnMap) {
        MsHTTPSamplerProxy a = proxy.get(0);
        Map<String, String> b = returnMap.get(0);
        System.out.println(">>>");
        System.out.println("***");
        System.out.println(tag);

        for (int i = 0; i < 100; i++) {
            doGenerateApi(a, b);
        }
        System.out.println("***");
        System.out.println(">>>");
    }

    private void doGenerateApi(MsHTTPSamplerProxy a, Map<String, String> b) {
        //todo api数据
        System.out.println("**");
        System.out.println("*");
        doGenerateUseCase();
    }

    private void doGenerateUseCase() {
    }
}
