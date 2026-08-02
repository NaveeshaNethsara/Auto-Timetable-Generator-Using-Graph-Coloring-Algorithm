package com.autotimetable.model;

import java.util.HashSet;
import java.util.Set;

public class Classroom {
    private String id;
    private String roomName;
    private int capacity;
    private Set<String> occupiedSlots; // Track assigned slot IDs

    public Classroom(String id, String roomName, int capacity) {
        this.id = id;
        this.roomName = roomName;
        this.capacity = capacity;
        this.occupiedSlots = new HashSet<>();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public Set<String> getOccupiedSlots() { return occupiedSlots; }
    public void setOccupiedSlots(Set<String> occupiedSlots) { this.occupiedSlots = occupiedSlots; }

    public boolean isAvailable(String slotId) { return !occupiedSlots.contains(slotId); }
    public void bookSlot(String slotId) { occupiedSlots.add(slotId); }
    public void freeSlot(String slotId) { occupiedSlots.remove(slotId); }
    public void clearBookings() { occupiedSlots.clear(); }

    @Override
    public String toString() {
        return roomName + " (Cap: " + capacity + ")";
    }
}
