package com.xiaonuoclean.command;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class XiaoNuoCleanCommandTest {
    @Test
    void includesShortAliasForMainCommand() {
        assertEquals(List.of("xiaonuoclean", "xnc"), XiaoNuoCleanCommand.rootCommandNames());
    }
}
