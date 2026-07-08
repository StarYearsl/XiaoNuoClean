package com.xiaonuoclean.command;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class XiaoNuoCleanCommandTest {
    @Test
    void includesShortAliasForMainCommand() {
        assertEquals(List.of("xiaonuoclean", "xnc"), XiaoNuoCleanCommand.rootCommandNames());
    }

    @Test
    void parsesWarningSecondsFromWhitespaceSeparatedText() {
        assertEquals(List.of(10, 5, 1), XiaoNuoCleanCommand.parseWarningSeconds("10 5 0 -2 1"));
    }

    @Test
    void rejectsWarningSecondsTextWithNonIntegerTokens() {
        assertEquals(List.of(), XiaoNuoCleanCommand.parseWarningSeconds("10 soon 5"));
    }
}
