package com.sogoodlabs.common_mapper;

import com.sogoodlabs.TestEntity;
import com.sogoodlabs.TestEntity2;
import com.sogoodlabs.TestEnum;
import com.sogoodlabs.TestSecondEntity;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

public class CommonMapperTests {

    TestEntity testEntityFromEntityById = new TestEntity();

    TestSecondEntity testSecondEntityFromEntityById = new TestSecondEntity();

    IEntityById entityById = new IEntityById<Long>() {
        @Override
        public Object get(Long id, Class clazz) {
            if(clazz == TestEntity.class){
                if(id==56){
                    return testEntityFromEntityById;
                }
            }
            if(clazz == TestSecondEntity.class){
                if(id==73){
                    return testSecondEntityFromEntityById;
                }
            }
            return null;
        }
    };

    @Before
    public void init(){
        testEntityFromEntityById.setId(56);
        testEntityFromEntityById.setTitle("test entity from entityById");

        testSecondEntityFromEntityById.setId(73);
        testSecondEntityFromEntityById.setTitle("test second ent from ent by id");
    }

    @Test
    public void toDtoTest(){
        CommonMapper commonMapperTest = new CommonMapper(entityById);

        TestEntity anotherEntity = new TestEntity();
        anotherEntity.setId(24);

        TestEntity testEntity = new TestEntity();
        testEntity.setId(123);
        testEntity.setTitle("test title");
        testEntity.setAnotherTestEntity(anotherEntity);
        testEntity.setTestEnum(TestEnum.val1);
        testEntity.setBooleanVal(false);

        Map<String, Object> dto = commonMapperTest.mapToDto(testEntity, new HashMap<>());

        assertTrue(dto.get("title").equals("test title"));
        assertTrue((long)dto.get("id")==123);
        assertTrue((long)dto.get("anotherTestEntityid")==24);
        assertTrue(dto.get("anotherTestEntity2id")==null);
        assertTrue(dto.get("testEnum").equals("val_1"));
        assertTrue((boolean)dto.get("booleanVal")==false);
    }

    @Test
    public void toDtoMappingEmptyFields(){
        Configuration configuration = new Configuration();
        configuration.mapEmptyFields = true;

        CommonMapper commonMapperTest = new CommonMapper(entityById, configuration);

        TestEntity testEntity = new TestEntity();
        Map<String, Object> dto = commonMapperTest.mapToDto(testEntity);

        assertTrue(dto.containsKey("title"));
        assertTrue(dto.containsKey("id"));

        TestEntity testEntity2 = new TestEntity();
        testEntity2.setId(123);
        testEntity2.setTitle("test title");

        Map<String, Object> dto2 = commonMapperTest.mapToDto(testEntity2);

        assertTrue(dto2.get("title").equals("test title"));
        assertTrue((long)dto2.get("id") == 123);

    }

    @Test
    public void toEntityTest(){
        CommonMapper commonMapperTest = new CommonMapper(entityById);

        Map<String, Object> dto = new HashMap<>();
        dto.put("id", "15");
        dto.put("title", "test title2");
        dto.put("testEnum", "val_1");
        dto.put("anotherTestEntity2id", 56);
        dto.put("anotherTestEntityid", null);
        dto.put("testSecondEntityid", 73);
        dto.put("booleanVal", true);

        dto.put("notExisting", "someval");
        dto.put("notExistingid", 43);
        dto.put("notExisting2id", "someval");
        dto.put("intvalue", "20");

        TestEntity testEntity = commonMapperTest.mapToEntity(dto, new TestEntity());

        assertTrue(testEntity.getId()==15);
        assertTrue(testEntity.getTitle().equals("test title2"));
        assertTrue(testEntity.getTestEnum()==TestEnum.val1);
        assertTrue(testEntity.getAnotherTestEntity2()==testEntityFromEntityById);
        assertTrue(testEntity.getAnotherTestEntity()==null);
        assertTrue(testEntity.getTestSecondEntity()==testSecondEntityFromEntityById);
        assertTrue(testEntity.getIntvalue()==20);
        assertTrue(testEntity.getBooleanVal());
    }

    @Test
    public void toEntityWhenTitleIsNumberTest(){
        CommonMapper commonMapperTest = new CommonMapper(entityById);

        Map<String, Object> dto = new HashMap<>();
        dto.put("id", "15");
        dto.put("title", "45");

        TestEntity testEntity = commonMapperTest.mapToEntity(dto, new TestEntity());

        assertTrue(testEntity.getId()==15);
        assertTrue(testEntity.getTitle().equals("45"));
    }

    @Test
    public void customMappingTest(){
        CommonMapper commonMapper = new CommonMapper(entityById){
            @Override
            public boolean customMapping(Object entity, Map<String, Object> result, Method method, Object fromGetter){
                if(method.getName().equals("getForCustomMapping")){
                    result.put("forCustomMapping", "default value for custom mapping");
                    return true;
                }
                return false;
            }
        };

        TestEntity testEntity = new TestEntity();
        testEntity.setForCustomMapping("some value");

        Map<String, Object> result = commonMapper.mapToDto(testEntity, new HashMap<>());

        assertTrue(result.get("forCustomMapping").equals("default value for custom mapping"));

    }



}
