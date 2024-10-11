# OSAssignment1
**Process Scheduling Simulator using the Round Robin scheduling algorithm.**  
This project simulates process scheduling using the Round Robin algorithm by visualizing the process execution timeline (like a Gantt chart) and calculating metrics such as CPU utilization, average turnaround time, and average waiting time.  
*(Note: We are ignoring preemption and context-switching between processes.)*

## Data Structures

### **ArrayList**:
- `processList`: Stores **all processes** that are involved in the scheduling.
- `completedProcesses`: Stores **processes that have finished execution**.

### **Queue**:
- `readyQueue`: A **queue of processes** that are ready to enter the CPU for execution (based on their arrival times and the CPU's availability).

