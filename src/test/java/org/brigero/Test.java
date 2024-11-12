package org.brigero;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;

public class Test {
    public static void main(String[] args)
            throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, FileNotFoundException {
        KeybindConfigurator.runFunctions(
                "config",
                "config/main.lua",
                new Testing());
    }
}
