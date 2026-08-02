package com.autotimetable.utils;

import com.autotimetable.model.ScheduleEntry;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class CSVExporter {

    /**
     * Exports a list of schedule entries to a CSV file.
     */
    public static void exportToCSV(List<ScheduleEntry> entries, File file) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            // Write CSV headers
            writer.write("Day,Time,Module Code,Module Name,Lecturer,Classroom,Batch");
            writer.newLine();

            // Write entries
            for (ScheduleEntry entry : entries) {
                String day = escapeCsvField(entry.getTimeSlot().getDay());
                String time = escapeCsvField(entry.getTimeSlot().getStartTime() + " - " + entry.getTimeSlot().getEndTime());
                String code = escapeCsvField(entry.getModule().getCode());
                String name = escapeCsvField(entry.getModule().getName());
                String lecturer = escapeCsvField(entry.getLecturer().getName());
                String room = escapeCsvField(entry.getClassroom().getRoomName());
                String batch = escapeCsvField(entry.getBatch().getName());

                writer.write(String.join(",", day, time, code, name, lecturer, room, batch));
                writer.newLine();
            }
        }
    }

    private static String escapeCsvField(String field) {
        if (field == null) {
            return "";
        }
        // If the field contains commas, quotes, or newlines, wrap it in quotes and double any internal quotes
        if (field.contains(",") || field.contains("\"") || field.contains("\n") || field.contains("\r")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }
}
