package com.lkl.ai.agent.tools;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.function.Function;

@Configuration(proxyBeanMethods = false)
public class WeatherTools {

    @Autowired
    private WeatherService weatherService;

    @Bean
    @Description("实时查询天气工具")
    Function<WeatherService.Req, WeatherService.Resp> currentWeather() {
        return weatherService;
    }

}
