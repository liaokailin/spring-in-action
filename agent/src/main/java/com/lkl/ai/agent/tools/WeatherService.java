package com.lkl.ai.agent.tools;

import groovy.util.logging.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class WeatherService implements Function<WeatherService.Req, WeatherService.Resp> {


    @Override
    public Resp apply(Req req) {

        System.out.println("执行天气工具查询：" + req);
        Resp resp = new Resp(RandomUtils.insecure().randomDouble(10d, 30d), Unit.F);
        System.out.println("天气查询成功：" + resp);
        return resp;
    }

    public enum Unit {C, F}

    public record Req(String location, Unit unit) {

    }

    public record Resp(double temp, Unit unit) {
    }

}
