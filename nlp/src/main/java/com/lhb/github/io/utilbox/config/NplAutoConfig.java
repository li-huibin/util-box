package com.lhb.github.io.utilbox.config;

import com.lhb.github.io.utilbox.handler.sensitiveWord.SensitiveWordHandler;
import com.lhb.github.io.utilbox.interceptor.SensitiveAnnotationInterceptor;
import com.lhb.github.io.utilbox.props.NlpProperties;
import com.lhb.github.io.utilbox.props.SensitiveWordProperties;
import org.springframework.aop.aspectj.AspectJExpressionPointcutAdvisor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@ConditionalOnWebApplication
@EnableConfigurationProperties(value = {NlpProperties.class, SensitiveWordProperties.class})
public class NplAutoConfig {
    @Autowired
    NlpProperties nlpProperties;
    @Autowired
    SensitiveWordProperties sensitiveWordProperties;

    @Bean
    @ConditionalOnProperty(name = "nlp.sensitive-word.point-cut")
    public AspectJExpressionPointcutAdvisor advice() {
        AspectJExpressionPointcutAdvisor advisor = new AspectJExpressionPointcutAdvisor();
        advisor.setExpression(sensitiveWordProperties.getPointCut());
        advisor.setAdvice(interceptor());
        return advisor;
    }

    @Bean
    public SensitiveAnnotationInterceptor interceptor() {
        SensitiveAnnotationInterceptor interceptor = new SensitiveAnnotationInterceptor();
        interceptor.setSensitiveWordHandler(sensitiveWordHandler());
        return interceptor;
    }

    @Bean
    public SensitiveWordHandler sensitiveWordHandler() {
        return new SensitiveWordHandler(sensitiveWordProperties,nlpProperties);
    }
}
