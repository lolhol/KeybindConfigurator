package org.brigero;

import com.moandjiezana.toml.Toml;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class KeybindConfigurator {

    public static <T, J> void runFunctions(File configPath, Class<T> configClass, J testingInstance) {
        Toml toml = new Toml().read(configPath);
        T config = toml.to(configClass);

        if (config == null) {
            System.err.println("Config is null! Stopping!");
            return;
        }

        processFields(config, testingInstance);
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
                        IEntree entreeInstance = (IEntree) fieldValue;
                        String functionName = entreeInstance.getFunctionName();

                        // Explicitly cast to Object[]
                        Object[] additionalOptions = (Object[]) entreeInstance.getAdditionalOptions();

                        // Dynamically determine parameter types for the method
                        Class<?>[] paramTypes = new Class<?>[additionalOptions.length + 1];
                        paramTypes[0] = String.class;  // First parameter is the field name
                        for (int i = 0; i < additionalOptions.length; i++) {
                            paramTypes[i + 1] = additionalOptions[i].getClass();
                        }

                        Method method = testingInstance.getClass().getMethod(functionName, paramTypes);
                        Object[] args = new Object[additionalOptions.length + 1];
                        args[0] = field.getName();  // Pass the field name as the first argument
                        System.arraycopy(additionalOptions, 0, args, 1, additionalOptions.length);

                        method.invoke(testingInstance, args);

                    } else if (fieldValue instanceof String) {
                        // Field is a String representing a function name
                        String functionName = (String) fieldValue;

                        Method method = testingInstance.getClass().getMethod(functionName, String.class);
                        method.invoke(testingInstance, field.getName());

                    } else if (fieldValue.getClass().isArray()) {
                        // Handle arrays
                        for (int i = 0; i < java.lang.reflect.Array.getLength(fieldValue); i++) {
                            Object element = java.lang.reflect.Array.get(fieldValue, i);
                            processFields(element, testingInstance);
                        }

                    } else if (fieldValue instanceof Iterable) {
                        // Handle Iterable (e.g., List)
                        for (Object element : (Iterable<?>) fieldValue) {
                            processFields(element, testingInstance);
                        }

                    } else if (!field.getType().isPrimitive() && !field.getType().isEnum() && !field.getType().equals(String.class)) {
                        // Recursively process fields of nested classes
                        processFields(fieldValue, testingInstance);
                    }
                }

            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                System.err.println("Error processing field: " + field.getName() + " in class: " + clazz.getName());
                e.printStackTrace();
            }
        }
    }
}
