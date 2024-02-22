package com.minecrafttas.mctcommon.events;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

public class EventListenerRegistry {
	
	private static HashMap<Class<?>, ArrayList<EventBase>> EVENTLISTENER_REGISTRY = new HashMap<>();
	
	
	public static void register(EventBase eventListener) {
		if (eventListener == null) {
			throw new NullPointerException("Tried to register a packethandler with value null");
		}
		for (Class<?> type : eventListener.getClass().getInterfaces()) {
			if(EventBase.class.isAssignableFrom(type)) {
				ArrayList<EventBase> registryList = EVENTLISTENER_REGISTRY.putIfAbsent(type, new ArrayList<>());
				if(registryList==null) {
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
		EVENTLISTENER_REGISTRY.remove(eventListener);
	}
	
	public static Object fireEvent(Class<? extends EventListenerRegistry.EventBase> eventClass) throws Exception {
		return fireEvent(eventClass, new Object[] {});
	}
	
	public static Object fireEvent(Class<? extends EventListenerRegistry.EventBase> eventClass, Object... eventParams) throws Exception {
		ArrayList<EventBase> registryList = EVENTLISTENER_REGISTRY.get(eventClass);
		if(registryList == null) {
			throw new Exception(String.format("The event {} has not been registered yet", eventClass));
		}
		
		Method methodToCheck = getEventMethod(eventClass);
		
		for (EventBase eventListener : registryList) {
			Method[] methodsInListener = eventListener.getClass().getDeclaredMethods();
			
			for (Method method : methodsInListener) {
				if(method.getName().equals(methodToCheck.getName())) {
					method.setAccessible(true);
					return method.invoke(eventListener, eventParams);
				}
			}
		}
		throw new Exception(String.format("The event {} has not been registered yet", eventClass));
	}
	
	public static Method getEventMethod(Class<? extends EventListenerRegistry.EventBase> eventClass) throws Exception {
		Method[] test = eventClass.getDeclaredMethods();
		if(test.length != 1) {
			throw new Exception("The event method is not properly defined. Only one method is allowed inside of an event");
		}
		
		return test[0];
	}
	
	public static interface EventBase {
		
	}
}
