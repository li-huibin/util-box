package com.lhb.github.io.utilbox.handler.sensitiveWord;

import com.lhb.github.io.utilbox.handler.NLPAbstractHandler;
import com.lhb.github.io.utilbox.props.NlpProperties;
import com.lhb.github.io.utilbox.props.SensitiveWordProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.HashMap;
import java.util.Set;

/**
 * 敏感词处理
 *
 * @author lihuibin
 */
public class SensitiveWordHandler extends NLPAbstractHandler implements ApplicationListener<ContextRefreshedEvent> {
    private SensitiveWordProperties sensitiveWord;

    public SensitiveWordHandler(SensitiveWordProperties sensitiveWord, NlpProperties nlpProperties) {
        this.sensitiveWord = sensitiveWord;
        super.setNlpProperties(nlpProperties);
    }

    /**
     * dfa敏感词库
     */
    private HashMap dfaWordMap;
    /**
     * 本地词库
     */
    private Set<String> wordModelSet;

    /**
     * 文本中是否包含敏感词
     *
     * @param text      文本
     * @param matchType 敏感词匹配类型 参考{@link NLPAbstractHandler}类中的{@link MATCH_TYPE}变量
     * @return 包含返回true，否则返回false
     */
    public boolean contains(String text, Enum matchType) {
        return contains(dfaWordMap, text, matchType);
    }

    /**
     * 文本中是否包含敏感词,默认最小匹配模式
     *
     * @param text 文本
     * @return 包含返回true，否则返回false
     */
    public boolean contains(String text) {
        return contains(dfaWordMap, text, MATCH_TYPE.MINIMUM_MATCH);
    }

    /**
     * 获取文本中的敏感词
     *
     * @param text      文本
     * @param matchType 敏感词匹配类型 参考{@link NLPAbstractHandler}类中的{@link MATCH_TYPE}变量
     * @return 返回敏感词集合
     */
    public Set<String> getSensitiveWordInText(String text, Enum matchType) {
        return getSensitiveWordInText(dfaWordMap, text, matchType);
    }

    /**
     * 获取文本中的敏感词, 默认最小模式匹配
     *
     * @param text 文本
     * @return 返回敏感词集合
     */
    public Set<String> getSensitiveWordInText(String text) {
        return getSensitiveWordInText(dfaWordMap, text, MATCH_TYPE.MINIMUM_MATCH);
    }

    /**
     * 替换文本中的敏感词
     *
     * @param text      文本
     * @param matchType 敏感词匹配类型 参考{@link NLPAbstractHandler}类中的{@link MATCH_TYPE}变量
     * @return 返回替换后的文本
     */
    public String replaceSensitiveWord(String text, Enum matchType) {
        return replaceSensitiveWord(dfaWordMap, text, sensitiveWord.getReplacement().charAt(0), matchType);
    }

    /**
     * 替换文本中的敏感词,默认最小模式匹配
     *
     * @param text 文本
     * @return 返回替换后的文本
     */
    public String replaceSensitiveWord(String text) {
        return replaceSensitiveWord(dfaWordMap, text, sensitiveWord.getReplacement().charAt(0), MATCH_TYPE.MINIMUM_MATCH);
    }


    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        this.wordModelSet = loadSensitiveWordResources();
        this.dfaWordMap = new HashMap(wordModelSet.size());
        init(this.dfaWordMap, wordModelSet);
    }
}
