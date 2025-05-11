package com.lkl.ai.mcp.stdio;


import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

@Service
public class WeatherService {

    @Tool(description = "获取指定经纬度地点的天气预报")
    public String getWeatherForecastByLocation(double latitude,   // Latitude coordinate
                                               double longitude   // Longitude coordinate
    ) {
        // Implementation
        return "天气一片晴朗stdio " + System.currentTimeMillis() + "," + latitude + "," + longitude;
    }

    @Tool(description = "获取指定地域的天气预警")
    public String getAlerts(String state  // Two-letter US state code (e.g., CA, NY)
    ) {
        // Implementation
        return "快跑，有毒,stdio," + System.currentTimeMillis() + "," + state;
    }
}
