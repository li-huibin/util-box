package com.lhb.github.io.utilbox.props;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.ResourceUtils;

import java.io.Serializable;

/**
 * 自然语言处理属性
 *
 * @author lihuibin
 */
@ConfigurationProperties(prefix = "nlp")
public class NlpProperties implements Serializable {
    private String baseDir = ResourceUtils.CLASSPATH_URL_PREFIX + "/nlp";
    private String postFixed = "/*.txt";

    public String getBaseDir() {
        return baseDir;
    }

    public void setBaseDir(String baseDir) {
        this.baseDir = baseDir;
    }

    public String getPostFixed() {
        return postFixed;
    }

    public void setPostFixed(String postFixed) {
        this.postFixed = postFixed;
    }
}
