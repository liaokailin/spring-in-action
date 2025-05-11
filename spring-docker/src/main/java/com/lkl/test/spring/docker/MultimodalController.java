package com.lkl.test.spring.docker;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.image.Image;
import org.springframework.ai.image.ImageMessage;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.model.Media;
import org.springframework.ai.openai.*;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Slf4j
@RestController
public class MultimodalController {

    private ChatClient chatClient;

    public MultimodalController(ChatClient.Builder builder) {

        this.chatClient = builder.build();
    }


    @GetMapping("/multimodal/pic")
    public String pic() {
        /**
         * 要切换到open-ai下   docker下的镜像也不支持
         */
        Resource imageResource = new ClassPathResource("/images/multimodal.test.png");



        var userMessage = new UserMessage("解释一下你在这幅图中看到了什么?", // content
                new Media(MimeTypeUtils.IMAGE_PNG, imageResource)); // media
        return chatClient.prompt(new Prompt(userMessage)).call().content();
    }

    @Autowired
    private OpenAiImageModel openAiImageModel;

    @GetMapping("/multimodal/outputImg")
    public void outputImg() throws IOException {
        // ImageMessage userMessage = new ImageMessage("18岁的中国女孩，高清相机，情绪大片");
        ImageMessage userMessage = new ImageMessage("""
                [主体对象配置]
                人物：22岁混血女黑客，紫色渐变短发，穿着带LED灯带的战术背心
                动作：正在虚拟键盘上快速输入，周围漂浮着半透明数据流
                
                [场景设置]
                地点：未来主义数据中心，服务器机柜发出幽蓝冷光
                环境：空气中悬浮着全息投影界面，地面有淡淡的雾气
                
                [渲染风格]
                视觉风格：《攻壳机动队》+《创：战纪》混合美学
                色彩方案：霓虹蓝紫主色调，配合荧光绿高光
                
                [摄像机参数]
                镜头：超广角鱼眼效果，轻微镜头畸变
                景别：中景，包含人物全身及环境
                视角：略微俯拍，展现环境规模
                
                [后期处理]
                分辨率：高清
                特效：体积光效果，动态模糊（手指动作）
                排除项：无品牌标识，无水印
                """);

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

    /**
     * 输出音频
     *
     * @return
     */
    @GetMapping("/multimodal/outputAudio")
    public void outputAudio() throws IOException {
        UserMessage userMessage = new UserMessage("说一个20个字以内的笑话");

        OpenAiChatOptions chatOptions = OpenAiChatOptions.builder().model(OpenAiApi.ChatModel.GPT_4_O_AUDIO_PREVIEW)  //注意如果是gpt-4o是无法生成音频文件的
                .outputModalities(List.of("text", "audio")).outputAudio(new OpenAiApi.ChatCompletionRequest.AudioParameters(OpenAiApi.ChatCompletionRequest.AudioParameters.Voice.ALLOY, OpenAiApi.ChatCompletionRequest.AudioParameters.AudioResponseFormat.MP3)).build();
        Prompt prompt = new Prompt(userMessage, chatOptions);


        ChatResponse chatResponse = chatClient.prompt(prompt).call().chatResponse();

        String text = chatResponse.getResult().getOutput().getText(); // audio transcript
        // 有一天，一只猪站在树底下，突然一只苹果掉下来，猪抬头说：“哇，苹果树上居然长苹果！”
        log.info("返回的文本内容text={}", text);

        byte[] waveAudio = chatResponse.getResult().getOutput().getMedia().get(0).getDataAsByteArray(); // audio data

        saveFile(waveAudio, "src/main/resources/audios", "joke111.mp3");
    }

    private void saveFile(byte[] bytes, String filePath, String fileName) throws IOException {

        Path targetPath = Paths.get(filePath, fileName);

        Files.write(targetPath, bytes);
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


    @Value("classpath:/audios/joke.mp3")
    private Resource audioFile;

    @Autowired
    private OpenAiAudioTranscriptionModel openAiAudioTranscriptionModel;


    @GetMapping("/multimodal/audioTranscription")
    public String audioTranscription() {


        //定义输出格式为 VTT（Web Video Text Tracks，字幕格式）
        OpenAiAudioApi.TranscriptResponseFormat responseFormat = OpenAiAudioApi.TranscriptResponseFormat.VTT;

        //纯文本格式
        // OpenAiAudioApi.TranscriptResponseFormat responseFormat = OpenAiAudioApi.TranscriptResponseFormat.TEXT;

        OpenAiAudioTranscriptionOptions transcriptionOptions = OpenAiAudioTranscriptionOptions.builder().language("zh").temperature(0f).responseFormat(responseFormat).build();
        AudioTranscriptionPrompt transcriptionRequest = new AudioTranscriptionPrompt(this.audioFile, transcriptionOptions);

        AudioTranscriptionResponse response = this.openAiAudioTranscriptionModel.call(transcriptionRequest);

        String content = response.getResult().getOutput();
        log.info("转译内容 content={}", content);
        return content;

    }

}
