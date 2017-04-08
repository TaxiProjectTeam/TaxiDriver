package com.ck.taxoteam.taxodriver.data;



public class CurrentDriver {
    private static Driver instance;

    private CurrentDriver() {
    }

    public static Driver getInstance(){
        if (instance == null) {
            instance = new Driver();
        }
        return instance;
    }

    public static void setInstance(Driver instance) {
        CurrentDriver.instance = instance;
    }
    public static void removeInstance(){
        instance = null;
    }
}
