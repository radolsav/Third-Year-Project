import eu.medsea.mimeutil.MimeUtil;
import io.orchestrate.client.Client;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


public class FileSystemTraverse extends Task<ObservableList<Malware>> implements FileVisitor<Path> {

  private Client client;
  private int infectedFiles;
  private ArrayList<String> files;
  private int FIRST_LAYER_OF_DIRS_SIZE;
  private Path scanPath;
  private ObservableList malware = FXCollections.observableArrayList();

  public FileSystemTraverse(Client client, Path scanPath, ObservableList data) {
    this.client = client;
    this.scanPath = scanPath;
    this.malware = data;
  }

  @Override
  public FileVisitResult preVisitDirectory(Path filePath, BasicFileAttributes attributes) throws IOException {
    Objects.requireNonNull(attributes);
    Objects.requireNonNull(filePath);
    return FileVisitResult.CONTINUE;
  }

  @Override
  public FileVisitResult visitFile(Path filePath, BasicFileAttributes attributes) throws IOException {
    Objects.requireNonNull(attributes);
    Objects.requireNonNull(filePath);
    if (attributes.isRegularFile()) {
/*      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }*/
      File newFile = new File(filePath.toString());
      Collection<?> mimeTypes = MimeUtil.getMimeTypes(newFile);
      System.out.println("Regular file : " + filePath);
      System.out.println("Type of file : " + mimeTypes);
      updateTitle(filePath.toString());
      if (mimeTypes.toString().equals("application/zip")) {
        unzipFile(filePath);
      }
      else if (Files.exists(filePath,
              LinkOption.NOFOLLOW_LINKS)) {
        FileInputStream inputStream = new FileInputStream(filePath.toString());
        checkFile(newFile.length(), filePath,inputStream,newFile.getName());
        inputStream.close();
      }
    } else if (attributes.isSymbolicLink()) {
      System.out.println("Symbolic link : " + filePath);
    } else {
      System.out.println("Other : " + filePath);
    }
    return FileVisitResult.CONTINUE;
  }

  private void checkFile(long fileSize, Path filePath, InputStream inputStream,String fileName) throws IOException {
    ArrayList<HashSignature> signatures = SignatureCompare.compareSignatures(client,fileSize );
    if (!signatures.isEmpty()) {
      // Might be more than one signatures with the same size
      for (HashSignature signature : signatures) {
        if (signature.getSignature().equals(readInputAndGenerateMd5(inputStream))) {
          infectedFiles++;
          updateMessage("Infected files: " + infectedFiles);
          malware.add(new Malware(filePath, fileSize,fileName));
        }
        inputStream.close();
      }
    }
  }

  private void unzipFile(Path path) throws IOException {
    try (ZipFile zipFile = new ZipFile(path.toString())) {
      Enumeration<? extends ZipEntry> entries = zipFile.entries();

      while (entries.hasMoreElements()) {
        ZipEntry entry = entries.nextElement();
        InputStream fileInputStream = zipFile.getInputStream(entry);
        checkFile(entry.getSize(),path,fileInputStream,entry.getName());
        fileInputStream.close();
      }
      zipFile.close();
    }
  }

  private String readInputAndGenerateMd5(InputStream fileInputStream) throws IOException {
    String md5hash = "";
    try {
      MessageDigest messageDigest = MessageDigest.getInstance("MD5");
      md5hash = getMd5(fileInputStream, messageDigest, 2048);
      messageDigest.reset();
    } catch (NoSuchAlgorithmException | IOException e) {
      e.printStackTrace();
    }
    return md5hash;
  }

  public static String getMd5(InputStream inputStream, MessageDigest messageDigest, int sizeOfArray)
          throws NoSuchAlgorithmException, IOException {

    messageDigest.reset();
    byte[] bytes = new byte[sizeOfArray];
    int numBytes;
    while ((numBytes = inputStream.read(bytes)) != -1) {
      messageDigest.update(bytes, 0, numBytes);
    }
    byte[] digest = messageDigest.digest();
    String result = new HexBinaryAdapter().marshal((digest));
    return result;
  }


  @Override
  public FileVisitResult postVisitDirectory(Path dir,
                                            IOException exc) {
    Objects.requireNonNull(dir);
    if (files.contains(dir.toString())) {
      files.remove(dir.toString());
      updateProgress(FIRST_LAYER_OF_DIRS_SIZE - files.size(), FIRST_LAYER_OF_DIRS_SIZE);
    }
    System.out.format("Directory: %s%n", dir);
    return FileVisitResult.CONTINUE;
  }

  @Override
  public FileVisitResult visitFileFailed(Path filePath,
                                         IOException exc) {
    Objects.requireNonNull(filePath);
    System.err.println(exc);
    return FileVisitResult.CONTINUE;
  }

  @Override
  protected ObservableList<Malware> call() throws Exception {
    if (scanPath.toFile().listFiles() != null) {
      files = new ArrayList<>(Arrays.asList((scanPath.toFile()).list(new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
          return new File(dir, name).isDirectory();
        }
      })));
      for (int i = 0; i < files.size(); i++) {
        files.set(i, "D:\\" + files.get(i));
      }
      FIRST_LAYER_OF_DIRS_SIZE = files.size();
    }
    updateProgress(0, FIRST_LAYER_OF_DIRS_SIZE);
    updateMessage("Infected files: 0");
    Files.walkFileTree(scanPath, this);
    return malware;
  }

}

