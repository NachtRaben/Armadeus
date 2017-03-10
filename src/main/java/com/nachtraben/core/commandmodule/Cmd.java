package com.nachtraben.core.commandmodule;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by NachtRaben on 2/3/2017.
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Cmd {

    String name();
    String format();
    String description();
    String[] aliases() default {};
    String[] flags() default {};

}
