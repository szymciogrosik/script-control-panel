package org.codefromheaven.context;

import org.springframework.context.ApplicationContext;

public class SpringContext {

    private static ApplicationContext context;

    public static void setContext(ApplicationContext context) {
        SpringContext.context = context;
    }

    public static <T> T getBean(Class<T> beanClass) {
        return context.getBean(beanClass);
    }
}
