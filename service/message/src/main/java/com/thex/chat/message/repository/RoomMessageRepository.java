package com.thex.chat.message.repository;

import com.thex.chat.message.model.RoomMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomMessageRepository extends JpaRepository<RoomMessage, Long> {
}
