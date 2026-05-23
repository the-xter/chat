package com.thex.chat.message.config;

import com.thex.chat.message.messaging.SendRoomMessageRequest;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
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
    public static final String ROOM_MESSAGES_QUEUE = "message.room-messages";

    @Bean
    public TopicExchange chatEventsExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue roomMessagesQueue() {
        return new Queue(ROOM_MESSAGES_QUEUE, true);
    }

    @Bean
    public Binding roomMessageSendBinding(Queue roomMessagesQueue, TopicExchange chatEventsExchange) {
        return BindingBuilder.bind(roomMessagesQueue).to(chatEventsExchange).with("room.message.send");
    }

    @Bean
    public MessageConverter messageConverter() {
        var converter = new JacksonJsonMessageConverter();
        var classMapper = new DefaultClassMapper();
        classMapper.setIdClassMapping(Map.of(
            "SendRoomMessageRequest", SendRoomMessageRequest.class
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

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
        ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        var factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        return factory;
    }
}
