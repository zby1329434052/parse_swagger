package com.auto.base.swagger.parse;

import com.auto.base.swagger.commons.constants.ApiImportPlatform;
import org.apache.commons.lang3.StringUtils;

public class ApiDefinitionImportParserFactory {
    public static ApiImportParser getApiImportParser(String platform) {
        if (StringUtils.equals(ApiImportPlatform.Swagger2.name(), platform)) {
            return new Swagger2Parser();
        }
        return null;
    }
}
