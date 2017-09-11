package com.viableindustries.waterreporter.data;

import android.content.Context;

import java.io.File;

/**
 * Created by brendanmcintyre on 12/5/16.
 */

public class CacheManager {

    private static final long MAX_SIZE = 5242880L; // 5MB

    private CacheManager() {

    }

    public static void deleteCache(Context aContext) {
        try {
            File dir = aContext.getCacheDir();
            deleteDir(dir);
        } catch (Exception e) {
            //
        }
    }

    private static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (String aChildren : children) {
                boolean success = deleteDir(new File(dir, aChildren));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else
            return dir != null && dir.isFile() && dir.delete();
    }

//    public static void cacheData(Context aContext, byte[] data, String name) throws IOException {
//
//        File cacheDir = context.getCacheDir();
//        long size = getDirSize(cacheDir);
//        long newSize = data.length + size;
//
//        if (newSize > MAX_SIZE) {
//            cleanDir(cacheDir, newSize - MAX_SIZE);
//        }
//
//        File file = new File(cacheDir, name);
//        FileOutputStream os = new FileOutputStream(file);
//
//        try {
//
//            os.write(data);
//
//        } finally {
//
//            os.flush();
//            os.close();
//
//        }
//    }
//
//    public static byte[] retrieveData(Context aContext, String name) throws IOException {
//
//        File cacheDir = context.getCacheDir();
//        File file = new File(cacheDir, name);
//
//        if (!file.exists()) {
//            // Data doesn't exist
//            return null;
//
//        }
//
//        byte[] data = new byte[(int) file.length()];
//        FileInputStream is = new FileInputStream(file);
//
//        try {
//
//            is.read(data);
//
//        } finally {
//
//            is.close();
//
//        }
//
//        return data;
//    }
//
//    private static void cleanDir(File dir, long bytes) {
//
//        long bytesDeleted = 0;
//        File[] files = dir.listFiles();
//
//        for (File file : files) {
//
//            bytesDeleted += file.length();
//            file.delete();
//
//            if (bytesDeleted >= bytes) {
//                break;
//            }
//
//        }
//    }
//
//    private static long getDirSize(File dir) {
//
//        long size = 0;
//        File[] files = dir.listFiles();
//
//        for (File file : files) {
//            if (file.isFile()) {
//                size += file.length();
//            }
//        }
//
//        return size;
//
//    }
}
