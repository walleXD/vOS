package me.walee.os_virtualization;

import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.Set;
import java.util.List;

public class InputHandler {
    private Boolean initiated;
    private OS virtualOS;
    private Scanner reader = new Scanner(System.in);

    public InputHandler() {
        initiated = true;

        // get ram size
        System.out.print("RAM Size (in bytes): ");
        Integer ramSize = Integer.parseInt(reader.nextLine());

        // get disk count
        System.out.print("Number of disks: ");
        Integer diskCount = Integer.parseInt(reader.nextLine());

        virtualOS = new OS(ramSize, diskCount);
    }

    public void start() {
        while(initiated) getInput();
    }

    private void getInput() {
        String input;
        System.out.print("> ");

        input = reader.nextLine();

        handleCommand(input.split(" "));

    }

    private void handleCommand(String[] input) {
        switch (input[0]) {
            case "help": helpCommand(); break;
            case "exit": exitCommand(); break;
            case "A": createProcessCommand(input); break;
            case "t": terminateCurrentProcessCommand(input); break;
            case "d": requestDiskReadCommand(input); break;
            case "D": completeDiskActivityCommand(input); break;
            case "Sr": displayRunningProcessCommand(); break;
            case "Si": displayProcessDiskUsageCommand(); break;
            case "Sm": displayMemoryCommand(); break;
            default: unknownCommand(); break;
        }
    }

    private void helpCommand() {
        System.out.println("A: Spawn new process - A Priority RAM");
        System.out.println("t: Terminate currently running process");
        System.out.println("d: Currently running process added to I/O queue from given disk to load given file - d number file_name");
        System.out.println("D: The hard disk #number has finished the work for one process - D number");
        System.out.println("Sr: Shows a process currently using the CPU and processes waiting in the ready-queue");
        System.out.println("Si: Shows what processes are currently using the hard disks and what processes are waiting to use them");
        System.out.println("Sm: Shows the state of memory");
        System.out.println("help: Lists all available commands");
        System.out.println("exit: Exits Virtualization");
    }

    private void exitCommand() {
        reader.close();
        initiated = false;
    }

    private void unknownCommand() {
        System.out.println("Unknown Command");
        System.out.println("Please run help to see all available commands");
    }

    private void createProcessCommand(String[] input) {
        Integer priority = Integer.parseInt(input[1]);
        Integer memSize = Integer.parseInt(input[2]);

        // TODO: add try/catch block for failures
        Process newProcess = virtualOS.createProcess(priority, memSize);
        System.out.println(
                "Spawned new Process with PID: " + newProcess.getPcb().getPid()
                        + " RAM: " + newProcess.getPcb().getRam()
                        + " Priority " + newProcess.getPcb().getPriority());
    }

    private void terminateCurrentProcessCommand(String[] input) {}

    private void requestDiskReadCommand(String[] input) {}

    private void completeDiskActivityCommand(String[] input) {
        Integer diskId = Integer.parseInt(input[1]);

        // ToDo: Move process migration from i/o to ready queue to OS
        int pid = virtualOS.getDisk(diskId).completeRunningProcess();
        virtualOS.addProcessToReadyQueue(pid);
    }

    private void displayRunningProcessCommand() {
        Set<PCB> allProcessPCB = virtualOS
                .getProcessTable()
                .entrySet()
                .stream()
                .map(e -> e.getValue())
                .filter(e -> e.getState() == "RUNNING" || e.getState() == "READY")
                .collect(Collectors.toSet());

        System.out.println("  PID  |  PRIORITY  ");
        System.out.println("-------|------------");

        for(PCB pcb : allProcessPCB) {
            System.out.print(pcb.getState() == "RUNNING" ? "> " : "  ");
            System.out.println(pcb.getPid() + "    |  " + pcb.getPriority());
        }
    }

    private void displayProcessDiskUsageCommand() {}

    private void displayMemoryCommand() {
        List<Memory.MemoryBlock> ram = virtualOS.getRam().getRawRam();

        int prevBlockAddress = 0;
        int blockIndex = 0;
        for(Memory.MemoryBlock block : ram) {
            Integer processPid = block.getContent();
            if (processPid != null) {
                System.out.println(
                        blockIndex + "  |  "
                                + processPid + "  |  "
                                + prevBlockAddress + " - "
                                + (prevBlockAddress + block.size())
                );
            } else {
                System.out.println(
                        blockIndex + "  |  "
                                + 0 + "  |  "
                                + prevBlockAddress + " - "
                                + (prevBlockAddress + block.size())
                );
            }

            prevBlockAddress += block.size() + 1;
            blockIndex += 1;
        }
    }
}
