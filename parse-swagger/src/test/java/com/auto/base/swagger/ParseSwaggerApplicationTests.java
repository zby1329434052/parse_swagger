package com.auto.base.swagger;

import com.auto.base.swagger.parse.ApiDefinitionImport;
import com.auto.base.swagger.parse.ApiTestImportRequest;
import com.auto.base.swagger.parse.Swagger2Parser;
import org.junit.jupiter.api.Test;

//@SpringBootTest
class ParseSwaggerApplicationTests {

    @Test
    void contextLoads() {
        ApiTestImportRequest apiTestImportRequest = new ApiTestImportRequest();
        apiTestImportRequest.setProjectId("dispatcher");
        apiTestImportRequest.setModuleId("");
        apiTestImportRequest.setType("schedule");
        apiTestImportRequest.setUserId("user1");

        apiTestImportRequest.setSwaggerUrl("http://camel.dev.yoohoor.com:82/swagger/api.json");
        ApiDefinitionImport result = new Swagger2Parser().parse(null, apiTestImportRequest);



    }

}
