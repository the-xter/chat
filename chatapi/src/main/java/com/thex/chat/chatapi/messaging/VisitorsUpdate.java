package com.thex.chat.chatapi.messaging;

import java.util.List;

public record VisitorsUpdate(
        List<String> registered,
        List<String> guests
) {}
