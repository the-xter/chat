package com.thex.chat.connection.config;

import com.thex.chat.connection.messaging.ConnectionsUpdate;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "chat.events";

    @Bean
    public TopicExchange chatEventsExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public MessageConverter messageConverter() {
        var converter = new JacksonJsonMessageConverter();
        var classMapper = new DefaultClassMapper();
        classMapper.setIdClassMapping(Map.of(
                "ConnectionsUpdate", ConnectionsUpdate.class
        ));
        classMapper.setTrustedPackages("*");
        classMapper.afterPropertiesSet();
        converter.setClassMapper(classMapper);
        return converter;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        var template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }
}
