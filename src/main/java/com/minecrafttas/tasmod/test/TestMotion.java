package com.minecrafttas.tasmod.test;

import com.minecrafttas.mctcommon.test.Test;

public class TestMotion extends TASmodTestBase {

	@Override
	public void beforeAll() {
		launchWorld("Void");
	}

	@Test
	public void test() {
		System.out.println("TEST1");
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void test2() {
		System.out.println("TEST2");
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void afterAll() {
		quitWorld();
	}
}
