package com.lkl.ai.mcp.springmvc;


import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

@Service
public class WeatherService {

    @Tool(description = "获取指定经纬度地点的天气预报")
    public String getWeatherForecastByLocation(double latitude, double longitude) {
        // Implementation
        return "天气一片晴朗MVC " + System.currentTimeMillis() + "," + latitude + "," + longitude;
    }

    @Tool(description = "获取指定地域的天气预警")
    public String getAlerts(String state) {
        // Implementation
        return "快跑，有毒,MVC" + System.currentTimeMillis() + "," + state;
    }
}
