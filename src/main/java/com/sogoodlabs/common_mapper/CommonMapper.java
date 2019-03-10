package com.sogoodlabs.common_mapper;


import com.sogoodlabs.common_mapper.annotations.MapForLazy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

import static com.sogoodlabs.common_mapper.util.GetterSetterUtils.*;

public class CommonMapper {

    private IEntityById entityById;

    public CommonMapper(IEntityById entityById){
        this.entityById = entityById;
    }

    public Map<String, Object> mapToDto(Object entity, Map<String, Object> result) {
//        for (Method m : entity.getClass().getDeclaredMethods()) {
//            if (m.getName().startsWith("get")) {
//                mapFromMethod(entity, result, m);
//            }
//        }
        return mapToDtoWithIncludes(entity, result, null);
    }

    public Map<String, Object> mapToDtoWithIncludes(Object entity, Map<String, Object> result, Set<String> toInclude) {
        for (Method m : entity.getClass().getDeclaredMethods()) {
            if (isGetter(m)
                    && (toInclude==null || (toInclude!=null && toInclude.contains(transformGetterToFieldName(m.getName()))))) {
                mapFromMethod(entity, result, m);
            }
        }
        return result;
    }

    public Map<String, Object> mapToDtoLazy(Object entity, Map<String, Object> result) {
        for (Method m : entity.getClass().getDeclaredMethods()) {

            if (isGetter(m) && m.isAnnotationPresent(MapForLazy.class)) {
                mapFromMethod(entity, result, m);
            }
        }
        return result;
    }


    private void mapFromMethod(Object entity, Map<String, Object> result, Method method) {
        try {
            Object fromGetter = method.invoke(entity);
            if (fromGetter != null) {
                if (fromGetter instanceof String || fromGetter instanceof Number || fromGetter.getClass().isPrimitive()
                    || fromGetter instanceof Boolean) {
                    //Number/String/Boolean
                    result.put(transformGetterToFieldName(method.getName()), fromGetter);
                } else {
                    if(fromGetter.getClass().isEnum()){
                        //Enum
                        Method valueMethod = fromGetter.getClass().getMethod("value");
                        result.put(transformGetterToFieldName(method.getName()), valueMethod.invoke(fromGetter));
                    } else {
                        //Object
                        Method getIdMethod = fromGetter.getClass().getMethod("getId");
                        long id = (long) getIdMethod.invoke(fromGetter);
                        result.put(transformGetterToFieldName(method.getName()) + "id", id);
                    }

                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }


    //-------------------------- Map to Entity -----------------------------------------------

    public Object mapToEntity(Map<String, Object> dto, Object entity) {
        try {
            for (Map.Entry<String, Object> entry : dto.entrySet()) {
                if (entry.getValue() != null){
                    if (entry.getKey().length() > 2 && entry.getKey().endsWith("id")) {
                        //Object
                        String fieldName = entry.getKey().substring(0, entry.getKey().length() - 2);
                        Class clazz = defineTypeByGetter(entity.getClass(), fieldName);
                        if(clazz!=null){
                            Method setter = entity.getClass().getMethod(transformToSetter(fieldName), clazz);
                            setter.invoke(entity,
                                    entityById.get(Long.parseLong("" + entry.getValue()), clazz));
                        }
                    } else {
                        Class clazz = defineTypeByGetter(entity.getClass(), entry.getKey());
                        if (clazz != null) {
                            mapToEntityBasicTypes(entity, entry, clazz);
                        }
                    }
                }
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        return entity;
    }

    private void mapToEntityBasicTypes(Object entity, Map.Entry<String, Object> entry, Class clazz) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method setter = entity.getClass().getMethod(transformToSetter(entry.getKey()), clazz);
        if (Number.class.isAssignableFrom(clazz) || clazz.isPrimitive()) {
            if(clazz==Boolean.class || clazz==boolean.class){
                //Boolean
                setter.invoke(entity, entry.getValue());
            } else {
                //Number
                setter.invoke(entity, numberParse(""+entry.getValue(), clazz));
            }
        } else {
            if(clazz.isEnum()){
                //Enum
                setter.invoke(entity, getEnumVal((String) entry.getValue(), clazz));
            } else {
                //String
                setter.invoke(entity, entry.getValue());
            }
        }
    }

    private Number numberParse(String val, Class clazz){
        if(clazz == Integer.class || clazz == int.class){
            return Integer.parseInt(val);
        }
        if(clazz == Long.class || clazz == long.class){
            return Integer.parseInt(val);
        }
        throw new RuntimeException("numberParse: Not supported number class " + clazz.getName());
    }


    public static Object getEnumVal(String stringval, Class enumClass) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method valueMethod = enumClass.getMethod("value");
        Method valuesMethod = enumClass.getMethod("values");
        for(Object enumVal : (Object[]) valuesMethod.invoke(enumClass)){
            if(valueMethod.invoke(enumVal).equals(stringval)){
                return enumVal;
            }
        }
        return null;
    }

}
