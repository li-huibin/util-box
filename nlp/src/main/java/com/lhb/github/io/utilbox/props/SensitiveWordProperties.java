package com.lhb.github.io.utilbox.props;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 敏感词相关属性
 *
 * @author lihuibin
 */
@ConfigurationProperties(value = "nlp.sensitive-word")
public class SensitiveWordProperties {
    private String replacement = "*";
    private String pointCut = "";

    public String getPointCut() {
        return pointCut;
    }

    public void setPointCut(String pointCut) {
        this.pointCut = pointCut;
    }

    public String getReplacement() {
        return replacement;
    }

    public void setReplacement(String replacement) {
        this.replacement = replacement;
    }
}
