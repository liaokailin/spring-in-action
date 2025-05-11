package lkl.spring.ai.tutorial.hello;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
public class PromptController {

    private ChatClient chatClient;

    public PromptController(ChatClient.Builder builder) {

        this.chatClient = builder.build();
    }


    @GetMapping("/horoscope")
    public String getHoroscope(
            @RequestParam(value = "zodiac" ,defaultValue = "白羊座") String zodiac,
            @RequestParam(value = "topic", defaultValue = "web3") String topic) {

        String prompt = """
    [专为开发者设计的星座运势]
    星座：{zodiac}
    领域：{topic}
    要求：
    - 用编程术语解释行星相位
    - 给出本周"代码"（幸运数字/颜色）
    - 包含一个debug小贴士

    示例输出：
    "本周水星逆行就像IDE突然卡顿，
    你的幸运数字是42（宇宙的答案），
    debug建议：多写单元测试"
    """;

        PromptTemplate template = new PromptTemplate(prompt);
        Map<String, Object> params = Map.of(
                "zodiac", zodiac,
                "topic", topic
        );

        return chatClient.prompt(template.create(params))
                .call()
                .content();
    }


    @GetMapping("/tarot")
    public String drawTarotCard(@RequestParam(value = "question",defaultValue = "我的代码烂在哪？用塔罗牌诊断技术债务！") String question) {
        String tarotPrompt = """
                [背景]
                你是一位融合了现代心理学和传统占卜智慧的AI占卜师，擅长用技术从业者熟悉的比喻解释牌面含义。
                
                [目的]
                通过三张塔罗牌分别解读：
                1. 现状（当前能量状态）
                2. 挑战（需要克服的问题）
                3. 指引（具体行动建议）
                
                [风格]
                混合技术隐喻和神秘学术语，例如：
                "正位的魔术师就像写了一段完美运行的代码，
                但逆位时可能是缺少了异常处理"
                
                [输出格式]
                "card": "牌名",
                "meaning": "基础解释",
                "techMetaphor": "技术领域比喻",
                "advice": "具体行动建议"
                
                
                玩法的问题是:
                {question}
               
                请开始占卜:
                """;

        PromptTemplate template = new PromptTemplate(tarotPrompt);
        Prompt prompt = template.create(Map.of("question", question));

        return chatClient.prompt(prompt)
                .call()
                .content();
    }

}
