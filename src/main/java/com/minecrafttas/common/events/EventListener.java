package com.minecrafttas.common.events;

import java.util.ArrayList;

public class EventListener {
	
	private static ArrayList<EventBase> EVENTLISTENER_REGISTRY = new ArrayList<>();
	
	
	public static void register(EventBase eventListener) {
		EVENTLISTENER_REGISTRY.add(eventListener);
	}
	
	public static void unregister(EventBase eventListener) {
		EVENTLISTENER_REGISTRY.remove(eventListener);
	}
	
	
	public static ArrayList<EventBase> getEventListeners(){
		return EVENTLISTENER_REGISTRY;
	}
}
