import io.orchestrate.client.Client;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;

/**
 * Created by Radoslav Ralinov on 30/12/2015. All rights reserved. Created as part of the Third Year Project
 * at University of Manchester. Third-Year-Project
 */
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

        setOnCancelled(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent workerStateEvent) {
                for (FileSystemTraverse task : taskList) {
                    task.cancel();
                }
            }
        });
        List<String> dirs = new ArrayList<>(Arrays.asList(pathToScan.toFile().list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return new File(dir, name).isDirectory();
            }
        })));

        int depthLevel;
        for (int i = 0; i < dirs.size(); i++) {
            if (i == 0) {
                dirs.set(i, pathToScan.toString());
                depthLevel = 1;
            } else {
                dirs.set(i, pathToScan.toString() + dirs.get(i));
                depthLevel = Integer.MAX_VALUE;
            }
            taskList.add(i, new FileSystemTraverse(client, Paths.get(dirs.get(i)), data, depthLevel));
            currentTasks.add(taskList.get(i));
            threads.add(i, createThread("Thread" + i, taskList.get(i)));
            final FileSystemTraverse currentTask = taskList.get(i);
            final Thread currentThread = threads.get(i);
            taskCount = taskList.size();
            currentTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                @Override
                public void handle(WorkerStateEvent event) {
                    taskCount--;
                    currentTasks.remove(currentTask);
                    threads.remove(currentThread);
                    for (Malware malware1 : currentTask.getValue()) {
                        if (!malware.contains(malware1)) {
                            malware.add(malware1);
                        }
                    }
                }
            });
        }
    }

    @Override
    protected ObservableList<Malware> call() throws Exception {
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
                for (Thread thread : threads) {
                    thread.suspend();
                }
                paused = false;
            }
            if (unpause) {
                for (Thread thread : threads) {
                    thread.resume();
                }
                unpause = false;
            }
            if (stopped) {
                for (Thread thread : threads) {
                    thread.stop();
                }
                Thread.currentThread().stop();
            }
            int randomTask = ((taskCount == 1) ? 0 : r.nextInt(taskCount));
            if (currentTasks.size() != 0) {
                updateTitle(currentTasks.get(randomTask).getCurrentFilePath());
            }
            updateMessage("Infected files: " + numberOfInfectedFiles);
            Thread.sleep(1000);
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
        ThreadFactory factory = new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName(threadName);
                thread.setDaemon(true);
                return thread;
            }
        };
        return factory.newThread(task);
    }

}
