package com.towster15.ImageVideoDateSorter;

import com.towster15.ImageVideoDateSorter.Sorters.ImageSorter;
import com.towster15.ImageVideoDateSorter.Sorters.VideoSorter;

import javax.swing.*;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Worker extends SwingWorker<Void, Void> {
    public boolean allowStart = false;
    private Logger logger;
    private File sourceDir;
    private File destinationDir;
    private boolean sortImages;
    private boolean moveVideos;
    private boolean separateBrokenImages;
    private boolean daySort;
    private boolean OSCreateDateSort;
    private boolean sortVideos;

    public Worker() {}

    public Worker (
            Logger log,
            File sourceDir,
            File destinationDir,
            boolean sortImages,
            boolean moveVideos,
            boolean separateBroken,
            boolean daySort,
            boolean OSCreateDateSort,
            boolean sortVideos
    ) {
        allowStart = true;
        logger = log;
        this.sourceDir = sourceDir;
        this.destinationDir = destinationDir;
        this.sortImages = sortImages;
        this.moveVideos = moveVideos;
        this.separateBrokenImages = separateBroken;
        this.daySort = daySort;
        this.OSCreateDateSort = OSCreateDateSort;
        this.sortVideos = sortVideos;
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
     * @throws Exception if unable to compute a result
     */
    @Override
    protected Void doInBackground() {
        ImageSorter imgSort = null;
        VideoSorter vidSort = null;
        if (sortImages) {
            imgSort = new ImageSorter(logger, sourceDir, destinationDir,
                    separateBrokenImages, daySort, OSCreateDateSort);
            imgSort.start();
        }
        if (moveVideos) {
            vidSort = new VideoSorter(logger, sourceDir, destinationDir,
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
