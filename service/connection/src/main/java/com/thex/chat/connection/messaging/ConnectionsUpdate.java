package com.thex.chat.connection.messaging;

import java.util.List;

public record ConnectionsUpdate(
        List<String> registered,
        List<String> guests
) {}
