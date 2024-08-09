package org.brigero;

import java.io.File;

public class Test {
    public static void main(String[] args) {
        KeybindConfigurator.runFunctions(
            new File("config.toml"),
            Config.class,
            new Testing());
    }
}
