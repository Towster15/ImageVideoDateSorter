package com.towster15.ImageVideoDateSorter.Sorters;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VideoSorter extends Sorter {
    private final Logger LOGGER;
    private final List<File> videos;
    private final boolean sortVideos;

    /**
     * @param log            the logger to report events to
     * @param videoList      list of videos to sort
     * @param destinationDir destination directory file
     * @param daySort        boolean to enable or disable sorting by days
     * @param sortVideos     boolean to enable or disable sorting videos
     *                       by date
     */
    public VideoSorter(
            Logger log,
            List<File> videoList,
            File destinationDir,
            boolean daySort,
            boolean sortVideos) {
        super(destinationDir, daySort);
        LOGGER = log;
        videos = videoList;
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
}