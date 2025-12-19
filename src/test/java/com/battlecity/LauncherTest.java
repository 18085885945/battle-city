package com.battlecity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class LauncherTest {

    @Test
    void launcherClassShouldBeLoadable() {
        assertDoesNotThrow(() -> Class.forName(Launcher.class.getName()));
    }
}
