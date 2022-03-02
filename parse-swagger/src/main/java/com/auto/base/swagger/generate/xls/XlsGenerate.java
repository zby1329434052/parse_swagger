package com.auto.base.swagger.generate.xls;

import com.alibaba.fastjson.JSON;
import com.auto.base.swagger.commons.utils.ExcelUtil;
import com.auto.base.swagger.commons.utils.MataUtil;
import com.auto.base.swagger.domain.ApiDefinitionWithBLOBs;
import com.auto.base.swagger.dto.Body;
import com.auto.base.swagger.dto.KeyValue;
import com.auto.base.swagger.generate.ExcelConstant;
import com.auto.base.swagger.generate.ExcelGenerate;
import com.auto.base.swagger.generate.ExcelMata;
import com.auto.base.swagger.generate.base.XlsExcel;
import com.auto.base.swagger.parse.ApiDefinitionImport;
import com.auto.base.swagger.parse.ApiTestImportRequest;
import com.auto.base.swagger.parse.Swagger2Parser;
import com.auto.base.swagger.parse.swagger.SwaggerTag;
import com.auto.base.swagger.request.MsHTTPSamplerProxy;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.util.StringUtils;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * xls模板类
 * @author zby
 * @date 2022/2/27 1:42
 */
@Slf4j
public class XlsGenerate extends XlsExcel implements ExcelGenerate {
    /**
     * 用例页 行数索引
     */
    private int sheetRowIndex = 0;

    /**
     * 用例页索引 swaggerHeaderMap 和 判断是第几个用例页
     */
    private int sheetIndex = 1;

    /**
     * swagger标签列表
     */
    private List<String> swaggerLabel = new ArrayList<>();

    /**
     * swagger请求头map k:sheetIndex, v:headerList
     */
    private Map<Integer, List<String>> swaggerHeaderMap = new HashMap<>();

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
            excelUtil.setSheetName(ExcelConstant.MATA_INDEX, ExcelConstant.MATA);
            int lastRowIndex = 0;

            if (swaggerMata.size() > 0) {
                for (int i = 0; i < swaggerMata.size() + 1; i++) {
                    if (i == 0) {
                        excelUtil.createRow(i, i);
                        excelUtil.createCell(i, i, i);
                        excelUtil.setValueAt(i, i, i, "**");
                        continue;
                    }
                    excelUtil.createRow(ExcelConstant.MATA_INDEX, i);
                    excelUtil.setRowValue(ExcelConstant.MATA_INDEX, MataUtil.mataToList(swaggerMata.get(i-1)), i);
                    if (swaggerMata.get(i-1) != null) {
                        swaggerLabel.add(swaggerMata.get(i-1).getSwaggerLabel());
                    }
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
                    excelUtil.createRow(ExcelConstant.MATA_INDEX, i + 1 + lastRowIndex);
                    excelUtil.setRowValue(ExcelConstant.MATA_INDEX, MataUtil.mataToList(dbMata.get(i-1)), i + 1 + lastRowIndex);
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
                    excelUtil.createRow(ExcelConstant.MATA_INDEX, i  + 1 + lastRowIndex);
                    excelUtil.setRowValue(ExcelConstant.MATA_INDEX, MataUtil.mataToList(redisMata.get(i-1)), i + 1 + lastRowIndex);
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
                    excelUtil.createRow(ExcelConstant.MATA_INDEX, i  + 1 + lastRowIndex);
                    excelUtil.setRowValue(ExcelConstant.MATA_INDEX, MataUtil.mataToList(setMata.get(i-1)), i + 1 + lastRowIndex);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        prepareGenerateSheet(mata);
        return workbook;
    }

    private void prepareGenerateSheet(ExcelMata mata) {
        @NotNull List<ExcelMata.SwaggerMata> swaggerMataList = mata.getSwaggerMata();
        for (int i = 0; i < swaggerMataList.size(); i++) {
            if (null != swaggerMataList.get(i).getSwaggerHeader()) {
                swaggerHeaderMap.put(i + 1, swaggerMataList.get(i).getSwaggerHeader());
            }
        }
    }

    @Override
    public Workbook GenerateSheet(Workbook workbook) {
        try {
            if (workbook == null) {
                workbook = new HSSFWorkbook();
            }
            ExcelUtil excelUtil = new ExcelUtil(workbook);

            // <测试用>
            ApiTestImportRequest apiTestImportRequest = new ApiTestImportRequest();
            apiTestImportRequest.setProjectId("dispatcher");
            apiTestImportRequest.setModuleId("");
            apiTestImportRequest.setType("schedule");
            apiTestImportRequest.setUserId("user1");
            apiTestImportRequest.setSwaggerUrl("http://camel.dev.yoohoor.com:9003/cms/carrier/v2/api-docs?group=cms-carrier");
            ApiDefinitionImport apiDefinitionImport = new Swagger2Parser().parse(null, apiTestImportRequest);
            // </测试用>

//            ApiDefinitionImport apiDefinitionImport = ApiSwaggerStore.apiDefinitionImport;
            doGenerateSheet(apiDefinitionImport, excelUtil);

            //设置excel格式
            excelUtil.makeStyle(1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return workbook;
    }

    public void doGenerateSheet(ApiDefinitionImport apiDefinitionImport, ExcelUtil excelUtil) throws IOException {
        excelUtil.createSheet();
        excelUtil.setSheetName(ExcelConstant.SHEET_INDEX, apiDefinitionImport.getSwaggerApi().getInfo().getTitle());

        List<ApiDefinitionWithBLOBs> data = apiDefinitionImport.getData();
        //对data进行分组 根据tag来分, k: tag, v:apiDefinition
        Map<String, List<ApiDefinitionWithBLOBs>> apiMap= new HashMap<>();
        data.forEach(apiDefinition -> {
            apiMap.computeIfAbsent(apiDefinition.getTags(), key -> new ArrayList<>());
            apiMap.get(apiDefinition.getTags()).add(apiDefinition);
        });
        List<SwaggerTag> tags = apiDefinitionImport.getSwaggerApi().getTags();
        for (SwaggerTag tag : tags) {
            List<ApiDefinitionWithBLOBs> apiList = apiMap.get(tag.getName());
            doGenerateGroup(tag.getName(), apiList, excelUtil);
        }
        sheetIndex++;
    }

    private void doGenerateGroup(String tag, List<ApiDefinitionWithBLOBs> apiList, ExcelUtil excelUtil) throws IOException {
        MsHTTPSamplerProxy proxy;
        Map<String, String> returnMap;
        excelUtil.createRow(ExcelConstant.SHEET_INDEX, sheetRowIndex);
        excelUtil.createCell(ExcelConstant.SHEET_INDEX, sheetRowIndex, 0);
        excelUtil.setValueAt(ExcelConstant.SHEET_INDEX, sheetRowIndex, 0, ">>>");
        sheetRowIndex += 1;
        excelUtil.createRow(ExcelConstant.SHEET_INDEX, sheetRowIndex);
        excelUtil.createCell(ExcelConstant.SHEET_INDEX, sheetRowIndex, 0);
        excelUtil.setValueAt(ExcelConstant.SHEET_INDEX, sheetRowIndex, 0, "***");
        excelUtil.createCell(ExcelConstant.SHEET_INDEX, sheetRowIndex, 1);
        excelUtil.setValueAt(ExcelConstant.SHEET_INDEX, sheetRowIndex, 1, tag);
        sheetRowIndex += 2;

        for (ApiDefinitionWithBLOBs apiDefinition : apiList) {
            proxy = JSON.parseObject(apiDefinition.getRequest(), MsHTTPSamplerProxy.class);
            returnMap = JSON.parseObject(apiDefinition.getResponse(), Map.class);
            doGenerateApi(proxy, returnMap, excelUtil);
        }

        sheetRowIndex += 1;
        excelUtil.createRow(ExcelConstant.SHEET_INDEX, sheetRowIndex);
        excelUtil.createCell(ExcelConstant.SHEET_INDEX, sheetRowIndex, 0);
        excelUtil.setValueAt(ExcelConstant.SHEET_INDEX, sheetRowIndex, 0, "***");
        sheetRowIndex += 1;
        excelUtil.createRow(ExcelConstant.SHEET_INDEX, sheetRowIndex);
        excelUtil.createCell(ExcelConstant.SHEET_INDEX, sheetRowIndex, 0);
        excelUtil.setValueAt(ExcelConstant.SHEET_INDEX, sheetRowIndex, 0, ">>>");
        sheetRowIndex += 1;
    }

    private void doGenerateApi(MsHTTPSamplerProxy proxy, Map<String, String> returnMap, ExcelUtil excelUtil) throws IOException {
        excelUtil.createRow(ExcelConstant.SHEET_INDEX, sheetRowIndex);
        excelUtil.createCell(ExcelConstant.SHEET_INDEX, sheetRowIndex, 0);
        excelUtil.setValueAt(ExcelConstant.SHEET_INDEX, sheetRowIndex, 0, "**");
        excelUtil.createCell(ExcelConstant.SHEET_INDEX, sheetRowIndex, 1);
        excelUtil.setValueAt(ExcelConstant.SHEET_INDEX, sheetRowIndex, 1, proxy.getName());

        excelUtil.createCell(ExcelConstant.SHEET_INDEX, sheetRowIndex, 2);
        excelUtil.setValueAt(ExcelConstant.SHEET_INDEX, sheetRowIndex, 2, buildUseCaseApi(proxy));

        excelUtil.createRow(ExcelConstant.SHEET_INDEX, sheetRowIndex + 1);
        excelUtil.createCell(ExcelConstant.SHEET_INDEX, sheetRowIndex + 1, 0);
        excelUtil.setValueAt(ExcelConstant.SHEET_INDEX, sheetRowIndex + 1, 0, "*");
        int columnIndex = 3;
        columnIndex = buildApiParam(filterHeader(proxy.getHeaders()), excelUtil, columnIndex);
        columnIndex = buildApiParam(proxy.getArguments(), excelUtil, columnIndex);
        columnIndex = buildApiParam(proxy.getBody(), excelUtil, columnIndex);
        buildApiParam(returnMap, excelUtil, columnIndex);
        sheetRowIndex += 1;
        doGenerateUseCase(excelUtil);
    }

    private Object filterHeader(List<KeyValue> headers) {
        List<KeyValue> result = headers.stream().filter(header -> {
            List<String> headerList = swaggerHeaderMap.get(sheetIndex);
            if (headerList != null) {
                for (String item : headerList) {
                    return item.toLowerCase().equals(header.getName().toLowerCase());
                }
            }
            return true;
        }).collect(Collectors.toList());
        return result;
    }

    private int buildApiParam(Object param, ExcelUtil excelUtil, int columnIndex) throws IOException {
        if (param instanceof List) {
            List<KeyValue> paramList = (List)param;
            if (paramList != null && paramList.size() > 0) {
                for (KeyValue item : paramList) {
                    excelUtil.createCell(ExcelConstant.SHEET_INDEX, sheetRowIndex, columnIndex);
                    excelUtil.setValueAt(ExcelConstant.SHEET_INDEX, sheetRowIndex, columnIndex, item.getDescription());
                    excelUtil.createCell(ExcelConstant.SHEET_INDEX, sheetRowIndex + 1, columnIndex);
                    excelUtil.setValueAt(ExcelConstant.SHEET_INDEX, sheetRowIndex + 1, columnIndex, item.getName());
                    columnIndex += 1;
                }
            }
        } else if (param instanceof Body) {
            Body body = (Body) param;
            for (KeyValue item : body.getKvs()) {
                excelUtil.createCell(ExcelConstant.SHEET_INDEX, sheetRowIndex, columnIndex);
                excelUtil.setValueAt(ExcelConstant.SHEET_INDEX, sheetRowIndex, columnIndex, item.getDescription());
                excelUtil.createCell(ExcelConstant.SHEET_INDEX, sheetRowIndex + 1, columnIndex);
                excelUtil.setValueAt(ExcelConstant.SHEET_INDEX, sheetRowIndex + 1, columnIndex, item.getName());
                columnIndex += 1;
            }
        } else {
            excelUtil.createCell(ExcelConstant.SHEET_INDEX, sheetRowIndex + 1, columnIndex - 1);
            excelUtil.setValueAt(ExcelConstant.SHEET_INDEX, sheetRowIndex + 1, columnIndex - 1, "||");
            Map<String, String> returnMap = (Map) param;
            for (Map.Entry<String, String> entry : returnMap.entrySet()) {
                String k = entry.getKey();
                excelUtil.createCell(ExcelConstant.SHEET_INDEX, sheetRowIndex + 1, columnIndex);
                excelUtil.setValueAt(ExcelConstant.SHEET_INDEX, sheetRowIndex + 1, columnIndex, k);
                columnIndex += 1;
            }
        }
        return columnIndex;
    }

    private String buildUseCaseApi(MsHTTPSamplerProxy proxy) {
        StringBuilder stringBuilder = new StringBuilder();
        if (!StringUtils.isEmpty(swaggerLabel.get(ExcelConstant.SHEET_INDEX - 1))) {
            stringBuilder.append("[").append(swaggerLabel.get(ExcelConstant.SHEET_INDEX - 1)).append("]");
            stringBuilder.append(" ");
        }
        stringBuilder.append(proxy.getMethod());
        stringBuilder.append(" ");
        stringBuilder.append(proxy.getPath());
        return stringBuilder.toString();
    }

    private void doGenerateUseCase(ExcelUtil excelUtil) {
        sheetRowIndex += 2;
    }
}
