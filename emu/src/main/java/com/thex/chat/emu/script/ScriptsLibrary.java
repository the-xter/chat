package com.thex.chat.emu.script;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Random;

@Component
@Slf4j
public class ScriptsLibrary {
    private final Random rand = new Random();
    private final ArrayList<Script> library = new ArrayList<>(50);

    public ScriptsLibrary(
        SmokeTests smokeTests
    ) {
        library.add(smokeTests.connectionTest());
        library.add(smokeTests.roomTest());
        library.add(smokeTests.roomNoLeaveTest());
    }

    public Script pickScript() {
        int index = rand.nextInt(library.size());
        return library.get(index);
    }
}
