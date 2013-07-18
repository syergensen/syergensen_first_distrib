package edu.neumont.server.services;

/**
 * User: Sean Yergensen
 */
public class MyClass {
    private static MyClass ourInstance = new MyClass();

    public static MyClass getInstance() {
        return ourInstance;
    }

    private MyClass() {
    }


}
