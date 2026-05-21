package com.thex.chat.emu.performer;

import com.thex.chat.emu.user.User;

public record PerformerParameters(
    User user,
    int runId,
    Object options
) {
}
