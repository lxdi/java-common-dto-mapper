package com.sogoodlabs.common_mapper;


import com.sogoodlabs.common_mapper.annotations.IncludeInDto;
import com.sogoodlabs.common_mapper.annotations.MapForLazy;
import com.sogoodlabs.common_mapper.annotations.MapToClass;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

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
        simple, enumeration, obj, list
    }

    private void mapFromMethod(Object entity, Map<String, Object> result, Method method) {
        try {
            MappingType mappingType = defineMappingType(method);
            Object fromGetter = method.invoke(entity);

            if (customMapping(entity, result, method, fromGetter)) {
                return;
            }

            if (fromGetter == null) {
                if (configuration.mapEmptyFields) {
                    if (mappingType == MappingType.obj) {
                        result.put(transformGetterToFieldName(method.getName()) + configuration.idOffset, null);
                    } else {
                        result.put(transformGetterToFieldName(method.getName()), null);
                    }
                }
                return;
            }

            if (mappingType == MappingType.simple) {
                //Number/String/Boolean
                result.put(transformGetterToFieldName(method.getName()), fromGetter);
                return;
            }

            if (mappingType == MappingType.list) {
                //List

                if(!method.isAnnotationPresent(IncludeInDto.class)){
                    return;
                }

                List<Map<String, Object>> list = new ArrayList<>();
                ((List)fromGetter).forEach(obj -> list.add(mapToDto(obj)));
                result.put(transformGetterToFieldName(method.getName()), list);
                return;
            }

            if (mappingType == MappingType.enumeration) {
                //Enum
                Method valueMethod = fromGetter.getClass().getMethod("value");
                result.put(transformGetterToFieldName(method.getName()), valueMethod.invoke(fromGetter));
                return;
            }

            //Object
            if(method.isAnnotationPresent(IncludeInDto.class)){
                result.put(transformGetterToFieldName(method.getName()), mapToDto(fromGetter));
                return;
            }

            Method getIdMethod = fromGetter.getClass().getMethod("getId");
            Object id = getIdMethod.invoke(fromGetter);
            result.put(transformGetterToFieldName(method.getName()) + configuration.idOffset, id);

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

        if(returnType.isAssignableFrom(List.class)){
            return MappingType.list;
        }

        return MappingType.obj;
    }

    public boolean customMapping(Object entity, Map<String, Object> result, Method method, Object fromGetter){
        return false;
    }


    //-------------------------- Map to Entity -----------------------------------------------

    public <T> T mapToEntity(Map<String, Object> dto, T entity) {
        return mapToEntity(dto, entity, null);
    }

    static class PredefinedEntity {
        private Object value;
        private String fieldName;

        public PredefinedEntity(Object value, String fieldName) {
            this.value = value;
            this.fieldName = fieldName;
        }
    }

    public <T> T mapToEntity(Map<String, Object> dto, T entity, List<PredefinedEntity> predefinedEntities) {
        for (Map.Entry<String, Object> entry : dto.entrySet()) {
            try {
                if (entry.getValue() == null) {
                    continue;
                }

                if (entry.getKey().length() > 2 && entry.getKey().endsWith(configuration.idOffset)) {

                    //Object
                    String fieldName = entry.getKey().substring(0, entry.getKey().length() - 2);
                    Class fieldClass = defineTypeByGetter(entity.getClass(), fieldName);

                    if (fieldClass == null) {
                        continue;
                    }

                    Method setter = entity.getClass().getMethod(transformToSetter(fieldName), fieldClass);
                    Object id = entry.getValue();

                    if (id instanceof Integer) {
                        id = ((Integer) id).longValue();
                    }

                    var preDefEnt = getPredefinedEntity(predefinedEntities, fieldClass, fieldName);

                    if(preDefEnt != null){
                        setter.invoke(entity, preDefEnt);
                    } else {
                        setter.invoke(entity, entityById.get(id, fieldClass));
                    }

                } else {
                    Class clazz = defineTypeByGetter(entity.getClass(), entry.getKey());

                    if(clazz==null){
                        continue;
                    }

                    mapToEntityBasicTypes(entity, entry, clazz);
                }

            } catch (NoSuchMethodException e) {
                //e.printStackTrace();
            } catch (IllegalAccessException e) {
                //e.printStackTrace();
            } catch (InvocationTargetException e) {
                //e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        }

        return entity;
    }

    private Object getPredefinedEntity(List<PredefinedEntity> predefinedEntities, Class clazz, String fieldName){
        if (predefinedEntities == null){
            return null;
        }

        for(var entity : predefinedEntities) {
            if (entity.value.getClass() == clazz && entity.fieldName.equals(fieldName)) {
                return entity.value;
            }
        }
        return null;
    }

    private void mapToEntityBasicTypes(Object entity, Map.Entry<String, Object> entry, Class clazz) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
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
            if(clazz == String.class){
                //String
                setter.invoke(entity, entry.getValue());
            }

            if(clazz.isAssignableFrom(List.class) && setter.isAnnotationPresent(MapToClass.class)){
                //List
                MapToClass mapToClass = setter.getAnnotation(MapToClass.class);
                Class listOfCls = mapToClass.value();
                List entityList = new ArrayList();

                List<PredefinedEntity> preDefEnts = null;

                if(!mapToClass.parentField().isEmpty()) {
                    preDefEnts = List.of(new PredefinedEntity(entity, mapToClass.parentField()));
                }

                for(Map<String, Object> dto : (List<Map<String, Object>>) entry.getValue()){

                    if (!mapToClass.parentField().isEmpty() && dto.get(mapToClass.parentField()+configuration.idOffset) == null){
                        dto = new HashMap<>(dto);
                        dto.put(mapToClass.parentField()+configuration.idOffset, "");
                    }

                    entityList.add(mapToEntity(dto, listOfCls.getConstructor().newInstance(), preDefEnts));
                }

                setter.invoke(entity, entityList);
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
