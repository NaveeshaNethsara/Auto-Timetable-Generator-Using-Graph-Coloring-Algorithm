package com.autotimetable.model;

public class ScheduleEntry {
    private LectureSession session;
    private Module module;
    private Lecturer lecturer;
    private Batch batch;
    private TimeSlot timeSlot;
    private Classroom classroom;

    public ScheduleEntry(LectureSession session, Module module, Lecturer lecturer, Batch batch, TimeSlot timeSlot, Classroom classroom) {
        this.session = session;
        this.module = module;
        this.lecturer = lecturer;
        this.batch = batch;
        this.timeSlot = timeSlot;
        this.classroom = classroom;
    }

    public LectureSession getSession() { return session; }
    public void setSession(LectureSession session) { this.session = session; }

    public Module getModule() { return module; }
    public void setModule(Module module) { this.module = module; }

    public Lecturer getLecturer() { return lecturer; }
    public void setLecturer(Lecturer lecturer) { this.lecturer = lecturer; }

    public Batch getBatch() { return batch; }
    public void setBatch(Batch batch) { this.batch = batch; }

    public TimeSlot getTimeSlot() { return timeSlot; }
    public void setTimeSlot(TimeSlot timeSlot) { this.timeSlot = timeSlot; }

    public Classroom getClassroom() { return classroom; }
    public void setClassroom(Classroom classroom) { this.classroom = classroom; }
}
