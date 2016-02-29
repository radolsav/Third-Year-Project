import io.orchestrate.client.Client;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;

/**
 * Created by Radoslav Ralinov on 30/12/2015. All rights reserved. Created as part of the Third Year Project
 * at University of Manchester. Third-Year-Project
 */
@SuppressWarnings("deprecation")
public class CoordinatingTask extends Task<ObservableList<Malware>> {

    private ArrayList<FileSystemTraverse> taskList = new ArrayList<>();
    private ArrayList<FileSystemTraverse> currentTasks = new ArrayList<>();
    private ObservableList<Malware> malware = FXCollections.observableArrayList();
    private ArrayList<Thread> threads = new ArrayList<>();
    private Client client;
    private int taskCount = 0;
    private int numberOfInfectedFiles = 0;
    private boolean paused = false;
    private boolean resume = false;
    private boolean stopped = false;

    public CoordinatingTask(Client client, ObservableList<Malware> data, Path pathToScan) {
        this.client = client;

        setOnCancelled(workerStateEvent -> taskList.forEach(Task::cancel));
        List<String> dirs = new ArrayList<>();
        if (pathToScan.toFile().isDirectory()) {
            dirs = new ArrayList<>(Arrays.asList(pathToScan.toFile().list((dir, name) ->
                    new File(dir, name).isDirectory())));
        }

        boolean fullScan = false;
        if (pathToScan.toString().equals("Desktop") || pathToScan.toString().equals("Computer")) {
            File[] roots = File.listRoots();
            List<String> rootList = new ArrayList<>();
            for (File root : roots) {
                rootList.add(root.toString());
            }
            dirs.addAll(rootList);
            fullScan = true;
        }

        if (dirs.size() == 0 && !fullScan)
            dirs.add("Single file case");

        int depthLevel;
        for (int i = 0; i < dirs.size(); i++) {
            if (!fullScan)
                if (i == 0) {
                    dirs.set(i, pathToScan.toString());
                    depthLevel = 1;
                } else {
                    dirs.set(i, pathToScan.toString() + System.getProperty("file.separator") + dirs.get(i));
                    depthLevel = Integer.MAX_VALUE;
                }
            else
                depthLevel = Integer.MAX_VALUE;
            taskList.add(i, new FileSystemTraverse(client, Paths.get(dirs.get(i)), data, depthLevel));
            currentTasks.add(taskList.get(i));
            threads.add(i, createThread("Thread" + i, taskList.get(i)));
            final FileSystemTraverse currentTask = taskList.get(i);
            final Thread currentThread = threads.get(i);
            taskCount = taskList.size();
            currentTask.setOnSucceeded(event -> {
                taskCount--;
                currentTasks.remove(currentTask);
                threads.remove(currentThread);
                currentTask.getValue().stream().filter(malware1 -> !malware.contains(malware1)).forEach(malware1
                        -> malware.add(malware1));
            });
        }
    }

    @Override
    protected ObservableList<Malware> call() {
        for (int i = 0; i < taskList.size(); i++) {
            threads.get(i).start();
        }
        try {
            client.ping();
        } catch (IOException e1) {
            e1.printStackTrace(System.err);
        }
        Random r = new Random();

        while (taskCount != 0) {
            numberOfInfectedFiles = 0;
            for (FileSystemTraverse aTask : taskList) {
                numberOfInfectedFiles += aTask.getInfectedFiles();
            }
            if (paused) {
                threads.forEach(Thread::suspend);
                paused = false;
            }
            if (resume) {
                threads.forEach(Thread::resume);
                resume = false;
            }
            if (stopped) {
                threads.forEach(Thread::stop);
                Thread.currentThread().stop();
            }
            int randomTask = ((taskCount == 1) ? 0 : r.nextInt(taskCount));
            if (currentTasks.size() != 0) {
                updateTitle(currentTasks.get(randomTask).getCurrentFilePath());
            }
            updateMessage("Infected files: " + numberOfInfectedFiles);
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return malware;
    }

    public void pause() {
        paused = true;
    }

    public void unPause() {
        resume = true;
    }

    public void stop() {
        stopped = true;
    }

    private Thread createThread(final String threadName, Task task) {
        ThreadFactory factory = r -> {
            Thread thread = new Thread(r);
            thread.setName(threadName);
            thread.setDaemon(true);
            return thread;
        };
        return factory.newThread(task);
    }

}
