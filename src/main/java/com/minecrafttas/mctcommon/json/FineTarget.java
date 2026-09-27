package com.minecrafttas.mctcommon.json;

import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface FineTarget {
	Class<?> value() default Object.class;

	Class<?> superclazz() default Object.class;

	Class<?> enclosingclazz() default Object.class;
}
