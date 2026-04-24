package com.thex.chat.chatapi.config;

import com.thex.chat.chatapi.messaging.JoinRoomRequest;
import com.thex.chat.chatapi.messaging.LeaveRoomRequest;
import com.thex.chat.chatapi.messaging.RoomVisitors;
import com.thex.chat.chatapi.messaging.ConnectionEvent;
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
    public static final String CONNECTION_UPDATES_QUEUE = "chatapi.connection-updates";
    public static final String ROOM_VISITORS_QUEUE = "chatapi.room-visitors";

    @Bean
    public TopicExchange chatEventsExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue connectionUpdatesQueue() {
        return new Queue(CONNECTION_UPDATES_QUEUE, true);
    }

    @Bean
    public Queue roomVisitorsQueue() {
        return new Queue(ROOM_VISITORS_QUEUE, true);
    }

    @Bean
    public Binding connectionUpdatesBinding(Queue connectionUpdatesQueue, TopicExchange chatEventsExchange) {
        return BindingBuilder.bind(connectionUpdatesQueue).to(chatEventsExchange).with("connection.*");
    }

    @Bean
    public Binding roomVisitorsBinding(Queue roomVisitorsQueue, TopicExchange chatEventsExchange) {
        return BindingBuilder.bind(roomVisitorsQueue).to(chatEventsExchange).with("room.visitors");
    }

    @Bean
    public MessageConverter messageConverter() {
        var converter = new JacksonJsonMessageConverter();
        var classMapper = new DefaultClassMapper();
        classMapper.setIdClassMapping(Map.of(
            "ConnectionEvent", ConnectionEvent.class,
            "JoinRoomRequest", JoinRoomRequest.class,
            "LeaveRoomRequest", LeaveRoomRequest.class,
            "RoomVisitors", RoomVisitors.class
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
