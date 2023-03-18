package com.sogoodlabs.common_mapper;

import com.sogoodlabs.TestEntity2;
import com.sogoodlabs.TestEntity3;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class CommonMappperTest_mappingFromLists {


    String testObj1Id = "testId1";
    String testObj2Id = "testId2";
    String testObj3Id = "testId3";
    String testObj4Id = "testId4";

    TestEntity2 testObj1;
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

        testObj1 = new TestEntity2();
        testObj1.setId(testObj1Id);
        testObj1.setTitle("testObj1Title");
        testObj1.setEntity3s(Arrays.asList(testObj2, testObj3, testObj4));
        testObj1.setEntity3s2(Arrays.asList(testObj2, testObj3, testObj4));
        testObj1.setSomeObj2(testObj2);

    }


    @Test
    public void mapToDtoTest(){

        CommonMapper commonMapper = new CommonMapper(entityById);

        Map<String, Object> dto = commonMapper.mapToDto(testObj1);

        assertNull(dto.get("entity3s2"));

        assertTrue(dto.get("entity3s") instanceof List);
        assertEquals(3, ((List)dto.get("entity3s")).size());
        assertEquals(testObj2Id, ((Map)((List)dto.get("entity3s")).get(0)).get("id"));
        assertEquals(testObj3Id, ((Map)((List)dto.get("entity3s")).get(1)).get("id"));
        assertEquals(testObj4Id, ((Map)((List)dto.get("entity3s")).get(2)).get("id"));

        assertNotNull(dto.get("someObj2"));
        assertEquals(testObj2Id, ((Map<String, Object>)dto.get("someObj2")).get("id"));
    }

    @Test
    public void mapToEntityTest(){
        CommonMapper commonMapper = new CommonMapper(entityById);

        var testEntDto = new HashMap<String, Object>();

        testEntDto.put("entity3s", List.of(
                Map.of("title", "children", "parentid", ""),
                Map.of("title", "children2")
        ));

        testEntDto.put("entity3s3", List.of(
                Map.of("title", "children3")
        ));

        var entity = commonMapper.mapToEntity(testEntDto, new TestEntity2());

        assertEquals(entity, entity.getEntity3s().get(0).getParent());
        assertEquals(entity, entity.getEntity3s().get(1).getParent());

        assertNull(entity.getEntity3s3().get(0).getParent());

    }

    private TestEntity3 create(String id, String title){
        TestEntity3 entity3 = new TestEntity3();
        entity3.setTitle(title);
        entity3.setId(id);
        return entity3;
    }

}
