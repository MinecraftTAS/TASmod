package com.minecrafttas.mctcommon.events;

import org.apache.commons.lang3.ClassUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

public class EventListenerRegistry {

    /**
     * Eventlistener Registry.<br>
     * <br>
     * Consists of multiple lists seperated by event types.<br>
     * If it were a single ArrayList, firing an event means that you'd have to unnecessarily iterate over the entire list,<br>
     * to find the correct events.<br>
     * <br>
     * With multiple lists like this, you iterate only over the objects, that have the correct event applied.
     */
    private static final HashMap<Class<?>, ArrayList<EventBase>> EVENTLISTENER_REGISTRY = new HashMap<>();


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
        return fireEvent(eventClass, new Object[]{});
    }

    /**
     * Fires an event with parameters
     *
     * @param eventClass  The event class to fire e.g. EventClientInit.class
     * @param eventParams List of parameters for the event. Number of arguments and types have to match.
     * @return The result of the event, might be null if the event returns nothing
     */
    public static Object fireEvent(Class<? extends EventListenerRegistry.EventBase> eventClass, Object... eventParams) {
        ArrayList<EventBase> registryList = EVENTLISTENER_REGISTRY.get(eventClass);
        if (registryList == null) {
            throw new EventException("The event has not been registered yet", eventClass);
        }

        EventException toThrow = null;

        Method methodToCheck = getEventMethod(eventClass);

        Object returnValue = null;
        for (EventBase eventListener : registryList) {
            Method[] methodsInListener = eventListener.getClass().getDeclaredMethods();

            for (Method method : methodsInListener) {

                if (!checkName(method, methodToCheck.getName())) {
                    continue;
                }

                if (!checkLength(method, eventParams)) {
                    toThrow = new EventException(String.format("Event fired with the wrong number of parameters. Expected: %s, Actual: %s", method.getParameterCount(), eventParams.length), eventClass);
                    continue;
                }

                if (checkTypes(method, eventParams)) {
                    toThrow = null;
                    method.setAccessible(true);
                    try {
                        returnValue = method.invoke(eventListener, eventParams);
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

    private static boolean checkName(Method method, String name) {
        return method.getName().equals(name);
    }

    private static boolean checkLength(Method method, Object... parameters) {
        return method.getParameterCount() == parameters.length;
    }

    private static boolean checkTypes(Method method, Object... parameters) {
        Class<?>[] methodParameterTypes = ClassUtils.primitivesToWrappers(method.getParameterTypes());
        Class<?>[] eventParameterTypes = getParameterTypes(parameters);

        for (int i = 0; i < methodParameterTypes.length; i++) {
            Class<?> paramName = methodParameterTypes[i];
            Class<?> eventName = eventParameterTypes[i];
            if (!paramName.equals(eventName)) {
                return false;
            }
        }
        return true;
    }

    private static Class<?>[] getParameterTypes(Object... parameters) {
        Class<?>[] out = new Class[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            out[i] = parameters[i].getClass();
        }
        return out;
    }

    /**
     * Removes all registry entries
     */
    public static void clear() {
        EVENTLISTENER_REGISTRY.clear();
    }

    public interface EventBase {
    }
}
