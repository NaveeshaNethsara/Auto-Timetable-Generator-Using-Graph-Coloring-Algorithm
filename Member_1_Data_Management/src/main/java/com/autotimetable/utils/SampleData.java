package com.autotimetable.utils;

import com.autotimetable.model.Batch;
import com.autotimetable.model.Classroom;
import com.autotimetable.model.Lecturer;
import com.autotimetable.model.Module;
import com.autotimetable.model.TimeSlot;

import java.util.ArrayList;
import java.util.List;

public class SampleData {

    public static List<TimeSlot> getSampleTimeSlots() {
        List<TimeSlot> slots = new ArrayList<>();
        slots.add(new TimeSlot("MON_0830", "Monday", "08:30", "10:30"));
        slots.add(new TimeSlot("MON_1030", "Monday", "10:30", "12:30"));
        slots.add(new TimeSlot("TUE_0830", "Tuesday", "08:30", "10:30"));
        slots.add(new TimeSlot("TUE_1030", "Tuesday", "10:30", "12:30"));
        slots.add(new TimeSlot("WED_0830", "Wednesday", "08:30", "10:30"));
        slots.add(new TimeSlot("WED_1030", "Wednesday", "10:30", "12:30"));
        slots.add(new TimeSlot("THU_0830", "Thursday", "08:30", "10:30"));
        slots.add(new TimeSlot("THU_1030", "Thursday", "10:30", "12:30"));
        slots.add(new TimeSlot("FRI_0830", "Friday", "08:30", "10:30"));
        slots.add(new TimeSlot("FRI_1030", "Friday", "10:30", "12:30"));
        return slots;
    }

    public static List<Lecturer> getSampleLecturers() {
        List<Lecturer> lecturers = new ArrayList<>();
        
        Lecturer prasanna = new Lecturer("L001", "Mr. Prasanna Silva", "prasanna@nibm.lk");
        prasanna.addAvailableSlot("MON_0830");
        prasanna.addAvailableSlot("MON_1030");
        prasanna.addAvailableSlot("TUE_0830");
        prasanna.addAvailableSlot("TUE_1030");
        prasanna.addAvailableSlot("WED_0830");
        lecturers.add(prasanna);

        Lecturer nuwan = new Lecturer("L002", "Dr. Nuwan Perera", "nuwan@nibm.lk");
        nuwan.addAvailableSlot("MON_1030");
        nuwan.addAvailableSlot("TUE_1030");
        nuwan.addAvailableSlot("WED_0830");
        nuwan.addAvailableSlot("WED_1030");
        nuwan.addAvailableSlot("THU_0830");
        nuwan.addAvailableSlot("THU_1030");
        lecturers.add(nuwan);

        Lecturer dilini = new Lecturer("L003", "Ms. Dilini Fernando", "dilini@nibm.lk");
        dilini.addAvailableSlot("TUE_0830");
        dilini.addAvailableSlot("TUE_1030");
        dilini.addAvailableSlot("THU_0830");
        dilini.addAvailableSlot("THU_1030");
        dilini.addAvailableSlot("FRI_0830");
        dilini.addAvailableSlot("FRI_1030");
        lecturers.add(dilini);

        Lecturer chaminda = new Lecturer("L004", "Mr. Chaminda Jayawardena", "chaminda@nibm.lk");
        chaminda.addAvailableSlot("MON_0830");
        chaminda.addAvailableSlot("WED_0830");
        chaminda.addAvailableSlot("THU_0830");
        chaminda.addAvailableSlot("FRI_0830");
        chaminda.addAvailableSlot("FRI_1030");
        lecturers.add(chaminda);

        return lecturers;
    }

    public static List<Batch> getSampleBatches() {
        List<Batch> batches = new ArrayList<>();
        batches.add(new Batch("B001", "HNDSE25.2F", 45)); // Software Engineering
        batches.add(new Batch("B002", "HNDSE25.1F", 50)); // Software Engineering
        batches.add(new Batch("B003", "HNDBIS25.1F", 35)); // Business Info Systems
        batches.add(new Batch("B004", "HNDCS25.1F", 28)); // Computer Science
        return batches;
    }

    public static List<Classroom> getSampleClassrooms() {
        List<Classroom> rooms = new ArrayList<>();
        rooms.add(new Classroom("R001", "Lab C1-03", 30));   // Small Lab
        rooms.add(new Classroom("R002", "Lab C1-04", 50));   // Medium Lab
        rooms.add(new Classroom("R003", "Hall L-201", 60));  // Large Lecture Hall
        rooms.add(new Classroom("R004", "Room S-102", 40));  // Seminar Room
        return rooms;
    }

    public static List<Module> getSampleModules() {
        List<Module> modules = new ArrayList<>();
        // Module(id, code, name, lecturerId, batchId, requiredSessions)
        
        // HNDSE25.2F (B001) modules
        modules.add(new Module("M001", "PDSA", "Data Structures & Algorithms", "L001", "B001", 2));
        modules.add(new Module("M002", "DBMS", "Database Management Systems", "L002", "B001", 2));

        // HNDSE25.1F (B002) modules
        modules.add(new Module("M003", "OOP", "Object Oriented Programming (Java)", "L003", "B002", 2));
        modules.add(new Module("M004", "WebDev", "Web Application Development", "L004", "B002", 1));

        // HNDBIS25.1F (B003) modules
        modules.add(new Module("M005", "EntArch", "Enterprise Architecture", "L002", "B003", 2));
        modules.add(new Module("M006", "SAD", "Systems Analysis & Design", "L003", "B003", 1));

        // HNDCS25.1F (B004) modules
        modules.add(new Module("M007", "AlgoComp", "Algorithms & Complexity", "L001", "B004", 2));
        modules.add(new Module("M008", "DataComm", "Data Communication & Networks", "L004", "B004", 1));

        return modules;
    }
}
