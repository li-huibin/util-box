package com.lhb.github.io.utilbox.interceptor;

import com.lhb.github.io.utilbox.annotation.Sensitive;
import com.lhb.github.io.utilbox.annotation.SensitiveWord;
import com.lhb.github.io.utilbox.handler.sensitiveWord.SensitiveWordHandler;
import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * 敏感词注解拦截器，拦截方法
 * 如果没有在{@link SensitiveWord}中配置忽略路径，这里会调用{@link SensitiveWordHandler}的getReplaceChars(char, int)
 * 方法将敏感词替换成{@link SensitiveWord}中replacement属性配置的字符
 *
 * @author lihuibin
 */
public class SensitiveAnnotationInterceptor implements MethodInterceptor, Advice {
    private SensitiveWordHandler sensitiveWordHandler;

    public void setSensitiveWordHandler(SensitiveWordHandler sensitiveWordHandler) {
        this.sensitiveWordHandler = sensitiveWordHandler;
    }

    private boolean ignoreSensitive(Method method, String[] ignoreApi) {
        List<String> ignoreApis = Arrays.asList(ignoreApi);
        RequestMapping requestMapping = method.getDeclaredAnnotation(RequestMapping.class);
        String requestUrl = "";
        if (requestMapping != null) {
            if (requestMapping.value().length > 0) {
                requestUrl += requestMapping.value()[0];
            }
        }
        if (method.getDeclaredAnnotation(GetMapping.class) != null) {
            String[] value = method.getDeclaredAnnotation(GetMapping.class).value();
            requestUrl += value.length > 0 ? value[0] : "";
        } else if (method.getDeclaredAnnotation(PostMapping.class) != null) {
            String[] value = method.getDeclaredAnnotation(PostMapping.class).value();
            requestUrl += value.length > 0 ? value[0] : "";
        } else if (method.getDeclaredAnnotation(RequestMapping.class) != null) {
            String[] value = method.getDeclaredAnnotation(RequestMapping.class).value();
            requestUrl += value.length > 0 ? value[0] : "";
        }
        if (ignoreApis.contains(requestUrl)) {
            return true;
        }
        return false;
    }

    private boolean isSensitiveClass(Object data) {
        List dataList = (List) data;
        if (dataList.isEmpty()) {
            return false;
        }
        for (Object o : dataList) {
            Sensitive sensitive = o.getClass().getDeclaredAnnotation(Sensitive.class);
            if (sensitive == null) {
                return false;
            }
        }
        return true;
    }

    private void replaceSensitiveWordHandle(Method method, Object data) {
        try {
            Sensitive sensitive = data.getClass().getDeclaredAnnotation(Sensitive.class);
            if (sensitive != null) {
                Field[] fields = data.getClass().getDeclaredFields();
                for (Field field : fields) {
                    SensitiveWord sensitiveWord = field.getDeclaredAnnotation(SensitiveWord.class);
                    if (sensitiveWord != null) {
                        if (!ignoreSensitive(method, sensitiveWord.ignoreApis())) {
                            char[] fieldNames = field.getName().toCharArray();
                            fieldNames[0] = Character.toUpperCase(fieldNames[0]);
                            Method getMethod = data.getClass().getMethod("get" + String.valueOf(fieldNames));
                            String context = (String) getMethod.invoke(data, null);
                            String replaceSensitiveWord = sensitiveWordHandler.replaceSensitiveWord(context);
                            Method setMethod = data.getClass().getMethod("set" + String.valueOf(fieldNames), String.class);
                            setMethod.invoke(data, replaceSensitiveWord);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Object proceed = invocation.proceed();
        Method getDataMethod = proceed.getClass().getDeclaredMethod("getData", null);
        Object data = getDataMethod.invoke(proceed, null);
        if (data == null) {
            return proceed;
        }
        if (data instanceof List) {
            List dataList = (List) data;
            if (!isSensitiveClass(data)) {
                return proceed;
            }
            for (Object o : dataList) {
                replaceSensitiveWordHandle(invocation.getMethod(), o);
            }

        } else {
            replaceSensitiveWordHandle(invocation.getMethod(), data);
        }
        return proceed;
    }
}
