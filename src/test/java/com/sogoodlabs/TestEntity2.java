package com.sogoodlabs;

import com.sogoodlabs.common_mapper.annotations.IncludeInDto;
import com.sogoodlabs.common_mapper.annotations.MapToClass;

import java.util.List;

public class TestEntity2 {

    String id;
    String title;

    TestEntity3 someObj;

    TestEntity3 someObj2;

    List<TestEntity3> entity3s;
    List<TestEntity3> entity3s2;

    List<TestEntity3> entity3s3;

    List<String> strings;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public TestEntity3 getSomeObj() {
        return someObj;
    }

    public void setSomeObj(TestEntity3 someObj) {
        this.someObj = someObj;
    }

    @IncludeInDto
    public List<TestEntity3> getEntity3s() {
        return entity3s;
    }

    @MapToClass(value = TestEntity3.class, parentField = "parent")
    public void setEntity3s(List<TestEntity3> entity3s) {
        this.entity3s = entity3s;
    }

    public List<TestEntity3> getEntity3s3() {
        return entity3s3;
    }

    @MapToClass(value = TestEntity3.class)
    public void setEntity3s3(List<TestEntity3> entity3s3) {
        this.entity3s3 = entity3s3;
    }

    public List<String> getStrings() {
        return strings;
    }

    public void setStrings(List<String> strings) {
        this.strings = strings;
    }

    public List<TestEntity3> getEntity3s2() {
        return entity3s2;
    }
    public void setEntity3s2(List<TestEntity3> entity3s2) {
        this.entity3s2 = entity3s2;
    }

    @IncludeInDto
    public TestEntity3 getSomeObj2() {
        return someObj2;
    }

    public void setSomeObj2(TestEntity3 someObj2) {
        this.someObj2 = someObj2;
    }
}
