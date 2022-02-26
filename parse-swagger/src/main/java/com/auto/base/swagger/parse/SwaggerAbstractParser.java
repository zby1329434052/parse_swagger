package com.auto.base.swagger.parse;

public abstract class SwaggerAbstractParser extends ApiImportAbstractParser<ApiDefinitionImport> {

//    protected void buildModule(ApiModule parentModule, ApiDefinitionWithBLOBs apiDefinition,
//                               List<String> tags, String selectModulePath) {
//        if (CollectionUtils.isEmpty(tags)) {
//            if (parentModule != null) {
//                apiDefinition.setModuleId(parentModule.getId());
//                apiDefinition.setModulePath(selectModulePath);
//            }
//        } else {
//            tags.forEach(tag -> {
//                ApiModule module = ApiDefinitionImportUtil.buildModule(parentModule, tag, this.projectId);
//                apiDefinition.setModuleId(module.getId());
//                if (StringUtils.isNotBlank(selectModulePath)) {
//                    apiDefinition.setModulePath(selectModulePath + "/" + tag);
//                } else {
//                    apiDefinition.setModulePath("/" + tag);
//                }
//            });
//        }
//    }

}
