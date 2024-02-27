package com.towster15.ImageVideoDateSorter.SortControllers;

import com.towster15.ImageVideoDateSorter.Sorters.VideoSorter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class VidSortController extends SortController {
    private final Logger LOGGER;
    private final List<List<File>> videoLists;
    private final boolean sortVideos;
    public VidSortController(
            Logger log,
            List<File> videoList,
            File destinationDir,
            boolean daySort,
            boolean sortVideos) {
        super(destinationDir, daySort);
        LOGGER = log;
        videoLists = splitFileList(videoList);
        this.sortVideos = sortVideos;
    }

    public void run() {
        List<Thread> sorterThreads = new ArrayList<>();
        for (int i = 0; i < core_count; i++) {
            sorterThreads.add(new VideoSorter(LOGGER, videoLists.get(i), destinationDir, daySort,
                    sortVideos));
            sorterThreads.getLast().start();
        }
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