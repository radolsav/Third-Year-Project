import javafx.application.Application;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Side;

import javafx.scene.Scene;
import javafx.scene.control.Button;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;


import javafx.scene.effect.DropShadow;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class ViradoGUI extends Application {
  private Button scanButton;

  public void start(Stage primaryStage) {


    TabPane tabbedPane = new TabPane();

    Tab overview_tab = new Tab();
    overview_tab.setText("Overview");
    overview_tab.setContent(new Rectangle(400, 400, Color.WHITE));
    tabbedPane.getTabs().add(overview_tab);

    Tab scan_tab = new Tab();
    scan_tab.setText("Scan");
    scanButton = new Button("Scan");
    final DropShadow shadow = new DropShadow();
    D
    scanButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent e) {
        scanButton.setEffect(shadow);
        scanButton.setText("Scan");
      }
    });
    scan_tab.setContent(scanButton);
    tabbedPane.getTabs().add(scan_tab);

    Tab dummy_tab = new Tab();
    dummy_tab.setText("Dummy");
    dummy_tab.setContent(new Rectangle(400, 400, Color.WHITE));
    tabbedPane.getTabs().add(dummy_tab);

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
