package com.followermaze.event;

/**
 * Enum for all the event types
 */
public enum EventEnum {
    FOLLOW("F"), UN_FOLLOW("U"), BROADCAST("B"), PRIVATE_MSG("P"), STATUS_UPDATE("S");

    private final String name;

    EventEnum(String s) {
        name = s;
    }


    public String toString() {
        return name;
    }

    public static EventEnum fromString(String name) {
        if (name != null) {
            for (EventEnum b : EventEnum.values()) {
                if (name.equalsIgnoreCase(b.name)) {
                    return b;
                }
            }
        }
        return null;
    }

}
