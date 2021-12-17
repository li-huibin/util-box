# 自然语言处理相关工具
1. 敏感词检测  
  基于DFA确定有限自动机实现的敏感词检测，后期会继续集成分词器敏感词检测。
2. 相关配置
  `baseDir`: 用来配置敏感词库所在的目录，默认值为“classpath:/nlp”
  `postFixed`: 用来配置敏感词文件后缀名，默认为`/*.txt`目录下所有txt文件
  `replacement`: 要代替敏感词的字符，默认为“*”
  `pointCut`: 切点表达式，配置需要要进行敏感词处理controller的目录
3. 相关注解
  `@Sensitive`: 用于标记敏感词所在类
  `@SensitiveWord`: 用于标记敏感词字段，其中的`ignoreApis`字段可以用来设置忽略敏感词检测的接口
4. 与springboot集成使用
  在application.yml中配置：  
  ```yaml
# 自然语言处理相关配置
nlp:
  base-dir: classpath:/nlp
  post-fixed:
  sensitive-word:
    replacement: "*"
    point-cut: "execution(public * com.lhb.nlp.test.WordController.*(..))"
```
response分装
```java
/**
 * @Program: nlp
 * @Description:
 * @Author: LHB
 * @Version: v0.0.1
 * @Time: 2021-12-17 14:16
 **/
public class ResponseUtil<T> {
    private T data;

    public ResponseUtil(T data) {
        this.data = data;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "ResponseUtil{" +
                "data=" + data +
                '}';
    }
}
```
返回参数分装
```java
/**
 * @Program: nlp
 * @Description:
 * @Author: LHB
 * @Version: v0.0.1
 * @Time: 2021-12-17 14:13
 **/
@Sensitive
public class Word {
    @SensitiveWord
    private String context;

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    @Override
    public String toString() {
        return "Word{" +
                "context='" + context + '\'' +
                '}';
    }
}
```
测试controller
```java
/**
 * @Program: nlp
 * @Description:
 * @Author: LHB
 * @Version: v0.0.1
 * @Time: 2021-12-17 14:15
 **/
@RestController
@RequestMapping("/test")
public class WordController {
    @Autowired
    private WordServer wordServer;

    @GetMapping("/word")
    public ResponseUtil<Word> getWord() {
        return wordServer.getWord();
    }
}
```
测试server层
```java
/**
 * @Program: nlp
 * @Description:
 * @Author: LHB
 * @Version: v0.0.1
 * @Time: 2021-12-17 14:17
 **/
@Service
public class WordServer {
    public ResponseUtil getWord() {

        Word word = new Word();
        word.setContext("炸药炸药炸药炸药炸药炸药炸药炸药炸药炸药炸药炸药炸药炸药炸药炸药炸药炸药炸药炸药炸药炸药炸药炸药炸药炸药炸药炸药炸药炸药炸药炸药炸药炸药炸药");
        return new ResponseUtil(word);
    }
}
```