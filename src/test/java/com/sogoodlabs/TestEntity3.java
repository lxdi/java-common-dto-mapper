package com.sogoodlabs;

public class TestEntity3 {

    String id;
    String title;

    TestEntity2 parent;

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

    public TestEntity2 getParent() {
        return parent;
    }

    public void setParent(TestEntity2 parent) {
        this.parent = parent;
    }
}
