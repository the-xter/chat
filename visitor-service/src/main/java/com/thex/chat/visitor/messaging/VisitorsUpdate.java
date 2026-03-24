package com.thex.chat.visitor.messaging;

import java.util.List;

public record VisitorsUpdate(
        List<String> registered,
        List<String> guests
) {}
