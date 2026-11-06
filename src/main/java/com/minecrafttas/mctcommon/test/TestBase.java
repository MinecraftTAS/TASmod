package com.minecrafttas.mctcommon.test;

import com.minecrafttas.mctcommon.events.EventListenerRegistry.EventBase;
import com.minecrafttas.mctcommon.registry.Registerable;

public abstract class TestBase implements Registerable, EventBase {

	public void beforeAll() {

	}

	public void beforeEach() {

	}

	public void afterAll() {

	}

	public void afterEach() {

	}

	@Override
	public String getExtensionName() {
		return this.getClass().getSimpleName();
	}
}
