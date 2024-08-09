package org.brigero;

import com.moandjiezana.toml.Toml;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class KeybindConfigurator {

    public static <T, J> void runFunctions(File configPath, Class<T> configClass, J testing) {
        Toml toml = new Toml().read(configPath);
        T config = toml.to(configClass);

        if (config == null) {
            System.err.println("Config is null! Stopping!");
            return;
        }

        processFields(config, testing);
    }

    private static <J> void processFields(Object obj, J testingInstance) {
        Class<?> clazz = obj.getClass();
        Class<IEntree> myInterface = IEntree.class;

        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);  // Allow access to private fields

            try {
                Object fieldValue = field.get(obj);

                if (fieldValue != null) {
                    if (myInterface.isAssignableFrom(field.getType())) {
                        // Field implements IEntree
                        IEntree<?> entreeInstance = (IEntree<?>) fieldValue;
                        String functionName = entreeInstance.getFunctionName();
                        Object additionalOptions = entreeInstance.getAdditionalOptions();

                        Method method = testingInstance.getClass()
                            .getMethod(functionName, String.class, additionalOptions.getClass());
                        method.invoke(testingInstance, field.getName(), additionalOptions);

                    } else if (fieldValue instanceof String) {
                        // Field is a String representing a function name
                        String functionName = (String) fieldValue;

                        Method method = testingInstance.getClass().getMethod(functionName, String.class);
                        method.invoke(testingInstance, field.getName());

                    } else if (fieldValue.getClass().isArray() || fieldValue instanceof Iterable) {
                        // Field is an array or iterable; handle each element (for flexibility)
                        for (Object element : (Iterable<?>) fieldValue) {
                            processFields(element, testingInstance);
                        }

                    } else if (!field.getType().isPrimitive() && !field.getType().isEnum() && !field.getType().equals(String.class)) {
                        // Recursively process fields of nested classes
                        processFields(fieldValue, testingInstance);
                    }
                }

            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
    }
}
