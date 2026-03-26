package com.thex.chat.room.config;

import com.thex.chat.room.messaging.RoomEvent;
import com.thex.chat.room.messaging.RoomUpdate;
import com.thex.chat.room.messaging.SessionEvent;
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
    public static final String SESSION_EVENTS_QUEUE = "room-service.session-events";
    public static final String ROOM_EVENTS_QUEUE = "room-service.room-events";

    @Bean
    public TopicExchange chatEventsExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue sessionEventsQueue() {
        return new Queue(SESSION_EVENTS_QUEUE, true);
    }

    @Bean
    public Queue roomEventsQueue() {
        return new Queue(ROOM_EVENTS_QUEUE, true);
    }

    @Bean
    public Binding sessionEventsBinding(Queue sessionEventsQueue, TopicExchange chatEventsExchange) {
        return BindingBuilder.bind(sessionEventsQueue).to(chatEventsExchange).with("session.*");
    }

    @Bean
    public Binding roomEventsBinding(Queue roomEventsQueue, TopicExchange chatEventsExchange) {
        return BindingBuilder.bind(roomEventsQueue).to(chatEventsExchange).with("room.*");
    }

    @Bean
    public MessageConverter messageConverter() {
        var converter = new JacksonJsonMessageConverter();
        var classMapper = new DefaultClassMapper();
        classMapper.setIdClassMapping(Map.of(
                "SessionEvent", SessionEvent.class,
                "RoomEvent", RoomEvent.class,
                "RoomUpdate", RoomUpdate.class
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
