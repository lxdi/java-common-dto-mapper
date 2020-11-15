package com.sogoodlabs.common_mapper;


import com.sogoodlabs.common_mapper.annotations.MapForLazy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.sogoodlabs.common_mapper.util.GetterSetterUtils.*;

public class CommonMapper {

    private IEntityById entityById;
    private Configuration configuration;

    public CommonMapper(IEntityById entityById){
        this.entityById = entityById;
        this.configuration = new Configuration();
    }

    public CommonMapper(IEntityById entityById, Configuration configuration){
        this.entityById = entityById;
        this.configuration = configuration;
    }

    public Map<String, Object> mapToDto(Object entity, Map<String, Object> result) {
        return mapToDtoWithIncludes(entity, result, null);
    }

    public Map<String, Object> mapToDto(Object entity) {
        return mapToDto(entity, new HashMap<>());
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

    enum MappingType{
        simple, enumeration, obj
    }

    private void mapFromMethod(Object entity, Map<String, Object> result, Method method) {
        try {
            MappingType mappingType = defineMappingType(method);
            Object fromGetter = method.invoke(entity);
            if (!customMapping(entity, result, method, fromGetter)) {
                if (fromGetter != null) {
                    if (mappingType == MappingType.simple) {
                        //Number/String/Boolean
                        result.put(transformGetterToFieldName(method.getName()), fromGetter);
                    } else {
                        if (mappingType == MappingType.enumeration) {
                            //Enum
                            Method valueMethod = fromGetter.getClass().getMethod("value");
                            result.put(transformGetterToFieldName(method.getName()), valueMethod.invoke(fromGetter));
                        } else {
                            //Object
                            Method getIdMethod = fromGetter.getClass().getMethod("getId");
                            Object id = getIdMethod.invoke(fromGetter);
                            result.put(transformGetterToFieldName(method.getName()) + configuration.idOffset, id);
                        }

                    }
                } else {
                    if(configuration.mapEmptyFields){
                        if(mappingType == MappingType.obj)
                            result.put(transformGetterToFieldName(method.getName()) + configuration.idOffset, null);
                        else
                            result.put(transformGetterToFieldName(method.getName()), null);
                    }
                }
            }
        } catch (IllegalAccessException e) {
            //e.printStackTrace();
        } catch (InvocationTargetException e) {
            //e.printStackTrace();
        } catch (NoSuchMethodException e) {
            //e.printStackTrace();
        }
    }

    private MappingType defineMappingType(Method method){
        Class returnType = method.getReturnType();
        if(returnType.isAssignableFrom(String.class) || returnType.isAssignableFrom(Number.class)
                || returnType.isPrimitive() || returnType.isAssignableFrom(Boolean.class)){
            return MappingType.simple;
        }
        if(returnType.isEnum()){
            return MappingType.enumeration;
        }
        return MappingType.obj;
    }

    public boolean customMapping(Object entity, Map<String, Object> result, Method method, Object fromGetter){
        return false;
    }


    //-------------------------- Map to Entity -----------------------------------------------

    public <T> T mapToEntity(Map<String, Object> dto, T entity) {
        for (Map.Entry<String, Object> entry : dto.entrySet()) {
            try {
                if (entry.getValue() != null) {
                    if (entry.getKey().length() > 2 && entry.getKey().endsWith(configuration.idOffset)) {
                        //Object
                        String fieldName = entry.getKey().substring(0, entry.getKey().length() - 2);
                        Class clazz = defineTypeByGetter(entity.getClass(), fieldName);
                        if (clazz != null) {
                            Method setter = entity.getClass().getMethod(transformToSetter(fieldName), clazz);
                            Object id = entry.getValue();
                            if(id instanceof Integer){
                                id = new Long((Integer)id);
                            }
                            setter.invoke(entity, entityById.get(id, clazz));
                        }
                    } else {
                        Class clazz = defineTypeByGetter(entity.getClass(), entry.getKey());
                        if (clazz != null) {
                            mapToEntityBasicTypes(entity, entry, clazz);
                        }
                    }
                }
            } catch (NoSuchMethodException e) {
                //e.printStackTrace();
            } catch (IllegalAccessException e) {
                //e.printStackTrace();
            } catch (InvocationTargetException e) {
                //e.printStackTrace();
            }
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
            }
            if(clazz.isAssignableFrom(String.class)){
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
