package lkl.spring.ai.tutorial.hello;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbacks;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.ai.tool.method.MethodToolCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;

@Slf4j
@RestController
public class ToolController {

    private ChatClient chatClient;

    public ToolController(ChatClient.Builder builder) {

        this.chatClient = builder.build();
    }


    @GetMapping("/tools/simple/test")
    public String simpleTest() {
        return chatClient.prompt("明天是星期几?").call().content();
    }


    class DateTimeTools {

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

        // 不带注解
        String getCurrentDateTimeWithoutAnnotation() {
            return LocalDateTime.now().atZone(LocaleContextHolder.getTimeZone().toZoneId()).toString();
        }

    }

    @GetMapping("/tools/programmatic")
    public String toolByProgrammatic() {
        //编程式定义
        Method method = ReflectionUtils.findMethod(DateTimeTools.class, "getCurrentDateTimeWithoutAnnotation");

        ToolDefinition toolDefinition = ToolDefinition.builder(method).description("获取用户时区的当前日期和时间").build();


        ToolCallback toolCallback = MethodToolCallback.builder().toolDefinition(toolDefinition).toolMethod(method).toolObject(new DateTimeTools()).build();


        return chatClient.prompt("明天是星期几?").tools(toolCallback).call().content();

    }

    @GetMapping("/tools/simple/call")
    public String callTool() {
        return chatClient.prompt("明天是星期几?").tools(new DateTimeTools()).call().content();
    }


    @GetMapping("/tool/alarm")
    public String setAlarm() {
        return chatClient.prompt("帮我设置一个10分钟以后的闹钟").tools(new DateTimeTools()).call().content();
    }


    /**
     * 模拟一个天气服务
     */
    public class WeatherService implements Function<WeatherRequest, WeatherResponse> {
        public WeatherResponse apply(WeatherRequest request) {
            //固定返回30度
            return new WeatherResponse(30.0, Unit.C);
        }
    }

    public enum Unit {C, F}

    public record WeatherRequest(String location, Unit unit) {
    }

    public record WeatherResponse(double temp, Unit unit) {
    }


    @Configuration(proxyBeanMethods = false)
    class WeatherTools {

        WeatherService weatherService = new WeatherService();

        @Bean
        @Description("依据位置获取天气信息")
        Function<WeatherRequest, WeatherResponse> currentWeather() {
            return weatherService;
        }
    }


    @GetMapping("/tool/function/annotation")
    public String toolFunctionAnnotation() {

        return chatClient.prompt("杭州天气怎么样?").tools("currentWeather").call().content();
    }


    @GetMapping("/tool/function/programmatic")
    public String toolFunctionProgrammatic() {
        ToolCallback toolCallback = FunctionToolCallback.builder("currentWeather", new WeatherService()).description("依据位置获取天气信息").inputType(WeatherRequest.class).build();

        return chatClient.prompt("杭州天气怎么样?").tools(toolCallback).call().content();
    }

    @GetMapping("/tool/control")
    public String controlTool() {
        ToolCallingManager toolCallingManager = ToolCallingManager.builder().build();

        ToolCallback[] customerTools = ToolCallbacks.from(new DateTimeTools());

        ChatOptions chatOptions = ToolCallingChatOptions.builder().toolCallbacks(customerTools).internalToolExecutionEnabled(false).build();

        Prompt prompt = new Prompt("明天星期几?", chatOptions);
        ChatResponse chatResponse = chatClient.prompt(prompt).call().chatResponse();
        while (chatResponse.hasToolCalls()) {

            log.info("控制工具执行 start");
            ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(prompt, chatResponse);
            prompt = new Prompt(toolExecutionResult.conversationHistory(), chatOptions);
            chatResponse = chatClient.prompt(prompt).call().chatResponse();
            log.info("控制工具执行 end");
        }

        return chatResponse.getResult().getOutput().getText();
    }

}
