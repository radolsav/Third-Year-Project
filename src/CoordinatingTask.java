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
    private boolean unpause = false;
    private boolean stopped = false;

    public CoordinatingTask(Client client, ObservableList<Malware> data, Path pathToScan) {
        this.client = client;

        setOnCancelled(workerStateEvent -> taskList.forEach(Task::cancel));
        List<String> dirs = new ArrayList<>(Arrays.asList(pathToScan.toFile().list((dir, name) ->
                new File(dir, name).isDirectory())));

        int depthLevel;
        for (int i = 0; i < dirs.size(); i++) {
            if (i == 0) {
                dirs.set(i, pathToScan.toString());
                depthLevel = 1;
            } else {
                dirs.set(i, pathToScan.toString() + System.getProperty("file.separator") + dirs.get(i));
                depthLevel = Integer.MAX_VALUE;
            }
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
            if (unpause) {
                threads.forEach(Thread::resume);
                unpause = false;
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
/*
        for (int i = 0; i < taskList.size(); i++) {
            executors.get(i).submit(taskList.get(i));
        }

        for (int i = 0; i < taskList.size(); i++) {
            executors.get(i).shutdown();
        }

        try {
            for (int i = 0; i < taskList.size(); i++) {
                executors.get(i).awaitTermination(1, TimeUnit.DAYS);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < taskList.size(); i++) {
            if (executors.get(i).isShutdown() && taskList.get(i).isDone()) {
                malware.addAll(taskList.get(i).getValue());
            }
        }*/

        return malware;
    }

    public void pause() {
        paused = true;
    }

    public void unPause() {
        unpause = true;
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
