package com.thex.chat.chatapi.chat.room;

import com.thex.chat.chatapi.chat.CometdDeliverer;
import com.thex.chat.chatapi.config.RabbitConfig;
import com.thex.chat.chatapi.messaging.RoomMessageDelivery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RabbitListener(queues = RabbitConfig.ROOM_MESSAGES_QUEUE)
public class RoomMessagesListener {

    private final CometdDeliverer deliverer;

    public RoomMessagesListener(CometdDeliverer deliverer) {
        this.deliverer = deliverer.createWith("room-messages");
    }

    @RabbitHandler
    public void onRoomMessageDelivery(RoomMessageDelivery delivery) {
        deliverer.deliver(
            delivery.recipients(),
            "/room/" + delivery.roomId() + "/message",
            toTransport(delivery)
        );
    }

    private Object toTransport(RoomMessageDelivery delivery) {
        return Map.of(
            "messageId", delivery.messageId(),
            "roomId", delivery.roomId(),
            "sender", delivery.sender(),
            "text", delivery.text(),
            "createdAt", delivery.createdAt()
        );
    }

    @RabbitHandler(isDefault = true)
    public void onUnknown(Object message) {
        log.warn("Unknown room messages message type: {}", message.getClass().getSimpleName());
    }
}
