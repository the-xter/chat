package com.thex.chat.emu.script;

import org.springframework.stereotype.Component;

@Component
public class SmokeTests {
    private final Script connectionTest;
    private final Script roomTest;
    private final Script roomNoLeaveTest;

    public SmokeTests(
        ConnectionScripts conn,
        UtilScripts util,
        RoomScripts room
    ) {
        this.connectionTest = new Script("connection smoke test")
            .add(conn.connect())
            .add(util.waitNs(), 15)
            .add(conn.disconnect())
            .add(util.waitNs(), 3);

        this.roomTest = new Script("room smoke test")
            .add(conn.connect())
            .add(room.join())
            .add(util.waitNs(), 15)
            .add(room.leave())
            .add(conn.disconnect())
            .add(util.waitNs(), 3);

        this.roomNoLeaveTest = new Script("room smoke no leave test")
            .add(conn.connect())
            .add(room.join())
            .add(util.waitNs(), 5)
            .add(conn.disconnect())
            .add(util.waitNs(), 3);
    }

    public Script connectionTest() {
        return connectionTest;
    }

    public Script roomTest() {
        return roomTest;
    }

    public Script roomNoLeaveTest() {
        return roomNoLeaveTest;
    }
}
