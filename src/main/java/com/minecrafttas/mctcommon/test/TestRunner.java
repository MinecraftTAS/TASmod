package com.minecrafttas.mctcommon.test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedHashMap;

import com.minecrafttas.mctcommon.MCTCommon;
import com.minecrafttas.mctcommon.events.EventClient.EventClientInit;
import com.minecrafttas.mctcommon.events.EventListenerRegistry;
import com.minecrafttas.mctcommon.registry.AbstractRegistry;

import net.minecraft.client.Minecraft;

public class TestRunner extends AbstractRegistry<TestBase> implements EventClientInit {

	private Thread testRunnerThread;

	public TestRunner() {
		super("TEST_RUNNER", new LinkedHashMap<>());
	}

	@Override
	public void register(TestBase registryObject) {
		EventListenerRegistry.register(registryObject);
		super.register(registryObject);
	}

	@Override
	public void onClientInit(Minecraft mc) {
		if (System.getProperty("mctcommon.testrunner", "false").equals("true")) {
			testRunnerThread = new Thread(() -> {
				runTests();
			}, "TestRunner");
			testRunnerThread.start();
		}
	}

	private void runTests() {
		MCTCommon.LOGGER.info(MCTCommon.Test, "Running Tests");
		Collection<TestBase> testClasses = this.REGISTRY.values();
		for (TestBase testClass : testClasses) {
			testClass.beforeAll();

			Method[] methods = testClass.getClass().getMethods();
			for (Method testMethod : methods) {
				if (testMethod.isAnnotationPresent(Test.class)) {
					testClass.beforeEach();
					try {
						testMethod.invoke(testClass);
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						e.printStackTrace();
					}
					testClass.afterEach();
				}
			}

			testClass.afterAll();
		}
		System.exit(0);
	}
}
