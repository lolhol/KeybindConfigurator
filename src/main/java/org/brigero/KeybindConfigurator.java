package org.brigero;

import com.moandjiezana.toml.Toml;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class KeybindConfigurator {

    public static <J> void runFunctions(File configPath, J testingInstance) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        Toml toml = new Toml().read(configPath);
        Map<String, Object> config = toml.toMap();

        if (config == null) {
            System.err.println("Config is null! Stopping!");
            return;
        }

        System.out.println(config);

        processFields(config, testingInstance);
    }

    private static <J> void processFields(Map<String, Object> configMap, J testingInstance) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class<?> clazz = testingInstance.getClass();

        for (Map.Entry<String, Object> entry : configMap.entrySet()) {
            String methodName = entry.getKey();
            Object fieldValue = entry.getValue();

            if (fieldValue instanceof Map) {
                Map<String, Object> params = (Map<String, Object>) fieldValue;
                Method method = findMatchingMethod(clazz, methodName, params);

                if (method != null) {
                    Object[] castedParams = castParameters(method.getParameterTypes(), params);
                    method.invoke(testingInstance, castedParams);
                } else {
                    System.err.println("No matching method found for: " + methodName);
                }
            }
        }
    }

    private static Method findMatchingMethod(Class<?> clazz, String methodName, Map<String, Object> params) {
        for (Method method : clazz.getMethods()) {
            if (method.getName().equals(methodName) && method.getParameterCount() == params.size()) {
                Class<?>[] paramTypes = method.getParameterTypes();
                if (isParameterMatch(paramTypes, params.values())) {
                    return method;
                }
            }
        }
        return null;
    }

    private static boolean isParameterMatch(Class<?>[] paramTypes, Iterable<Object> paramValues) {
        int i = 0;
        for (Object value : paramValues) {
            if (!paramTypes[i].isAssignableFrom(value.getClass())) {
                if (paramTypes[i].isArray() && List.class.isAssignableFrom(value.getClass())) {
                    continue; // allow List to be converted to array
                }
                return false;
            }
            i++;
        }
        return true;
    }

    private static Object[] castParameters(Class<?>[] paramTypes, Map<String, Object> params) {
        Object[] castedParams = new Object[paramTypes.length];
        int i = 0;
        for (Object value : params.values()) {
            if (paramTypes[i].isArray() && value instanceof List) {
                castedParams[i] = convertListToArray((List<?>) value, paramTypes[i].getComponentType());
            } else {
                castedParams[i] = paramTypes[i].cast(value);
            }
            i++;
        }
        return castedParams;
    }

    private static Object convertListToArray(List<?> list, Class<?> componentType) {
        Object array = Array.newInstance(componentType, list.size());

        for (int i = 0; i < list.size(); i++) {
            Object value = list.get(i);

            if (componentType == int.class) {
                Array.set(array, i, ((Number) value).intValue());
            } else if (componentType == double.class) {
                Array.set(array, i, ((Number) value).doubleValue());
            } else if (componentType == long.class) {
                Array.set(array, i, ((Number) value).longValue());
            } else if (componentType == float.class) {
                Array.set(array, i, ((Number) value).floatValue());
            } else if (componentType == short.class) {
                Array.set(array, i, ((Number) value).shortValue());
            } else if (componentType == byte.class) {
                Array.set(array, i, ((Number) value).byteValue());
            } else {
                Array.set(array, i, componentType.cast(value));
            }
        }

        return array;
    }

}
