package application.gui;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Path;

/**
 * Created by Radoslav Ralinov on 27/02/2016. All rights reserved. Created as part of the Third Year Project
 * at University of Manchester. Third-Year-Project
 */
public class CustomScanDialog {
    TreeView desktopTree;

    public void start(Stage stage, ViradoGUI gui) {
        stage.setTitle("Choose disk/file/folder to scan");
        stage.setWidth(310);
        stage.setHeight(350);
        initializeCheckBoxTree();

        VBox root = new VBox();
        root.setSpacing(10);

        Scene scene = new Scene(root, 310, 350);

        Button okButton = new Button("OK");
        Button cancelButton = new Button("Cancel");
        okButton.setPrefSize(70, 20);
        cancelButton.setPrefSize(80, 20);
        HBox hBox = new HBox(okButton, cancelButton);
        hBox.setAlignment(Pos.BOTTOM_CENTER);
        hBox.setSpacing(20);
        hBox.setPadding(new Insets(0, 0, 10, 0));

        final ObservableList[] selectedFiles = {null};
        okButton.setOnAction(event -> {
            selectedFiles[0] = desktopTree.getSelectionModel().getSelectedItems();
            Path[] paths = new Path[10];
            int index = 0;
            if(selectedFiles.length != 0) {
                for (Object objects : selectedFiles[0]) {
                    TreeItem item = (TreeItem) objects;
                    paths[index] = ((File) item.getValue()).toPath();
                    index++;
                }
                gui.scanProcessTabUI(paths);
                stage.close();
            }
        });
        Separator separator = new Separator();
        separator.setOrientation(Orientation.HORIZONTAL);
        root.getChildren().addAll(desktopTree, separator, hBox);
        stage.setScene(scene);
        stage.show();
        stage.setOnCloseRequest(event -> enableButtons(gui));
        cancelButton.setOnAction(event -> {
            enableButtons(gui);
            stage.close();
        });
    }

    private void enableButtons(ViradoGUI gui) {
        gui.customScanButton.setDisable(false);
        gui.quickScanButton.setDisable(false);
        gui.fullScanButton.setDisable(false);
    }


    private void initializeCheckBoxTree() {
        File homeFile = new File(System.getProperty("user.home"));
        FileSystemTreeItem<File> desktopRootItem = new FileSystemTreeItem<>(new File("Desktop")); // rootIcon
        desktopRootItem.setExpanded(true);
        File[] drivePaths;
        FileSystemTreeItem<File> computerTreeItem = new FileSystemTreeItem<>(new File("Computer"));
        FileSystemTreeItem homeTreeItem = new FileSystemTreeItem(homeFile);
        desktopRootItem.getChildren().add(computerTreeItem);
        desktopRootItem.getChildren().add(homeTreeItem);

        drivePaths = File.listRoots();
        desktopTree = new TreeView<>(desktopRootItem);
        desktopTree.setEditable(true);
        for (File drivePath : drivePaths) {
            FileSystemTreeItem item = createNode(drivePath);
            computerTreeItem.getChildren().add(item);
        }
    }

    protected static FileSystemTreeItem createNode(File f) {
        return new FileSystemTreeItem(f);
    }
}
