import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Created by Radoslav Ralinov on 30/12/2015. All rights reserved. Created as part of the Third Year Project
 * at University of Manchester. Third-Year-Project
 */
public class ProgressDialog extends Application {

  final Float[] values = new Float[]{-1.0f, 0f, 0.6f, 1.0f};
  final Label[] labels = new Label[values.length];
  final ProgressBar[] pbs = new ProgressBar[values.length];
  final ProgressIndicator[] pins = new ProgressIndicator[values.length];
  final HBox hbs[] = new HBox[values.length];

  @Override
  public void start(Stage stage) {
//    scene.getStylesheets().add("progresssample/Style.css");
    Group root = new Group();
    Scene scene = new Scene(root, 300, 150);
    scene.getStylesheets().add("progresssample/Style.css");
    stage.setScene(scene);
    stage.setTitle("Progress Controls");

    final VBox vBox = new VBox();
    vBox.setSpacing(5);
    vBox.getChildren().addAll(hbs);
//    scene.setRoot(vb);
    stage.show();
  }

  public static void ProgressDialog(String[] args) {
  }
}