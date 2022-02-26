package com.auto.base.swagger.request;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;
import com.auto.base.swagger.commons.utilss.ScriptEngineUtils;
import com.auto.base.swagger.dto.Body;
import com.auto.base.swagger.dto.KeyValue;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.http.control.Header;
import org.apache.jmeter.protocol.http.control.HeaderManager;
import org.apache.jmeter.protocol.http.util.HTTPArgument;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.collections.HashTree;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@Data
@EqualsAndHashCode(callSuper = false)
@JSONType(typeName = "HTTPSamplerProxy")
public class MsHTTPSamplerProxy {
    private String type = "HTTPSamplerProxy";
    private String clazzName = "com.auto.base.swagger.request.MsHTTPSamplerProxy";

    /**
     * 请求名一般为 接口总结
     */
    private String name;

    /**
     * 请求的uuid
     */
    private String id;

    @JSONField(ordinal = 20)
    private String protocol;

    @JSONField(ordinal = 21)
    private String domain;

    @JSONField(ordinal = 22)
    private String port;

    @JSONField(ordinal = 23)
    private String method;

    @JSONField(ordinal = 24)
    private String path;

    @JSONField(ordinal = 25)
    private String connectTimeout;

    @JSONField(ordinal = 26)
    private String responseTimeout;

    /**
     * 请求头参数包含cookie参数
     */
    @JSONField(ordinal = 27)
    private List<KeyValue> headers;

    /**
     * body参数
     */
    @JSONField(ordinal = 28)
    private Body body;

    /**
     * 路径参数
     */
    @JSONField(ordinal = 29)
    private List<KeyValue> rest;

    @JSONField(ordinal = 30)
    private String url;

    @JSONField(ordinal = 31)
    private boolean followRedirects;

    @JSONField(ordinal = 32)
    private boolean doMultipartPost;

    @JSONField(ordinal = 33)
    private String useEnvironment;

    /**
     * query参数
     */
    @JSONField(ordinal = 34)
    private List<KeyValue> arguments;

    @JSONField(ordinal = 37)
    private Boolean isRefEnvironment;

    @JSONField(ordinal = 38)
    private String alias;

    @JSONField(ordinal = 39)
    private boolean customizeReq;

    private String refType;

//    private void setRefElement() {
//        try {
//            ApiDefinitionService apiDefinitionService = CommonBeanFactory.getBean(ApiDefinitionService.class);
//            ObjectMapper mapper = new ObjectMapper();
//            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//            MsHTTPSamplerProxy proxy = null;
//            if (StringUtils.equals(this.getRefType(), "CASE")) {
//                ApiTestCaseService apiTestCaseService = CommonBeanFactory.getBean(ApiTestCaseService.class);
//                ApiTestCaseWithBLOBs bloBs = apiTestCaseService.get(this.getId());
//                if (bloBs != null) {
//                    this.setProjectId(bloBs.getProjectId());
//                    JSONObject element = JSON.parseObject(bloBs.getRequest());
//                    ElementUtil.dataFormatting(element);
//                    proxy = mapper.readValue(element.toJSONString(), new TypeReference<MsHTTPSamplerProxy>() {
//                    });
//                    this.setName(bloBs.getName());
//                }
//            } else {
//                ApiDefinitionWithBLOBs apiDefinition = apiDefinitionService.getBLOBs(this.getId());
//                if (apiDefinition != null) {
//                    this.setName(apiDefinition.getName());
//                    this.setProjectId(apiDefinition.getProjectId());
//                    proxy = mapper.readValue(apiDefinition.getRequest(), new TypeReference<MsHTTPSamplerProxy>() {
//                    });
//                }
//            }
//            if (proxy != null) {
//                this.setHashTree(proxy.getHashTree());
//                this.setMethod(proxy.getMethod());
//                this.setBody(proxy.getBody());
//                this.setRest(proxy.getRest());
//                this.setArguments(proxy.getArguments());
//                this.setHeaders(proxy.getHeaders());
//            }
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            LogUtil.error(ex);
//        }
//    }






    /**
     * 自定义请求如果是完整url时不拼接mock信息
     *
     * @param url
     * @return
     */
    private boolean isCustomizeReqCompleteUrl(String url) {
        if (isCustomizeReq() && (url.startsWith("http://") || url.startsWith("https://"))) {
            return true;
        }
        return false;
    }

    // 兼容旧数据
//    private void compatible(ParameterConfig config) {
//        if (this.isCustomizeReq() && this.isRefEnvironment == null) {
//            if (StringUtils.isNotBlank(this.url)) {
//                this.isRefEnvironment = false;
//            } else {
//                this.isRefEnvironment = true;
//            }
//        }
//
//        // 数据兼容处理
//        if (config.getConfig() != null && StringUtils.isNotEmpty(this.getProjectId()) && config.getConfig().containsKey(this.getProjectId())) {
//            // 1.8 之后 当前正常数据
//        } else if (config.getConfig() != null && config.getConfig().containsKey(getParentProjectId())) {
//            // 1.8 前后 混合数据
//            this.setProjectId(getParentProjectId());
//        } else {
//            // 1.8 之前 数据
//            if (config.getConfig() != null) {
//                if (!config.getConfig().containsKey(RunModeConstants.HIS_PRO_ID.toString())) {
//                    // 测试计划执行
//                    Iterator<String> it = config.getConfig().keySet().iterator();
//                    if (it.hasNext()) {
//                        this.setProjectId(it.next());
//                    }
//                } else {
//                    this.setProjectId(RunModeConstants.HIS_PRO_ID.toString());
//                }
//            }
//        }
//    }

//    private boolean isUrl() {
//        // 自定义字段没有引用环境则非url
//        if (this.isCustomizeReq()) {
//            if (this.isRefEnvironment) {
//                return false;
//            }
//            return true;
//        }
//        if (StringUtils.isNotEmpty(this.getUrl()) && ElementUtil.isURL(this.getUrl())) {
//            return true;
//        }
//        return false;
//    }

    private boolean isVariable(String path, String value) {
        Pattern p = Pattern.compile("(\\$\\{)([\\w]+)(\\})");
        Matcher m = p.matcher(path);
        while (m.find()) {
            String group = m.group(2);
            if (group.equals(value)) {
                return true;
            }
        }
        return false;
    }

    private String getRestParameters(String path) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(path);
        stringBuffer.append("/");
        Map<String, String> keyValueMap = new HashMap<>();
        this.getRest().stream().filter(KeyValue::isEnable).filter(KeyValue::isValid).filter(KeyValue::valueIsNotEmpty).forEach(keyValue ->
                keyValueMap.put(keyValue.getName(), keyValue.getValue() != null && keyValue.getValue().startsWith("@") ?
                        ScriptEngineUtils.buildFunctionCallString(keyValue.getValue()) : keyValue.getValue())
        );
        try {
            Pattern p = Pattern.compile("(\\{)([\\w]+)(\\})");
            Matcher m = p.matcher(path);
            while (m.find()) {
                String group = m.group(2);
                if (!isVariable(path, group) && keyValueMap.containsKey(group)) {
                    path = path.replace("{" + group + "}", keyValueMap.get(group));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return path;
    }

    private String getPostQueryParameters(String path) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(path);
        stringBuffer.append("?");
        this.getArguments().stream().filter(KeyValue::isEnable).filter(KeyValue::isValid).forEach(keyValue -> {
            stringBuffer.append(keyValue.getName());
            if (keyValue.getValue() != null) {
                stringBuffer.append("=").append(keyValue.getValue().startsWith("@") ?
                        ScriptEngineUtils.buildFunctionCallString(keyValue.getValue()) : keyValue.getValue());
            }
            stringBuffer.append("&");
        });
        return stringBuffer.substring(0, stringBuffer.length() - 1);
    }

    private Arguments httpArguments(List<KeyValue> list) {
        Arguments arguments = new Arguments();
        list.stream().filter(KeyValue::isValid).filter(KeyValue::isEnable).forEach(keyValue -> {
                    HTTPArgument httpArgument = new HTTPArgument(keyValue.getName(), StringUtils.isNotEmpty(keyValue.getValue()) && keyValue.getValue().startsWith("@") ? ScriptEngineUtils.buildFunctionCallString(keyValue.getValue()) : keyValue.getValue());
                    if (keyValue.getValue() == null) {
                        httpArgument.setValue("");
                    }
                    httpArgument.setAlwaysEncoded(keyValue.isEncode());
                    if (StringUtils.isNotBlank(keyValue.getContentType())) {
                        httpArgument.setContentType(keyValue.getContentType());
                    }
                    arguments.addArgument(httpArgument);
                }
        );
        return arguments;
    }

    public void setHeader(HashTree tree, List<KeyValue> headers) {
        // 合并header
        HeaderManager headerManager = new HeaderManager();
        headerManager.setEnabled(true);
        headerManager.setName(StringUtils.isNotEmpty(this.getName()) ? this.getName() + "HeaderManager" : "HeaderManager");
        headerManager.setProperty(TestElement.TEST_CLASS, HeaderManager.class.getName());
        headerManager.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("HeaderPanel"));
        boolean isAdd = true;
        for (Object key : tree.keySet()) {
            if (key instanceof HeaderManager) {
                headerManager = (HeaderManager) key;
                isAdd = false;
            }
        }
        //  header 也支持 mock 参数
        List<KeyValue> keyValues = headers.stream().filter(KeyValue::isValid).filter(KeyValue::isEnable).collect(Collectors.toList());
        for (KeyValue keyValue : keyValues) {
            boolean hasHead = false;
            //检查是否已经有重名的Head。如果Header重复会导致执行报错
            if (headerManager.getHeaders() != null) {
                for (int i = 0; i < headerManager.getHeaders().size(); i++) {
                    Header header = headerManager.getHeader(i);
                    String headName = header.getName();
                    if (StringUtils.equals(headName, keyValue.getName())) {
                        hasHead = true;
                        break;
                    }
                }
            }

            if (!hasHead) {
                headerManager.add(new Header(keyValue.getName(), ScriptEngineUtils.buildFunctionCallString(keyValue.getValue())));
            }
        }
        if (headerManager.getHeaders().size() > 0 && isAdd) {
            tree.add(headerManager);
        }
    }

    /**
     * 按照环境规则匹配环境
     *
     * @param parameterConfig
     * @return
     */
//    private HttpConfig matchConfig(ParameterConfig parameterConfig) {
//        HttpConfig httpConfig = parameterConfig.getConfig().get(this.getProjectId()).getHttpConfig();
//        boolean isNext = true;
//        if (CollectionUtils.isNotEmpty(httpConfig.getConditions())) {
//            for (HttpConfigCondition item : httpConfig.getConditions()) {
//                if (item.getType().equals(ConditionType.PATH.name())) {
//                    HttpConfig config = httpConfig.getPathCondition(this.getPath(), item);
//                    if (config != null) {
//                        isNext = false;
//                        httpConfig = config;
//                        break;
//                    }
//                } else if (item.getType().equals(ConditionType.MODULE.name())) {
//                    ApiDefinition apiDefinition = null;
//                    ApiDefinitionService apiDefinitionService = CommonBeanFactory.getBean(ApiDefinitionService.class);
//                    ApiTestCaseService apiTestCaseService = CommonBeanFactory.getBean(ApiTestCaseService.class);
//                    if (StringUtils.isNotEmpty(this.getRefType()) && this.getRefType().equals("CASE")) {
//                        ApiTestCaseWithBLOBs caseWithBLOBs = apiTestCaseService.get(this.getId());
//                        if (caseWithBLOBs != null) {
//                            apiDefinition = apiDefinitionService.get(caseWithBLOBs.getApiDefinitionId());
//                        }
//                    } else {
//                        apiDefinition = apiDefinitionService.get(this.getId());
//                        if (apiDefinition == null) {
//                            ApiTestCaseWithBLOBs apiTestCaseWithBLOBs = apiTestCaseService.get(this.getId());
//                            if (apiTestCaseWithBLOBs == null) {
//                                apiTestCaseWithBLOBs = apiTestCaseService.get(this.getName());
//                            }
//                            if (apiTestCaseWithBLOBs != null) {
//                                apiDefinition = apiDefinitionService.get(apiTestCaseWithBLOBs.getApiDefinitionId());
//                            } else {
//                                TestPlanApiCaseService testPlanApiCaseService = CommonBeanFactory.getBean(TestPlanApiCaseService.class);
//                                TestPlanApiCase testPlanApiCase = testPlanApiCaseService.getById(this.getId());
//                                if (testPlanApiCase != null) {
//                                    ApiTestCaseWithBLOBs caseWithBLOBs = apiTestCaseService.get(testPlanApiCase.getApiCaseId());
//                                    if (caseWithBLOBs != null) {
//                                        apiDefinition = apiDefinitionService.get(caseWithBLOBs.getApiDefinitionId());
//                                    }
//                                }
//                            }
//                        }
//                    }
//                    if (apiDefinition != null) {
//                        HttpConfig config = httpConfig.getModuleCondition(apiDefinition.getModuleId(), item);
//                        if (config != null) {
//                            isNext = false;
//                            httpConfig = config;
//                            break;
//                        }
//                    }
//                }
//            }
//            if (isNext) {
//                for (HttpConfigCondition item : httpConfig.getConditions()) {
//                    if (item.getType().equals(ConditionType.NONE.name())) {
//                        httpConfig = httpConfig.initHttpConfig(item);
//                        break;
//                    }
//                }
//            }
//        }
//        return httpConfig;
//    }

    /**
     * 环境通用变量，这里只适用用接口定义和用例，场景自动化会加到场景中
     */
//    private Arguments getConfigArguments(ParameterConfig config) {
//        Arguments arguments = new Arguments();
//        arguments.setEnabled(true);
//        arguments.setName(StringUtils.isNotEmpty(this.getName()) ? this.getName() : "Arguments");
//        arguments.setProperty(TestElement.TEST_CLASS, Arguments.class.getName());
//        arguments.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("ArgumentsPanel"));
//        // 环境通用变量
//        if (config.isEffective(this.getProjectId()) && config.getConfig().get(this.getProjectId()).getCommonConfig() != null
//                && CollectionUtils.isNotEmpty(config.getConfig().get(this.getProjectId()).getCommonConfig().getVariables())) {
//            config.getConfig().get(this.getProjectId()).getCommonConfig().getVariables().stream().filter(KeyValue::isValid).filter(KeyValue::isEnable).forEach(keyValue ->
//                    arguments.addArgument(keyValue.getName(), keyValue.getValue(), "=")
//            );
//            // 清空变量，防止重复添加
//            config.getConfig().get(this.getProjectId()).getCommonConfig().getVariables().clear();
//        }
//        if (arguments.getArguments() != null && arguments.getArguments().size() > 0) {
//            return arguments;
//        }
//        return null;
//    }

    private void addArguments(HashTree tree, String key, String value) {
        Arguments arguments = new Arguments();
        arguments.setEnabled(true);
        arguments.setName(StringUtils.isNotEmpty(this.getName()) ? this.getName() + "-KeyStoreAlias" : "KeyStoreAlias");
        arguments.setProperty(TestElement.TEST_CLASS, Arguments.class.getName());
        arguments.setProperty(TestElement.GUI_CLASS, SaveService.aliasToClass("ArgumentsPanel"));
        arguments.addArgument(key, value, "=");
        tree.add(arguments);
    }

    private boolean isRest() {
        return this.getRest().stream().filter(KeyValue::isEnable).filter(KeyValue::isValid).toArray().length > 0;
    }

//    public static List<MsHTTPSamplerProxy> findHttpSampleFromHashTree(MsTestElement hashTree) {
//        return ElementUtil.findFromHashTreeByType(hashTree, MsHTTPSamplerProxy.class, null);
//    }
}

