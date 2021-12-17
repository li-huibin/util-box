package com.lhb.github.io.utilbox.handler;

import com.lhb.github.io.utilbox.props.NlpProperties;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.ResourceUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * 自然语言处理抽象类
 *
 * @author lihuibin
 */
public abstract class NLPAbstractHandler implements InitializingBean {
    private Logger log = Logger.getLogger("NLPAbstractHandler");
    private NlpProperties nlpProperties;

    private String nplDefaultFile = "";

    private InputStream[] wordTextInputStreams;

    protected static final String IS_END = "isEnd";

    public void setNlpProperties(NlpProperties nlpProperties) {
        this.nlpProperties = nlpProperties;
    }

    protected enum IS_END_TYPE {
        /**
         * 检索词结束
         */
        ZERO,
        /**
         * 检索词未结束
         */
        ONE
    }

    /**
     * 匹配模式
     */
    protected enum MATCH_TYPE {
        /**
         * 最小匹配规则，如：敏感词库["中国","中国人"]，语句："我是中国人"，匹配结果：我是[中国]人
         */
        MINIMUM_MATCH,
        /**
         * 最大匹配规则，如：敏感词库["中国","中国人"]，语句："我是中国人"，匹配结果：我是[中国人]
         */
        MAXIMUM_MATCH
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Resource[] resources;
        if (nlpProperties.getBaseDir().startsWith(ResourceUtils.CLASSPATH_URL_PREFIX)) {
            nplDefaultFile = nlpProperties.getBaseDir().substring(ResourceUtils.CLASSPATH_URL_PREFIX.length());
            resources = new PathMatchingResourcePatternResolver().getResources(ResourceUtils.CLASSPATH_URL_PREFIX + nplDefaultFile + "/*.txt");
        } else {
            resources = new PathMatchingResourcePatternResolver().getResources(nlpProperties.getBaseDir());
        }
        if (resources.length <= 0) {
            log.warning("词库文件为空，自然语言功能将不起作用");
            return;
        }
        wordTextInputStreams = new InputStream[resources.length];
        for (int i = 0; i < resources.length; i++) {
            log.info(String.format("读取词库文件:%s", URLDecoder.decode(resources[i].getURL().getFile(), "utf-8")));
            wordTextInputStreams[i] = resources[i].getInputStream();
        }
    }

    /**
     * 初始化DFA处理模型
     *
     * @param dfaWordMap
     * @param wordSet
     */
    protected void init(HashMap dfaWordMap, Set<String> wordSet) {
        log.info("开始初始化词库");
        long start = System.currentTimeMillis();
        HashMap currentMap;
        Iterator<String> iterator = wordSet.iterator();
        while (iterator.hasNext()) {
            currentMap = dfaWordMap;
            String currentWord = iterator.next();
            for (int i = 0; i < currentWord.length(); i++) {
                char keyWord = currentWord.charAt(i);
                Object nextMap = currentMap.get(keyWord);
                if (nextMap != null) {
                    // 将map的指针指向nextMap，以便于下次循环使用
                    currentMap = (HashMap) nextMap;
                } else {
                    HashMap<Object, Object> newWord = new HashMap();
                    newWord.put(IS_END, IS_END_TYPE.ZERO);
                    currentMap.put(keyWord, newWord);
                    // 将map指针指向当前新创建的词节点，下次的新词要在这个新词后面添加
                    currentMap = newWord;
                }

                // 如果字符是词的结尾字符，设置结束表示isEnd=1
                // 这里减1是因为执行完本次循环，i会自增，所以这里需要先减1
                if (i == currentWord.length() - 1) {
                    currentMap.put(IS_END, IS_END_TYPE.ONE);
                }
            }
        }
        log.info(String.format("词库初始化完成,一共%d个词,用时%d ms", wordSet.size(), (System.currentTimeMillis() - start)));
    }

    /**
     * 判断文本中是否存在词库中指定的词，存在则返回字符串长度
     *
     * @param dfaWordMap 词库
     * @param text       文本
     * @param beginIndex 文本开始位置
     * @param matchType  匹配模式,参考 {@link MATCH_TYPE}
     * @return 返回敏感词长度
     */
    protected int checkWord(HashMap dfaWordMap, String text, int beginIndex, Enum matchType) {
        // 敏感词长度
        AtomicInteger wordLength = new AtomicInteger(0);
        Map currentMap = dfaWordMap;
        boolean flag = false;
        for (int i = beginIndex; i < text.length(); i++) {
            char keyWord = text.charAt(i);
            currentMap = (Map) currentMap.get(keyWord);
            if (currentMap != null) {
                wordLength.getAndIncrement();
                if (IS_END_TYPE.ONE == (currentMap.get(IS_END))) {
                    flag = true;
                    if (MATCH_TYPE.MINIMUM_MATCH == matchType) {
                        break;
                    }
                }

            } else {
                break;
            }
        }
        if (wordLength.get() < 1 || !flag) {
            wordLength.set(0);
        }
        return wordLength.get();
    }

    /**
     * 是否包含词库中的敏感词
     *
     * @param dfaWordMap 词库
     * @param text       文本
     * @param matchType  匹配类型 参考{@link MATCH_TYPE}
     * @return
     */
    protected boolean contains(HashMap dfaWordMap, String text, Enum matchType) {
        for (int i = 0; i < text.length(); i++) {
            int wordLength = checkWord(dfaWordMap, text, i, matchType);
            if (wordLength > 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取文本中的敏感词
     *
     * @param dfaWordMap 词库
     * @param text       文本
     * @param matchType  匹配模式
     * @return 返回敏感词集合
     */
    protected Set<String> getSensitiveWordInText(HashMap dfaWordMap, String text, Enum matchType) {
        HashSet<String> sensitiveWordSet = new HashSet();
        for (int i = 0; i < text.length(); i++) {
            int wordLength = checkWord(dfaWordMap, text, i, matchType);
            if (wordLength > 0) {
                sensitiveWordSet.add(text.substring(i, i + wordLength));
                i = i + wordLength - 1;
            }
        }
        return sensitiveWordSet;
    }

    /**
     * 替换文本中的敏感词
     *
     * @param dfaWordMap  词库
     * @param text        文本
     * @param replaceChar 代表敏感词的字符，替换后文本将使用这里指定的符号代表敏感词
     * @param matchType   匹配类型
     * @return 返回替换后的文本
     */
    protected String replaceSensitiveWord(HashMap dfaWordMap, String text, char replaceChar, Enum matchType) {
        Set<String> sensitiveWordSet = getSensitiveWordInText(dfaWordMap, text, matchType);
        Iterator<String> iterator = sensitiveWordSet.iterator();
        String resultText = "";
        while (iterator.hasNext()) {
            String sensitive = iterator.next();
            String replaceChars = getReplaceChars(replaceChar, sensitive.length());
            resultText = text.replaceAll(sensitive, replaceChars);
        }
        return resultText;
    }

    /**
     * 获取替换的字符
     *
     * @param replaceChar 替换字符
     * @param length      字符长度
     * @return 返回替换字符
     */
    protected String getReplaceChars(char replaceChar, int length) {
        String replaceStr = String.valueOf(replaceChar);
        for (int i = 0; i < length; i++) {
            replaceStr += replaceStr;
        }
        return replaceStr;
    }

    /**
     * 加载敏感词本地文件
     *
     * @return
     */
    protected Set<String> loadSensitiveWordResources() {
        if (this.wordTextInputStreams == null) {
            log.warning(String.format("在%s下没有找到相应的txt词库文件，敏感词检测将不能使用!!", nlpProperties.getBaseDir()));
            return new HashSet();
        }
        log.info("开始加载本地词库");
        long start = System.currentTimeMillis();
        HashSet<String> wordSet = new HashSet(4096);
        for (InputStream fileInputStream : wordTextInputStreams) {
            wordSet.addAll(loadFile(fileInputStream));
        }
        log.info(String.format("本地词库加载完成，共有%d个词，耗时%dms", wordSet.size(), (System.currentTimeMillis() - start)));
        return wordSet;
    }

    /**
     * 加载文件
     *
     * @param fileInputStream 文件读入流
     * @return
     */
    protected Set<String> loadFile(InputStream fileInputStream) {
        HashSet<String> words = new HashSet(256);
        BufferedReader reader = null;
        try {
            String line = null;
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
            reader = new BufferedReader(inputStreamReader, 2048);
            while ((line = reader.readLine()) != null) {
                words.add(line.trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return words;
    }
}
