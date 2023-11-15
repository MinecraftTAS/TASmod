package com.minecrafttas.common.events;

import java.util.ArrayList;

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

	}
}
