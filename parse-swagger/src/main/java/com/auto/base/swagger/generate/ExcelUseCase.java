package com.auto.base.swagger.generate;

public interface ExcelUseCase {

    /**
     * 旧版excel模板生成
     */
    public void xlsExcelGenerate(ExcelMata mata, String swaggerJson);

    /**
     * 新版excel模板生成
     */
    public void xlsxExcelGenerate(ExcelMata mata, String swaggerJson);

}
