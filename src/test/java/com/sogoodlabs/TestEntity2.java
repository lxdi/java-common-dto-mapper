package com.sogoodlabs;

import com.sogoodlabs.common_mapper.annotations.MapToClass;

import java.util.List;

public class TestEntity2 {

    String id;
    String title;

    TestEntity3 someObj;

    List<TestEntity3> entity3s;

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

    public List<TestEntity3> getEntity3s() {
        return entity3s;
    }

    @MapToClass(value = TestEntity3.class)
    public void setEntity3s(List<TestEntity3> entity3s) {
        this.entity3s = entity3s;
    }

    public List<String> getStrings() {
        return strings;
    }

    public void setStrings(List<String> strings) {
        this.strings = strings;
    }
}
