package edu.neumont.server.test;

/**
 * User: Sean Yergensen
 */
public class MyTest {
    String name;

    public MyTest(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
