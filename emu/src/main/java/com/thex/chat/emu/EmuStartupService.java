package com.thex.chat.emu;

import com.thex.chat.emu.script.Script;
import com.thex.chat.emu.script.ScriptsLibrary;
import com.thex.chat.emu.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmuStartupService {
    private final EmuProperties properties;
    private final ScriptsLibrary library;

    @EventListener(ApplicationReadyEvent.class)
    public void connectAll() {
        List<EmuProperties.EmuUser> users = properties.users();
        if (users == null || users.isEmpty()) {
            log.info("No emulator users configured");
            return;
        }

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (var user : users) {
                executor.execute(() -> runUser(user));
            }
        }
    }

    private final AtomicInteger runCounter = new AtomicInteger();
    private void runUser(EmuProperties.EmuUser credentials) {
        boolean allOk = true;
        while (allOk) {
            int runId = runCounter.incrementAndGet();
            Script script = library.pickScript();
            User user = new User(credentials.name(), credentials.password());
            log.info("Run script {} with runId {} for {}", script, runId, user);
            try {
                script.perform(user, runId);
            } catch (Exception e) {
                log.error("Error of script running {} for {}", script, user, e);
                allOk = false;
            }
        }
    }
}
