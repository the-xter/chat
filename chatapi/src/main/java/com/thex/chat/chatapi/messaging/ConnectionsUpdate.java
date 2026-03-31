package com.thex.chat.chatapi.messaging;

import java.util.List;

public record ConnectionsUpdate(
        List<String> registered,
        List<String> guests
) {}
