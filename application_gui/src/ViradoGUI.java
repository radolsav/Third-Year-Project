import eu.medsea.mimeutil.MimeUtil;
import javafx.application.Application;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;

import javafx.scene.Scene;
import javafx.scene.control.*;

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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ViradoGUI extends Application {

  private int infectedFiles = 0;

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
      public void handle(ActionEvent e) {
        MimeUtil.registerMimeDetector("eu.medsea.mimeutil.detector.MagicMimeMimeDetector");
//        ProgressDialog progressDialog = new ProgressDialog();
//        Stage newStage = new Stage();
//        progressDialog.start(newStage);

        Task task = new Task<Void>() {
          @Override
          public Void call() throws IOException {
            Path path = Paths.get("D:\\");
            FileSystemTraverse printFiles = new FileSystemTraverse();
            Files.walkFileTree(path, printFiles);
            if (isCancelled()) {
              printFiles.isCancelled(true);
            }
            updateProgress(10, 20);
            return null;
          }
        };

        // Create scanning label, progress bar and progress indicator.
        final Label scanLabel = new Label();
        scanLabel.setText("Scanning...");

        final ProgressBar pb = new ProgressBar();
        pb.progressProperty().bind(task.progressProperty());

        final ProgressIndicator pin = new ProgressIndicator();
        pin.progressProperty().bind(task.progressProperty());
        final HBox progresshb = new HBox();
        progresshb.setSpacing(5);
        progresshb.setAlignment(Pos.CENTER);
        progresshb.getChildren().addAll(scanLabel, pb, pin);

        // Create stop and pause buttons
        Image imageStop = new Image(getClass().getResourceAsStream("/resources/images/stop.png"));
        final Button stopButton = new Button("Stop", new ImageView(imageStop));
//        stopButton.setGraphic(new ImageView(imageStop));
        stopButton.setStyle("-fx-background-radius: 80");
        stopButton.setMinSize(25, 25);

        Image imagePause = new Image(getClass().getResourceAsStream("/resources/images/pause.png"));
        final Button pauseButton = new Button("Pause", new ImageView(imagePause));
//        pauseButton.setGraphic(new ImageView(imagePause));
        pauseButton.setStyle("-fx-background-radius: 80");
        pauseButton.setMinSize(25, 25);

        final HBox processButtons = new HBox();
        processButtons.setSpacing(5);
        processButtons.setAlignment(Pos.CENTER);
        processButtons.getChildren().addAll(pauseButton, stopButton);

        // Add progress items and buttons together to the tab
        final VBox vb = new VBox();
        vb.setSpacing(5);
        vb.setAlignment(Pos.CENTER);
        vb.getChildren().addAll(processButtons, progresshb);
        scan_tab.setContent(vb);

        // Start the thread
        final Thread thread = new Thread(task);
        thread.start();

        // Interrupt thread if clicked.
        stopButton.setOnAction(new EventHandler<ActionEvent>() {
          @Override
          public void handle(ActionEvent actionEvent) {
            thread.stop();
            scanLabel.setText("Stopped");
          }
        });
        pauseButton.setOnAction(new EventHandler<ActionEvent>() {
          @Override
          public void handle(ActionEvent actionEvent) {
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

  public void setInfectedFiles() {
    this.infectedFiles++;
  }


  public static void main(String[] args) {
  }

  private void createUIComponents() {
    // TODO: place custom component creation code here
  }
}
