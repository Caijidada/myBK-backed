package com.blog.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * Jackson配置
 */
@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper objectMapper = builder.createXmlMapper(false).build();
        // 禁用Unicode转义，使JSON中的中文可读
        objectMapper.getFactory().configure(
            com.fasterxml.jackson.core.JsonGenerator.Feature.ESCAPE_NON_ASCII,
            false
        );
        return objectMapper;
    }
}
