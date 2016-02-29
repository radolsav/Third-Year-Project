import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import java.io.File;

/**
 * Created by Radoslav Ralinov on 29/02/2016. All rights reserved. Created as part of the Third Year Project
 * at University of Manchester. Third-Year-Project.
 */
public class FileSystemTreeItem<T> extends TreeItem {
    private boolean isLeaf;
    private boolean isFirstTimeChildren = true;
    private boolean isFirstTimeLeaf = true;

    public FileSystemTreeItem(File file) {
        super(file);
    }

    @Override
    public ObservableList<FileSystemTreeItem> getChildren() {
        if (isFirstTimeChildren) {
            isFirstTimeChildren = false;
            super.getChildren().setAll(populateChildren(this));
        }
        return super.getChildren();
    }

    @Override
    public boolean isLeaf() {
        if (isFirstTimeLeaf) {
            isFirstTimeLeaf = false;
            File f = (File) getValue();
            isLeaf = f.isFile();
        }
        return isLeaf;
    }

    private ObservableList<FileSystemTreeItem> populateChildren(TreeItem<File> item) {
        File file =  item.getValue();
        if (file.isDirectory() && file.exists()) {
            File[] files = file.listFiles();
            if (files != null) {
                ObservableList<FileSystemTreeItem> children = FXCollections
                        .observableArrayList();
                for (File childFile : files) {
                    children.add(CustomScanDialog.createNode(childFile));
                }
                return children;
            }
        }
        return FXCollections.emptyObservableList();
    }
}

