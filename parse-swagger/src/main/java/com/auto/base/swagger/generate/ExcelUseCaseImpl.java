package com.auto.base.swagger.generate;

public class ExcelUseCaseImpl implements ExcelUseCase {

    @Override
    public void xlsExcelGenerate(ExcelMata mata, String swaggerJson) {
        //判断是否提供mata数据
        if (mata == null) {
//            xls
        } else {

        }
    }

    @Override
    public void xlsxExcelGenerate(ExcelMata mata, String swaggerJson) {

    }
}
