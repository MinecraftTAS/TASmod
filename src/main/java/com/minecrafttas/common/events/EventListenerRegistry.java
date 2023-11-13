package com.minecrafttas.common.events;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EventListenerRegistry {
	
	private static ArrayList<EventBase> EVENTLISTENER_REGISTRY = new ArrayList<>();
	
	
	public static void register(EventBase eventListener) {
		if (eventListener == null) {
			throw new NullPointerException("Tried to register a packethandler with value null");
		}
		EVENTLISTENER_REGISTRY.add(eventListener);
	}
	
	public static void unregister(EventBase eventListener) {
		if (eventListener == null) {
			throw new NullPointerException("Tried to unregister a packethandler with value null");
		}
		EVENTLISTENER_REGISTRY.remove(eventListener);
	}
	
	public static ArrayList<EventBase> getEventListeners(){
		return EVENTLISTENER_REGISTRY;
	}
	
	public static interface EventBase {

		/**
		 * Fires the event specified in eventClass
		 * @param eventClass The event handler class
		 * @param eventParams The parameters for this event
		 * @return The return value of the event
		 */
		public static Object fireEvent(Class<? extends EventListenerRegistry.EventBase> eventClass, Object... eventParams) {
			
			Method methodToFire = null;
			
			for(Method method : eventClass.getMethods()) {
				if(Modifier.isAbstract(method.getModifiers())) {
					methodToFire = method;
					break;
				}
			}
			
			for(EventListenerRegistry.EventBase eventListener : EventListenerRegistry.EVENTLISTENER_REGISTRY) {
				Class<?>[] interfaces = eventListener.getClass().getInterfaces();
				for(Class<?> interfaze : interfaces) {
					if(interfaze.equals(eventClass)) {
						Method methodInClass = null;
							Method[] methods = eventListener.getClass().getMethods();
							for(Method method : methods) {
								if(method.getName().equals(methodToFire.getName())) {
									methodInClass = method;
									break;
								}
							}
						
						try {
							return methodInClass.invoke(eventListener, eventParams);
						} catch (IllegalAccessException e) {
							e.printStackTrace();
							return null;
						} catch (IllegalArgumentException e) {
							e.printStackTrace();
							return null;
						} catch (InvocationTargetException e) {
							e.printStackTrace();
							return null;
						}
					}
				}
			}
			return null;
		}
	}
}
