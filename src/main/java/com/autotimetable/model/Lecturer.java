package com.autotimetable.model;

import java.util.ArrayList;
import java.util.List;

public class Lecturer {
    private String id;
    private String name;
    private String email;
    private List<String> availableSlots; // e.g., ["MON_0830", "MON_1030", "TUE_0830"]

    public Lecturer(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.availableSlots = new ArrayList<>();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public List<String> getAvailableSlots() { return availableSlots; }
    public void setAvailableSlots(List<String> availableSlots) { this.availableSlots = availableSlots; }
    public void addAvailableSlot(String slotId) { this.availableSlots.add(slotId); }
    public void removeAvailableSlot(String slotId) { this.availableSlots.remove(slotId); }

    @Override
    public String toString() {
        return name + " (" + id + ")";
    }
}
