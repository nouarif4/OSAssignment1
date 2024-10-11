import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import javax.swing.*;  // Keep this for the GUI components
import java.awt.*;
import java.awt.event.*;
public class Main {
    public static void main(String[] args) {
        // Create and display the GUI
        SwingUtilities.invokeLater(() -> new SchedulerGUI());
    }
}

// GUI for the Round Robin Scheduler
class SchedulerGUI extends JFrame {
    private JTextField timeQuantumField;
    private JTextField processCountField;
    private JTextArea processDetailsArea;
    private JButton runButton;
    private JTextArea resultArea;

    private Scheduler scheduler;

    public SchedulerGUI() {
        // Setup the JFrame
        setTitle("Round Robin Scheduler");
        setSize(500, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create GUI components
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        JLabel timeQuantumLabel = new JLabel("Time Quantum:");
        timeQuantumField = new JTextField(10);

        JLabel processCountLabel = new JLabel("Number of Processes:");
        processCountField = new JTextField(10);

        JLabel processDetailsLabel = new JLabel("Enter Processes (Arrival Time and Burst Time separated by space):");
        processDetailsArea = new JTextArea(5, 30);
        processDetailsArea.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        runButton = new JButton("Run Simulation");
        runButton.addActionListener(new RunButtonListener());

        resultArea = new JTextArea(10, 30);
        resultArea.setEditable(false);
        resultArea.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        // Add components to panel
        panel.add(timeQuantumLabel);
        panel.add(timeQuantumField);
        panel.add(processCountLabel);
        panel.add(processCountField);
        panel.add(processDetailsLabel);
        panel.add(new JScrollPane(processDetailsArea));
        panel.add(runButton);
        panel.add(new JScrollPane(resultArea));

        add(panel);
        setVisible(true);
    }

    // ActionListener for the Run button
    private class RunButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            try {
                // Get inputs
                int timeQuantum = Integer.parseInt(timeQuantumField.getText());
                int numProcesses = Integer.parseInt(processCountField.getText());

                scheduler = new Scheduler(timeQuantum);

                String[] processDetails = processDetailsArea.getText().split("\n");
                for (int i = 0; i < numProcesses; i++) {
                    String[] parts = processDetails[i].split(" ");
                    int arrivalTime = Integer.parseInt(parts[0]);
                    int burstTime = Integer.parseInt(parts[1]);
                    scheduler.addProcess(new Process("P" + (i + 1), arrivalTime, burstTime));
                }

                // Run simulation
                scheduler.runSimulation();

                // Display the results
                StringBuilder results = new StringBuilder();
                results.append("Execution Timeline:\n");
                for (String entry : scheduler.executionTimeline) {
                    results.append(entry).append("\n");
                }

                results.append("\nGantt Chart:\n");
                results.append(scheduler.getGanttChart());

                results.append("\n\nPerformance Metrics:\n");
                results.append(scheduler.getMetrics());

                resultArea.setText(results.toString());

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Invalid input! Please try again.");
            }
        }
    }
}

// Process class remains unchanged
class Process {
    String id;
    int arrivalTime;
    int burstTime;
    int remainingTime;
    int completionTime;
    int turnaroundTime;
    int waitingTime;

    public Process(String id, int arrivalTime, int burstTime) {
        this.id = id;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.remainingTime = burstTime;
    }

    public void calculateTurnaroundTime() {
        this.turnaroundTime = this.completionTime - this.arrivalTime;
    }

    public void calculateWaitingTime() {
        this.waitingTime = this.turnaroundTime - this.burstTime;
    }
}

// Scheduler class remains mostly unchanged but adds methods to get Gantt chart and metrics as text
class Scheduler {
    List<Process> processList;
    int timeQuantum;
    int cpuTime;

    List<String> executionTimeline;
    List<Integer> startTimes;
    List<Integer> endTimes;
    List<String> processIDs;

    public Scheduler(int timeQuantum) {
        this.processList = new ArrayList<>();
        this.timeQuantum = timeQuantum;
        this.cpuTime = 0;
        this.executionTimeline = new ArrayList<>();
        this.startTimes = new ArrayList<>();
        this.endTimes = new ArrayList<>();
        this.processIDs = new ArrayList<>();
    }

    public void addProcess(Process process) {
        processList.add(process);
    }

    public List<Process> roundRobinScheduling() {
        Queue<Process> readyQueue = new LinkedList<>();
        List<Process> completedProcesses = new ArrayList<>();

        for (Process process : processList) {
            if (process.arrivalTime == 0) {
                readyQueue.add(process);
            }
        }

        while (!readyQueue.isEmpty()) {
            Process currentProcess = readyQueue.poll();
            int startTime = cpuTime;

            if (currentProcess.remainingTime > timeQuantum) {
                cpuTime += timeQuantum;
                currentProcess.remainingTime -= timeQuantum;
                executionTimeline.add(startTime + "-" + cpuTime + " " + currentProcess.id);
                startTimes.add(startTime);
                endTimes.add(cpuTime);
                processIDs.add(currentProcess.id);

                for (Process process : processList) {
                    if (process.arrivalTime > cpuTime - timeQuantum && process.arrivalTime <= cpuTime) {
                        readyQueue.add(process);
                    }
                }

                readyQueue.add(currentProcess);
            } else {
                cpuTime += currentProcess.remainingTime;
                currentProcess.remainingTime = 0;
                currentProcess.completionTime = cpuTime;
                currentProcess.calculateTurnaroundTime();
                currentProcess.calculateWaitingTime();
                completedProcesses.add(currentProcess);

                executionTimeline.add(startTime + "-" + cpuTime + " " + currentProcess.id);
                startTimes.add(startTime);
                endTimes.add(cpuTime);
                processIDs.add(currentProcess.id);

                for (Process process : processList) {
                    if (process.arrivalTime > cpuTime - currentProcess.burstTime && process.arrivalTime <= cpuTime) {
                        readyQueue.add(process);
                    }
                }
            }
        }
        return completedProcesses;
    }

    public String getGanttChart() {
        StringBuilder chart = new StringBuilder();
        chart.append(" ");
        for (int i = 0; i < startTimes.size(); i++) {
            chart.append("--------");
        }
        chart.append("\n|");
        for (int i = 0; i < processIDs.size(); i++) {
            chart.append(String.format("  %-4s |", processIDs.get(i)));
        }
        chart.append("\n ");
        for (int i = 0; i < startTimes.size(); i++) {
            chart.append("--------");
        }
        chart.append("\n").append(startTimes.get(0));
        for (int i = 0; i < endTimes.size(); i++) {
            chart.append(String.format("      %2d", endTimes.get(i)));
        }
        return chart.toString();
    }

    public String getMetrics() {
        int totalTurnaroundTime = 0;
        int totalWaitingTime = 0;
        int totalBurstTime = 0;

        for (Process process : processList) {
            totalBurstTime += process.burstTime;
        }

        for (Process process : processList) {
            totalTurnaroundTime += process.turnaroundTime;
            totalWaitingTime += process.waitingTime;
        }

        double avgTurnaroundTime = (double) totalTurnaroundTime / processList.size();
        double avgWaitingTime = (double) totalWaitingTime / processList.size();
        double cpuUtilization = ((double) totalBurstTime / cpuTime) * 100;

        return String.format("Average Turnaround Time: %.2f\nAverage Waiting Time: %.2f\nCPU Utilization: %.2f%%", 
                             avgTurnaroundTime, avgWaitingTime, cpuUtilization);
    }

    public void runSimulation() {
        List<Process> completedProcesses = roundRobinScheduling();
    }
}
