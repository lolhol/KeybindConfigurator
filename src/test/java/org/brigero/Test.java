package org.brigero;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

public class Test {
    public static void main(String[] args) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        KeybindConfigurator.runFunctions(
            new File("config.toml"),
            new Testing());
    }
}
