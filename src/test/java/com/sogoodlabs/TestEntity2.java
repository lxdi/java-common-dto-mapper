package com.sogoodlabs;

public class TestEntity2 {

    String id;
    String title;

    TestEntity3 someObj;

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
}
