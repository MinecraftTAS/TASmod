package com.minecrafttas.mctcommon.events;

import org.apache.commons.lang3.ClassUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Registry for making objects available to listen for events.<br>
 * <br>
 * Implement an EventInterface into your class, then implement the function.<br>
 * <br>
 * In your initializer method, register the instance of the class with
 * {@link EventListenerRegistry#register(EventBase)} <br>
 * Example:
 * 
 * <pre>
 * public class ClassWithListener implements EventInit {
 *
 * 	&#64;Override
 * 	public void onEventInit() {
 * 		// Implement event specific code
 * 	}
 * }
 * </pre>
 * 
 * <br>
 * To create and fire your own events, check {@link EventBase}<br>
 */
public class EventListenerRegistry {

	/**
	 * Base interface for events.<br>
	 * <br>
	 * To create a new event, create an interface and extend EventBase.<br>
	 * Only 1 method is accepted in an event, so it's best to add
	 * {@link FunctionalInterface} to the interface.<br>
	 * <br>
	 * Example:
	 * 
	 * <pre>
	 * &#64;FunctionalInterface
	 * public interface EventInit extends EventBase {
	 * 	public void onEventInit();
	 * }
	 *
	 * &#64;FunctionalInterface
	 * public interface EventAdd extends EventBase {
	 * 	public int onEventAdd(int a, int b); // Accepts parameters and return types
	 * }
	 * </pre>
	 *
	 * To fire your event, use {@link EventListenerRegistry#fireEvent(Class)} with
	 * the parameter being the event class.<br>
	 *
	 * <pre>
	 * EventListenerRegistry.fireEvent(EventInit.class);
	 * </pre>
	 *
	 * To fire an event with parameters and return types:
	 * 
	 * <pre>
	 * int eventResult = (int) EventListenerRegistry.fireEvent(EventAdd.class, a, b);
	 * </pre>
	 * 
	 * When using parameters, the type and the amount of parameters has to match!
	 */
	public interface EventBase {
	}

	/**
	 * Stores the event listener objects and calls their event methods during
	 * {@link EventListenerRegistry#fireEvent(Class, Object...)}<br>
	 * <br>
	 * Consists of multiple lists seperated by event types.<br>
	 * If it were a single ArrayList, firing an event means that you'd have to
	 * unnecessarily iterate over the entire list,<br>
	 * to find the correct events.<br>
	 * <br>
	 * With multiple lists like this, you iterate only over the objects, that have
	 * the correct event applied.
	 */
	private static final HashMap<Class<?>, ArrayList<EventBase>> EVENTLISTENER_REGISTRY = new HashMap<>();

	/**
	 * Registers an object to be an event listener. The object must implement an
	 * event extending {@link EventBase}
	 * 
	 * @param eventListener The event listener to register
	 */
	public static void register(EventBase eventListener) {
		if (eventListener == null) {
			throw new NullPointerException("Tried to register a packethandler with value null");
		}
		for (Class<?> type : eventListener.getClass().getInterfaces()) {
			if (EventBase.class.isAssignableFrom(type)) {

                // If a new event type is being registered, add a new arraylist
                ArrayList<EventBase> registryList = EVENTLISTENER_REGISTRY.putIfAbsent(type, new ArrayList<>());
                if (registryList == null) {
                    registryList = EVENTLISTENER_REGISTRY.get(type);
                }
                registryList.add(eventListener);
            }
        }
    }
    
    public static void register(EventBase... eventListeners) {
    	for(EventBase eventListener : eventListeners) {
    		register(eventListener);
    	}
    }

	/**
	 * Unregisters an object from being an event listener.
	 * 
	 * @param eventListener The event listener to unregister
	 */
	public static void unregister(EventBase eventListener) {
		if (eventListener == null) {
			throw new NullPointerException("Tried to unregister a packethandler with value null");
		}
		for (Class<?> type : eventListener.getClass().getInterfaces()) {
			if (EventBase.class.isAssignableFrom(type)) {
				ArrayList<EventBase> registryList = EVENTLISTENER_REGISTRY.get(type);
				if (registryList != null) {
					registryList.remove(eventListener);

					if (registryList.isEmpty()) {
						EVENTLISTENER_REGISTRY.remove(type);
					}
				}
			}
		}
	}

	/**
	 * Fires an event without parameters
	 *
	 * @param eventClass The event class to fire e.g. EventClientInit.class
	 * @return The result of the event, might be null if the event returns nothing
	 */
	public static Object fireEvent(Class<? extends EventListenerRegistry.EventBase> eventClass) {
		return fireEvent(eventClass, new Object[] {});
	}

	/**
	 * Fires an event with parameters
	 *
	 * @param eventClass  The event class to fire e.g. EventClientInit.class
	 * @param eventParams List of parameters for the event. Number of arguments and
	 *                    types have to match.
	 * @return The result of the event, might be null if the event returns nothing
	 */
	public static Object fireEvent(Class<? extends EventListenerRegistry.EventBase> eventClass, Object... eventParams) {
		ArrayList<EventBase> listenerList = EVENTLISTENER_REGISTRY.get(eventClass);
		if (listenerList == null) {
			return null;
		}

		// Exception to be thrown at the end of the method. If null then no exception is
		// thrown
		EventException toThrow = null;

		// Get the method from the event that we are looking for in the event listeners
		Method methodToFind = getEventMethod(eventClass);

		// Variable for the return value. The last registered listener will return its
		// value
		Object returnValue = null;

		// Iterate through the list of eventListeners
		for (EventBase eventListener : listenerList) {
			// Get all methods in this event listener
			Method[] methodsInListener = eventListener.getClass().getDeclaredMethods();

			// Iterate through all methods
			for (Method method : methodsInListener) {

				// Check if the current method has the same name as the method we are looking
				// for
				if (!checkName(method, methodToFind.getName())) {
					continue;
				}

				// Check if the length is the same before we check for types
				if (!checkLength(method, eventParams)) {
					toThrow = new EventException(String.format("Event fired with the wrong number of parameters. Expected: %s, Actual: %s", method.getParameterCount(), eventParams.length), eventClass);
					continue;
				}

				// Check the types of the method
				if (checkTypes(method, eventParams)) {
					toThrow = null; // Reset toThrow as the correct method was found
					method.setAccessible(true);
					try {
						returnValue = method.invoke(eventListener, eventParams); // Call the method
					} catch (IllegalAccessException | InvocationTargetException e) {
						throw new EventException(eventClass, e);
					} catch (IllegalArgumentException e) {
						throw new EventException(String.format("Event fired with the wrong number of parameters. Expected: %s, Actual: %s", method.getParameterCount(), eventParams.length), eventClass, e);
					}
				} else {
					toThrow = new EventException("Event seems to be fired with the wrong parameter types or in the wrong order", eventClass);
				}
			}
		}

		// Throw the exception
		if (toThrow != null) {
			throw toThrow;
		}

		return returnValue;
	}

	private static Method getEventMethod(Class<? extends EventListenerRegistry.EventBase> eventClass) {
		Method[] test = eventClass.getDeclaredMethods();
		if (test.length != 1) {
			throw new EventException("The event method is not properly defined. Only one method is allowed inside of an event", eventClass);
		}

		return test[0];
	}

	/**
	 * @param method The method to check
	 * @param name   The name to check
	 * @return If method.getName equals name
	 */
	private static boolean checkName(Method method, String name) {
		return method.getName().equals(name);
	}

	/**
	 * @param method     The method to check
	 * @param parameters The list of parameters
	 * @return True, if length of the method parameters is equal to the length of
	 *         the object parameters
	 */
	private static boolean checkLength(Method method, Object... parameters) {
		return method.getParameterCount() == parameters.length;
	}

	/**
	 * @param method     The method to check
	 * @param parameters The list of parameters
	 * @return True, if the types of the parameters equal the object parameters
	 */
	private static boolean checkTypes(Method method, Object... parameters) {
		Class<?>[] methodParameterTypes = ClassUtils.primitivesToWrappers(method.getParameterTypes());
		Class<?>[] eventParameterTypes = getParameterTypes(parameters);

		for (int i = 0; i < methodParameterTypes.length; i++) {
			Class<?> paramName = methodParameterTypes[i];
			Class<?> eventName = eventParameterTypes[i];

			if (paramName == null || eventName == null) {
				continue;
			}

			if (!paramName.equals(eventName) && !paramName.isAssignableFrom(eventName)) {
				return false;
			}
		}
		return true;
	}

	private static Class<?>[] getParameterTypes(Object... parameters) {
		Class<?>[] out = new Class[parameters.length];
		for (int i = 0; i < parameters.length; i++) {
			out[i] = parameters[i] == null ? null : parameters[i].getClass();
		}
		return out;
	}

	/**
	 * Removes all registry entries
	 */
	public static void clear() {
		EVENTLISTENER_REGISTRY.clear();
	}
}
