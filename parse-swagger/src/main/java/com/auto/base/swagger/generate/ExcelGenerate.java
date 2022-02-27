package com.auto.base.swagger.generate;

import org.apache.poi.ss.usermodel.Workbook;

/**
 * 生成模板接口
 * @author zby
 * @date 2022/2/27 1:44
 */
public interface ExcelGenerate {

    public Workbook GenerateMata(ExcelMata mata);

    public Workbook GenerateSheet();
}
