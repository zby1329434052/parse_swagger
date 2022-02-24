package com.auto.base.swagger.parse;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.auto.base.swagger.commons.constants.SwaggerParameterType;
import com.auto.base.swagger.domain.ApiDefinitionWithBLOBs;
import com.auto.base.swagger.domain.ApiModule;
import com.auto.base.swagger.dto.Body;
import com.auto.base.swagger.dto.KeyValue;
import com.auto.base.swagger.request.MsHTTPSamplerProxy;
import com.auto.base.swagger.request.RequestType;
import com.auto.base.swagger.response.HttpResponse;
import io.swagger.models.ArrayModel;
import io.swagger.models.HttpMethod;
import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.RefModel;
import io.swagger.models.Response;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.AbstractSerializableParameter;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.CookieParameter;
import io.swagger.models.parameters.FormParameter;
import io.swagger.models.parameters.HeaderParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.PathParameter;
import io.swagger.models.parameters.QueryParameter;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.BaseIntegerProperty;
import io.swagger.models.properties.DecimalProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import io.swagger.parser.SwaggerParser;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class Swagger2Parser extends SwaggerAbstractParser {

    private Map<String, Model> definitions = null;

    @Override
    public ApiDefinitionImport parse(InputStream source, ApiTestImportRequest request) {
        Swagger swagger;
        String sourceStr = "";
        if (StringUtils.isNotBlank(request.getSwaggerUrl())) {  //  使用 url 导入 swagger
            swagger = new SwaggerParser().read(request.getSwaggerUrl());
        } else {
            sourceStr = getApiTestStr(source);  //  导入的二进制文件转换为 String
            swagger = new SwaggerParser().readWithInfo(sourceStr).getSwagger();
        }

        if (swagger == null || swagger.getSwagger() == null) {  //  不是 2.0 版本，则尝试转换 3.0
            Swagger3Parser swagger3Parser = new Swagger3Parser();
            return swagger3Parser.parse(sourceStr, request);
        }

        ApiDefinitionImport definitionImport = new ApiDefinitionImport();
        this.projectId = request.getProjectId();
        definitionImport.setData(parseRequests(swagger, request));
        return definitionImport;
    }

    private List<ApiDefinitionWithBLOBs> parseRequests(Swagger swagger, ApiTestImportRequest importRequest) {
        Map<String, Path> paths = swagger.getPaths();
        Set<String> pathNames = paths.keySet();

        this.definitions = swagger.getDefinitions();

        List<ApiDefinitionWithBLOBs> results = new ArrayList<>();

        ApiModule selectModule = null;
        String selectModulePath = null;
        if (StringUtils.isNotBlank(importRequest.getModuleId())) {
            selectModule = ApiDefinitionImportUtil.getSelectModule(importRequest.getModuleId());
            if (selectModule != null) {
                selectModulePath = ApiDefinitionImportUtil.getSelectModulePath(selectModule.getName(), selectModule.getParentId());
            }
        }

        String basePath = swagger.getBasePath();
        for (String pathName : pathNames) {
            Path path = paths.get(pathName);
            Map<HttpMethod, Operation> operationMap = path.getOperationMap();
            Set<HttpMethod> httpMethods = operationMap.keySet();
            for (HttpMethod method : httpMethods) {
                Operation operation = operationMap.get(method);
                MsHTTPSamplerProxy request = buildRequest(operation, pathName, method.name());
                ApiDefinitionWithBLOBs apiDefinition = buildApiDefinition(request.getId(), operation, pathName, method.name(),importRequest);
                parseParameters(operation, request);
                addBodyHeader(request);
                if (StringUtils.isNotBlank(basePath)) {
                    String pathStr = (basePath + apiDefinition.getPath()).replaceAll("//","/");
                    apiDefinition.setPath(pathStr);
                    request.setPath(pathStr);
                }
                apiDefinition.setRequest(JSON.toJSONString(request));
                apiDefinition.setResponse(JSON.toJSONString(parseResponse(operation, operation.getResponses())));
                buildModule(selectModule, apiDefinition, operation.getTags(), selectModulePath);
                if (operation.isDeprecated() != null && operation.isDeprecated()) {
                    apiDefinition.setTags("[\"Deleted\"]");
                }
                results.add(apiDefinition);
            }
        }

        this.definitions = null;
        return results;
    }

    private ApiDefinitionWithBLOBs buildApiDefinition(String id, Operation operation, String path, String method,ApiTestImportRequest importRequest) {
        String name = "";
        if (StringUtils.isNotBlank(operation.getSummary())) {
            name = operation.getSummary();
        } else  if (StringUtils.isNotBlank(operation.getOperationId())) {
            name = operation.getOperationId();
        } else {
            name = path;
        }
        return buildApiDefinition(id, name, path, method,importRequest);
    }

    private MsHTTPSamplerProxy buildRequest(Operation operation, String path, String method) {
        String name = "";
        if (StringUtils.isNotBlank(operation.getSummary())) {
            name = operation.getSummary();
        } else {
            name = operation.getOperationId();
        }
        return buildRequest(name, path, method);
    }

    private void parseParameters(Operation operation, MsHTTPSamplerProxy request) {

        List<Parameter> parameters = operation.getParameters();
        request.getBody().setType(getBodyType(operation));

        // todo 路径变量 {xxx} 是否要转换

        for (Parameter parameter : parameters) {
            switch (parameter.getIn()) {
                case SwaggerParameterType.PATH:
                    parsePathParameters(parameter, request.getRest());
                    break;
                case SwaggerParameterType.QUERY:
                    parseQueryParameters(parameter, request.getArguments());
                    break;
                case SwaggerParameterType.FORM_DATA:
                    parseFormDataParameters((FormParameter) parameter, request.getBody());
                    break;
                case SwaggerParameterType.BODY:
                    parseRequestBodyParameters(parameter, request.getBody());
                    break;
                case SwaggerParameterType.HEADER:
                    parseHeaderParameters(parameter, request.getHeaders());
                    break;
                case SwaggerParameterType.COOKIE:
                    parseCookieParameters(parameter, request.getHeaders());
                    break;
//                case SwaggerParameterType.FILE:
//                    parsePathParameters(parameter, request);
//                    break;
            }
        }
    }

    private String getBodyType(Operation operation) {
        if (CollectionUtils.isEmpty(operation.getConsumes())) {
            return Body.JSON;
        }
        String contentType = operation.getConsumes().get(0);
        return getBodyType(contentType);
    }

    private String getResponseBodyType(Operation operation) {
        if (CollectionUtils.isEmpty(operation.getProduces())) {
            return Body.JSON;
        }
        String contentType = operation.getProduces().get(0);
        return getBodyType(contentType);
    }

    private void parsePathParameters(Parameter parameter, List<KeyValue> rests) {
        PathParameter pathParameter = (PathParameter) parameter;
        rests.add(new KeyValue(pathParameter.getName(), getDefaultValue(pathParameter), getDefaultStringValue(parameter.getDescription()), pathParameter.getRequired()));
    }

    private String getDefaultValue(AbstractSerializableParameter parameter) {
        if (parameter.getDefault() != null) {
            return getDefaultStringValue(parameter.getDefault().toString());
        }
        return "";
    }

    private String getDefaultStringValue(String val) {
        return StringUtils.isBlank(val) ? "" : val;
    }

    private void parseCookieParameters(Parameter parameter, List<KeyValue> headers) {
        CookieParameter cookieParameter = (CookieParameter) parameter;
        addCookie(headers, cookieParameter.getName(), getDefaultValue(cookieParameter), getDefaultStringValue(cookieParameter.getDescription()), parameter.getRequired());
    }

    private void parseHeaderParameters(Parameter parameter, List<KeyValue> headers) {
        HeaderParameter headerParameter = (HeaderParameter) parameter;
        addHeader(headers, headerParameter.getName(), getDefaultValue(headerParameter), getDefaultStringValue(headerParameter.getDescription()),
                "", parameter.getRequired());
    }

    private HttpResponse parseResponse(Operation operation, Map<String, Response> responses) {
        HttpResponse msResponse = new HttpResponse();
        msResponse.setBody(new Body());
        msResponse.getBody().setKvs(new ArrayList<>());
        msResponse.setHeaders(new ArrayList<>());
        msResponse.setType(RequestType.HTTP);
        msResponse.getBody().setType(getResponseBodyType(operation));
        // todo 状态码要调整？
        msResponse.setStatusCode(new ArrayList<>());
        if (responses != null && responses.size() > 0) {
            responses.forEach((responseCode, response) -> {
                msResponse.getStatusCode().add(new KeyValue(responseCode, responseCode));
                String body = parseSchema(response.getResponseSchema());
                if (StringUtils.isNotBlank(body)) {
                    msResponse.getBody().setRaw(body);
                }
                parseResponseHeader(response, msResponse.getHeaders());
            });
        }
        return msResponse;
    }

    private void parseResponseHeader(Response response, List<KeyValue> msHeaders) {
        Map<String, Property> headers = response.getHeaders();
        if (headers != null) {
            headers.forEach((k, v) -> {
                msHeaders.add(new KeyValue(k, "", v.getDescription()));
            });
        }
    }

    private void parseRequestBodyParameters(Parameter parameter, Body body) {
        BodyParameter bodyParameter = (BodyParameter) parameter;
        body.setRaw(parseSchema(bodyParameter.getSchema()));
    }

    private String parseSchema(Model schema) {
        // 引用模型
        if (schema instanceof RefModel) {
            String simpleRef = "";
            RefModel refModel = (RefModel) schema;
            String originalRef = refModel.getOriginalRef();
            if (refModel.getOriginalRef().split("/").length > 3) {
                simpleRef = originalRef.replace("#/definitions/", "");
            } else {
                simpleRef = refModel.getSimpleRef();
            }
            Model model = this.definitions.get(simpleRef);
            HashSet<String> refSet = new HashSet<>();
            refSet.add(simpleRef);
            if (model != null) {
                JSONObject bodyParameters = getBodyParameters(model.getProperties(), refSet);
                return bodyParameters.toJSONString();
            }
        } else if (schema instanceof ArrayModel) {
            //模型数组
            ArrayModel arrayModel = (ArrayModel) schema;
            Property items = arrayModel.getItems();
            JSONArray propertyList = new JSONArray();
            if (items instanceof RefProperty) {
                RefProperty refProperty = (RefProperty) items;
                String simpleRef = refProperty.getSimpleRef();
                HashSet<String> refSet = new HashSet<>();
                refSet.add(simpleRef);
                Model model = definitions.get(simpleRef);
                if (model != null) {
                    propertyList.add(getBodyParameters(model.getProperties(), refSet));
                } else {
                    propertyList.add(new JSONObject(true));
                }
            }
            return propertyList.toString();
        } else if (schema instanceof ModelImpl) {
            ModelImpl model = (ModelImpl) schema;
            Map<String, Property> properties = model.getProperties();
            if (model != null && properties != null) {
                JSONObject bodyParameters = getBodyParameters(properties, new HashSet<>());
                return bodyParameters.toJSONString();
            }
        }
        return "";
    }

    private JSONObject getBodyParameters(Map<String, Property> properties, HashSet<String> refSet) {
        JSONObject jsonObject = new JSONObject(true);
        if (properties != null) {
            properties.forEach((key, value) -> {
                if (value instanceof ObjectProperty) {
                    ObjectProperty objectProperty = (ObjectProperty) value;
                    jsonObject.put(key, getBodyParameters(objectProperty.getProperties(), refSet));
                } else if (value instanceof ArrayProperty) {
                    ArrayProperty arrayProperty = (ArrayProperty) value;
                    Property items = arrayProperty.getItems();
                    if (items instanceof RefProperty) {
                        RefProperty refProperty = (RefProperty) items;
                        String simpleRef = refProperty.getSimpleRef();
                        if (refSet.contains(simpleRef)) {
                            //避免嵌套死循环
                            jsonObject.put(key, new JSONArray());
                            return;
                        }
                        refSet.add(simpleRef);
                        Model model = this.definitions.get(simpleRef);
                        JSONArray propertyList = new JSONArray();
                        if (model != null) {
                            propertyList.add(getBodyParameters(model.getProperties(), refSet));
                        } else {
                            propertyList.add(new JSONObject(true));
                        }
                        jsonObject.put(key, propertyList);
                    } else if (items instanceof ObjectProperty) {
                        JSONArray propertyList = new JSONArray();
                        if (items != null) {
                            propertyList.add(getBodyParameters(((ObjectProperty) items).getProperties(), refSet));
                        }
                        jsonObject.put(key, propertyList);
                    }
                    else {
                        jsonObject.put(key, new ArrayList<>());
                    }
                } else if (value instanceof RefProperty) {
                    RefProperty refProperty = (RefProperty) value;
                    String simpleRef = refProperty.getSimpleRef();
                    if (refSet.contains(simpleRef)) {
                        //避免嵌套死循环
                        jsonObject.put(key, new JSONArray());
                        return;
                    }
                    refSet.add(simpleRef);
                    Model model = definitions.get(simpleRef);
                    if (model != null) {
                        jsonObject.put(key, getBodyParameters(model.getProperties(), refSet));
                    }
                } else {
                    jsonObject.put(key, getDefaultValueByPropertyType(value));
                }
            });
        }
        return jsonObject;
    }

    private Object getDefaultValueByPropertyType(Property value) {
        if (value instanceof LongProperty || value instanceof IntegerProperty
                || value instanceof BaseIntegerProperty) {
            return 0;
        } else if (value instanceof FloatProperty || value instanceof DoubleProperty
                || value instanceof DecimalProperty) {
            return 0.0;
        } else {// todo 其他类型?
            return getDefaultStringValue(value.getDescription());
        }
    }

    private void parseFormDataParameters(FormParameter parameter, Body body) {
        List<KeyValue> keyValues = Optional.ofNullable(body.getKvs()).orElse(new ArrayList<>());
        KeyValue kv = new KeyValue(parameter.getName(), getDefaultValue(parameter), getDefaultStringValue(parameter.getDescription()), parameter.getRequired());
        if (StringUtils.equals(parameter.getType(), "file")) {
            kv.setType("file");
        }
        keyValues.add(kv);
        body.setKvs(keyValues);
    }

    private void parseQueryParameters(Parameter parameter, List<KeyValue> arguments) {
        QueryParameter queryParameter = (QueryParameter) parameter;
        arguments.add(new KeyValue(queryParameter.getName(), getDefaultValue(queryParameter), getDefaultStringValue(queryParameter.getDescription()), queryParameter.getRequired()));
    }
}
