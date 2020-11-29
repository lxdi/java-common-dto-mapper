package com.sogoodlabs.common_mapper;

import com.sogoodlabs.TestEntity;
import com.sogoodlabs.TestEntity2;
import com.sogoodlabs.TestEntity3;
import com.sogoodlabs.TestSecondEntity;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class CommonMappperTest_idString {


    String testObj2Id = "testId2";
    String testObj3Id = "testId3";
    String testObj4Id = "testId4";

    TestEntity3 testObj2;
    TestEntity3 testObj3;
    TestEntity3 testObj4;

    private IEntityById entityById = new IEntityById<String>() {
        @Override
        public Object get(String id, Class clazz) {
            if(clazz == TestEntity3.class){
                if(id.equals(testObj2Id)){
                    return testObj2;
                }
            }
            return null;
        }
    };

    @Before
    public void init(){
        testObj2 = create(testObj2Id, "testTitle2");
        testObj3 = create(testObj3Id, "testTitle3");
        testObj4 = create(testObj4Id, "testTitle4");
    }

    private TestEntity3 create(String id, String title){
        TestEntity3 entity3 = new TestEntity3();
        entity3.setTitle(title);
        entity3.setId(id);
        return entity3;
    }


    @Test
    public void mapToDtoStringId(){
        CommonMapper commonMapper = new CommonMapper(entityById);

        TestEntity2 testObj = new TestEntity2();
        testObj.setId("e41392f5-6042-474f-92d0-b8b7b6c58e55");
        testObj.setTitle("testTitle");
        testObj.setSomeObj(testObj2);

        Map<String, Object> dto = commonMapper.mapToDto(testObj);

        assertEquals("e41392f5-6042-474f-92d0-b8b7b6c58e55", dto.get("id"));
        assertEquals(testObj2Id, dto.get("someObjid"));

    }

    @Test(expected = NullPointerException.class)
    public void mapToDtoNull(){
        CommonMapper commonMapper = new CommonMapper(entityById);
        commonMapper.mapToDto(null);
    }

    @Test
    public void mapToEntityTest(){

        CommonMapper commonMapper = new CommonMapper(entityById);

        List<Map<String, Object>> entities = new ArrayList<>();
        entities.add(createEntity3Dto("someId1", "someTitle1"));
        entities.add(createEntity3Dto("someId2", "someTitle2"));

        Map<String, Object> dto = new HashMap();
        dto.put("id", "id1");
        dto.put("title", "testTitle");
        dto.put("someObjid", testObj2Id);
        dto.put("entity3s", entities);
        dto.put("strings", Arrays.asList("someString1", "someString2"));


        TestEntity2 someObj = commonMapper.mapToEntity(dto, new TestEntity2());

        assertEquals(testObj2, someObj.getSomeObj());
        assertNotNull(someObj.getEntity3s());
        assertEquals(2, someObj.getEntity3s().size());
        assertNull(someObj.getStrings());
    }

    private Map<String, Object> createEntity3Dto(String id, String title){
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", id);
        dto.put("title", title);
        return dto;
    }

}
