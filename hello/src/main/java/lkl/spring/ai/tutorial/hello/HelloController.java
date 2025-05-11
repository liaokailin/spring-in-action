package lkl.spring.ai.tutorial.hello;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.image.Image;
import org.springframework.ai.image.ImageMessage;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.model.Media;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.openai.*;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.ai.openai.audio.speech.SpeechPrompt;
import org.springframework.ai.openai.audio.speech.SpeechResponse;
import org.springframework.ai.openai.metadata.audio.OpenAiAudioSpeechResponseMetadata;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbacks;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.ai.tool.method.MethodToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.MimeTypeUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Slf4j
@RestController
public class HelloController {

    private ChatClient chatClient;

    public HelloController(ChatClient.Builder builder) {

        this.chatClient = builder.build();
    }


    @GetMapping("/prompt/simple")
    public String simplePrompt(@RequestParam(value = "input", defaultValue = "讲一个笑话") String input) {
        Prompt prompt = new Prompt(input);
        return chatClient.prompt(prompt).call().content();
    }


    @GetMapping("/prompt/format")
    public String formatPrompt(@RequestParam(value = "actorName", defaultValue = "刘德华") String actorName) {
        PromptTemplate promptTemplate = new PromptTemplate("""
                
                根据豆瓣评分推荐一部{actor}主演的电影，如果不知道请直接回答不知道
                
                """);

        Prompt prompt = new Prompt(promptTemplate.render(Map.of("actor", actorName)));

        return chatClient.prompt(prompt).call().content();
    }

    @GetMapping("/prompt/message")
    public String messagePrompt(@RequestParam(value = "movie", defaultValue = "肖申克的救赎") String movie) {

        UserMessage userMessage = new UserMessage(movie);

        SystemMessage systemMessage = new SystemMessage("你是一个专业的影评人，给出电影评分以及评语，并以JSON格式输出");

        Prompt prompt = new Prompt(List.of(userMessage, systemMessage));


        return chatClient.prompt(prompt).call().content();
    }

    @Value("classpath:/prompts/game.txt")
    private Resource gamePrompt;

    @GetMapping("/prompt/game")
    public String game(@RequestParam(value = "question", defaultValue = "爱情") String question) {
        PromptTemplate promptTemplate = new PromptTemplate(gamePrompt);
        Prompt prompt = new Prompt(promptTemplate.render(Map.of("question", question)));
        return chatClient.prompt(prompt).call().content();
    }


    @GetMapping("/hello")
    public String hello(@RequestParam(value = "input", defaultValue = "讲一个笑话") String input) {

        return chatClient.prompt(input).call().content();

    }

    @GetMapping(value = "/hello/stream", produces = "text/html;charset=UTF-8")
    public Flux<String> helloStream(@RequestParam(value = "input", defaultValue = "讲一个笑话") String input) {

        return chatClient.prompt(input).stream().content();
    }


    private ExecutorService executor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MICROSECONDS, new LinkedBlockingQueue<>(10));

    @GetMapping(value = "/mock/stream", produces = "text/html;charset=UTF-8")
    public Flux<String> mockStream() {

        Sinks.Many<String> sink = Sinks.many().multicast().onBackpressureBuffer();

        executor.submit(() -> {
            for (int i = 0; i < 100; i++) {

                sink.tryEmitNext(i + " ");
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        return sink.asFlux();
    }


    @GetMapping("/movies")
    public String movies(@RequestParam(value = "actor", defaultValue = "刘德华") String actor) {

        String content = chatClient.prompt().user(u -> {
            u.text("罗列10部{actor}主演的电影").param("actor", actor);
        }).call().content();

        System.out.println(content);
        return content;
    }

    @GetMapping("/movies/entity")
    public String moviesEntity(@RequestParam(value = "actor", defaultValue = "刘德华") String actor) {

        ActorsFilms actorsFilms = chatClient.prompt().user(u -> {
            u.text("罗列10部{actor}主演的电影").param("actor", actor);
        }).call().entity(ActorsFilms.class);

        System.out.println(actorsFilms);
        return actorsFilms.toString();
    }


    record ActorsFilms(String actor, List<String> movies) {
    }


    @GetMapping("/multimodality")
    public String multimodality() {
        /**
         * 要切换到open-ai下
         */
        Resource imageResource = new ClassPathResource("/images/multimodal.test.png");

        var userMessage = new UserMessage("Explain what do you see in this picture?", // content
                new Media(MimeTypeUtils.IMAGE_PNG, imageResource)); // media
        return chatClient.prompt(new Prompt(userMessage)).call().content();
    }

    @Value("classpath:/images/multimodal.test.png")
    private Resource imageResource;


    @Autowired
    private OpenAiImageModel openAiImageModel;


    /**
     * spring ai中对openai的调用 https://docs.spring.io/spring-ai/reference/api/image/openai-image.html
     * 输出图片
     * 阿里云的文  https://help.aliyun.com/zh/model-studio/use-cases/text-to-image-prompt?spm=a2c4g.11186623.help-menu-2400256.d_4_6.1c1b13f0WsKWTW
     *
     * @return
     */
    @GetMapping("/outputImg")
    public void outputImg() throws IOException {
        ImageMessage userMessage = new ImageMessage("18岁的中国女孩，高清相机，情绪大片");

        /**
         * vivid（生动模式）‌: 使模型倾向于生成具有超现实感和戏剧张力的图像
         * natural（自然模式）‌: 引导模型生成更接近真实场景、减少夸张效果的图像
         */
        OpenAiImageOptions chatOptions = OpenAiImageOptions.builder().model("dall-e-3").quality("hd").N(1).height(1024).width(1024).build();


        ImagePrompt prompt = new ImagePrompt(userMessage, chatOptions);

        ImageResponse imageResponse = openAiImageModel.call(prompt);

        Image image = imageResponse.getResult().getOutput();

        InputStream in = new URL(image.getUrl()).openStream();
        saveStreamToFile(in, "src/main/resources/images", "girl" + RandomUtils.insecure().randomInt(0, 100) + ".png");
        System.out.println(imageResponse);

    /*  测试发现 N如果传递>1 为直接抛错(估计是账号等级的问题)
      int i = 0;
        for (ImageGeneration imageGeneration : imageResponse.getResults()) {
            i++;
            Image image = imageGeneration.getOutput();
            saveFile(image.getB64Json().getBytes(), "src/main/resources/images", i + ".png");
        }*/

        //返回的url地址: https://oaidalleapiprodscus.blob.core.windows.net/private/org-yoEHKXe2ghuSjMLzFAcfqrHT/user-8swGdtAb9uRgrDlYEaGwqv8r/img-kJOzct134vjVYzowMOqrKK8s.png?st=2025-04-04T11%3A14%3A52Z&se=2025-04-04T13%3A14%3A52Z&sp=r&sv=2024-08-04&sr=b&rscd=inline&rsct=image/png&skoid=d505667d-d6c1-4a0a-bac7-5c84a87759f8&sktid=a48cca56-e6da-484e-a814-9c849652bcb3&skt=2025-04-04T04%3A24%3A09Z&ske=2025-04-05T04%3A24%3A09Z&sks=b&skv=2024-08-04&sig=nVMJ2WCFVtc5R9Yq8i3COE1eUIvzfb1rIMxK2jQdph8%3D


    }

    @GetMapping("/outputImg2")
    public void outputImg2() throws IOException {
        String url = "https://oaidalleapiprodscus.blob.core.windows.net/private/org-yoEHKXe2ghuSjMLzFAcfqrHT/user-8swGdtAb9uRgrDlYEaGwqv8r/img-kJOzct134vjVYzowMOqrKK8s.png?st=2025-04-04T11%3A14%3A52Z&se=2025-04-04T13%3A14%3A52Z&sp=r&sv=2024-08-04&sr=b&rscd=inline&rsct=image/png&skoid=d505667d-d6c1-4a0a-bac7-5c84a87759f8&sktid=a48cca56-e6da-484e-a814-9c849652bcb3&skt=2025-04-04T04%3A24%3A09Z&ske=2025-04-05T04%3A24%3A09Z&sks=b&skv=2024-08-04&sig=nVMJ2WCFVtc5R9Yq8i3COE1eUIvzfb1rIMxK2jQdph8%3D";
        Image image = new Image(url, null);

        //返回的url地址: https://oaidalleapiprodscus.blob.core.windows.net/private/org-yoEHKXe2ghuSjMLzFAcfqrHT/user-8swGdtAb9uRgrDlYEaGwqv8r/img-kJOzct134vjVYzowMOqrKK8s.png?st=2025-04-04T11%3A14%3A52Z&se=2025-04-04T13%3A14%3A52Z&sp=r&sv=2024-08-04&sr=b&rscd=inline&rsct=image/png&skoid=d505667d-d6c1-4a0a-bac7-5c84a87759f8&sktid=a48cca56-e6da-484e-a814-9c849652bcb3&skt=2025-04-04T04%3A24%3A09Z&ske=2025-04-05T04%3A24%3A09Z&sks=b&skv=2024-08-04&sig=nVMJ2WCFVtc5R9Yq8i3COE1eUIvzfb1rIMxK2jQdph8%3D
        InputStream in = new URL(image.getUrl()).openStream();

        saveStreamToFile(in, "src/main/resources/images", "test-girl.png");


    }

    public String saveStreamToFile(InputStream inputStream, String filePath, String fileName) throws IOException {

        // 创建目录（如果不存在）
        Path dirPath = Paths.get(filePath);
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }

        // 构建完整路径
        Path targetPath = dirPath.resolve(fileName);

        // 使用 try-with-resources 确保流关闭
        try (inputStream) {
            Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }

        return targetPath.toAbsolutePath().toString();
    }

    /**
     * 输出音频
     *
     * @return
     */
    @GetMapping("/outputAudio")
    public void outputAudio() throws IOException {
        UserMessage userMessage = new UserMessage("说一个20个字以内的笑话");
        //  this.chatClient


        OpenAiChatOptions chatOptions = OpenAiChatOptions.builder().model(OpenAiApi.ChatModel.GPT_4_O_AUDIO_PREVIEW)  //注意如果是gpt-4o是无法生成音频文件的
                .outputModalities(List.of("text", "audio")).outputAudio(new OpenAiApi.ChatCompletionRequest.AudioParameters(OpenAiApi.ChatCompletionRequest.AudioParameters.Voice.ALLOY, OpenAiApi.ChatCompletionRequest.AudioParameters.AudioResponseFormat.WAV)).build();
        Prompt prompt = new Prompt(userMessage, chatOptions);


        ChatResponse chatResponse = chatClient.prompt(prompt).call().chatResponse();

        String text = chatResponse.getResult().getOutput().getText(); // audio transcript
        // 有一天，一只猪站在树底下，突然一只苹果掉下来，猪抬头说：“哇，苹果树上居然长苹果！”
        System.out.println("返回的文本内容:" + text);

        byte[] waveAudio = chatResponse.getResult().getOutput().getMedia().get(0).getDataAsByteArray(); // audio data

        saveFile(waveAudio, "src/main/resources/audios", "joke111.mp3");
    }


    @GetMapping("/audio")
    public void audio() {

        try {
            saveFile(imageResource.getContentAsByteArray(), "src/main/resources/images", "copy.png");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


    private void saveFile(byte[] bytes, String filePath, String fileName) throws IOException {

        Path targetPath = Paths.get(filePath, fileName);

        Files.write(targetPath, bytes);
    }

    @Value("classpath:/audios/joke.mp3")
    private Resource audioFile;

    @Autowired
    private OpenAiAudioTranscriptionModel openAiAudioTranscriptionModel;

    /**
     * 音频翻译，把mp3中的音频转为字母格式VTT
     */
    @GetMapping("/audioTranscription")
    public String audioTranscription() {

        //定义输出格式为 VTT（Web Video Text Tracks，字幕格式）
        //  OpenAiAudioApi.TranscriptResponseFormat responseFormat = OpenAiAudioApi.TranscriptResponseFormat.VTT;


        //也可以范围为纯文本格式
        OpenAiAudioApi.TranscriptResponseFormat responseFormat = OpenAiAudioApi.TranscriptResponseFormat.TEXT;


        OpenAiAudioTranscriptionOptions transcriptionOptions = OpenAiAudioTranscriptionOptions.builder().language("zh")
                //.prompt("Ask not this, but ask that")
                .temperature(0f).responseFormat(responseFormat).build();
        AudioTranscriptionPrompt transcriptionRequest = new AudioTranscriptionPrompt(this.audioFile, transcriptionOptions);

        AudioTranscriptionResponse response = this.openAiAudioTranscriptionModel.call(transcriptionRequest);

        String content = response.getResult().getOutput();
        log.info("audioTranscription content={}", content);
        return content;


        /**
         * WEBVTT
         *
         * 00:00:00.000 --> 00:00:03.000
         * 有一天,一只猪站在树底下,
         *
         * 00:00:03.000 --> 00:00:05.400
         * 突然一只苹果掉下来。
         *
         * 00:00:05.400 --> 00:00:10.000
         * 猪抬头说:「哇!苹果树上居然长苹果!」
         */

    }

    @Autowired
    private OpenAiAudioSpeechModel openAiAudioSpeechModel;
    private static final Float SPEED = 1.0f;

    /**
     * 语音合成
     */
    @GetMapping("/audioSpeech")
    public void audioSpeech() throws IOException {


        /**
         * 语音名称	音色特征描述	适用场景示例
         * ALLOY	中性化、清晰且自然的声音，无明显性别偏向，发音稳定。	教育内容、新闻播报、技术解说
         * ECHO	低沉、浑厚的男声，带有权威感和稳重感。	企业宣传片、历史纪录片、播客
         * FABLE	富有故事性的音色，带有温和的叙事感，类似童话旁白。	儿童故事、有声书、动画配音
         * ONYX	磁性且成熟的男声，适合表达专业或严肃的内容。	商业报告、法律声明、金融播报
         * NOVA	明亮、轻快的女声，带有活力和亲和力。	客服语音、广告旁白、生活类内容
         * SHIMMER	柔和且温暖的女声，略带童声音色，适合轻松场景。	儿童教育、游戏角色、娱乐内容
         * SAGE	中性偏女性的声音，冷静且理性，适合知识性内容。	医疗科普、学术课程、科技播客
         * CORAL	热情、富有感染力的声音，适合情感表达丰富的场景。	情感类播客、诗歌朗诵、激励演讲
         * ASH	沙哑且略带沧桑感的男声，适合需要独特个性的场景。	悬疑故事、户外探险、复古风格内容
         */
        OpenAiAudioSpeechOptions speechOptions = OpenAiAudioSpeechOptions.builder().voice(OpenAiAudioApi.SpeechRequest.Voice.ALLOY) //设置语音类型为 "ALLOY"（合金音色）
                .speed(SPEED)  // 设置语速（例如 1.0 为正常速度）
                .responseFormat(OpenAiAudioApi.SpeechRequest.AudioResponseFormat.MP3)// 输出格式为 MP3
                .model(OpenAiAudioApi.TtsModel.TTS_1.value)   // 指定语音合成模型为 TTS-1
                .build();
        SpeechPrompt speechPrompt = new SpeechPrompt("今天又是美好的一天，加油，打工人~", speechOptions);
        // 调用 OpenAI 语音合成模型，生成语音
        SpeechResponse response = this.openAiAudioSpeechModel.call(speechPrompt);
        OpenAiAudioSpeechResponseMetadata metadata = response.getMetadata();
        log.info("audioSpeech response={}", metadata);
        saveFile(response.getResult().getOutput(), "src/main/resources/audios", "speech.mp3");
        log.info("语音已完成，请在文件夹查看");
    }

    /**多模态end **/


    /**********工具调用************/


    @GetMapping("/getDate")
    public String getDate() {
        return chatClient.prompt("明天是星期几?").call().content();
        //抱歉，我无法知道具体哪一天是“明天”，因为我不知道今天的日期。如果你告诉我今天是星期几，我可以帮你推算明天是星期几。
    }

    @GetMapping("/tool/getDate")
    public String getDateByTool() {
        return chatClient.prompt("明天是星期几?").tools(new DateTimeTools()).call().content();
        //今天是2025年4月7日（星期一），所以明天是星期二。
    }


    @GetMapping("/tool/setAlarm")
    public String setAlarm() {
        return chatClient.prompt("请设置10分钟后的闹钟").tools(new DateTimeTools()).call().content();
        //今天是2025年4月7日（星期一），所以明天是星期二。
    }

    @GetMapping("/tool/todo")
    public String setTodo() {
        return chatClient.prompt("把写一篇技术博客做为我的待办事项").tools(new DateTimeTools()).call().content();
        //今天是2025年4月7日（星期一），所以明天是星期二。
    }


    class DateTimeTools {

        // 查询
        @Tool(description = "获取用户时区的当前日期和时间")
        String getCurrentDateTime() {
            return LocalDateTime.now().atZone(LocaleContextHolder.getTimeZone().toZoneId()).toString();
        }

        //执行动作
        @Tool(description = "为用户设置闹钟，时间参数需为ISO-8601格式")
        void setAlarm(String time) {
            log.info("setAlarm time={}", time);
            LocalDateTime alarmTime = LocalDateTime.parse(time, DateTimeFormatter.ISO_DATE_TIME);
            System.out.println("-------模拟系统调用操作，为用户设置闹钟-------");
            System.out.println("设置闹钟成功! " + alarmTime);
        }

        //定义参数格式
        @Tool(description = "为用户设置一个待办事项")
        void setToDo(@ToolParam(description = "代办内容") String todo) {
            System.out.println("待办内容已设置完成，  " + todo);
        }
    }


    class DateTimeTools2 {

        String getCurrentDateTime() {
            return LocalDateTime.now().atZone(LocaleContextHolder.getTimeZone().toZoneId()).toString();
        }

    }





    /**********上面是一个方法做为tool ********/


    /**********下面是一个function做为tool *******/




    public class WeatherService implements Function<WeatherRequest, WeatherResponse> {
        public WeatherResponse apply(WeatherRequest request) {
            log.info("WeatherRequest={}", request);
            return new WeatherResponse(30.0, Unit.C);
        }
    }

    public enum Unit {C, F}

    public record WeatherRequest(String location, Unit unit) {
    }

    public record WeatherResponse(double temp, Unit unit) {
    }





    @GetMapping("/tool/function/anno")
    public String toolFucntionAnno() {


        return chatClient.prompt("北京天气怎么样?").tools("currentWeather").call().content();

    }

    class OrderQuery {

        @Tool(description = "查询用户交易订单信息",returnDirect = true)
        String getTradeOrderInfo(@ToolParam(description = "交易订单号" )  Long id, ToolContext toolContext) {

            return "订单内容:" + id + " 租户Id:" + toolContext.getContext().get("tenantId") ;
        }

        @Tool(description = "查询用户退款单信息", returnDirect = true)
        String getRefundOrderInfo(Long id) {

            return "订单内容:" + id + " ,我是自定义后缀";
        }
    }


    @GetMapping("/tool/context/order")
    public String queryOrder() {
        //上下文
        return chatClient.prompt("查询订单编号为123的交易订单信息").tools(new OrderQuery()).toolContext(Map.of("tenantId", "zh")).call().content();

    }

    @GetMapping("/tool/context/refund")
    public String queryRefundOrder() {
        //直接返回
        return chatClient.prompt("查询订单编号为123的退款单信息").tools(new OrderQuery()).call().content();

    }

    @GetMapping("/tool/control2")
    public String controlTool2() {
        ToolCallingManager toolCallingManager = ToolCallingManager.builder().build();

        ToolCallback[] customerTools = ToolCallbacks.from(new DateTimeTools());

        ChatOptions chatOptions = ToolCallingChatOptions.builder().toolCallbacks(customerTools).internalToolExecutionEnabled(false).build();

        Prompt prompt = new Prompt("明天星期几?", chatOptions);
        ChatResponse chatResponse = chatClient.prompt(prompt).call().chatResponse();
        while (chatResponse.hasToolCalls()) {
            ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(prompt, chatResponse);

            prompt = new Prompt(toolExecutionResult.conversationHistory(), chatOptions);

            chatResponse = chatClient.prompt(prompt).call().chatResponse();
        }

        return chatResponse.getResult().getOutput().getText();
    }


    //--------------记忆
    InMemoryChatMemory inMemoryChatMemory = new InMemoryChatMemory();

    @GetMapping("/chat/memory")
    public String chat(@RequestParam(value = "input", defaultValue = "我是张三") String input) {
        Prompt prompt = new Prompt(input);
        return chatClient.prompt(prompt).advisors(new MessageChatMemoryAdvisor(inMemoryChatMemory)).call().content();
    }

}
