package com.auto.base.swagger.domain;

import lombok.Data;

import java.io.Serializable;

@Data
public class ApiModule implements Serializable {
    private String id;

    private String projectId;

    private String name;

    private String protocol;

    private String parentId;

    private Integer level;

    private Long createTime;

    private Long updateTime;

    private Double pos;

    private String createUser;

    private static final long serialVersionUID = 1L;
}
