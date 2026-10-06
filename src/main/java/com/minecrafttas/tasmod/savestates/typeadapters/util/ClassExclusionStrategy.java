package com.minecrafttas.tasmod.savestates.typeadapters.util;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

public class ClassExclusionStrategy implements ExclusionStrategy {

	protected final Class<?> clazz;

	protected final Class<?> declaringClass;

	public ClassExclusionStrategy(Class<?> clazz) {
		this.clazz = clazz;
		this.declaringClass = null;
	}

	public ClassExclusionStrategy(Class<?> clazz, Class<?> declaringClass) {
		this.clazz = clazz;
		this.declaringClass = declaringClass;
	}

	@Override
	public boolean shouldSkipField(FieldAttributes f) {
		if (declaringClass == null)
			return false;
		return declaringClass.equals(f.getDeclaringClass());
	}

	@Override
	public boolean shouldSkipClass(Class<?> clazz) {
		return this.clazz.equals(clazz) || this.clazz.isAssignableFrom(clazz);
	}

}
