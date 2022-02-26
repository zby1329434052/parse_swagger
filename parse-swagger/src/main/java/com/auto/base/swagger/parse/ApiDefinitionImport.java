package com.auto.base.swagger.parse;

import com.auto.base.swagger.domain.ApiDefinitionWithBLOBs;
import lombok.Data;

import java.util.List;

@Data
public class ApiDefinitionImport {
    private List<ApiDefinitionWithBLOBs> Data;

}
