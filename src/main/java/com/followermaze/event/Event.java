package com.followermaze.event;

import com.followermaze.event.EventEnum;

import java.io.IOException;
import java.util.Comparator;

/**
 * Base Event class
 */
public class Event implements Comparator<Event>, Comparable<Event> {
    private static final String REGEX = "\\|";
    private Long sequenceNo;
    private EventEnum type;
    private Integer fromUserId;
    private Integer toUserId;

    public Event(String event) {
        String[] list = event.split(REGEX);
        if (list.length > 0)
            sequenceNo = Long.decode(list[0]);
        if (list.length > 1)
            type = EventEnum.fromString(list[1]);
        if (list.length > 2)
            fromUserId = Integer.decode(list[2]);
        if (list.length > 3)
            toUserId = Integer.decode(list[3]);
    }


    public int compare(Event o1, Event o2) {
        return o1.compareTo(o2);
    }

    public int compareTo(Event o) {
        return sequenceNo.compareTo(o.sequenceNo);
    }

    public Long getSequenceNo() {
        return sequenceNo;
    }

    public EventEnum getType() {
        return type;
    }

    public Integer getFromUserId() {
        return fromUserId;
    }

    public Integer getToUserId() {
        return toUserId;
    }
}
