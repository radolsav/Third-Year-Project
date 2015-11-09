import eu.medsea.mimeutil.MimeUtil;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


public class FileSystemTraverse extends SimpleFileVisitor<Path> {

  @Override
  public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
    if (attributes.isRegularFile()) {
      File newFile = new File(file.toString());
      Collection<?> mimeTypes = MimeUtil.getMimeTypes(newFile);
      System.out.println("Regular file : " + file);
      System.out.println("Type of file : " + mimeTypes);
      if (mimeTypes.toString().equals("application/zip") && file.toString().equals("D:\\test2.zip")) {
        unzipFile(file);
      }
      if(file.toString().equals("D:\\install\\putty.exe"))
      {
        FileInputStream inputStream = new FileInputStream(file.toString());
        System.out.println(readInputAndGenerateMd5(inputStream));
      }
    } else if (attributes.isSymbolicLink()) {
      System.out.println("Symbolic link : " + file);
    } else {
      System.out.println("Other : " + file);
    }
    return FileVisitResult.CONTINUE;
  }

  private void unzipFile(Path path) throws IOException {
    ZipFile zipFile = new ZipFile(path.toString());
    Enumeration<? extends ZipEntry> entries = zipFile.entries();
    while (entries.hasMoreElements()) {
      ZipEntry entry = entries.nextElement();
      System.out.println(entry.getName());
      InputStream fileInputStream = zipFile.getInputStream(entry);
      System.out.println(readInputAndGenerateMd5(fileInputStream));
      System.out.println(entry.getSize());
    }
  }

  private String readInputAndGenerateMd5(InputStream fileInputStream) throws UnsupportedEncodingException {
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
    System.out.format("Directory: %s%n", dir);
    return FileVisitResult.CONTINUE;
  }

  @Override
  public FileVisitResult visitFileFailed(Path file,
                                         IOException exc) {
    System.err.println(exc);
    return FileVisitResult.CONTINUE;
  }

}

