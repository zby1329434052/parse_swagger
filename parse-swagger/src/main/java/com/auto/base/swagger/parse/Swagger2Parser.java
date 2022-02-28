package com.auto.base.swagger.parse;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.auto.base.swagger.commons.constants.SwaggerParameterType;
import com.auto.base.swagger.commons.utils.BeanUtils;
import com.auto.base.swagger.domain.ApiDefinitionWithBLOBs;
import com.auto.base.swagger.dto.Body;
import com.auto.base.swagger.dto.KeyValue;
import com.auto.base.swagger.parse.swagger.SwaggerApi;
import com.auto.base.swagger.parse.swagger.SwaggerInfo;
import com.auto.base.swagger.parse.swagger.SwaggerTag;
import com.auto.base.swagger.request.MsHTTPSamplerProxy;
import com.auto.base.swagger.request.RequestType;
import com.auto.base.swagger.response.HttpResponse;
import io.swagger.models.ArrayModel;
import io.swagger.models.HttpMethod;
import io.swagger.models.Info;
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
import io.swagger.models.properties.DoubleProperty;
import io.swagger.models.properties.FloatProperty;
import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.LongProperty;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import io.swagger.parser.SwaggerParser;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
//            Swagger3Parser swagger3Parser = new Swagger3Parser();
//            return swagger3Parser.parse(sourceStr, request);
            return null;
        }

        ApiDefinitionImport definitionImport = new ApiDefinitionImport();
        this.projectId = request.getProjectId();
        definitionImport.setData(parseRequests(swagger, request));
        definitionImport.setSwaggerApi(parseSwaggerApi(swagger));
        return definitionImport;
    }

    private SwaggerApi parseSwaggerApi(Swagger swagger) {
        SwaggerApi swaggerApi = new SwaggerApi();
        swaggerApi.setSwagger(swagger.getSwagger());
        swaggerApi.setInfo(parseSwaggerInfo(swagger.getInfo()));
        swaggerApi.setHost(swagger.getHost());
        swaggerApi.setBasePath(swagger.getBasePath());
        if (swagger.getSchemes() != null) {
            swaggerApi.setSchemes(swagger.getSchemes().stream().map(scheme -> scheme.toValue()).collect(Collectors.toList()));
        }
        swaggerApi.setTags(swagger.getTags().stream().map(tag -> {
            SwaggerTag swaggerTag = new SwaggerTag();
            BeanUtils.copyBean(swaggerTag, tag);
            return swaggerTag;
        }).collect(Collectors.toList()));
        swaggerApi.setPaths(JSON.parseObject(JSON.toJSONString(swagger.getPaths())));
        swaggerApi.setDefinitions(JSON.parseObject(JSON.toJSONString(swagger.getDefinitions())));
        return swaggerApi;
    }

    private SwaggerInfo parseSwaggerInfo(Info info) {
        SwaggerInfo swaggerInfo = new SwaggerInfo();
        BeanUtils.copyBean(swaggerInfo, info);
        return swaggerInfo;
    }

    private List<ApiDefinitionWithBLOBs> parseRequests(Swagger swagger, ApiTestImportRequest importRequest) {
        Map<String, Path> paths = swagger.getPaths();
        Set<String> pathNames = paths.keySet();

        this.definitions = swagger.getDefinitions();

        List<ApiDefinitionWithBLOBs> results = new ArrayList<>();

        String basePath = swagger.getBasePath();
        for (String pathName : pathNames) {
            Path path = paths.get(pathName);
            Map<HttpMethod, Operation> operationMap = path.getOperationMap();
            Set<HttpMethod> httpMethods = operationMap.keySet();
            for (HttpMethod method : httpMethods) {
                Operation operation = operationMap.get(method);
                //构建请求
                MsHTTPSamplerProxy request = buildRequest(operation, pathName, method.name());
                //pathName 请求路径 构建请求接口信息 类似接口说明
                ApiDefinitionWithBLOBs apiDefinition = buildApiDefinition(request.getId(), operation, pathName, method.name(),importRequest);
                //request中设置填充各种参数集合
                parseParameters(operation, request);
                // 补充设置请求头
                addBodyHeader(request);
                if (StringUtils.isNotBlank(basePath)) {
                    String pathStr = (basePath + apiDefinition.getPath()).replaceAll("//","/");
                    //api 接口路径拼接
                    apiDefinition.setPath(pathStr);
                    //api 接口路径拼接
                    request.setPath(pathStr);
                }
                apiDefinition.setRequest(JSON.toJSONString(request));
                //解析返回值 设置response
                apiDefinition.setResponse(JSON.toJSONString(parseResponse(operation, operation.getResponses())));
//                buildModule(selectModule, apiDefinition, operation.getTags(), selectModulePath);
                if (operation.isDeprecated() != null && operation.isDeprecated()) {
                    apiDefinition.setTags("[\"Deleted\"]");
                }
                results.add(apiDefinition);
            }
        }

        this.definitions = null;
        return results;
    }

    /**
     * 构建请求接口信息 类似接口说明
     * @param id 路径的请求的id
     * @param operation 路径的操作
     * @param path 路径 含有方法和相对地址
     * @param method 请求方法名
     * @param importRequest 最外层的解析请求参数
     * @return 接口说明信息
     */
    private ApiDefinitionWithBLOBs buildApiDefinition(String id, Operation operation, String path, String method,ApiTestImportRequest importRequest) {
        String name = "";
        if (StringUtils.isNotBlank(operation.getSummary())) {
            name = operation.getSummary();
        } else  if (StringUtils.isNotBlank(operation.getOperationId())) {
            name = operation.getOperationId();
        } else {
            name = path;
        }
        String tagName = operation.getTags().get(0);
        return buildApiDefinition(id, tagName, name, path, method,importRequest);
    }

    /**
     * 构建请求
     * @param operation 路径操作内含参数等
     * @param path 路径
     * @param method 请求方法
     * @return
     */
    private MsHTTPSamplerProxy buildRequest(Operation operation, String path, String method) {
        String name = "";
        if (StringUtils.isNotBlank(operation.getSummary())) {
            name = operation.getSummary();
        } else {
            name = operation.getOperationId();
        }
        return buildRequest(name, path, method);
    }

    /**
     * 解析接口参数存放到对应的集合中 请求体类型
     * @param operation
     * @param request
     */
    private void parseParameters(Operation operation, MsHTTPSamplerProxy request) {

        //获取接口的参数
        List<Parameter> parameters = operation.getParameters();
        //设置请求体的类型 json或formdata等等
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

    /**
     * 解析swagger 的返回格式
     * @param operation 接口操作 富含接口信息
     * @param responses k:返回状态码,v 返回值
     * @return
     */
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
                //解析返回值实体
                String body = parseSchema(response.getResponseSchema());
                if (StringUtils.isNotBlank(body)) {
                    msResponse.getBody().setRaw(body);
                }
                //把返回值参数的headers设置到 HttpResponse里
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

    /**
     * 解析实体模型
     * @param schema swagger模型引用
     * @return
     */
    private String parseSchema(Model schema) {
        // 引用模型
        if (schema instanceof RefModel) {
            //简单引用 引用的实体模型名
            String simpleRef = "";
            //引用模型 模型接口的一种实现 swagger的请求和返回都会使用到
            RefModel refModel = (RefModel) schema;
            //originalRef 完整引用包含前置字段
            String originalRef = refModel.getOriginalRef();
            if (refModel.getOriginalRef().split("/").length > 3) {
                simpleRef = originalRef.replace("#/definitions/", "");
            } else {
                simpleRef = refModel.getSimpleRef();
            }
            //swagger官方解析好的实体模型map 只有一层
            Model model = this.definitions.get(simpleRef);
            //判断嵌套使用
            HashSet<String> refSet = new HashSet<>();
            refSet.add(simpleRef);
            if (model != null) {
                //引用类型 设置body参数  递归解析属性
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

    /**
     * 递归实现 解析引用实体模型  需要看
     * @param properties
     * @param refSet
     * @return
     */
    private JSONObject getBodyParameters(Map<String, Property> properties, HashSet<String> refSet) {
        //ordered设置内部维护linkedHashMap
        JSONObject jsonObject = new JSONObject(true);
        if (properties != null) {
            properties.forEach((key, value) -> {
                //swagger官方封装好的类型
                if (value instanceof ObjectProperty) {
                    ObjectProperty objectProperty = (ObjectProperty) value;
                    //对象参数的对象参数 递归
                    jsonObject.put(key, getBodyParameters(objectProperty.getProperties(), refSet));
                } else if (value instanceof ArrayProperty) {
                    ArrayProperty arrayProperty = (ArrayProperty) value;
                    Property items = arrayProperty.getItems();
                    //数组里的参数类型是否为引用类型
                    if (items instanceof RefProperty) {
                        //数组里有引用参数 判断是否为嵌套
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
                            //解析数组里的引用类型
                            propertyList.add(getBodyParameters(model.getProperties(), refSet));
                        } else {
                            propertyList.add(new JSONObject(true));
                        }
                        //递归 解析
                        jsonObject.put(key, propertyList);
                    } else if (items instanceof ObjectProperty) {
                        //数组里的参数类型是否为对象类型
                        JSONArray propertyList = new JSONArray();
                        if (items != null) {
                            //properties是相同的 作为传参可以方便递归
                            propertyList.add(getBodyParameters(((ObjectProperty) items).getProperties(), refSet));
                        }
                        jsonObject.put(key, propertyList);
                    }
                    else {
                        //不用递归的则设置 数组类型里的值为对象类型
                        jsonObject.put(key, new ArrayList<>());
                    }
                } else if (value instanceof RefProperty) {
                    //这里处理方式和数组内引用类型一样
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
                    //简单类型可以直接设置 并附默认值 默认值很简单 0 0.0 字符串为参数描述 或""
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
