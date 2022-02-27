package com.auto.base.swagger.parse;

import com.auto.base.swagger.domain.ApiDefinitionWithBLOBs;
import com.auto.base.swagger.parse.swagger.SwaggerApi;
import lombok.Data;

import java.util.List;

@Data
public class ApiDefinitionImport {
    private List<ApiDefinitionWithBLOBs> Data;

    private SwaggerApi swaggerApi;
}
