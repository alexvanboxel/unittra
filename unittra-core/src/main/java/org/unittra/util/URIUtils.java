package org.unittra.util;

import java.io.File;
import java.net.URI;

public class URIUtils {
    
    public static String getFileName(String path) {
        throw new RuntimeException();
    }
    
    public static String getDir(String path) {
        throw new RuntimeException();
    }
    
    public static String getFileName(URI uri) {
        String path = uri.getPath();
        return path.substring(path.lastIndexOf('/')+1);
    }
    
    public static String getDir(URI uri) {
        String path = uri.getPath();
        return path.substring(0,path.lastIndexOf('/'));
    }
    
    public static URI dir(String parent, String child) {
        File dir = new File(parent);
        return dir(dir.toURI(), child);
    }
    
    public static URI dir(String parent) {
        File dir = new File(parent);
        return dir.toURI();
    }
    
    public static URI dir(URI parent, String child) {
        if (child.startsWith("/") || child.startsWith("\\"))
            child = child.substring(1);
        if (child.length() > 1 && (!(child.endsWith("/") || (child.endsWith("\\")))))
            child = child + "/";
        return parent.resolve(child);
    }

    public static URI parent(URI file) {
        String path = file.getPath();
        path =  path.substring(0,path.lastIndexOf('/')+1);
        return file.resolve(path);
    }

    public static  URI file(URI parent, String child) {
        if (child.startsWith("/") || child.startsWith("\\"))
            child = child.substring(1);
        child = child.replaceAll(" ", "%20");
        return parent.resolve(child);
    }
}
