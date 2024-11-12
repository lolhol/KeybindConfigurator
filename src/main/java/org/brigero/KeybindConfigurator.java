package org.brigero;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.jse.JsePlatform;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

public class KeybindConfigurator {

    public static <J> void runFunctions(String luaScriptDirectory, String mainLuaFilePath, J testingInstance)
            throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, FileNotFoundException {
        Globals globals = JsePlatform.standardGlobals();
        String packagePath = luaScriptDirectory + "/?.lua;" + luaScriptDirectory + "/?/init.lua;" +
                luaScriptDirectory + "/**/*.lua";
        globals.set("package.path", LuaValue.valueOf(packagePath));

        FileInputStream fis = new FileInputStream(new File(mainLuaFilePath));
        LuaValue chunk = globals.load(fis, "myLuaScript", "bt", globals);

        LuaValue result = chunk.call();

        processLuaTable(result, testingInstance);
    }

    private static <J> void processLuaTable(LuaValue value, J testingInstance)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if (value.istable()) {
            LuaTable table = (LuaTable) value;
            LuaValue key = LuaValue.NIL;
            while (true) {
                Varargs n = table.next(key);
                key = n.arg1();
                LuaValue val = n.arg(2);
                if (key.isnil())
                    break;

                if (key.isstring() && val.isfunction()) {
                    String methodName = key.tojstring();
                    LuaFunction luaFunction = (LuaFunction) val;

                    Varargs luaReturnValues = luaFunction.invoke();

                    List<Object> parameters = new ArrayList<>();
                    for (int i = 1; i <= luaReturnValues.narg(); i++) {
                        LuaValue luaParam = luaReturnValues.arg(i);
                        // System.out.println(luaParam);
                        Object param = convertLuaValueToJava(luaParam);
                        parameters.add(param);
                        // System.out.println(param.getClass());
                    }

                    Method method = findMatchingMethod(testingInstance.getClass(), methodName, parameters);
                    if (method != null) {
                        method.invoke(testingInstance, parameters.toArray());
                    } else {
                        System.err.println("No matching method found for: " + methodName);
                    }
                }
            }
        }
    }

    /**
     * Converts a Lua value to a Java object.
     * 
     * @param val The Lua value to convert.
     * @return The converted Java object.
     * 
     * @note this is a hack fix. TODO: fix this in the future.
     */
    private static Object convertLuaValueToJava(LuaValue val) {
        String stringValue = val.tojstring();

        // Try to convert to Integer
        try {
            return Integer.parseInt(stringValue);
        } catch (NumberFormatException e) {
            // Not an integer, move to the next type
        }

        // Try to convert to Double
        try {
            return Double.parseDouble(stringValue);
        } catch (NumberFormatException e) {
            // Not a double, move to the next type
        }

        // Try to convert to Boolean
        if (stringValue.equalsIgnoreCase("true") || stringValue.equalsIgnoreCase("false")) {
            return Boolean.parseBoolean(stringValue);
        }

        // If it's a Lua table, convert to a Java List
        if (val.istable()) {
            LuaTable table = (LuaTable) val;
            List<Object> list = new ArrayList<>();
            for (int i = 1; i <= table.length(); i++) {
                LuaValue element = table.get(i);
                list.add(convertLuaValueToJava(element)); // Recursively convert elements
            }
            // System.out.println(list);
            return list;
        }

        // If no other type matched, return as a String
        return stringValue;
    }

    private static Method findMatchingMethod(Class<?> clazz, String methodName, List<Object> params) {
        for (Method method : clazz.getMethods()) {
            if (method.getName().equals(methodName) && method.getParameterCount() == params.size()) {
                if (isParameterMatch(method.getParameters(), params)) {
                    return method;
                }
            }
        }
        return null;
    }

    private static boolean isParameterMatch(Parameter[] paramTypes, List<Object> paramValues) {
        for (int i = 0; i < paramTypes.length; i++) {
            Class<?> paramType = paramTypes[i].getType();
            Object paramValue = paramValues.get(i);
            if (paramValue == null) {
                if (paramType.isPrimitive())
                    return false;
            } else {
                Class<?> valueType = paramValue.getClass();
                if (!isAssignable(paramType, valueType)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean isAssignable(Class<?> paramType, Class<?> valueType) {
        if (paramType.isAssignableFrom(valueType)) {
            return true;
        }
        if (paramType.isPrimitive()) {
            if ((paramType == int.class && valueType == Integer.class)
                    || (paramType == double.class && valueType == Double.class)
                    || (paramType == boolean.class && valueType == Boolean.class)
                    || (paramType == long.class && valueType == Long.class)
                    || (paramType == float.class && valueType == Float.class)
                    || (paramType == char.class && valueType == Character.class)
                    || (paramType == byte.class && valueType == Byte.class)
                    || (paramType == short.class && valueType == Short.class)) {
                return true;
            }
        }
        if (paramType.isAssignableFrom(List.class) && List.class.isAssignableFrom(valueType)) {
            return true;
        }
        return false;
    }
}