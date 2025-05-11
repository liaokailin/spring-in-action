package lkl.spring.ai.tutorial.hello;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.converter.ListOutputConverter;
import org.springframework.ai.converter.MapOutputConverter;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
public class OutputController {

    private ChatClient chatClient;

    public OutputController(ChatClient.Builder builder) {

        this.chatClient = builder.build();
    }


    @GetMapping("/film/entity")
    public Film queryFilmEntity(@RequestParam(value = "filmName", defaultValue = "无间道") String filmName) {

        Film resp = chatClient.prompt().user(u -> {
            u.text("""
                    从豆瓣上查询影视作品{filmName}的信息 
                    """).param("filmName", filmName);
        }).call().entity(Film.class);

        return resp;
    }


    @GetMapping("/idol/films")
    public String idolFilms(@RequestParam(value = "idol", defaultValue = "刘德华") String idol) {

        String content = chatClient.prompt().user(u -> {
            u.text("帮我找五部{idol}主演的电影").param("idol", idol);
        }).call().content();

        log.info(content);
        return content;
    }

    @GetMapping("/film")
    public Film queryFilm(@RequestParam(value = "filmName", defaultValue = "无间道") String filmName) {

        BeanOutputConverter<Film> beanOutputConverter = new BeanOutputConverter<>(new ParameterizedTypeReference<Film>() {
        });

        String content = chatClient.prompt().user(u -> {
            u.text("""
                    从豆瓣上查询影视作品{filmName}的信息 
                    {format}
                    """).params(Map.of("filmName", filmName, "format", beanOutputConverter.getFormat()));
        }).call().content();

        return beanOutputConverter.convert(content);
    }


    @GetMapping("/idol/film/bean/list/format")
    public List<Film> idolFilmsBeanListFormat(@RequestParam(value = "idol", defaultValue = "刘德华") String idol) {

        BeanOutputConverter<List<Film>> beanOutputConverter = new BeanOutputConverter<>(new ParameterizedTypeReference<List<Film>>() {
        });


        String content = chatClient.prompt().user(u -> {
            u.text("""
                    帮我找五部{idol}主演的电影
                    
                    {format}
                    """).params(Map.of("idol", idol, "format", beanOutputConverter.getFormat()));
        }).call().content();


        List<Film> filmList = beanOutputConverter.convert(content);

        log.info("BeanOutputConverter#filmList,size={},resp={}", filmList.size(), filmList);
        return filmList;


    }

    @GetMapping("/bean/map/format")
    public Map<String, Object> beanMapFormat(@RequestParam(value = "style", defaultValue = "华语流行") String style) {

        BeanOutputConverter<Map<String, Object>> beanOutputConverter = new BeanOutputConverter<>(new ParameterizedTypeReference<Map<String, Object>>() {
        });


        String content = chatClient.prompt().user(u -> {
            u.text("""
                    帮我找五部{style}的电影，以电影名为分组键，值为电影信息，电影信息需要包含电影名称、上映时间、导演名、电影简介等内容
                    
                    {format}
                    """).params(Map.of("style", style, "format", beanOutputConverter.getFormat()));
        }).call().content();


        Map<String, Object> filmMap = beanOutputConverter.convert(content);

        log.info("BeanOutputConverter#filmMap,size={},resp={}", filmMap.size(), filmMap);
        return filmMap;
    }


    record Film(@JsonPropertyDescription("电影名称") String name,
                @JsonPropertyDescription("上映时间") String releaseDate,
                @JsonPropertyDescription("导演名称") String directorName,
                @JsonPropertyDescription("电影简介") String desc) {
    }


    @GetMapping("/list/converter/films")
    public List<String> toListFilms(@RequestParam(value = "idol", defaultValue = "刘德华") String idol) {

        ListOutputConverter listOutputConverter = new ListOutputConverter(new DefaultConversionService());

        String content = chatClient.prompt().user(u -> {
            u.text("""
                    帮我找五部{idol}主演的电影
                    {format}
                    """).params(Map.of("idol", idol, "format", listOutputConverter.getFormat()));
        }).call().content();

        List<String> list = listOutputConverter.convert(content);
        log.info("ListOutputConverter#toListFilms,size={},resp={}", list.size(), list);
        return list;
    }

    @GetMapping("/list/converter/films/entity")
    public List<String> toListFilmsEntity(@RequestParam(value = "idol", defaultValue = "刘德华") String idol) {


        List<String> list = chatClient.prompt().user(u -> {
            u.text("""
                    帮我找五部{idol}主演的电影
                    """).params(Map.of("idol", idol));
        }).call().entity(new ListOutputConverter(new DefaultConversionService()));

        log.info("ListOutputConverter#toListFilms,size={},resp={}", list.size(), list);
        return list;
    }


    @GetMapping("/map/converter/films")
    public Map<String, Object> toMapFilms(@RequestParam(value = "style", defaultValue = "华语流行") String style) {

        MapOutputConverter mapOutputConverter = new MapOutputConverter();

        String content = chatClient.prompt().user(u -> {
            u.text("""
                    帮我找五部{style}的电影，以电影名为分组键，值为电影信息，电影信息需要包含电影名称、上映时间、导演名、电影简介等内容
                    {format}
                    """).params(Map.of("style", style, "format", mapOutputConverter.getFormat()));
        }).call().content();

        Map<String, Object> resp = mapOutputConverter.convert(content);
        log.info("MapOutputConverter#toListFilms,size={},resp={}", resp.size(), resp);
        return resp;
    }

    @GetMapping("/map/converter/films/entity")
    public Map<String, Object> toMapFilmsEntity(@RequestParam(value = "style", defaultValue = "华语流行") String style) {


        Map<String, Object> resp = chatClient.prompt().user(u -> {
            u.text("""
                    帮我找五部{style}的电影，以电影名为分组键，值为电影信息，电影信息需要包含电影名称、上映时间、导演名、电影简介等内容
                    """).params(Map.of("style", style));
        }).call().entity(new ParameterizedTypeReference<Map<String, Object>>() {
        });

        log.info("MapOutputConverter#toListFilms,size={},resp={}", resp.size(), resp);
        return resp;
    }

    @GetMapping("/weekend-recommendation")
    public List<Film> getWeekendRecommendation() {
        return chatClient.prompt()
                .user("推荐三部适合周末放松的优质电影")
                .call().entity(new ParameterizedTypeReference<List<Film>>() {});
    }


}
