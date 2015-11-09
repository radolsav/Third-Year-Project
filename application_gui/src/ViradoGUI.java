import eu.medsea.mimeutil.MimeUtil;
import javafx.application.Application;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Side;

import javafx.scene.Scene;
import javafx.scene.control.Button;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ViradoGUI extends Application {

  public void start(final Stage primaryStage) {
    TabPane tabbedPane = new TabPane();
    Tab overview_tab = new Tab();
    overview_tab.setText("Overview");
    overview_tab.setContent(new Rectangle(400, 400, Color.DEEPSKYBLUE));
    tabbedPane.getTabs().add(overview_tab);

    Tab scan_tab = new Tab();
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
        Path path = Paths.get("D:\\");
        FileSystemTraverse printFiles = new FileSystemTraverse();
        try {
          MimeUtil.registerMimeDetector("eu.medsea.mimeutil.detector.MagicMimeMimeDetector");
          Files.walkFileTree(path,printFiles);
        } catch (IOException e1) {
          e1.printStackTrace();
        }

      }
    });
    customScanButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent actionEvent) {
        try {
          SignatureCompare.compareSignatures();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    });
    VBox vbox = new VBox();
    VBox.setMargin(quickScanButton, new Insets(23, 100, 0, 100));
    VBox.setMargin(fullScanButton, new Insets(0, 100, 0, 100));
    VBox.setMargin(customScanButton, new Insets(0, 100, 0, 100));

    vbox.setSpacing(25);
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
  }


  public static void main(String[] args) {
  }

  private void createUIComponents() {
    // TODO: place custom component creation code here
  }
}
