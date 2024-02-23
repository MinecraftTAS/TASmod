package com.minecrafttas.mctcommon.events;

public class EventException extends RuntimeException {

    public EventException(String message, Class<? extends EventListenerRegistry.EventBase> eventClass) {
        super(eventClass.getName() + ": " + message);
    }

    public EventException(String message, Class<? extends EventListenerRegistry.EventBase> eventClass, Throwable cause) {
        super(eventClass.getName() + ": " + message, cause);
    }

    public EventException(Class<? extends EventListenerRegistry.EventBase> eventClass, Throwable cause) {
        super(eventClass.getName(), cause);
    }
}
