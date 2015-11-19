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

import java.nio.file.Path;
import java.nio.file.Paths;


public class ViradoGUI extends Application {
  private TableView<Malware> tableView = new TableView<>();
  private ObservableList malwareData = FXCollections.observableArrayList();
  ;
  private final Client client = new OrchestrateClient("e4f5cbf3-991a-41ab-aa6e-346d53c0ac2b");


  public void start(final Stage primaryStage) {
    final TabPane tabbedPane = new TabPane();
    Tab overview_tab = new Tab();
    overview_tab.setText("Overview");
    overview_tab.setContent(new Rectangle(400, 400, Color.DEEPSKYBLUE));
    tabbedPane.getTabs().add(overview_tab);

    final Tab scan_tab = new Tab();
    scan_tab.setText("Scan");
    final Button quickScanButton = new Button("Quick Scan");
    Button fullScanButton = new Button("Full Scan");
    Button customScanButton = new Button("Custom Scan");
    quickScanButton.setMinSize(100, 100);
    fullScanButton.setMinSize(100, 100);
    customScanButton.setMinSize(100, 100);
    quickScanButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(final ActionEvent e) {
        MimeUtil.registerMimeDetector("eu.medsea.mimeutil.detector.MagicMimeMimeDetector");
        Path pathToScan = Paths.get("D:\\");
        final FileSystemTraverse task = new FileSystemTraverse(client, pathToScan, malwareData);
       /* final Map<Thread, Task> threadTaskMap = new HashMap<>();
        List<String> dirs;
        dirs = new ArrayList<>(Arrays.asList(pathToScan.toFile().list(new FilenameFilter() {
          @Override
          public boolean accept(File dir, String name) {
            return new File(dir, name).isDirectory();
          }
        })));
        for (int i = 0; i < dirs.size(); i++) {
          dirs.set(i, "D:\\" + dirs.get(i));

          if((FileUtils.sizeOfDirectory(Paths.get(dirs.get(i)).toFile())) > 0) {
            threadTaskMap.put(new Thread(new FileSystemTraverse(client, Paths.get(dirs.get(i)))),
                    new FileSystemTraverse(client, Paths.get(dirs.get(i))));
            System.out.println((FileUtils.sizeOfDirectory(Paths.get(dirs.get(i)).toFile())));
          }
        }*/

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
        });
        infectedSoFarBox.getChildren().addAll(infectedSoFarLabel);
        infectedSoFarBox.setAlignment(Pos.CENTER);

        VBox tableVbox = new VBox();
        final Label malwareLabel = new Label("Found malware:");
        tableView.setEditable(false);
        tableVbox.setSpacing(5);
        tableVbox.setPadding(new Insets(10, 10, 10, 10));
        TableColumn fileNameColumn = new TableColumn("File name");
//        fileNameColumn.setMinWidth(50);
        fileNameColumn.setCellValueFactory(
                new PropertyValueFactory<Malware, String>("fileName"));
        TableColumn fileSizeColumn = new TableColumn("Size");

//        fileSizeColumn.setMinWidth(50);
        fileSizeColumn.setCellValueFactory(
                new PropertyValueFactory<Malware, Long>("size"));
        TableColumn filePathColumn = new TableColumn("Path");
//        filePathColumn.setMinWidth(50);
        filePathColumn.setCellValueFactory(
                new PropertyValueFactory<Malware, Path>("path"));
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


       /* for (Map.Entry<Thread, Task> mapEntry : threadTaskMap.entrySet()) {
          mapEntry.getKey().start();
        }*/
        // Start the thread
        final Thread thread = new Thread(task);
        thread.start();

        // Interrupt thread if clicked.
        stopButton.setOnAction(new EventHandler<ActionEvent>() {
          @Override
          public void handle(ActionEvent actionEvent) {
//            for (Map.Entry<Thread, Task> entry : threadTaskMap.entrySet()) {
            thread.stop();
//            }
//            thread.stop();
            scanLabel.setText("Stopped");
          }
        });
        pauseButton.setOnAction(new EventHandler<ActionEvent>() {
          @Override
          public void handle(ActionEvent actionEvent) {
//            for (Map.Entry<Thread, Task> entry : threadTaskMap.entrySet()) {
            pauseAction(thread, pauseButton, scanLabel);
//            }
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

    Tab dummy_tab = new Tab();
    dummy_tab.setText("Dummy");
    dummy_tab.setContent(new Rectangle(400, 400, Color.DEEPSKYBLUE));
    tabbedPane.getTabs().add(dummy_tab);

    tabbedPane.rotateGraphicProperty();
    tabbedPane.setSide(Side.LEFT);
    tabbedPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
    tabbedPane.rotateGraphicProperty();
    StackPane root = new StackPane();
    root.getChildren().add(tabbedPane);

    Scene scene = new Scene(root, 400, 400);
    scene.getStylesheets().add("ViradoGUI.css");
    primaryStage.setTitle("Virado");
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

  private HBox createButtonsUI() {
    // Create stop and pause buttons
    Image imageStop = new Image(getClass().getResourceAsStream("/resources/images/stop.png"));
    final Button stopButton = new Button("Stop", new ImageView(imageStop));
    stopButton.setStyle("-fx-background-radius: 80");
    stopButton.setMinSize(25, 25);

    Image imagePause = new Image(getClass().getResourceAsStream("/resources/images/pause.png"));
    final Button pauseButton = new Button("Pause", new ImageView(imagePause));
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
    Image magnifierImage = new Image(getClass().getResourceAsStream("/resources/images/magnifier.png"));
    final Label scanLabel = new Label("Scanning...", new ImageView(magnifierImage));

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
    if (!thread.isInterrupted() && pauseButton.getText().equals("Pause")) {
      thread.suspend();
      scanLabel.setText("Paused");
      pauseButton.setText("Resume");
    } else {
      thread.resume();
      scanLabel.setText("Scanning");
      pauseButton.setText("Pause");
    }
  }


  public static void main(String[] args) {
  }

  private void createUIComponents() {
    // TODO: place custom component creation code here
  }
}
