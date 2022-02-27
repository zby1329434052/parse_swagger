package com.auto.base.swagger.generate.xlsx;

import com.auto.base.swagger.generate.ExcelGenerate;
import com.auto.base.swagger.generate.ExcelMata;
import com.auto.base.swagger.generate.base.XlsExcel;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * xls模板类
 * @author zby
 * @date 2022/2/27 1:42
 */
public class XlsxGenerate extends XlsExcel implements ExcelGenerate {

    @Override
    public Workbook GenerateMata(ExcelMata mata) {
        return null;
    }

    @Override
    public Workbook GenerateSheet() {
        return null;
    }
}
