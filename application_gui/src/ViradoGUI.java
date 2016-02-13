import eu.medsea.mimeutil.MimeUtil;
import io.orchestrate.client.Client;
import io.orchestrate.client.OrchestrateClient;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by Radoslav Ralinov on 30/12/2015. All rights reserved. Created as part of the Third Year Project
 * at University of Manchester. Third-Year-Project
 */
public class ViradoGUI extends Application {
    private static final String FOUND_MALWARE = "Found malware:";
    private static final String SCAN_SUCCESSFUL = "Scan completed successfully!";
    private static final String COMPLETED_MSG = "Completed";
    private static final String FILE_NAME_COLUMN = "File name";
    private static final String SIZE_COLUMN = "Size";
    private static final String PATH_COLUMN = "Path";


    private TableView<Malware> tableView = new TableView<>();
    private ObservableList<Malware> malwareData = FXCollections.observableArrayList();

    private static final Client client = new OrchestrateClient("e4f5cbf3-991a-41ab-aa6e-346d53c0ac2b");
    private static final String MAGIC_MIME_DETECTOR = "eu.medsea.mimeutil.detector.MagicMimeMimeDetector";
    private static final String EXTENSION_MIME_DETECTOR = "eu.medsea.mimeutil.detector.ExtensionMimeDetector";

    private static final String QUICK_SCAN = "Quick Scan";
    private static final String FULL_SCAN = "Full Scan";
    private static final String CUSTOM_SCAN = "Custom Scan";
    private static final String OVERVIEW = "Overview";
    private static final String SCAN = "Scan";
    private static final String PAUSE = "Pause";
    private static final String SCANNING = "Scanning...";
    private static final String PAUSED = "Paused";
    private static final String RESUME = "Resume";
    private static final String STOPPED = "Stopped";
    private static final String STOP = "Stop";
    private static final String APPLICATION_NAME = "Virado";
    private static final String GUI_CSS = "ViradoGUI.css";

    private static final String SCAN_IMG = "/resources/images/magnifier.png";
    private static final String STOP_BUTTON_IMG = "/resources/images/stop.png";
    private static final String PAUSE_IMG = "/resources/images/pause.png";


    public void start(final Stage primaryStage) {
        final TabPane tabbedPane = new TabPane();
        Tab overview_tab = new Tab();
        overview_tab.setText(OVERVIEW);
        overview_tab.setContent(new Rectangle(400, 400, Color.DEEPSKYBLUE));
        tabbedPane.getTabs().add(overview_tab);

        final Tab scan_tab = new Tab();
        scan_tab.setText(SCAN);
        final Button quickScanButton = new Button(QUICK_SCAN);
        Button fullScanButton = new Button(FULL_SCAN);
        Button customScanButton = new Button(CUSTOM_SCAN);
        quickScanButton.setMinSize(100, 100);
        fullScanButton.setMinSize(100, 100);
        customScanButton.setMinSize(100, 100);
        quickScanButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(final ActionEvent e) {
                MimeUtil.registerMimeDetector(MAGIC_MIME_DETECTOR);
                MimeUtil.registerMimeDetector(EXTENSION_MIME_DETECTOR);
//        MimeUtil.registerMimeDetector("eu.medsea.mimeutil.detector.OpendesktopMimeDetector");
                Path pathToScan = Paths.get("D:\\");
                final CoordinatingTask task = new CoordinatingTask(client, malwareData);

                // Create progress HBox element with progress bar and progress indicator
                final HBox progressHb = createProgressUI(task);
                final Label scanLabel = (Label) progressHb.getChildren().get(0);
//        scanLabel.textProperty().bind(task.messageProperty());

                final HBox currentFIleHBox = new HBox();
                final Label currentFileLabel = new Label();
                currentFileLabel.textProperty().bind(task.titleProperty());
                currentFIleHBox.setAlignment(Pos.CENTER);
                currentFIleHBox.getChildren().addAll(currentFileLabel);

                // Create progress buttons HBox
                HBox processButtons = createButtonsUI();
                final Button pauseButton = (Button) processButtons.getChildren().get(0);
                final Button stopButton = (Button) processButtons.getChildren().get(1);

                final HBox infectedSoFarBox = new HBox();
                final Labeled infectedSoFarLabel = new Label();
                infectedSoFarLabel.textProperty().bind(task.messageProperty());
                task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
                    @Override
                    public void handle(WorkerStateEvent workerStateEvent) {
                        updateSucceededUI(infectedSoFarLabel, progressHb, currentFileLabel, scanLabel, task);
                    }
                });
                infectedSoFarBox.getChildren().addAll(infectedSoFarLabel);
                infectedSoFarBox.setAlignment(Pos.CENTER);

                VBox tableVbox = new VBox();
                final Label malwareLabel = new Label(FOUND_MALWARE);
                tableView.setEditable(false);
                tableVbox.setSpacing(5);
                tableVbox.setPadding(new Insets(10, 10, 10, 10));
                TableColumn<Malware, String> fileNameColumn = new TableColumn<>(FILE_NAME_COLUMN);
                fileNameColumn.setCellValueFactory(
                        new PropertyValueFactory<Malware, String>("fileName"));
                TableColumn<Malware, String> fileSizeColumn = new TableColumn<>(SIZE_COLUMN);
                fileSizeColumn.setCellValueFactory(
                        new PropertyValueFactory<Malware, String>("size"));
                TableColumn<Malware, Path> filePathColumn = new TableColumn<>(PATH_COLUMN);
                filePathColumn.setCellValueFactory(
                        new PropertyValueFactory<Malware, Path>("path"));
                // bind columns to table so that when you resize table columns resize too.
                fileNameColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(0.3));
                filePathColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(0.4));
                fileSizeColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(0.3));
                tableView.setItems(malwareData);
                tableView.getColumns().addAll(fileNameColumn, fileSizeColumn, filePathColumn);
                tableVbox.getChildren().addAll(malwareLabel, tableView);
                tableVbox.setAlignment(Pos.BOTTOM_CENTER);

                // Add HBoxes together to VBox and to the tab
                final VBox vb = new VBox();
                vb.setSpacing(5);
                vb.setAlignment(Pos.CENTER);
                vb.getChildren().addAll(processButtons, currentFIleHBox, progressHb, infectedSoFarBox, tableVbox);
                scan_tab.setContent(vb);

                // Start the thread
                final Thread thread = new Thread(task);
                thread.setName("WalkFileTree");
                thread.setDaemon(true);
                try {
                    client.ping();
                } catch (IOException e1) {
                    e1.printStackTrace(System.err);
                }
                thread.start();

                // Interrupt thread if clicked.
                stopButton.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent actionEvent) {
                        thread.stop();
                        scanLabel.setText(STOPPED);
                    }
                });
                pauseButton.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent actionEvent) {
                        pauseAction(thread, pauseButton, scanLabel);
                    }
                });
            }
        });
        customScanButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
            }
        });
        VBox vbox = new VBox();

        vbox.setSpacing(25);
        vbox.setAlignment(Pos.CENTER);
        vbox.setStyle("-fx-background-color: inherit ");
        vbox.getChildren().addAll(quickScanButton, fullScanButton, customScanButton);
        scan_tab.setContent(vbox);
        tabbedPane.getTabs().add(scan_tab);

        Tab statistics_tab = new Tab();
        statistics_tab.setText("Statistics");
        statistics_tab.setContent(new Rectangle(400, 400, Color.DEEPSKYBLUE));
        tabbedPane.getTabs().add(statistics_tab);

        tabbedPane.rotateGraphicProperty();
        tabbedPane.setSide(Side.LEFT);
        tabbedPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabbedPane.rotateGraphicProperty();
        StackPane root = new StackPane();
        root.getChildren().add(tabbedPane);

        Scene scene = new Scene(root, 400, 400);
        scene.getStylesheets().add(GUI_CSS);
        primaryStage.setTitle(APPLICATION_NAME);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Close threads when application shuts down.
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent e) {
                Platform.exit();
                System.exit(0);
            }
        });
    }

    private void updateSucceededUI(Labeled infectedSoFarLabel, HBox progressHb, Label currentFileLabel, Label scanLabel, CoordinatingTask task) {
        infectedSoFarLabel.textProperty().unbind();
        infectedSoFarLabel.setText(SCAN_SUCCESSFUL);
        ((ProgressBar) (progressHb.getChildren().get(1))).progressProperty().unbind();
        ((ProgressBar) (progressHb.getChildren().get(1))).setProgress(1);
        ((ProgressIndicator) (progressHb.getChildren().get(2))).progressProperty().unbind();
        ((ProgressIndicator) (progressHb.getChildren().get(2))).setProgress(1);
        currentFileLabel.textProperty().unbind();
        currentFileLabel.setText("");
        scanLabel.setText(COMPLETED_MSG);
        malwareData = task.getValue();
    }

    private HBox createButtonsUI() {
        // Create stop and pause buttons
        Image imageStop = new Image(getClass().getResourceAsStream(STOP_BUTTON_IMG));
        final Button stopButton = new Button(STOP, new ImageView(imageStop));
        stopButton.setStyle("-fx-background-radius: 80");
        stopButton.setMinSize(25, 25);

        Image imagePause = new Image(getClass().getResourceAsStream(PAUSE_IMG));
        final Button pauseButton = new Button(PAUSE, new ImageView(imagePause));
        pauseButton.setStyle("-fx-background-radius: 80");
        pauseButton.setMinSize(25, 25);

        final HBox processButtons = new HBox();
        processButtons.setSpacing(5);
        processButtons.setPadding(new Insets(10, 0, 0, 0));
        processButtons.setAlignment(Pos.CENTER);
        processButtons.getChildren().addAll(pauseButton, stopButton);
        return processButtons;
    }

    private HBox createProgressUI(Task task) {
        // Create scanning label, progress bar and progress indicator.
        Image magnifierImage = new Image(getClass().getResourceAsStream(SCAN_IMG));
        final Label scanLabel = new Label(SCANNING, new ImageView(magnifierImage));

        final ProgressBar pb = new ProgressBar();
        pb.progressProperty().bind(task.progressProperty());

        final ProgressIndicator pin = new ProgressIndicator();
        pin.progressProperty().bind(task.progressProperty());
        final HBox progresshb = new HBox();
        progresshb.setSpacing(5);
        progresshb.setAlignment(Pos.CENTER);
        progresshb.getChildren().addAll(scanLabel, pb, pin);
        return progresshb;
    }

    private void pauseAction(Thread thread, Button pauseButton, Label scanLabel) {
        if (!thread.isInterrupted() && pauseButton.getText().equals(PAUSE)) {
            thread.suspend();
            scanLabel.setText(PAUSED);
            pauseButton.setText(RESUME);
        } else {
            thread.resume();
            scanLabel.setText(SCANNING);
            pauseButton.setText(PAUSE);
        }
    }


    public static void main(String[] args) {
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }
}
