import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter the time quantum: ");
        int timeQuantum = scanner.nextInt();
        
        Scheduler scheduler = new Scheduler(timeQuantum); 

        System.out.print("Enter the number of processes: ");
        int numProcesses = scanner.nextInt();
        
        for (int i = 1; i <= numProcesses; i++) {
            System.out.println("Enter details for Process P" + i + ":");
            System.out.print("Arrival Time: ");
            int arrivalTime = scanner.nextInt();
            System.out.print("Burst Time: ");
            int burstTime = scanner.nextInt();
            
            scheduler.addProcess(new Process("P" + i, arrivalTime, burstTime));
        }

        scheduler.runSimulation();
        scanner.close();
    }
}

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

        processList.sort(Comparator.comparingInt(p -> p.arrivalTime));

        int index = 0; 

        while (!readyQueue.isEmpty() || index < processList.size()) {
            while (index < processList.size() && processList.get(index).arrivalTime <= cpuTime) {
                readyQueue.add(processList.get(index));
                index++;
            }

            if (readyQueue.isEmpty()) {
                cpuTime++;
                continue;
            }

            Process currentProcess = readyQueue.poll();
            int startTime = cpuTime;

            if (currentProcess.remainingTime > timeQuantum) {
                cpuTime += timeQuantum;
                currentProcess.remainingTime -= timeQuantum;

                executionTimeline.add(startTime + "-" + cpuTime + " " + currentProcess.id);
                startTimes.add(startTime);
                endTimes.add(cpuTime);
                processIDs.add(currentProcess.id);

                while (index < processList.size() && processList.get(index).arrivalTime <= cpuTime) {
                    readyQueue.add(processList.get(index));
                    index++;
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
            }
        }

        return completedProcesses;
    }

    public void calculateMetrics(List<Process> completedProcesses) {
        int totalTurnaroundTime = 0;
        int totalWaitingTime = 0;


        for (Process process : completedProcesses) {
            totalTurnaroundTime += process.turnaroundTime;
            totalWaitingTime += process.waitingTime;
        }


        double avgTurnaroundTime = (double) totalTurnaroundTime / completedProcesses.size();
        double avgWaitingTime = (double) totalWaitingTime / completedProcesses.size();

   
        int totalBurstTime = processList.stream().mapToInt(p -> p.burstTime).sum();
        double cpuUtilization = ((double) totalBurstTime / cpuTime) * 100;


        System.out.println("Average Turnaround Time: " + avgTurnaroundTime);
        System.out.println("Average Waiting Time: " + avgWaitingTime);
        System.out.println("CPU Utilization: " + cpuUtilization + "%");
    }

    public void displayGanttChart() {
        System.out.println("\nGantt Chart:");
        System.out.print(" ");
        for (int i = 0; i < startTimes.size(); i++) {
            System.out.print("--------");
        }
        System.out.println();

        System.out.print("|");
        for (int i = 0; i < processIDs.size(); i++) {
            System.out.printf("  %-4s |", processIDs.get(i));
        }
        System.out.println();

        System.out.print(" ");
        for (int i = 0; i < startTimes.size(); i++) {
            System.out.print("--------");
        }
        System.out.println();

        System.out.print(startTimes.get(0));
        for (int i = 0; i < endTimes.size(); i++) {
            System.out.printf("      %2d", endTimes.get(i));
        }
        System.out.println();
    }

    public void displayExecutionTimeline() {
        System.out.println("\nExecution Timeline:");
        for (String entry : executionTimeline) {
            System.out.println(entry);
        }
    }

    public void runSimulation() {
        List<Process> completedProcesses = roundRobinScheduling();
        displayExecutionTimeline();
        displayGanttChart();
        calculateMetrics(completedProcesses);
    }
}