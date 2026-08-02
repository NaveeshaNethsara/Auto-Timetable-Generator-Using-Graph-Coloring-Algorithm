package com.autotimetable.ui;

import com.autotimetable.algorithm.BFSTraversal;
import com.autotimetable.datastructure.ConflictGraph;
import com.autotimetable.model.Lecturer;
import com.autotimetable.model.Module;
import com.autotimetable.model.Batch;
import com.autotimetable.model.Classroom;
import com.autotimetable.model.TimeSlot;
import com.autotimetable.model.LectureSession;
import com.autotimetable.model.ScheduleEntry;
import com.autotimetable.service.ConflictReportService;
import com.autotimetable.service.ConflictReportService.ConflictAnalyzerReport;
import com.autotimetable.service.QualityCalculator.QualityMetrics;
import com.autotimetable.service.TimetableGenerator;
import com.autotimetable.service.TimetableGenerator.GenerationResult;
import com.autotimetable.utils.CSVExporter;
import com.autotimetable.utils.SampleData;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MainForm extends JFrame {

    // Domain lists
    private final List<Lecturer> lecturers = new ArrayList<>();
    private final List<Batch> batches = new ArrayList<>();
    private final List<Module> modules = new ArrayList<>();
    private final List<Classroom> classrooms = new ArrayList<>();
    private final List<TimeSlot> timeSlots = new ArrayList<>();

    // Services
    private final TimetableGenerator generator = new TimetableGenerator();
    private final ConflictReportService conflictAnalyzer = new ConflictReportService();
    private final BFSTraversal bfsTraversal = new BFSTraversal();

    // Results state
    private GenerationResult currentResult = null;

    // Swing UI Components
    private JTabbedPane mainTabbedPane;
    
    // Management Table Models
    private DefaultTableModel lecturerMgmtModel;
    private DefaultTableModel batchMgmtModel;
    private DefaultTableModel moduleMgmtModel;
    private DefaultTableModel roomMgmtModel;
    private DefaultTableModel slotMgmtModel;

    // Timetable Tables
    private JTable masterTimetableTable;
    private DefaultTableModel masterTableModel;

    private JTable filteredTimetableTable;
    private DefaultTableModel filteredTableModel;
    private JComboBox<String> filterTypeCombo;
    private JComboBox<Object> filterSelectionCombo;

    // Metric Labels
    private JLabel schedRateValLabel;
    private JLabel schedRateSubLabel;
    private JLabel roomUtilValLabel;
    private JLabel roomUtilSubLabel;
    private JLabel slotUtilValLabel;
    private JLabel slotUtilSubLabel;
    private JLabel execTimeValLabel;
    private JLabel execTimeSubLabel;

    // Diagnostic text areas
    private JTextArea preGenReportArea;
    private JTextArea postGenMetricsArea;
    private DefaultListModel<String> unscheduledListModel;

    public MainForm() {
        setTitle("Auto Timetable Generator - NIBM PDSA Systems");
        setSize(1280, 850);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        initComponents();
        updateManagementTables();
        runPreGenAnalysis();
    }

    private void initComponents() {
        // --- Left Sidebar Control & Metrics Panel ---
        JPanel sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setPreferredSize(new Dimension(300, 850));
        sidebarPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(60, 60, 60)),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        sidebarPanel.setBackground(new Color(30, 30, 30));

        // Title Label
        JLabel appTitleLabel = new JLabel("Auto Timetable");
        appTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        appTitleLabel.setForeground(Color.WHITE);
        appTitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel appSubTitleLabel = new JLabel("Graph Coloring Engine");
        appSubTitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        appSubTitleLabel.setForeground(new Color(150, 150, 150));
        appSubTitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        sidebarPanel.add(appTitleLabel);
        sidebarPanel.add(appSubTitleLabel);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Action Buttons
        JButton loadSampleBtn = new JButton("Load NIBM Sample Data");
        loadSampleBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        loadSampleBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        loadSampleBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        loadSampleBtn.addActionListener(e -> loadSampleData());

        JButton runGenBtn = new JButton("Run Schedule Generator");
        runGenBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        runGenBtn.setBackground(new Color(35, 120, 240));
        runGenBtn.setForeground(Color.WHITE);
        runGenBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        runGenBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        runGenBtn.addActionListener(e -> generateTimetable());

        JButton exportCsvBtn = new JButton("Export Timetable to CSV");
        exportCsvBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        exportCsvBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        exportCsvBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        exportCsvBtn.addActionListener(e -> exportToCsv());

        JButton resetBtn = new JButton("Reset Database");
        resetBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        resetBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        resetBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        resetBtn.addActionListener(e -> resetDatabase());

        sidebarPanel.add(loadSampleBtn);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebarPanel.add(runGenBtn);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebarPanel.add(exportCsvBtn);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebarPanel.add(resetBtn);
        
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 25)));

        // Metrics Section Divider
        JLabel metricsTitle = new JLabel("SCHEDULING METRICS");
        metricsTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        metricsTitle.setForeground(new Color(120, 120, 120));
        metricsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebarPanel.add(metricsTitle);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Metric Cards (4 rows of custom panels)
        JPanel rateCard = createMetricCard("Scheduling Success Rate", "N/A", "0 of 0 sessions");
        schedRateValLabel = (JLabel) rateCard.getClientProperty("valLabel");
        schedRateSubLabel = (JLabel) rateCard.getClientProperty("subLabel");

        JPanel roomCard = createMetricCard("Room Space Utilization", "N/A", "Capacity vs batch count");
        roomUtilValLabel = (JLabel) roomCard.getClientProperty("valLabel");
        roomUtilSubLabel = (JLabel) roomCard.getClientProperty("subLabel");

        JPanel slotCard = createMetricCard("Time Slots Usage Rate", "N/A", "Unique slots filled");
        slotUtilValLabel = (JLabel) slotCard.getClientProperty("valLabel");
        slotUtilSubLabel = (JLabel) slotCard.getClientProperty("subLabel");

        JPanel speedCard = createMetricCard("Greedy Generator Speed", "N/A", "Execution duration");
        execTimeValLabel = (JLabel) speedCard.getClientProperty("valLabel");
        execTimeSubLabel = (JLabel) speedCard.getClientProperty("subLabel");

        sidebarPanel.add(rateCard);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebarPanel.add(roomCard);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebarPanel.add(slotCard);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebarPanel.add(speedCard);

        add(sidebarPanel, BorderLayout.WEST);

        // --- Center Tabbed Content Panel ---
        mainTabbedPane = new JTabbedPane();
        mainTabbedPane.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Tab 1: Master Timetable Dashboard
        JPanel masterTab = new JPanel(new BorderLayout(10, 10));
        masterTableModel = new DefaultTableModel(new Object[]{"Session ID", "Day", "Time Slot", "Module Code", "Module Name", "Lecturer", "Classroom", "Student Batch"}, 0);
        masterTimetableTable = createReadOnlyTable(masterTableModel);
        masterTab.add(new JLabel("Master Complete Timetable Grid"), BorderLayout.NORTH);
        masterTab.add(new JScrollPane(masterTimetableTable), BorderLayout.CENTER);
        mainTabbedPane.addTab("Master Timetable", masterTab);

        // Tab 2: Filtered Timetable View
        JPanel filteredTab = new JPanel(new BorderLayout(10, 10));
        JPanel filterControlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        
        filterControlPanel.add(new JLabel("Filter By:"));
        filterTypeCombo = new JComboBox<>(new String[]{"Student Batch", "Lecturer", "Classroom"});
        filterControlPanel.add(filterTypeCombo);

        filterControlPanel.add(new JLabel("Select Entity:"));
        filterSelectionCombo = new JComboBox<>();
        filterControlPanel.add(filterSelectionCombo);

        filterTypeCombo.addActionListener(e -> updateFilterSelectionOptions());
        filterSelectionCombo.addActionListener(e -> updateFilteredTimetable());

        filteredTab.add(filterControlPanel, BorderLayout.NORTH);

        filteredTableModel = new DefaultTableModel(new Object[]{"Day", "Time Slot", "Module Code", "Module Name", "Lecturer", "Classroom", "Batch"}, 0);
        filteredTimetableTable = createReadOnlyTable(filteredTableModel);
        filteredTab.add(new JScrollPane(filteredTimetableTable), BorderLayout.CENTER);
        mainTabbedPane.addTab("Filtered Schedule Views", filteredTab);

        // Tab 3: Data Management Dashboard
        JTabbedPane mgmtTabbedPane = new JTabbedPane();
        mgmtTabbedPane.addTab("Lecturers", createLecturerMgmtPanel());
        mgmtTabbedPane.addTab("Student Batches", createBatchMgmtPanel());
        mgmtTabbedPane.addTab("Modules", createModuleMgmtPanel());
        mgmtTabbedPane.addTab("Classrooms", createClassroomMgmtPanel());
        mgmtTabbedPane.addTab("Time Slots", createSlotMgmtPanel());
        
        JPanel mgmtContainerPanel = new JPanel(new BorderLayout());
        mgmtContainerPanel.add(mgmtTabbedPane, BorderLayout.CENTER);
        mainTabbedPane.addTab("Manage Entities", mgmtContainerPanel);

        // Tab 4: Diagnostics, Analysis & Graph Metrics
        JPanel diagnosticTab = new JPanel(new GridLayout(1, 2, 15, 0));
        
        // Left - Pre-Generation
        JPanel preGenPanel = new JPanel(new BorderLayout(5, 5));
        preGenPanel.setBorder(BorderFactory.createTitledBorder("Pre-Generation Bottleneck Diagnostics"));
        preGenReportArea = new JTextArea();
        preGenReportArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        preGenReportArea.setEditable(false);
        preGenPanel.add(new JScrollPane(preGenReportArea), BorderLayout.CENTER);
        
        // Right - Post-Generation & Heap / Unscheduled details
        JPanel postGenPanel = new JPanel(new BorderLayout(5, 5));
        postGenPanel.setBorder(BorderFactory.createTitledBorder("Post-Generation Analysis & Connected Components"));
        postGenMetricsArea = new JTextArea();
        postGenMetricsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        postGenMetricsArea.setEditable(false);
        
        // Unscheduled sessions sub-panel
        JPanel unscheduledSubPanel = new JPanel(new BorderLayout(5, 5));
        unscheduledSubPanel.setPreferredSize(new Dimension(300, 200));
        unscheduledSubPanel.setBorder(BorderFactory.createTitledBorder("Unallocated Sessions (Clash / Deficit)"));
        unscheduledListModel = new DefaultListModel<>();
        JList<String> unscheduledList = new JList<>(unscheduledListModel);
        unscheduledSubPanel.add(new JScrollPane(unscheduledList), BorderLayout.CENTER);

        JSplitPane rightSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(postGenMetricsArea), unscheduledSubPanel);
        rightSplit.setDividerLocation(300);
        postGenPanel.add(rightSplit, BorderLayout.CENTER);

        diagnosticTab.add(preGenPanel);
        diagnosticTab.add(postGenPanel);
        mainTabbedPane.addTab("Diagnostics & Graph Analysis", diagnosticTab);

        add(mainTabbedPane, BorderLayout.CENTER);
    }

    private JPanel createMetricCard(String title, String defaultValue, String subtext) {
        JPanel card = new JPanel(new BorderLayout(4, 4));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 60, 60), 1, true),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        card.setBackground(new Color(38, 38, 38));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 75));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        titleLabel.setForeground(new Color(160, 160, 160));

        JLabel valLabel = new JLabel(defaultValue);
        valLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        valLabel.setForeground(Color.WHITE);

        JLabel subLabel = new JLabel(subtext);
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        subLabel.setForeground(new Color(130, 130, 130));

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valLabel, BorderLayout.CENTER);
        card.add(subLabel, BorderLayout.SOUTH);

        card.putClientProperty("valLabel", valLabel);
        card.putClientProperty("subLabel", subLabel);

        return card;
    }

    private JTable createReadOnlyTable(DefaultTableModel model) {
        JTable table = new JTable(model) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        table.setRowHeight(25);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        return table;
    }

    // --- Dynamic Filters Logic ---
    private void updateFilterSelectionOptions() {
        filterSelectionCombo.removeAllItems();
        int selectedIndex = filterTypeCombo.getSelectedIndex();

        if (selectedIndex == 0) { // Student Batch
            for (Batch b : batches) {
                filterSelectionCombo.addItem(b.getName());
            }
        } else if (selectedIndex == 1) { // Lecturer
            for (Lecturer l : lecturers) {
                filterSelectionCombo.addItem(l.getName());
            }
        } else if (selectedIndex == 2) { // Classroom
            for (Classroom r : classrooms) {
                filterSelectionCombo.addItem(r.getRoomName());
            }
        }
        updateFilteredTimetable();
    }

    private void updateFilteredTimetable() {
        filteredTableModel.setRowCount(0);
        if (currentResult == null || filterSelectionCombo.getSelectedItem() == null) {
            return;
        }

        String filterType = (String) filterTypeCombo.getSelectedItem();
        String selectedVal = filterSelectionCombo.getSelectedItem().toString();

        for (ScheduleEntry entry : currentResult.getScheduleEntries()) {
            boolean match = false;
            if ("Student Batch".equals(filterType) && entry.getBatch().getName().equals(selectedVal)) {
                match = true;
            } else if ("Lecturer".equals(filterType) && entry.getLecturer().getName().equals(selectedVal)) {
                match = true;
            } else if ("Classroom".equals(filterType) && entry.getClassroom().getRoomName().equals(selectedVal)) {
                match = true;
            }

            if (match) {
                filteredTableModel.addRow(new Object[]{
                        entry.getTimeSlot().getDay(),
                        entry.getTimeSlot().getStartTime() + " - " + entry.getTimeSlot().getEndTime(),
                        entry.getModule().getCode(),
                        entry.getModule().getName(),
                        entry.getLecturer().getName(),
                        entry.getClassroom().getRoomName(),
                        entry.getBatch().getName()
                });
            }
        }
    }

    // --- Action Listeners ---
    private void loadSampleData() {
        lecturers.clear();
        lecturers.addAll(SampleData.getSampleLecturers());

        batches.clear();
        batches.addAll(SampleData.getSampleBatches());

        timeSlots.clear();
        timeSlots.addAll(SampleData.getSampleTimeSlots());

        classrooms.clear();
        classrooms.addAll(SampleData.getSampleClassrooms());

        modules.clear();
        modules.addAll(SampleData.getSampleModules());

        updateManagementTables();
        runPreGenAnalysis();
        updateFilterSelectionOptions();

        JOptionPane.showMessageDialog(this, "Realistic NIBM academic sample dataset loaded successfully!\nClick 'Run Schedule Generator' to create the timetable.", "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    private void generateTimetable() {
        if (lecturers.isEmpty() || batches.isEmpty() || classrooms.isEmpty() || modules.isEmpty() || timeSlots.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Cannot generate timetable. Please populate database or load sample data first.", "Missing Data", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Run generators
        currentResult = generator.generateTimetable(lecturers, modules, batches, classrooms, timeSlots);

        // Update Dashboard Master Table
        masterTableModel.setRowCount(0);
        for (ScheduleEntry entry : currentResult.getScheduleEntries()) {
            masterTableModel.addRow(new Object[]{
                    entry.getSession().getSessionId(),
                    entry.getTimeSlot().getDay(),
                    entry.getTimeSlot().getStartTime() + " - " + entry.getTimeSlot().getEndTime(),
                    entry.getModule().getCode(),
                    entry.getModule().getName(),
                    entry.getLecturer().getName(),
                    entry.getClassroom().getRoomName(),
                    entry.getBatch().getName()
            });
        }

        // Update Metrics Cards
        QualityMetrics metrics = currentResult.getQualityMetrics();
        schedRateValLabel.setText(metrics.getSchedulingRate() + "%");
        schedRateSubLabel.setText(String.format("Scheduled: %d / %d sessions", metrics.getScheduledSessions(), metrics.getTotalSessions()));

        roomUtilValLabel.setText(metrics.getRoomUtilizationRate() + "%");
        roomUtilSubLabel.setText("Room spaces matched by capacity");

        slotUtilValLabel.setText(metrics.getSlotUtilizationRate() + "%");
        slotUtilSubLabel.setText(String.format("Used: %d slots", uniqueSlotsUsedCount()));

        execTimeValLabel.setText(currentResult.getExecutionTimeMs() + " ms");
        execTimeSubLabel.setText("Welsh-Powell Greedy algorithm");

        // Update Unscheduled Sessions details
        unscheduledListModel.clear();
        for (LectureSession s : currentResult.getUnscheduledSessions()) {
            // Find cause (Lecturer bottleneck or Classroom space constraint)
            String cause = findSchedulingFailReason(s);
            unscheduledListModel.addElement(s.getSessionId() + " (" + s.getModule().getCode() + ") - Reason: " + cause);
        }

        // Update Filtered Table
        updateFilteredTimetable();

        // Update Post Gen diagnostic panel (incorporating BFS Traversal)
        updatePostGenAnalysis();

        JOptionPane.showMessageDialog(this, "Schedule Generated Successfully!\nTotal execution time: " + currentResult.getExecutionTimeMs() + " ms.\nTimetable Quality Score: " + metrics.getOverallScore() + "%", "Scheduling Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private String findSchedulingFailReason(LectureSession s) {
        String lecturerId = s.getModule().getLecturerId();
        // check if lecturer availability list is empty
        Lecturer lec = null;
        for (Lecturer l : lecturers) {
            if (l.getId().equals(lecturerId)) {
                lec = l;
                break;
            }
        }
        if (lec == null) {
            return "No Lecturer assigned";
        }
        if (lec.getAvailableSlots().isEmpty()) {
            return "Lecturer has no available hours set";
        }
        
        // If timeslot assignment is null
        if (s.getAssignedSlotId() == null) {
            return "Lecturer available slots fully occupied by clashing classes";
        }
        
        // If room assignment is null but slot is colored
        if (s.getAssignedRoomId() == null) {
            return "No available classroom matches the batch size during this slot";
        }
        
        return "Unknown constraint conflict";
    }

    private int uniqueSlotsUsedCount() {
        if (currentResult == null) return 0;
        java.util.Set<String> uniqueSlots = new java.util.HashSet<>();
        for (ScheduleEntry e : currentResult.getScheduleEntries()) {
            uniqueSlots.add(e.getTimeSlot().getId());
        }
        return uniqueSlots.size();
    }

    private void exportToCsv() {
        if (currentResult == null || currentResult.getScheduleEntries().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No active schedule to export. Run the generator first.", "Export Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Timetable CSV File");
        fileChooser.setSelectedFile(new File("nibm_generated_timetable.csv"));
        
        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try {
                CSVExporter.exportToCSV(currentResult.getScheduleEntries(), fileToSave);
                JOptionPane.showMessageDialog(this, "Timetable exported to CSV file successfully:\n" + fileToSave.getAbsolutePath(), "Export Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "An error occurred while writing CSV:\n" + ex.getMessage(), "Export Failure", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void resetDatabase() {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to clear all data in the database?", "Reset Database", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            lecturers.clear();
            batches.clear();
            modules.clear();
            classrooms.clear();
            timeSlots.clear();
            currentResult = null;

            // Clear models
            masterTableModel.setRowCount(0);
            filteredTableModel.setRowCount(0);
            unscheduledListModel.clear();

            // Clear metrics
            schedRateValLabel.setText("N/A");
            schedRateSubLabel.setText("0 of 0 sessions");
            roomUtilValLabel.setText("N/A");
            roomUtilSubLabel.setText("Capacity vs batch count");
            slotUtilValLabel.setText("N/A");
            slotUtilSubLabel.setText("Unique slots filled");
            execTimeValLabel.setText("N/A");
            execTimeSubLabel.setText("Execution duration");

            updateManagementTables();
            runPreGenAnalysis();
            updateFilterSelectionOptions();
            postGenMetricsArea.setText("");

            JOptionPane.showMessageDialog(this, "Database cleared successfully.", "Reset Done", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // --- Diagnostics Logic ---
    private void runPreGenAnalysis() {
        ConflictAnalyzerReport report = conflictAnalyzer.analyzeConflicts(lecturers, modules, batches, classrooms, timeSlots);
        preGenReportArea.setText(report.toString());
    }

    private void updatePostGenAnalysis() {
        if (currentResult == null) return;
        
        ConflictGraph graph = currentResult.getConflictGraph();
        List<Set<String>> components = bfsTraversal.findConnectedComponents(graph);
        QualityMetrics metrics = currentResult.getQualityMetrics();

        StringBuilder sb = new StringBuilder();
        sb.append("=== POST-GENERATION STRUCTURAL GRAPH ANALYSIS ===\n\n");
        sb.append("CONFLICT GRAPH STATISTICS:\n");
        sb.append(" - Vertices (Lecture Sessions): ").append(graph.getVertices().size()).append("\n");
        
        int totalEdges = 0;
        for (String v : graph.getVertices()) {
            totalEdges += graph.getDegree(v);
        }
        totalEdges = totalEdges / 2; // Undirected graph
        
        sb.append(" - Edges (Degree of Clashes):  ").append(totalEdges).append("\n");
        sb.append(" - Connected Components (BFS): ").append(components.size()).append("\n\n");
        
        sb.append("CONNECTED COMPONENTS BREAKDOWN:\n");
        int count = 1;
        for (Set<String> component : components) {
            sb.append(" Component #").append(count++).append(" (size: ").append(component.size()).append("):\n");
            sb.append("   Sessions: ").append(String.join(", ", component)).append("\n");
        }
        sb.append("\n");
        
        sb.append("TIMETABLE EVALUATION SUMMARY:\n");
        sb.append(" - Scheduled Success rate : ").append(metrics.getSchedulingRate()).append("%\n");
        sb.append(" - Space Allocation efficiency: ").append(metrics.getRoomUtilizationRate()).append("%\n");
        sb.append(" - System Slots occupancy  : ").append(metrics.getSlotUtilizationRate()).append("%\n");
        sb.append(" ---------------------------------------------\n");
        sb.append(" - Calculated Quality Score : ").append(metrics.getOverallScore()).append("%\n");
        sb.append("=================================================");

        postGenMetricsArea.setText(sb.toString());
    }

    // --- Entity Management Tables Sync ---
    private void updateManagementTables() {
        // Lecturers table
        lecturerMgmtModel.setRowCount(0);
        for (Lecturer l : lecturers) {
            lecturerMgmtModel.addRow(new Object[]{l.getId(), l.getName(), l.getEmail(), String.join(", ", l.getAvailableSlots())});
        }

        // Batches table
        batchMgmtModel.setRowCount(0);
        for (Batch b : batches) {
            batchMgmtModel.addRow(new Object[]{b.getId(), b.getName(), b.getStudentCount()});
        }

        // Modules table
        moduleMgmtModel.setRowCount(0);
        for (Module m : modules) {
            moduleMgmtModel.addRow(new Object[]{m.getId(), m.getCode(), m.getName(), m.getLecturerId(), m.getBatchId(), m.getRequiredSessions()});
        }

        // Classrooms table
        roomMgmtModel.setRowCount(0);
        for (Classroom r : classrooms) {
            roomMgmtModel.addRow(new Object[]{r.getId(), r.getRoomName(), r.getCapacity()});
        }

        // Slots table
        slotMgmtModel.setRowCount(0);
        for (TimeSlot s : timeSlots) {
            slotMgmtModel.addRow(new Object[]{s.getId(), s.getDay(), s.getStartTime(), s.getEndTime()});
        }
    }

    // --- UI Panels Builders (CRUD Panels) ---
    private JPanel createLecturerMgmtPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        lecturerMgmtModel = new DefaultTableModel(new Object[]{"ID", "Name", "Email", "Available Slots"}, 0);
        JTable table = createReadOnlyTable(lecturerMgmtModel);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addBtn = new JButton("Add Lecturer");
        JButton editBtn = new JButton("Edit Lecturer");
        JButton delBtn = new JButton("Delete Lecturer");

        addBtn.addActionListener(e -> showLecturerDialog(null));
        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) showLecturerDialog(lecturers.get(row));
            else JOptionPane.showMessageDialog(this, "Select a lecturer to edit.", "Selection Required", JOptionPane.WARNING_MESSAGE);
        });
        delBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                lecturers.remove(row);
                updateManagementTables();
                runPreGenAnalysis();
                updateFilterSelectionOptions();
            } else {
                JOptionPane.showMessageDialog(this, "Select a lecturer to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            }
        });

        btnPanel.add(addBtn);
        btnPanel.add(editBtn);
        btnPanel.add(delBtn);

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);
        return panel;
    }

    private void showLecturerDialog(Lecturer lecturer) {
        JTextField idField = new JTextField(lecturer != null ? lecturer.getId() : "");
        JTextField nameField = new JTextField(lecturer != null ? lecturer.getName() : "");
        JTextField emailField = new JTextField(lecturer != null ? lecturer.getEmail() : "");
        
        if (lecturer != null) {
            idField.setEnabled(false); // ID is primary key
        }

        // Availability multi-selector
        JList<String> slotJList;
        boolean hasSlots = !timeSlots.isEmpty();
        if (!hasSlots) {
            slotJList = new JList<>(new String[]{"(Go to 'Time Slots' tab to define slots first)"});
            slotJList.setEnabled(false);
        } else {
            slotJList = new JList<>(timeSlots.stream().map(TimeSlot::getId).toArray(String[]::new));
            slotJList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            if (lecturer != null) {
                List<Integer> indices = new ArrayList<>();
                for (int i = 0; i < timeSlots.size(); i++) {
                    if (lecturer.getAvailableSlots().contains(timeSlots.get(i).getId())) {
                        indices.add(i);
                    }
                }
                slotJList.setSelectedIndices(indices.stream().mapToInt(Integer::intValue).toArray());
            }
        }

        JPanel dialogPanel = new JPanel(new GridBagLayout());
        dialogPanel.setPreferredSize(new Dimension(420, 360));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
        dialogPanel.add(new JLabel("Lecturer ID:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        dialogPanel.add(idField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        dialogPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        dialogPanel.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0;
        dialogPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        dialogPanel.add(emailField, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.weightx = 1.0;
        dialogPanel.add(new JLabel("Available Time Slots (Hold Ctrl to multi-select):"), gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        JScrollPane scrollPane = new JScrollPane(slotJList);
        dialogPanel.add(scrollPane, gbc);

        int result = JOptionPane.showConfirmDialog(this, dialogPanel, 
                lecturer != null ? "Edit Lecturer Details" : "Add New Lecturer", 
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String id = idField.getText().trim();
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();

            if (id.isEmpty() || name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "ID and Name cannot be empty.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            List<String> selectedSlots = hasSlots ? slotJList.getSelectedValuesList() : new ArrayList<>();

            if (lecturer == null) {
                // Check unique constraint
                for (Lecturer l : lecturers) {
                    if (l.getId().equals(id)) {
                        JOptionPane.showMessageDialog(this, "Lecturer ID must be unique.", "Duplicate ID", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
                Lecturer newL = new Lecturer(id, name, email);
                newL.setAvailableSlots(new ArrayList<>(selectedSlots));
                lecturers.add(newL);
            } else {
                lecturer.setName(name);
                lecturer.setEmail(email);
                lecturer.setAvailableSlots(new ArrayList<>(selectedSlots));
            }

            updateManagementTables();
            runPreGenAnalysis();
            updateFilterSelectionOptions();
        }
    }

    private JPanel createBatchMgmtPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        batchMgmtModel = new DefaultTableModel(new Object[]{"ID", "Name", "Student Count"}, 0);
        JTable table = createReadOnlyTable(batchMgmtModel);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addBtn = new JButton("Add Batch");
        JButton editBtn = new JButton("Edit Batch");
        JButton delBtn = new JButton("Delete Batch");

        addBtn.addActionListener(e -> showBatchDialog(null));
        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) showBatchDialog(batches.get(row));
            else JOptionPane.showMessageDialog(this, "Select a batch to edit.", "Selection Required", JOptionPane.WARNING_MESSAGE);
        });
        delBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                batches.remove(row);
                updateManagementTables();
                runPreGenAnalysis();
                updateFilterSelectionOptions();
            } else {
                JOptionPane.showMessageDialog(this, "Select a batch to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            }
        });

        btnPanel.add(addBtn);
        btnPanel.add(editBtn);
        btnPanel.add(delBtn);

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);
        return panel;
    }

    private void showBatchDialog(Batch batch) {
        JTextField idField = new JTextField(batch != null ? batch.getId() : "");
        JTextField nameField = new JTextField(batch != null ? batch.getName() : "");
        JSpinner countSpinner = new JSpinner(new SpinnerNumberModel(batch != null ? batch.getStudentCount() : 30, 1, 500, 1));

        if (batch != null) {
            idField.setEnabled(false);
        }

        JPanel dialogPanel = new JPanel(new GridBagLayout());
        dialogPanel.setPreferredSize(new Dimension(380, 150));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
        dialogPanel.add(new JLabel("Batch ID:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        dialogPanel.add(idField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        dialogPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        dialogPanel.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0;
        dialogPanel.add(new JLabel("Student Count:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        dialogPanel.add(countSpinner, gbc);

        int result = JOptionPane.showConfirmDialog(this, dialogPanel, 
                batch != null ? "Edit Student Batch" : "Add Student Batch", 
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String id = idField.getText().trim();
            String name = nameField.getText().trim();
            int count = (Integer) countSpinner.getValue();

            if (id.isEmpty() || name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "ID and Name cannot be empty.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (batch == null) {
                for (Batch b : batches) {
                    if (b.getId().equals(id)) {
                        JOptionPane.showMessageDialog(this, "Batch ID must be unique.", "Duplicate ID", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
                batches.add(new Batch(id, name, count));
            } else {
                batch.setName(name);
                batch.setStudentCount(count);
            }

            updateManagementTables();
            runPreGenAnalysis();
            updateFilterSelectionOptions();
        }
    }

    private JPanel createModuleMgmtPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        moduleMgmtModel = new DefaultTableModel(new Object[]{"ID", "Module Code", "Name", "Lecturer ID", "Batch ID", "Required Sessions"}, 0);
        JTable table = createReadOnlyTable(moduleMgmtModel);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addBtn = new JButton("Add Module");
        JButton editBtn = new JButton("Edit Module");
        JButton delBtn = new JButton("Delete Module");

        addBtn.addActionListener(e -> showModuleDialog(null));
        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) showModuleDialog(modules.get(row));
            else JOptionPane.showMessageDialog(this, "Select a module to edit.", "Selection Required", JOptionPane.WARNING_MESSAGE);
        });
        delBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                modules.remove(row);
                updateManagementTables();
                runPreGenAnalysis();
                updateFilterSelectionOptions();
            } else {
                JOptionPane.showMessageDialog(this, "Select a module to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            }
        });

        btnPanel.add(addBtn);
        btnPanel.add(editBtn);
        btnPanel.add(delBtn);

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);
        return panel;
    }

    private void showModuleDialog(Module module) {
        JTextField idField = new JTextField(module != null ? module.getId() : "");
        JTextField codeField = new JTextField(module != null ? module.getCode() : "");
        JTextField nameField = new JTextField(module != null ? module.getName() : "");
        
        JComboBox<String> lecCombo;
        boolean hasLecs = !lecturers.isEmpty();
        if (!hasLecs) {
            lecCombo = new JComboBox<>(new String[]{"(Go to 'Lecturers' tab to add lecturers first)"});
            lecCombo.setEnabled(false);
        } else {
            lecCombo = new JComboBox<>(lecturers.stream().map(l -> l.getId() + " - " + l.getName()).toArray(String[]::new));
        }

        JComboBox<String> batchCombo;
        boolean hasBatches = !batches.isEmpty();
        if (!hasBatches) {
            batchCombo = new JComboBox<>(new String[]{"(Go to 'Student Batches' tab to add batches first)"});
            batchCombo.setEnabled(false);
        } else {
            batchCombo = new JComboBox<>(batches.stream().map(b -> b.getId() + " - " + b.getName()).toArray(String[]::new));
        }
        
        JSpinner sessionsSpinner = new JSpinner(new SpinnerNumberModel(module != null ? module.getRequiredSessions() : 1, 1, 10, 1));

        if (module != null) {
            idField.setEnabled(false);
            
            // set selection
            if (hasLecs) {
                for (int i = 0; i < lecturers.size(); i++) {
                    if (lecturers.get(i).getId().equals(module.getLecturerId())) {
                        lecCombo.setSelectedIndex(i);
                        break;
                    }
                }
            }
            if (hasBatches) {
                for (int i = 0; i < batches.size(); i++) {
                    if (batches.get(i).getId().equals(module.getBatchId())) {
                        batchCombo.setSelectedIndex(i);
                        break;
                    }
                }
            }
        }

        JPanel dialogPanel = new JPanel(new GridBagLayout());
        dialogPanel.setPreferredSize(new Dimension(420, 250));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
        dialogPanel.add(new JLabel("Module ID:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        dialogPanel.add(idField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        dialogPanel.add(new JLabel("Module Code:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        dialogPanel.add(codeField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0;
        dialogPanel.add(new JLabel("Module Name:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        dialogPanel.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.0;
        dialogPanel.add(new JLabel("Lecturer:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        dialogPanel.add(lecCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.0;
        dialogPanel.add(new JLabel("Batch:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        dialogPanel.add(batchCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0.0;
        dialogPanel.add(new JLabel("Required Sessions:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        dialogPanel.add(sessionsSpinner, gbc);

        int result = JOptionPane.showConfirmDialog(this, dialogPanel, 
                module != null ? "Edit Module Settings" : "Add Module", 
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String id = idField.getText().trim();
            String code = codeField.getText().trim();
            String name = nameField.getText().trim();
            int sessions = (Integer) sessionsSpinner.getValue();

            if (id.isEmpty() || code.isEmpty() || name.isEmpty() || !hasLecs || !hasBatches) {
                JOptionPane.showMessageDialog(this, "All fields are required. Please check that lecturers and batches exist.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String lId = hasLecs ? lecCombo.getSelectedItem().toString().split(" - ")[0] : "";
            String bId = hasBatches ? batchCombo.getSelectedItem().toString().split(" - ")[0] : "";

            if (module == null) {
                for (Module m : modules) {
                    if (m.getId().equals(id)) {
                        JOptionPane.showMessageDialog(this, "Module ID must be unique.", "Duplicate ID", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
                modules.add(new Module(id, code, name, lId, bId, sessions));
            } else {
                module.setCode(code);
                module.setName(name);
                module.setLecturerId(lId);
                module.setBatchId(bId);
                module.setRequiredSessions(sessions);
            }

            updateManagementTables();
            runPreGenAnalysis();
            updateFilterSelectionOptions();
        }
    }

    private JPanel createClassroomMgmtPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        roomMgmtModel = new DefaultTableModel(new Object[]{"ID", "Room Name", "Capacity"}, 0);
        JTable table = createReadOnlyTable(roomMgmtModel);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addBtn = new JButton("Add Room");
        JButton editBtn = new JButton("Edit Room");
        JButton delBtn = new JButton("Delete Room");

        addBtn.addActionListener(e -> showRoomDialog(null));
        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) showRoomDialog(classrooms.get(row));
            else JOptionPane.showMessageDialog(this, "Select a room to edit.", "Selection Required", JOptionPane.WARNING_MESSAGE);
        });
        delBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                classrooms.remove(row);
                updateManagementTables();
                runPreGenAnalysis();
                updateFilterSelectionOptions();
            } else {
                JOptionPane.showMessageDialog(this, "Select a room to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            }
        });

        btnPanel.add(addBtn);
        btnPanel.add(editBtn);
        btnPanel.add(delBtn);

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);
        return panel;
    }

    private void showRoomDialog(Classroom room) {
        JTextField idField = new JTextField(room != null ? room.getId() : "");
        JTextField nameField = new JTextField(room != null ? room.getRoomName() : "");
        JSpinner capSpinner = new JSpinner(new SpinnerNumberModel(room != null ? room.getCapacity() : 40, 1, 1000, 1));

        if (room != null) {
            idField.setEnabled(false);
        }

        JPanel dialogPanel = new JPanel(new GridBagLayout());
        dialogPanel.setPreferredSize(new Dimension(380, 150));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
        dialogPanel.add(new JLabel("Room ID:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        dialogPanel.add(idField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        dialogPanel.add(new JLabel("Room Name:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        dialogPanel.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0;
        dialogPanel.add(new JLabel("Capacity:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        dialogPanel.add(capSpinner, gbc);

        int result = JOptionPane.showConfirmDialog(this, dialogPanel, 
                room != null ? "Edit Room Details" : "Add Classroom/Lab", 
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String id = idField.getText().trim();
            String name = nameField.getText().trim();
            int cap = (Integer) capSpinner.getValue();

            if (id.isEmpty() || name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "ID and Name cannot be empty.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (room == null) {
                for (Classroom r : classrooms) {
                    if (r.getId().equals(id)) {
                        JOptionPane.showMessageDialog(this, "Room ID must be unique.", "Duplicate ID", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
                classrooms.add(new Classroom(id, name, cap));
            } else {
                room.setRoomName(name);
                room.setCapacity(cap);
            }

            updateManagementTables();
            runPreGenAnalysis();
            updateFilterSelectionOptions();
        }
    }

    private JPanel createSlotMgmtPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        slotMgmtModel = new DefaultTableModel(new Object[]{"ID", "Day", "Start Time", "End Time"}, 0);
        JTable table = createReadOnlyTable(slotMgmtModel);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addBtn = new JButton("Add TimeSlot");
        JButton editBtn = new JButton("Edit TimeSlot");
        JButton delBtn = new JButton("Delete TimeSlot");

        addBtn.addActionListener(e -> showSlotDialog(null));
        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) showSlotDialog(timeSlots.get(row));
            else JOptionPane.showMessageDialog(this, "Select a slot to edit.", "Selection Required", JOptionPane.WARNING_MESSAGE);
        });
        delBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                timeSlots.remove(row);
                updateManagementTables();
                runPreGenAnalysis();
                updateFilterSelectionOptions();
            } else {
                JOptionPane.showMessageDialog(this, "Select a slot to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            }
        });

        btnPanel.add(addBtn);
        btnPanel.add(editBtn);
        btnPanel.add(delBtn);

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);
        return panel;
    }

    private void showSlotDialog(TimeSlot slot) {
        JTextField idField = new JTextField(slot != null ? slot.getId() : "");
        JComboBox<String> dayCombo = new JComboBox<>(new String[]{"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"});
        JTextField startField = new JTextField(slot != null ? slot.getStartTime() : "08:30");
        JTextField endField = new JTextField(slot != null ? slot.getEndTime() : "10:30");

        if (slot != null) {
            idField.setEnabled(false);
            dayCombo.setSelectedItem(slot.getDay());
        }

        JPanel dialogPanel = new JPanel(new GridBagLayout());
        dialogPanel.setPreferredSize(new Dimension(380, 180));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
        dialogPanel.add(new JLabel("TimeSlot ID:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        dialogPanel.add(idField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        dialogPanel.add(new JLabel("Day:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        dialogPanel.add(dayCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0;
        dialogPanel.add(new JLabel("Start Time:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        dialogPanel.add(startField, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.0;
        dialogPanel.add(new JLabel("End Time:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        dialogPanel.add(endField, gbc);

        int result = JOptionPane.showConfirmDialog(this, dialogPanel, 
                slot != null ? "Edit TimeSlot" : "Add TimeSlot", 
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String id = idField.getText().trim();
            String day = dayCombo.getSelectedItem().toString();
            String start = startField.getText().trim();
            String end = endField.getText().trim();

            if (id.isEmpty() || start.isEmpty() || end.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (slot == null) {
                for (TimeSlot s : timeSlots) {
                    if (s.getId().equals(id)) {
                        JOptionPane.showMessageDialog(this, "TimeSlot ID must be unique.", "Duplicate ID", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
                timeSlots.add(new TimeSlot(id, day, start, end));
            } else {
                slot.setDay(day);
                slot.setStartTime(start);
                slot.setEndTime(end);
            }

            updateManagementTables();
            runPreGenAnalysis();
            updateFilterSelectionOptions();
        }
    }
}
