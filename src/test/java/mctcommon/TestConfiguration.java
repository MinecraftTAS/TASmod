package mctcommon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.minecrafttas.mctcommon.Configuration;
import com.minecrafttas.mctcommon.Configuration.ConfigOptions;

class TestConfiguration {

	private static Configuration config;
	
	private static final File configPath = new File("./config.xml");
	
	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		config = new Configuration("Test config", configPath);
	}

	@BeforeEach
	void resetOptions() throws Exception {
		config.reset(ConfigOptions.FileToOpen);
	}
	
	@AfterAll
	static void tearDownAfterClass() throws Exception {
		configPath.delete();
	}

	/**
	 * Test if the config is successfully initialized
	 */
	@Test
	void testIfInitialized() {
		assertNotNull(config);
	}
	
	/**
	 * Test if the default option is correctly set
	 */
	@Test
	void testDefault() {
		configPath.delete();
		config = new Configuration("Test config", configPath);
		assertEquals("", config.get(ConfigOptions.FileToOpen));
	}
	
	/**
	 * Setting a value and recreating the config should result in the value still being set
	 */
	@Test
	void testSavingAndLoading() {
		config.set(ConfigOptions.FileToOpen, "Test");
		config = new Configuration("Test config", configPath);
		assertEquals("Test", config.get(ConfigOptions.FileToOpen));
	}
	
	/**
	 * Test if integers can be set
	 */
	@Test
	void testIntegers() {
		config.set(ConfigOptions.FileToOpen, 3);
		assertEquals(3, config.getInt(ConfigOptions.FileToOpen));
	}

	/**
	 * Test if booleans can be set
	 */
	@Test
	void testBooleans() {
		config.set(ConfigOptions.FileToOpen, true);
		assertEquals(true, config.getBoolean(ConfigOptions.FileToOpen));
	}
	
	/**
	 * Test if deleting and unsetting a config value works
	 */
	@Test
	void testDeleteAndContains() {
		config.delete(ConfigOptions.FileToOpen);
		assertFalse(config.has(ConfigOptions.FileToOpen));
	}
	
	/**
	 * Test if resetting to default works
	 */
	@Test
	void resetToDefault() {
		config.reset(ConfigOptions.FileToOpen);
		assertEquals("", config.get(ConfigOptions.FileToOpen));
	}
}
