package com.sogoodlabs.common_mapper;

public interface IEntityById<T> {

    Object get(T id, Class clazz);

}
