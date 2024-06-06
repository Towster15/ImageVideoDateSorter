package com.towster15.ImageVideoDateSorter.SortControllers;

import com.towster15.ImageVideoDateSorter.Sorters.ImageSorter;
import com.towster15.ImageVideoDateSorter.Sorters.aaeSorter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ImgSortController extends SortController {
    private final Logger LOGGER;
    private final List<File> images;
    private final List<File> aaeList;
    private final boolean sortAAEs;
    private final boolean separateBroken;
    private final boolean OSCreateDateSort;

    public ImgSortController(
            Logger log,
            List<File> imageList,
            File destinationDir,
            boolean separateBroken,
            boolean daySort,
            boolean OSCreateDateSort,
            boolean copyInsteadOfMove) {
        super(destinationDir, daySort, copyInsteadOfMove);
        LOGGER = log;
        images = imageList;
        this.aaeList = new ArrayList<>();
        this.sortAAEs = false;
        this.separateBroken = separateBroken;
        this.OSCreateDateSort = OSCreateDateSort;
    }

    public ImgSortController(
            Logger log,
            List<File> imageList,
            List<File> aaeList,
            File destinationDir,
            boolean separateBroken,
            boolean sortAAEs,
            boolean daySort,
            boolean OSCreateDateSort,
            boolean copyInsteadOfMove) {
        super(destinationDir, daySort, copyInsteadOfMove);
        LOGGER = log;
        images = imageList;
        this.aaeList = aaeList;
        this.sortAAEs = sortAAEs;
        this.separateBroken = separateBroken;
        this.OSCreateDateSort = OSCreateDateSort;
    }

    public void run() {
        if (sortAAEs) {
            ImageSorter imageSorter = new ImageSorter(LOGGER, images, aaeList, destinationDir,
                    separateBroken, true, daySort, OSCreateDateSort, copyInsteadOfMove);
            imageSorter.start();
            try {
                imageSorter.join();
            } catch (InterruptedException e) {
                // TODO: handle this properly to allow cancellation of
                //  the process
            }
        } else {
            List<Thread> sorterThreads = new ArrayList<>();
            for (int i = 0; i < core_count; i++) {
                sorterThreads.add(new ImageSorter(LOGGER, images,destinationDir,
                        separateBroken, daySort, OSCreateDateSort, copyInsteadOfMove));
                sorterThreads.getLast().start();
            }
            sorterThreads.add(new aaeSorter(LOGGER, aaeList, destinationDir, copyInsteadOfMove));
            for (Thread thread : sorterThreads) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    // TODO: handle this properly to allow cancellation of
                    //  the process
                }
            }
        }
    }
}
