package mctcommon.event;

import org.junit.jupiter.api.Test;

import com.minecrafttas.mctcommon.events.EventListenerRegistry;
import com.minecrafttas.mctcommon.events.EventListenerRegistry.EventBase;

public class EventTest {

	@FunctionalInterface
	private interface TestEvent extends EventBase{
		
		public void onTestEvent();
		
		public static void test() {};
	}
	
	@Test
	void testRegisteringEvents() {
		
		
		TestEvent event = new TestEvent() {
			@Override
			public void onTestEvent() {
				System.out.println("Test");
			}
		};
		
		EventListenerRegistry.register(event);
		
		try {
			EventListenerRegistry.fireEvent(TestEvent.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
