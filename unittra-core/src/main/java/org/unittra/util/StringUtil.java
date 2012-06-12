package org.unittra.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

//import org.apache.http.util.CharArrayBuffer;

public class StringUtil {
    
//    public static String toString(InputStream input) throws IOException {
//        return toString(input, "UTF-8");
//    }
    
//    public static String toString(InputStream input, String charset) throws IOException {
//        Reader reader = new InputStreamReader(input, charset);
//        CharArrayBuffer buffer = new CharArrayBuffer(4096);
//        try {
//            char[] tmp = new char[1024];
//            int l;
//            while ((l = reader.read(tmp)) != -1) {
//                buffer.append(tmp, 0, l);
//            }
//        } finally {
//            reader.close();
//        }
//        return buffer.toString();
//        
//    }

    public static void toFile(InputStream in, String filename) throws IOException {
        OutputStream out = new FileOutputStream(new File(filename));
        byte buffer[] = new byte[512000];
        int length;
        long transfered = 0;
        while ((length = in.read(buffer)) != -1) {
            System.out.print("#");
            out.write(buffer, 0, length);
            transfered += length;
        }
        in.close();
        out.close();
    }
}
