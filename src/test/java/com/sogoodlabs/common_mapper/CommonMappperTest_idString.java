package com.sogoodlabs.common_mapper;

import com.sogoodlabs.TestEntity;
import com.sogoodlabs.TestEntity2;
import com.sogoodlabs.TestEntity3;
import com.sogoodlabs.TestSecondEntity;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class CommonMappperTest_idString {


    String testObj2Id = "testId2";
    TestEntity3 testObj2;

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
        testObj2 = new TestEntity3();
        testObj2.setId(testObj2Id);
        testObj2.setTitle("testTitle2");
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

        Map<String, Object> dto = new HashMap();
        dto.put("id", "id1");
        dto.put("title", "testTitle");
        dto.put("someObjid", testObj2Id);

        TestEntity2 someObj = commonMapper.mapToEntity(dto, new TestEntity2());

        assertEquals(testObj2, someObj.getSomeObj());
    }

}
