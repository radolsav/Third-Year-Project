import eu.medsea.mimeutil.MimeUtil;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;


public class FileSystemTraverse extends SimpleFileVisitor<Path> {

  @Override
  public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
    if (attributes.isRegularFile()) {
      File newFile = new File(file.toString());
      Collection<?> mimeTypes = MimeUtil.getMimeTypes(newFile);
      System.out.println("Regular file : " + file);
      System.out.println("Type of file : " + mimeTypes);
    } else if (attributes.isSymbolicLink()) {
      System.out.println("Symbolic link : " + file);
    } else {
      System.out.println("Other : " + file);
    }
    return FileVisitResult.CONTINUE;
  }

  @Override
  public FileVisitResult postVisitDirectory(Path dir,
                                            IOException exc) {
    System.out.format("Directory: %s%n", dir);
    return FileVisitResult.CONTINUE;
  }

  @Override
  public FileVisitResult visitFileFailed(Path file,
                                         IOException exc) {
    System.err.println(exc);
    return FileVisitResult.CONTINUE;
  }

  /*public static void traverse()
  {
    File[] paths;
    FileSystemView fileSystemView = FileSystemView.getFileSystemView();

    paths = File.listRoots();
    for(File path : paths)
    {
      System.out.println("Drive name: " + path);
      if (path.toString().equals("D:\\")) {
        if (fileSystemView.getSystemTypeDescription(path).equals("Local Disk")) {
          search(path);
        }
      }
      System.out.println("Desc: " + fileSystemView.getSystemTypeDescription(path));
    }*/


  /*// Recursion through the disk
  private static void search(File path) {
    File[] paths = path.listFiles();
    if (paths != null) {
      for (File newPath : paths) {
        if (newPath.isDirectory()) {
          System.out.println("Dir" + newPath.getName());
          search(newPath);
        } else {
          System.out.println("File" + newPath.getName());
        }
      }
    }
  }*/

}

