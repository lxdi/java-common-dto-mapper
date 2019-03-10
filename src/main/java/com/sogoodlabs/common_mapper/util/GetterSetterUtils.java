package com.sogoodlabs.common_mapper.util;

import java.lang.reflect.Method;

public class GetterSetterUtils {

    public static Class defineTypeByGetter(Class clazz, String field){
        String getter = transformToGetter(field);
        try {
            return clazz.getMethod(getter).getReturnType();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String transformGetterToFieldName(String getter){
        getter = getter.substring(3);
        return Character.toLowerCase(getter.charAt(0)) + getter.substring(1);
    }

    public static String transformToSetter(String str){
        return "set" + Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    public static String transformToGetter(String str){
        return "get" + Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    public static boolean isGetter(Method method){
        return method.getName().startsWith("get");
    }

}
