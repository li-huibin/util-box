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


/**
 * 全局配置
 *
 * @author lihuibin
 */
@Configuration
@ConditionalOnWebApplication
@EnableConfigurationProperties(value = {NlpProperties.class, SensitiveWordProperties.class})
public class NplAutoConfig {
    @Autowired
    NlpProperties nlpProperties;
    @Autowired
    SensitiveWordProperties sensitiveWordProperties;

    /**
     * 设置切点表达式和切点通知处理器
     *
     * @return 返回Spring AOP Advisor的包装类{@link AspectJExpressionPointcutAdvisor}
     */
    @Bean
    @ConditionalOnProperty(name = "nlp.sensitive-word.point-cut")
    public AspectJExpressionPointcutAdvisor advice() {
        AspectJExpressionPointcutAdvisor advisor = new AspectJExpressionPointcutAdvisor();
        advisor.setExpression(sensitiveWordProperties.getPointCut());
        advisor.setAdvice(interceptor());
        return advisor;
    }

    /**
     * 初始化注解拦截器
     *
     * @return 返回注解处理类 {@link SensitiveAnnotationInterceptor}
     */
    @Bean
    public SensitiveAnnotationInterceptor interceptor() {
        SensitiveAnnotationInterceptor interceptor = new SensitiveAnnotationInterceptor();
        interceptor.setSensitiveWordHandler(sensitiveWordHandler());
        return interceptor;
    }

    /**
     * 初始化敏感词处理类，敏感词处理逻辑在此处理类 {@link SensitiveWordHandler}中实现
     *
     * @return 返回敏感词处理类 {@link SensitiveWordHandler}
     */
    @Bean
    public SensitiveWordHandler sensitiveWordHandler() {
        return new SensitiveWordHandler(sensitiveWordProperties, nlpProperties);
    }
}
