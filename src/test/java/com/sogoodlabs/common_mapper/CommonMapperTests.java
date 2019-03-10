package com.sogoodlabs.common_mapper;

import com.sogoodlabs.TestEntity;
import com.sogoodlabs.TestEnum;
import com.sogoodlabs.TestSecondEntity;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.assertTrue;

public class CommonMapperTests {

    TestEntity testEntityFromEntityById = new TestEntity();

    TestSecondEntity testSecondEntityFromEntityById = new TestSecondEntity();

    IEntityById entityById = new IEntityById() {
        @Override
        public Object get(long id, Class clazz) {
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

        TestEntity testEntity = (TestEntity) commonMapperTest.mapToEntity(dto, new TestEntity());

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

        TestEntity testEntity = (TestEntity) commonMapperTest.mapToEntity(dto, new TestEntity());

        assertTrue(testEntity.getId()==15);
        assertTrue(testEntity.getTitle().equals("45"));
    }

}
