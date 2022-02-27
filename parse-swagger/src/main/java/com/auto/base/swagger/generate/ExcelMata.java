package com.auto.base.swagger.generate;

import com.auto.base.swagger.generate.base.Mata;
import javafx.util.Pair;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 自动化测试用力mata页元素
 * @author zby
 * @date 2022/2/27 1:24
 */
@Data
public class ExcelMata {

    @NotNull
    private List<SwaggerMata> swaggerMata;

    /**
     * mysql配置
     */
    @NotNull
    private List<DbMata> dbMata;

    /**
     * redis配置
     */
    private List<RedisMata> redisMata;

    /**
     * set 全局标签 k,v
     */
    private List<SetMata> setMata;

    @Data
    public static class SwaggerMata extends Mata {
        /**
         * swaggerLabel
         */
        @NotNull
        private String swaggerLabel;

        /**
         * swaggerJsonUrl
         */
        @NotNull
        private String swaggerJsonUrl;

        /**
         * baseUrl
         */
        @NotNull
        private String baseUrl;

        /**
         * dbMap [{"k":",,,","v":",,,"}]
         */
        @NotNull
        private List<Pair<String, String>> dbList;
    }

    @Data
    public static class DbMata extends Mata {
        /**
         * mysql标签
         */
        @NotNull
        private String dbLabel;

        /**
         * mysql host
         */
        @NotNull
        private String dbHost;

        /**
         * 端口号
         */
        @NotNull
        private String dbPort;

        /**
         * mysql用户名
         */
        @NotNull
        private String dbUsername;

        /**
         * mysql密码
         */
        @NotNull
        private String dbPassword;
   }

   @Data
    public static class RedisMata extends Mata {
        /**
         * redis标签
         */
        @NotNull
        private String redisLabel;

        /**
         * redis host
         */
        @NotNull
        private String redisHost;

        /**
         * 端口号
         */
        @NotNull
        private String redisPort;

        /**
         * redis密码
         */
        private String redisPassword;

        /**
         * redis 库编号
         */
        @NotNull
        private String redisNo;
    }

   @Data
    public static class SetMata extends Mata {
        /**
         * setKey
         */
        @NotNull
        private String setKey;

        /**
         * setValue
         */
        @NotNull
        private String setValue;
    }

}
