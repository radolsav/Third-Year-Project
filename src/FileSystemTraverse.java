import eu.medsea.mimeutil.MimeUtil;
import io.orchestrate.client.Client;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import org.apache.commons.io.FileUtils;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by Radoslav Ralinov on 30/12/2015. All rights reserved. Created as part of the Third Year Project
 * at University of Manchester. Third-Year-Project
 */
public class FileSystemTraverse extends Task<ObservableList<Malware>> implements FileVisitor<Path> {

    private Client client;
    private int infectedFiles;
    private ArrayList<String> files;
    private int FIRST_LAYER_OF_DIRS_SIZE;
    private Path scanPath;
    private ObservableList<Malware> malware = FXCollections.observableArrayList();

    public FileSystemTraverse(Client client, Path scanPath, ObservableList<Malware> data) {
        this.client = client;
        this.scanPath = scanPath;
        this.malware = data;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path filePath, BasicFileAttributes attributes) {
        Objects.requireNonNull(attributes);
        Objects.requireNonNull(filePath);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path filePath, BasicFileAttributes attributes) {
        Objects.requireNonNull(attributes);
        Objects.requireNonNull(filePath);
        try {
            if (attributes.isRegularFile()) {
                File newFile = new File(filePath.toString());
                Collection mimeTypes = MimeUtil.getMimeTypes(newFile);
                System.out.println("Regular file : " + filePath);
                System.out.println("Type of file : " + mimeTypes);
                updateTitle(filePath.toString());
                boolean mime = false;
                for (Object mimeType : mimeTypes) {
                    if (mimeType.toString().equals("application/zip")) {
                        mime = true;
                    }
                }
                if (mime) {
                    unzipFile(filePath);
                } else if (Files.exists(filePath,
                        LinkOption.NOFOLLOW_LINKS)) {
                    FileInputStream inputStream = new FileInputStream(filePath.toString());
                    checkFile(newFile.length(), filePath, inputStream, newFile.getName());
                    inputStream.close();
                }
            } else {
                System.out.println("Other : " + filePath);
            }
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
        return FileVisitResult.CONTINUE;
    }

    private void checkFile(long fileSize, Path filePath, InputStream inputStream, String fileName) {
        ArrayList<HashSignature> signatures = SignatureCompare.compareHashSignatures(client, fileSize);
        try {
            Thread.sleep(100);
        } catch (InterruptedException exc) {
            exc.printStackTrace(System.err);
        }
//    SignatureCompare.compareByteSignatures(client,"58354f2150254041505b345c505a58353428505e2937434329377d24454943415dfsdf");
        if (!signatures.isEmpty()) {
            String hashOfFile = readInputAndGenerateHash(inputStream);
            // Might be more than one signatures with the same size
            for (HashSignature signature : signatures) {
                if (signature.getSignature().equals(hashOfFile)) {
                    infectedFiles++;
                    updateMessage("Infected files: " + infectedFiles);
                    malware.add(new Malware(filePath, FileUtils.byteCountToDisplaySize(fileSize), fileName));
                }
            }
        }
    }

    private void unzipFile(Path path) {
        try (ZipFile zipFile = new ZipFile(path.toString())) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                InputStream fileInputStream = zipFile.getInputStream(entry);
                checkFile(entry.getSize(), path, fileInputStream, entry.getName());
                fileInputStream.close();
            }
            zipFile.close();
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }

    private String readInputAndGenerateHash(InputStream fileInputStream) {
        String md5hash = "";
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            md5hash = getHash(fileInputStream, messageDigest, 2048);
            messageDigest.reset();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace(System.err);
        }
        return md5hash;
    }

    public static String getHash(InputStream inputStream, MessageDigest messageDigest, int sizeOfArray) {
        String result = "";
        try {
            messageDigest.reset();
            byte[] bytes = new byte[sizeOfArray];
            int numBytes;
            while ((numBytes = inputStream.read(bytes)) != -1) {
                messageDigest.update(bytes, 0, numBytes);
            }
            byte[] digest = messageDigest.digest();
            result = new HexBinaryAdapter().marshal((digest));
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
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
        exc.printStackTrace();
        return FileVisitResult.CONTINUE;
    }

    @Override
    protected ObservableList<Malware> call() {
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
        try {
            Files.walkFileTree(scanPath, this);
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
        return malware;
    }

}