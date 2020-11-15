package com.sogoodlabs.common_mapper;

import com.sogoodlabs.TestEntity;
import com.sogoodlabs.TestEnum;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.assertTrue;

public class CommonMapper_LazyMappingTests {

    IEntityById entityById = new IEntityById<Long>(){

        @Override
        public Object get(Long id, Class clazz) {
            return null;
        }
    };

    @Test
    public void basicTest(){
        CommonMapper commonMapperTest = new CommonMapper(entityById);

        TestEntity testEntity = new TestEntity();
        testEntity.setId(123);
        testEntity.setTitle("test title");
        testEntity.setTestEnum(TestEnum.val1);
        testEntity.setBooleanVal(true);
        testEntity.setIntvalue(56);

        Map<String, Object> dto = commonMapperTest.mapToDtoLazy(testEntity, new HashMap<>());

        assertTrue(dto.get("title").equals("test title"));
        assertTrue((int)dto.get("intvalue")==56);
        assertTrue(dto.get("id")==null);
        assertTrue(dto.get("testEnum")==null);
        assertTrue(dto.get("booleanVal")==null);
    }

}
