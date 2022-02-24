package com.auto.base.swagger.response;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.annotation.JSONType;
import com.auto.base.swagger.dto.Body;
import com.auto.base.swagger.dto.KeyValue;
import com.auto.base.swagger.request.RequestType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@JSONType(typeName = RequestType.HTTP)
public class HttpResponse extends Response {
    // type 必须放最前面，以便能够转换正确的类
    private String type = RequestType.HTTP;
    @JSONField(ordinal = 1)
    private List<KeyValue> headers;
    @JSONField(ordinal = 2)
    private List<KeyValue> statusCode;
    @JSONField(ordinal = 3)
    private Body body;

}
