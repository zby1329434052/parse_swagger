package com.auto.base.swagger;

import com.alibaba.fastjson.JSONObject;
import com.auto.base.swagger.domain.ApiDefinitionWithBLOBs;
import com.auto.base.swagger.mock.MockApiUtils;
import com.auto.base.swagger.parse.ApiDefinitionImport;
import com.auto.base.swagger.parse.ApiTestImportRequest;
import com.auto.base.swagger.parse.Swagger2Parser;
import com.auto.base.swagger.request.MsHTTPSamplerProxy;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Map;

//@SpringBootTest
@Slf4j
class ParseSwaggerApplicationTests {

    @Test
    void contextLoads() {
        ApiTestImportRequest apiTestImportRequest = new ApiTestImportRequest();
        apiTestImportRequest.setProjectId("dispatcher");
        apiTestImportRequest.setModuleId("");
        apiTestImportRequest.setType("schedule");
        apiTestImportRequest.setUserId("user1");

        ObjectMapper mapper = new ObjectMapper();
        MsHTTPSamplerProxy proxy;

        Map<String, String> returnMap;


        apiTestImportRequest.setSwaggerUrl("http://camel.dev.yoohoor.com:9003/cms/carrier/v2/api-docs?group=cms-carrier");
//        apiTestImportRequest.setSwaggerUrl("http://camel.dev.yoohoor.com:82/swagger/api.json");
        ApiDefinitionImport result = new Swagger2Parser().parse(null, apiTestImportRequest);
        for (ApiDefinitionWithBLOBs item : result.getData()) {
            //解析方式有问题
//                proxy = mapper.readValue(item.getRequest(), new TypeReference<MsHTTPSamplerProxy>() {});
            proxy = JSONObject.parseObject(item.getRequest(), MsHTTPSamplerProxy.class);
            returnMap = MockApiUtils.getApiResponse(item.getResponse());


            System.out.println("end");
        }
    }
}
