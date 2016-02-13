import io.orchestrate.client.Client;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;

/**
 * Created by Radoslav Ralinov on 30/12/2015. All rights reserved. Created as part of the Third Year Project
 * at University of Manchester. Third-Year-Project
 */
public class CoordinatingTask extends Task<ObservableList<Malware>> {
    private Client client;
    private ObservableList<Malware> data;
    private ArrayList<FileSystemTraverse> taskList = new ArrayList<>();
    private ObservableList<Malware> malware = FXCollections.observableArrayList();

    public CoordinatingTask(Client client, ObservableList<Malware> data) {
        this.client = client;
        this.data = data;
        setOnCancelled(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent workerStateEvent) {
                for (FileSystemTraverse task : taskList) {
                    task.cancel();
                }
            }
        });
    }


    @Override
    protected ObservableList<Malware> call() throws Exception {
        Path pathToScan = Paths.get("D:\\");

        List<String> dirs;
        dirs = new ArrayList<>(Arrays.asList(pathToScan.toFile().list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return new File(dir, name).isDirectory();
            }
        })));

        ArrayList<ExecutorService> executors = new ArrayList<>();
        for (int i = 0; i < dirs.size(); i++) {
            dirs.set(i, "D:\\" + dirs.get(i));
            executors.add(i, createExecutor("Executor" + i));
            taskList.add(i, new FileSystemTraverse(client, Paths.get(dirs.get(i)), data));
            executors.get(i).submit(taskList.get(i));
        }


        for (int i = 0; i < dirs.size(); i++) {
            executors.get(i).shutdown();
        }

        try {
            for (int i = 0; i < dirs.size(); i++) {
                executors.get(i).awaitTermination(1, TimeUnit.DAYS);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for(int i=0;i<dirs.size();i++)
        {
            if (executors.get(i).isShutdown() && taskList.get(i).isDone())
            {
                malware.addAll(taskList.get(i).getValue());
            }
        }

        return malware;
    }

    private ExecutorService createExecutor(final String executorName) {
        ThreadFactory factory = new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName(executorName);
                thread.setDaemon(true);
                return thread;
            }
        };
        return Executors.newSingleThreadExecutor(factory);
    }

}
