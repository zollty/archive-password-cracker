package org.zollty.passwdcr;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

public class CRCUtils {

    public static String toHexString(CRC32 crc32) {
        return Long.toHexString(crc32.getValue()).toUpperCase();
    }

    public static CRC32 loadCRC32(String filePath) {
        try {
            return loadCRC32(new FileInputStream(new File(filePath)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static CRC32 loadCRC32(InputStream inputStream) {
        CRC32 crc32 = new CRC32();
        CheckedInputStream checkedinputstream = null;
        try {
            checkedinputstream = new CheckedInputStream(inputStream, crc32);
            while (checkedinputstream.read() != -1) {
            }
        } catch (IOException e) {
            // e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
            }
            if (checkedinputstream != null) {
                try {
                    checkedinputstream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return crc32;
    }

    public static void test(String[] args) {
        String path = "E:\\shanshuo-yang\\shanshuo\\7zcracker\\7-zip.chm";
        System.out.println(loadCRC32(path));
    }
}