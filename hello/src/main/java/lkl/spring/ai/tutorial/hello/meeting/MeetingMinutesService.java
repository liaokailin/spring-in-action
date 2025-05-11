package lkl.spring.ai.tutorial.hello.meeting;

import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.ai.openai.OpenAiAudioTranscriptionOptions;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Service
public class MeetingMinutesService {

    private final OpenAiAudioTranscriptionModel transcriptionModel;
    private final ChatClient chatClient;

    @Value("classpath:/prompts/minutes-prompt.txt")
    private Resource minutesPromptTemplate;

    public MeetingMinutesService(OpenAiAudioTranscriptionModel transcriptionModel, ChatClient.Builder builder) {
        this.transcriptionModel = transcriptionModel;
        this.chatClient = builder.build();
    }

    public MeetingMinutes generateMinutes(MeetingInput input) throws IOException {
        // 1. 语音转文本
        String transcript = transcribeAudio(input.audioFile());

        // 2. 生成结构化会议纪要
        return generateStructuredMinutes(transcript, input.metadata());
    }

    private String transcribeAudio(Resource audioFile) {


        OpenAiAudioApi.TranscriptResponseFormat responseFormat = OpenAiAudioApi.TranscriptResponseFormat.TEXT;

        OpenAiAudioTranscriptionOptions transcriptionOptions = OpenAiAudioTranscriptionOptions.builder().language("zh").temperature(0f).responseFormat(responseFormat).build();
        AudioTranscriptionPrompt transcriptionRequest = new AudioTranscriptionPrompt(audioFile, transcriptionOptions);

        var prompt = new AudioTranscriptionPrompt(audioFile, transcriptionOptions);
        AudioTranscriptionResponse response = transcriptionModel.call(prompt);

        return response.getResult().getOutput();
    }

    private MeetingMinutes generateStructuredMinutes(String transcript, MeetingMetadata metadata) throws IOException {
        // 加载提示词模板
        String promptTemplate = FileCopyUtils.copyToString(new InputStreamReader(minutesPromptTemplate.getInputStream()));

        // 构建完整提示词
        Map<String, Object> params = new HashMap<>();
        params.put("transcript", transcript);
        params.put("meetingType", metadata.meetingType());
        params.put("participants", String.join(", ", metadata.participants()));
        params.put("startTime", metadata.startTime());

        String promptText = new PromptTemplate(promptTemplate).render(params);

        // 调用AI生成结构化结果
        return chatClient.prompt().user(promptText).call().entity(MeetingMinutes.class);
    }
}