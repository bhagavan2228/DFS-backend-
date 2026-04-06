package com.fourm.discussion_forum;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
public class SecurityClassesDump {
    public static void main(String[] args) throws Exception {
        Class<?> clazz = Class.forName("org.springframework.security.authentication.dao.DaoAuthenticationProvider");
        for (Constructor<?> c : clazz.getConstructors()) {
            System.out.println("Constructor: " + c);
        }
        for (Method m : clazz.getMethods()) {
            if (m.getName().startsWith("set")) {
                System.out.println("Method: " + m);
            }
        }
    }
}
