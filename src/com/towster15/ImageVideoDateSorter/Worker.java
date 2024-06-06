package com.towster15.ImageVideoDateSorter;

import com.towster15.ImageVideoDateSorter.SortControllers.ImgSortController;
import com.towster15.ImageVideoDateSorter.SortControllers.VidSortController;
import com.towster15.ImageVideoDateSorter.Sorters.ImageSorter;
import com.towster15.ImageVideoDateSorter.Sorters.VideoSorter;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Worker extends SwingWorker<Void, Void> {
    public boolean allowStart = false;
    private Logger logger;
    private File sourceDir;
    private File destinationDir;
    private List<File> imageList;
    private List<File> aaeList;
    private List<File> videoList;
    private boolean sortImages;
    private boolean separateBrokenImages;
    private boolean moveAAEs;
    private boolean sortAAEs;
    private boolean moveVideos;
    private boolean sortVideos;
    private boolean daySort;
    private boolean OSCreateDateSort;
    private boolean sortAllFiles;
    private boolean copyInsteadOfMove;

    public Worker() {}

    public Worker (
            Logger log,
            File sourceDir,
            File destinationDir,
            boolean sortImages,
            boolean separateBroken,
            boolean moveAAEs,
            boolean sortAAEs,
            boolean moveVideos,
            boolean sortVideos,
            boolean daySort,
            boolean OSCreateDateSort,
            boolean sortAllFiles,
            boolean copyInsteadOfMove
    ) {
        allowStart = true;
        logger = log;
        this.sourceDir = sourceDir;
        this.destinationDir = destinationDir;
        this.sortImages = sortImages;
        this.separateBrokenImages = separateBroken;
        this.moveAAEs = moveAAEs;
        this.sortAAEs = sortAAEs;
        this.moveVideos = moveVideos;
        this.sortVideos = sortVideos;
        this.daySort = daySort;
        this.OSCreateDateSort = OSCreateDateSort;
        this.sortAllFiles = sortAllFiles;
        this.copyInsteadOfMove = copyInsteadOfMove;
    }

    private static List<File> listAllFiles(File cwd) {
        List<File> allFiles = new ArrayList<>();

        File[] thisFolderFileList = cwd.listFiles();
        if (thisFolderFileList != null) {
            for (File file : thisFolderFileList) {
                if (file.isFile()) {
                    allFiles.add(file);
                } else if (file.isDirectory()) {
                    allFiles.addAll(listAllFiles(file));
                }
            }
        }
        return allFiles;
    }

    private void splitFileList() {
        imageList = new ArrayList<>();
        aaeList = new ArrayList<>();
        videoList = new ArrayList<>();
        for (File file : listAllFiles(sourceDir)) {
            if (checkImageFile(file)) {
                imageList.add(file);
            } else if (file.getName().endsWith(".aae") || file.getName().endsWith(".AAE")) {
                aaeList.add(file);
            } else if (checkVideoFile(file) || sortAllFiles) {
                videoList.add(file);
            }
        }
    }

    /**
     * Checks to see if the file provided has an image file extension.
     *
     * @param file the string of the path of the file to test
     * @return returns true if the file given is a valid image,
     * else returns false.
     */
    private static boolean checkImageFile(File file) {
        String fileName = file.toPath().getFileName().toString().toLowerCase();
        if (fileName.startsWith(".")) {
            return false;
        }
        return fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")
                || fileName.endsWith(".jpe") || fileName.endsWith(".jif")
                || fileName.endsWith(".jfif") || fileName.endsWith(".jfi")
                || fileName.endsWith(".jp2") || fileName.endsWith(".j2k")
                || fileName.endsWith(".jpf") || fileName.endsWith(".jpx")
                || fileName.endsWith(".jpm") || fileName.endsWith(".mj2")
                || fileName.endsWith(".raw") || fileName.endsWith(".dib")
                || fileName.endsWith(".svg") || fileName.endsWith(".svgz")
                || fileName.endsWith(".png") || fileName.endsWith(".webp")
                || fileName.endsWith(".gif") || fileName.endsWith(".bmp")
                || fileName.endsWith(".heic") || fileName.endsWith(".heif")
                || fileName.endsWith(".tiff") || fileName.endsWith(".tif");
    }

    /**
     * Checks to see if the file provided has a video file extension.
     *
     * @param file the string of the path of the file to test
     * @return returns true if the file given is a valid image,
     * else returns false.
     */
    private static boolean checkVideoFile(File file) {
        String fileName = file.toPath().getFileName().toString().toLowerCase();
        if (fileName.startsWith(".")) {
            return false;
        }
        return fileName.endsWith(".webm") || fileName.endsWith(".mkv")
                || fileName.endsWith(".flv") || fileName.endsWith(".ogv")
                || fileName.endsWith(".avi") || fileName.endsWith(".mts")
                || fileName.endsWith(".m2ts") || fileName.endsWith(".ts")
                || fileName.endsWith(".mov") || fileName.endsWith(".qt")
                || fileName.endsWith(".wmv") || fileName.endsWith(".rm")
                || fileName.endsWith(".rmvb") || fileName.endsWith(".viv")
                || fileName.endsWith(".asf") || fileName.endsWith(".amv")
                || fileName.endsWith(".mp4") || fileName.endsWith(".m4p")
                || fileName.endsWith(".m4v") || fileName.endsWith(".mpg")
                || fileName.endsWith(".mp2") || fileName.endsWith(".mpeg")
                || fileName.endsWith(".mpe") || fileName.endsWith(".mpv")
                || fileName.endsWith(".m2v") || fileName.endsWith(".svi")
                || fileName.endsWith(".3gp") || fileName.endsWith(".3g2")
                || fileName.endsWith(".mxf") || fileName.endsWith(".roq")
                || fileName.endsWith(".nsv") || fileName.endsWith(".f4v")
                || fileName.endsWith(".f4p") || fileName.endsWith(".f4a")
                || fileName.endsWith(".f4b");
    }

    /**
     * Computes a result, or throws an exception if unable to do so.
     *
     * <p>
     * Note that this method is executed only once.
     *
     * <p>
     * Note: this method is executed in a background thread.
     *
     * @return the computed result
     */
    @Override
    protected Void doInBackground() {
        splitFileList();
        ImgSortController imgSort = null;
        VidSortController vidSort = null;
        if (sortImages) {
            if (moveAAEs) {
                imgSort = new ImgSortController(logger, imageList, aaeList, destinationDir,
                        separateBrokenImages, sortAAEs, daySort, OSCreateDateSort);
            } else {
                imgSort = new ImgSortController(logger, imageList, destinationDir,
                        separateBrokenImages, daySort, OSCreateDateSort);
            }
            imgSort.start();
        }
        if (moveVideos) {
            vidSort = new VidSortController(logger, videoList, destinationDir,
                    daySort, sortVideos);
            vidSort.start();
        }

        if (imgSort != null) {
            try {
                imgSort.join();
            } catch (InterruptedException intEx) {
                logger.log(Level.WARNING, "Main thread interrupted for img");
            }
        }
        if (vidSort != null) {
            try {
                vidSort.join();
            } catch (InterruptedException intEx) {
                logger.log(Level.WARNING, "Main thread interrupted for vid");
            }
        }
        return null;
    }
}
