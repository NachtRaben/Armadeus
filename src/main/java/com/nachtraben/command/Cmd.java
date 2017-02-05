package com.nachtraben.command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Cmd {
    String name();
    PermissionLevel permission() default PermissionLevel.USER;
    String[] aliases() default {};
    String format();
    String description();
}
