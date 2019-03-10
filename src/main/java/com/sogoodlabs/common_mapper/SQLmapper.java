package com.sogoodlabs.common_mapper;

import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class SQLmapper {

    private DataSource dataSource;
    private CommonMapper commonMapper;

    public SQLmapper(DataSource dataSource){
        this.dataSource = dataSource;
    }

    public SQLmapper(DataSource dataSource, CommonMapper commonMapper){
        this.dataSource = dataSource;
        this.commonMapper = commonMapper;
    }

    public Map<String, Object> mapToDto(long id, Class clazz, Map<String, Object> result){
        String tableName = getTableName(clazz);
        try {
            Connection connection = dataSource.getConnection();
            Statement statement = connection.createStatement();
            statement.execute("select * from "+tableName+" where id="+id);
            ResultSet resultSet = statement.getResultSet();
            if(resultSet!=null){
                if(!resultSet.next()){
                    throw new SQLException("Not found object with id = "+id+", class = "+clazz.getCanonicalName());
                }
                Map<String, String> colNameToJavaField = getColumnNameToJavaFieldName(clazz);
                for(int i =1; i<=resultSet.getMetaData().getColumnCount();i++){
                    String fieldName = colNameToJavaField.get(resultSet.getMetaData().getColumnName(i).toLowerCase());
                    if(fieldName!=null){
                        //result.put(fieldName, resultSet.getString(i));
                        result.put(fieldName, resolveValue(resultSet.getString(i), clazz, fieldName));
                    }
                }
                if(resultSet.next()){
                    throw new SQLException("More than 1 results with id = " + id);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    private String getTableName(Class clazz){
        //TODO hanle @Table annotation
        return clazz.getSimpleName().toLowerCase();
    }

    private Map<String, String> getColumnNameToJavaFieldName(Class clazz){
        Map<String, String> result = new HashMap<>();
        for(Field field : clazz.getDeclaredFields()){
            //TODO handle @Column annotation
            String fieldNameDB = field.getName().toLowerCase();
            String fieldNameMapper = field.getName();
            if(field.getDeclaredAnnotation(ManyToMany.class)!=null
                    && field.getDeclaredAnnotation(ManyToOne.class)!=null
                    && field.getDeclaredAnnotation(OneToMany.class)!=null
                    && field.getDeclaredAnnotation(OneToMany.class)!=null){
                fieldNameDB = fieldNameDB+"_id";
                fieldNameMapper = fieldNameMapper+"id";
            }
            result.put(fieldNameDB, fieldNameMapper);
        }
        return result;
    }

    private Object resolveValue(String value, Class clazz, String fieldName){
        try {
            Class fieldType = clazz.getDeclaredField(fieldName).getType();
            if(fieldType == String.class){
                return value;
            }
            if(fieldType==int.class || fieldType==Integer.class){
                return Integer.parseInt(value);
            }
            if(fieldType==boolean.class || fieldType==Boolean.class){
                return Boolean.parseBoolean(value);
            }
            return Long.parseLong(value);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Object mapToEntity(Map<String, Object> dto, Object entity) {
        if(this.commonMapper==null){
            throw new UnsupportedOperationException();
        }
        return entity;
    }


}
