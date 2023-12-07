package com.towster15.ImageVideoDateSorter.Sorters;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VideoSorter extends Sorter {
    private final Logger LOGGER;
    private final boolean sortVideos;

    /**
     * @param log            the logger to report events to
     * @param sourceDir      source directory file
     * @param destinationDir destination directory file
     * @param daySort        boolean to enable or disable sorting by days
     * @param sortVideos     boolean to enable or disable sorting videos
     *                       by date
     */
    public VideoSorter(
            Logger log,
            File sourceDir,
            File destinationDir,
            boolean daySort,
            boolean sortVideos) {
        super(sourceDir, destinationDir, daySort);
        LOGGER = log;
        this.sortVideos = sortVideos;
    }

    /**
     * Run method for when an instance of this class, as a thread, is
     * called.
     */
    public void run() {
        sortVideos();
    }

    /**
     * Sorts all the videos in the source directory into either dated
     * folders or a dedicated videos folder, within the destination
     * directory.
     */
    public void sortVideos() {
        List<File> videos = listVideoFiles(sourceDir);
        if (!sortVideos) {
            File file = new File(destinationDir + "/Videos");
            if (!file.mkdirs()) {
                LOGGER.log(Level.WARNING, "Failed to make Videos folder, not sorting images");
                return;
            }
        }
        for (File video : videos) {
            String date;
            try {
                date = getDate(video);
            } catch (IOException IOex) {
                LOGGER.log(Level.WARNING, "IOException reading file creation date", IOex);
                continue;
            }
            if (sortVideos && !checkDateFolderExists(date)) {
                if (!makeDateFolder(date)) {
                    LOGGER.log(Level.WARNING, "Failed to make date folder", date);
                    continue;
                }
            }
            try {
                if (sortVideos) {
                    moveDatedFile(video.toPath(), date);
                } else {
                    moveToFolder(video.toPath(), "Videos");
                }
            } catch (IOException ioEx) {
                LOGGER.log(Level.WARNING, "Failed to make date folder, IOException", date);
                LOGGER.log(Level.WARNING, "ex: ", ioEx.getMessage());
            }
        }
    }

    /**
     * Traverses through a folder and its sub-folders to find all
     * image files and adds them to a list.
     * <p>
     * This uses the {@code checkImageFile} method to test whether
     * each file is a valid image, as to not disturb other files in
     * said folders.
     *
     * @param cwd the folder to scan, current working directory
     * @return the list of all images found
     */
    private static List<File> listVideoFiles(File cwd) {
        List<File> returnImages = new ArrayList<>();
        List<File> thisFolderFileList = listAllFiles(cwd);
        for (File file : thisFolderFileList) {
            if (checkVideoFile(file)) {
                returnImages.add(file);
            } else if (file.isDirectory()) {
                returnImages.addAll(listVideoFiles(file));
            }
        }
        return returnImages;
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
}