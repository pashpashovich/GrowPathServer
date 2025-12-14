package by.bsuir.growpathserver.trainee.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;

@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper mapper = builder
                .featuresToDisable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .featuresToDisable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
                .featuresToEnable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
                .featuresToEnable(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT)
                .featuresToEnable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
                .propertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE)
                .build();

        // Разрешить использование конструкторов через рефлексию
        mapper.setVisibility(new VisibilityChecker.Std(JsonAutoDetect.Visibility.DEFAULT)
                                     .withCreatorVisibility(JsonAutoDetect.Visibility.ANY));

        return mapper;
    }
}
