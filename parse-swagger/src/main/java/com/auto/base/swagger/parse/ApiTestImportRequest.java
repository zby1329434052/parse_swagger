package com.auto.base.swagger.parse;

import lombok.Data;

@Data
public class ApiTestImportRequest {

    private String swaggerUrl;

    private String projectId;

    private String moduleId;

    private String userId;
    //调用类型
    private String type;

}
