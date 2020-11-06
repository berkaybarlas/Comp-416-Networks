package utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class FileManager {

    public static String getFileType(String fileLocation) {
        String fileType = "JSON";
        if (fileLocation.toLowerCase().contains(".png")) {
            fileType = "png";
        }
        return fileType;
    }

    public static byte[] fileToByte(String fileLocation) throws IOException {
        File file = new File(fileLocation);
        return fileToByte(file);
    }

    public static byte[] fileToByte(File file) throws IOException {
        System.out.println("Reading file...");
        byte [] fileByteArray  = new byte [(int)file.length()];
        FileInputStream fis = new FileInputStream(file);

        BufferedInputStream bis = new BufferedInputStream(fis);
        bis.read(fileByteArray,0,fileByteArray.length);
        System.out.println("File read complete. File size: " + fileByteArray.length);

        return fileByteArray;
    }
}
