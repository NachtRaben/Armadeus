package dev.armadeus.bot.api.annotations;

import net.dv8tion.jda.api.Permission;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention( RetentionPolicy.RUNTIME )
@Target( {ElementType.TYPE, ElementType.METHOD} )
public @interface DiscordPermission {
    Permission value() default Permission.MESSAGE_WRITE;
}
