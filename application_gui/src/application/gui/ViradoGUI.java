package application.gui;

import eu.medsea.mimeutil.MimeUtil;
import io.orchestrate.client.Client;
import io.orchestrate.client.OrchestrateClient;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;

import malware.scan.*;
import images.*;


/**
 * Created by Radoslav Ralinov on 30/12/2015. All rights reserved. Created as part of the Third Year Project
 * at University of Manchester. Third-Year-Project
 */
public class ViradoGUI extends Application {
    private static final String FILE_NAME_COLUMN = "File name";
    private static final String SIZE_COLUMN = "Size";
    private static final String PATH_COLUMN = "Path";
    private static final int APPLICATION_MAX_HEIGHT = 550;
    private static final int APPLICATION_MAX_WIDTH = 550;

    private TableView<Malware> tableView = new TableView<>();
    private ObservableList<Malware> malwareData = FXCollections.observableArrayList();

    private static final Client client = OrchestrateClient.builder("e4f5cbf3-991a-41ab-aa6e-346d53c0ac2b")
        .host("https://api.aws-eu-west-1.orchestrate.io").build();
    private static final String MAGIC_MIME_DETECTOR = "eu.medsea.mimeutil.detector.MagicMimeMimeDetector";
    private static final String EXTENSION_MIME_DETECTOR = "eu.medsea.mimeutil.detector.ExtensionMimeDetector";

    private static final String OVERVIEW = "Overview";

    private static final String PAUSE = "Pause";
    private static final String SCANNING = "Scanning...";
    private static final String STOP = "Stop";
    private static final String APPLICATION_NAME = "Virado";
    private static final String GUI_CSS = "application/gui/ViradoGUI.css";
    private static final ApplicationImages applicationImages = new ApplicationImages();


    private volatile Thread thread;
    private final Tab scan_tab = new Tab();

    protected Button quickScanButton = new Button("Quick Scan");
    protected Button customScanButton = new Button("Custom Scan");
    protected Button fullScanButton = new Button("Full Scan");

    ArrayList<Malware> encryptedMalware = new ArrayList<>();

    public void start(final Stage primaryStage) {
        final TabPane tabbedPane = new TabPane();
        Tab overview_tab = new Tab();
        overview_tab.setText(OVERVIEW);
//        Rectangle rectangle = new Rectangle(400, 400, Color.DEEPSKYBLUE);
        TextArea textArea = new TextArea("");
        textArea.setEditable(false);
//        textArea.setPrefSize( Double.MAX_VALUE, Double.MAX_VALUE );
        textArea.prefWidthProperty().bind(tabbedPane.tabMaxWidthProperty());
        textArea.prefHeightProperty().bind(tabbedPane.tabMaxHeightProperty());
        textArea.setFont(new Font(12));
//        textArea.setBackground();
        StackPane stackPane = new StackPane();
        stackPane.getChildren().addAll(textArea);
        overview_tab.setContent(stackPane);
        tabbedPane.getTabs().add(overview_tab);

        scan_tab.setText("Scan");

        initialScanTabState();
        tabbedPane.getTabs().add(scan_tab);

        Tab statistics_tab = new Tab();
        statistics_tab.setText("Statistics");

        statistics_tab.setContent(new Rectangle(APPLICATION_MAX_WIDTH, APPLICATION_MAX_HEIGHT, Color.DEEPSKYBLUE));
        tabbedPane.getTabs().add(statistics_tab);

        initializeTabbedPane(tabbedPane);

        StackPane root = new StackPane();
        root.getChildren().add(tabbedPane);

        Scene scene = new Scene(root, APPLICATION_MAX_WIDTH, APPLICATION_MAX_HEIGHT);
        scene.getStylesheets().add(GUI_CSS);
        primaryStage.setTitle(APPLICATION_NAME);
        primaryStage.setMaxWidth(APPLICATION_MAX_WIDTH);
        primaryStage.setMaxHeight(APPLICATION_MAX_HEIGHT);
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Close threads when application shuts down.
        primaryStage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });
    }

    private void initializeScanButtons() {
        quickScanButton.setMinSize(100, 100);
        fullScanButton.setMinSize(100, 100);
        customScanButton.setMinSize(100, 100);
        quickScanButton.setTooltip(new Tooltip("Scan vulnerable directories"));
        fullScanButton.setTooltip(new Tooltip("Do a full scan"));
        customScanButton.setTooltip(new Tooltip("Choose folder/disk/file to scan"));

        CustomScanDialog dialog = new CustomScanDialog();
        quickScanButton.setOnAction(e -> {
            String windir = System.getenv("WINDIR");
            String programFiles = System.getenv("ProgramFiles");

            Path[] pathToScan = new Path[2];
            pathToScan[0] = Paths.get(windir);
            pathToScan[1] = Paths.get(programFiles);

            scanProcessTabUI(pathToScan);
        });

        customScanButton.setOnAction(actionEvent -> {
            dialog.start(new Stage(), this);
            customScanButton.setDisable(true);
            quickScanButton.setDisable(true);
            fullScanButton.setDisable(true);
        });

        fullScanButton.setOnAction(event -> {
            Path[] paths = new Path[1];
            paths[0] = Paths.get("Computer");
            scanProcessTabUI(paths);
        });

    }

    protected void scanProcessTabUI(Path[] pathsToScan) {
        MimeUtil.registerMimeDetector(MAGIC_MIME_DETECTOR);
        MimeUtil.registerMimeDetector(EXTENSION_MIME_DETECTOR);
//        MimeUtil.registerMimeDetector("eu.medsea.mimeutil.detector.OpendesktopMimeDetector");
        final CoordinatingTask task = new CoordinatingTask(client, malwareData, pathsToScan);

        // Create progress HBox element with progress bar and progress indicator
        final HBox progressHb = createProgressUI(task);
        final Label scanLabel = (Label) progressHb.getChildren().get(0);
//        scanLabel.textProperty().bind(task.essageProperty());

        final HBox currentFIleHBox = new HBox();
        final Label currentFileLabel = new Label();
        currentFileLabel.textProperty().bind(task.titleProperty());
        currentFIleHBox.setAlignment(Pos.CENTER);
        currentFIleHBox.getChildren().addAll(currentFileLabel);

        // Create progress buttons HBox
        HBox processButtons = createProcessButtonsUI();
        final Button pauseButton = (Button) ((HBox) (processButtons.getChildren().get(1))).getChildren().get(0);
        final Button stopButton = (Button) ((HBox) (processButtons.getChildren().get(1))).getChildren().get(1);

        final HBox infectedSoFarBox = new HBox();
        final Labeled infectedSoFarLabel = new Label();
        infectedSoFarLabel.textProperty().bind(task.messageProperty());
        task.setOnSucceeded(workerStateEvent -> updateSucceededUI(infectedSoFarLabel, progressHb, currentFileLabel, scanLabel, task));
        infectedSoFarBox.getChildren().addAll(infectedSoFarLabel);
        infectedSoFarBox.setAlignment(Pos.CENTER);

        VBox tableVbox = new VBox();
        createMalwareTable(tableVbox);

        // Add HBoxes together to VBox and to the tab
        final VBox vb = new VBox();
        vb.setSpacing(5);
        vb.setAlignment(Pos.CENTER);
        vb.getChildren().addAll(processButtons, currentFIleHBox, progressHb, infectedSoFarBox, tableVbox);
        scan_tab.setContent(vb);

        // Start the thread
        thread = new Thread(task);
        thread.setName("WalkFileTree");
        thread.setDaemon(true);
        thread.start();

        // Interrupt thread if clicked.
        stopButton.setOnAction(actionEvent -> {
            if (!task.isDone()) {
                task.stop();
                malwareData.clear();
//                thread.interrupt();
                scanLabel.setText("Stopped");
            }
        });
        pauseButton.setOnAction(actionEvent -> pauseAction(thread, pauseButton, scanLabel, task));
    }


    private void initializeTabbedPane(TabPane tabbedPane) {
        tabbedPane.rotateGraphicProperty();
        tabbedPane.setSide(Side.LEFT);
        tabbedPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabbedPane.rotateGraphicProperty();
        tabbedPane.setMaxSize(APPLICATION_MAX_WIDTH, APPLICATION_MAX_HEIGHT);
    }

    private void createMalwareTable(VBox tableVbox) {
        final Label malwareLabel = new Label("Found malware:");
        tableView.setEditable(false);
        tableVbox.setSpacing(5);
        tableVbox.setPadding(new Insets(10, 10, 10, 10));
        TableColumn<Malware, String> fileNameColumn = new TableColumn<>(FILE_NAME_COLUMN);
        fileNameColumn.setCellValueFactory(
            new PropertyValueFactory<>("fileName"));
        TableColumn<Malware, String> fileSizeColumn = new TableColumn<>(SIZE_COLUMN);
        fileSizeColumn.setCellValueFactory(
            new PropertyValueFactory<>("size"));
        TableColumn<Malware, Path> filePathColumn = new TableColumn<>(PATH_COLUMN);
        filePathColumn.setCellValueFactory(
            new PropertyValueFactory<>("path"));
        TableColumn<Malware, Path> quarantineColumn = new TableColumn<>("Quarantine");
        quarantineColumn.setCellValueFactory(
            new PropertyValueFactory<>("quarantine"));
        // bind columns to table so that when you resize table columns resize too.
        fileNameColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(0.25));
        filePathColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(0.4));
        fileSizeColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(0.15));
        quarantineColumn.prefWidthProperty().bind(tableView.widthProperty().multiply(0.2));

        MenuItem deleteMenuItem = new MenuItem("Delete");
        deleteMenuItem.setOnAction(event -> {
            try {
                Malware selectedMalware = tableView.getSelectionModel().getSelectedItem();
                if (selectedMalware != null) {
                    malwareData.remove(selectedMalware);
                    Files.delete(selectedMalware.getPath());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        MenuItem quarantineMenuItem = new MenuItem("Quarantine/Remove from quarantine");

        ContextMenu contextMenu = new ContextMenu();
        contextMenu.setAutoFix(true);
        contextMenu.setHideOnEscape(true);
        contextMenu.setPrefSize(40, 40);
        contextMenu.getItems().addAll(deleteMenuItem, quarantineMenuItem);
        quarantineAction(quarantineMenuItem, contextMenu);
        tableView.setContextMenu(contextMenu);
        tableView.setItems(malwareData);
        tableView.getColumns().add(fileNameColumn);
        tableView.getColumns().add(fileSizeColumn);
        tableView.getColumns().add(filePathColumn);
        tableView.getColumns().add(quarantineColumn);
        tableVbox.getChildren().addAll(malwareLabel, tableView);
        tableVbox.setAlignment(Pos.BOTTOM_CENTER);
    }

    private void quarantineAction(MenuItem quarantineMenuItem, ContextMenu contextMenu) {
        quarantineMenuItem.setOnAction(event ->
            {
                Malware selectedMalware = tableView.getSelectionModel().getSelectedItem();
                try {
                    if (selectedMalware != null) {
                        boolean decrypted = false;
                        for (Malware malware : encryptedMalware) {
                            if (selectedMalware.equals(malware)) {
                                decrypted = true;
                                Quarantine.decryptAES(selectedMalware.getPath());
                                selectedMalware.setQuarantine(false);
                                tableView.refresh();
                                encryptedMalware.remove(selectedMalware);
                                contextMenu.hide();
                            }
                        }
                        if (!decrypted) {
                            Quarantine.encryptAES(selectedMalware.getPath());
                            selectedMalware.setQuarantine(true);
                            tableView.refresh();
                            encryptedMalware.add(selectedMalware);
                            contextMenu.hide();
                        }
                    }
                }
                catch (ConcurrentModificationException e)
                {
                    e.printStackTrace();
                }

            }
        );
    }

    private void updateSucceededUI(Labeled infectedSoFarLabel, HBox progressHb, Label currentFileLabel, Label scanLabel, CoordinatingTask task) {
        infectedSoFarLabel.textProperty().unbind();
        infectedSoFarLabel.setText("Scan completed successfully!");
        ((ProgressBar) (progressHb.getChildren().get(1))).progressProperty().unbind();
        ((ProgressBar) (progressHb.getChildren().get(1))).setProgress(1);
        ((ProgressIndicator) (progressHb.getChildren().get(2))).progressProperty().unbind();
        ((ProgressIndicator) (progressHb.getChildren().get(2))).setProgress(1);
        currentFileLabel.textProperty().unbind();
        currentFileLabel.setText("");
        scanLabel.setText("Completed");
        malwareData = task.getValue();
    }

    private HBox createProcessButtonsUI() {
        final HBox processButtons = new HBox();

        // Create stop and pause buttons
        final Button stopButton = new Button(STOP, new ImageView(applicationImages.getSTOP_BUTTON_IMG()));
        stopButton.setStyle("-fx-background-radius: 80");
        stopButton.setMinSize(25, 25);

        final Button pauseButton = new Button(PAUSE, new ImageView(applicationImages.getPAUSE_IMG()));
        pauseButton.setStyle("-fx-background-radius: 80");
        pauseButton.setMinSize(25, 25);

        final HBox buttonHbox = new HBox(pauseButton, stopButton);
        HBox.setHgrow(buttonHbox, Priority.ALWAYS);
        buttonHbox.setAlignment(Pos.CENTER);
        buttonHbox.setPadding(new Insets(0, 40, 0, 0));
        buttonHbox.setSpacing(5);

        final Button backButton = new Button("<<");
        backButton.setMinSize(5, 5);
        backButton.setTooltip(new Tooltip("Back"));
        backButton.setOnAction(event -> {
            stopButton.fire();
            initialScanTabState();
            malwareData.clear();
        });

        final HBox backHbox = new HBox(backButton);
//        HBox.setHgrow(backHbox, Priority.ALWAYS);
        backHbox.setPadding(new Insets(0, 0, 0, 10));
        backHbox.setAlignment(Pos.CENTER_LEFT);

//        processButtons.setSpacing(5);
        processButtons.setPadding(new Insets(10, 0, 0, 0));
        processButtons.setFillHeight(true);
        processButtons.setSpacing(8);
        processButtons.setAlignment(Pos.CENTER_LEFT);
        processButtons.getChildren().addAll(backHbox, buttonHbox);
        return processButtons;
    }

    private void initialScanTabState() {
        initializeScanButtons();
        VBox scanTabVbox = new VBox();
        scanTabVbox.setSpacing(25);
        scanTabVbox.setAlignment(Pos.CENTER);
        scanTabVbox.setStyle("-fx-background-color: inherit ");
        scanTabVbox.getChildren().addAll(quickScanButton, fullScanButton, customScanButton);
        scan_tab.setContent(scanTabVbox);
        quickScanButton.setDisable(false);
        fullScanButton.setDisable(false);
        customScanButton.setDisable(false);
    }

    private HBox createProgressUI(Task task) {
        // Create scanning label, progress bar and progress indicator.
        final Label scanLabel = new Label(SCANNING, new ImageView(applicationImages.getSCAN_IMG()));

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

    private void pauseAction(Thread thread, Button pauseButton, Label scanLabel, CoordinatingTask task) {
        if (!thread.isInterrupted() && pauseButton.getText().equals(PAUSE) && !task.isDone()) {
            task.pause();
            scanLabel.setText("Paused");
            pauseButton.setText("Resume");
        } else if (!task.isDone()) {
            task.unPause();
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
