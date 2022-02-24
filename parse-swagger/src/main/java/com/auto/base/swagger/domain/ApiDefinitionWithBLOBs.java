package com.auto.base.swagger.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ApiDefinitionWithBLOBs extends ApiDefinition implements Serializable {
    private String description;

    private String request;

    private String response;

    private String remark;

    private static final long serialVersionUID = 1L;
}
