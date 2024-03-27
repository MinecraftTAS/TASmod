package mctcommon.event;

import com.minecrafttas.mctcommon.events.EventException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import com.minecrafttas.mctcommon.events.EventListenerRegistry;
import com.minecrafttas.mctcommon.events.EventListenerRegistry.EventBase;

import static org.junit.jupiter.api.Assertions.*;

public class EventTest {

    @FunctionalInterface
    private interface TestEvent extends EventBase {

        public int onTestEvent();

    }

    @FunctionalInterface
    private interface AdditionEvent extends EventBase {

        public int onAdditionEvent(int left, int right);

    }

    @AfterEach
    void afterEach() {
        EventListenerRegistry.clear();
    }

    /**
     * Registers an event which returns 5
     */
    @Test
    void testSimpleEvent() {
        TestEvent event = () -> 5;

        EventListenerRegistry.register(event);

        int actual = (int) EventListenerRegistry.fireEvent(TestEvent.class);
        assertEquals(5, actual);
    }

    /**
     * Test event with parameters
     */
    @Test
    void testParameterEvent() {
        AdditionEvent event = Integer::sum;

        EventListenerRegistry.register(event);

        int actual = (int) EventListenerRegistry.fireEvent(AdditionEvent.class, 3, 6);

        assertEquals(9, actual);
    }

    /**
     * Test event with parameters, but too few parameters are fired
     */
    @Test
    void testParameterEventTooFew() {
        AdditionEvent event = Integer::sum;

        EventListenerRegistry.register(event);

        Exception exception = assertThrows(EventException.class, () -> {
            EventListenerRegistry.fireEvent(AdditionEvent.class, 3);
        });

        String expected = "mctcommon.event.EventTest$AdditionEvent: Event fired with the wrong number of parameters. Expected: 2, Actual: 1";
        assertEquals(expected, exception.getMessage());
    }

    /**
     * Test event with parameters, but too many parameters are fired
     */
    @Test
    void testParameterEventTooMany() {
        AdditionEvent event = new AdditionEvent() {
            @Override
            public int onAdditionEvent(int left, int right) {
                return left + right;
            }
        };

        EventListenerRegistry.register(event);

        Exception exception = assertThrows(EventException.class, () -> {
            EventListenerRegistry.fireEvent(AdditionEvent.class, 3, 1, 3);
        });

        String expected = "mctcommon.event.EventTest$AdditionEvent: Event fired with the wrong number of parameters. Expected: 2, Actual: 3";
        assertEquals(expected, exception.getMessage());
    }

    /**
     * Test multiple return values
     */
    @Test
    void testMultipleReturnValues() {
        TestEvent event = () -> 5;
        TestEvent event2 = () -> 7;

        EventListenerRegistry.register(event);
        EventListenerRegistry.register(event2);

        int actual = (int) EventListenerRegistry.fireEvent(TestEvent.class);
        assertEquals(7, actual);
    }

    /**
     * Tests a class which has an additional method with the same name but different parameters
     */
    @Test
    void testEventMethodwithSameName() {

        class TestClass implements TestEvent {

            @Override
            public int onTestEvent() {
                return 1;
            }
        }

        TestClass test = new TestClass();

        EventListenerRegistry.register(test);

        int actual = (int) EventListenerRegistry.fireEvent(TestEvent.class);

        assertEquals(1, actual);
    }

    /**
     * Test mismatched types
     */
    @Test
    void testParameterEventWrongTypes() {
        AdditionEvent event = Integer::sum;

        EventListenerRegistry.register(event);

        Exception exception = assertThrows(EventException.class, () -> EventListenerRegistry.fireEvent(AdditionEvent.class, 3, 6D));

        String expected = "mctcommon.event.EventTest$AdditionEvent: Event seems to be fired with the wrong parameter types or in the wrong order";
        assertEquals(expected, exception.getMessage());
    }

    /**
     * Test unregistering eventlistener
     */
    @Test
    void testUnregister() {
        TestEvent event = () -> 5;

        EventListenerRegistry.register(event);

        EventListenerRegistry.unregister(event);

        Exception exception = assertThrows(EventException.class, () -> EventListenerRegistry.fireEvent(TestEvent.class));

        String expected = "mctcommon.event.EventTest$TestEvent: The event has not been registered yet";
        assertEquals(expected, exception.getMessage());
    }
}
