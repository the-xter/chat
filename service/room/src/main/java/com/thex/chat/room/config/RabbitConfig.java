package com.thex.chat.room.config;

import com.thex.chat.room.messaging.*;
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
    public static final String ROOM_EVENTS_QUEUE = "room.room-events";

    @Bean
    public TopicExchange chatEventsExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue roomEventsQueue() {
        return new Queue(ROOM_EVENTS_QUEUE, true);
    }

    @Bean
    public Binding roomJoinBinding(Queue roomEventsQueue, TopicExchange chatEventsExchange) {
        return BindingBuilder.bind(roomEventsQueue).to(chatEventsExchange).with("room.join.request");
    }

    @Bean
    public Binding roomLeaveBinding(Queue roomEventsQueue, TopicExchange chatEventsExchange) {
        return BindingBuilder.bind(roomEventsQueue).to(chatEventsExchange).with("room.leave.request");
    }

    @Bean
    public Binding connectionConnectedBinding(Queue roomEventsQueue, TopicExchange chatEventsExchange) {
        return BindingBuilder.bind(roomEventsQueue).to(chatEventsExchange).with("connection.connected");
    }

    @Bean
    public Binding connectionDisconnectedBinding(Queue roomEventsQueue, TopicExchange chatEventsExchange) {
        return BindingBuilder.bind(roomEventsQueue).to(chatEventsExchange).with("connection.disconnected");
    }

    @Bean
    public Binding roomMessageNewBinding(Queue roomEventsQueue, TopicExchange chatEventsExchange) {
        return BindingBuilder.bind(roomEventsQueue).to(chatEventsExchange).with("room.message.new");
    }

    @Bean
    public MessageConverter messageConverter() {
        var converter = new JacksonJsonMessageConverter();
        var classMapper = new DefaultClassMapper();
        classMapper.setIdClassMapping(Map.of(
            "JoinRoomRequest", JoinRoomRequest.class,
            "LeaveRoomRequest", LeaveRoomRequest.class,
            "RoomVisitors", RoomVisitors.class,
            "RoomVisitorUpdate", RoomVisitorUpdate.class,
            "ConnectionEvent", ConnectionEvent.class,
            "RoomMessageEvent", RoomMessageEvent.class
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
