package com.towster15.ImageVideoDateSorter.SortControllers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SortController extends Thread {
    protected final File destinationDir;
    protected final boolean daySort;
    protected final int core_count = Runtime.getRuntime().availableProcessors();

    public SortController(File destinationDir, boolean daySort) {
        this.destinationDir = destinationDir;
        this.daySort = daySort;
    }

    protected List<List<File>> splitFileList(List<File> inputList) {
        List<List<File>> outputList = new ArrayList<>();
        int step = Math.floorDiv(inputList.size(), core_count);
        for (int i = 0; i < core_count; i++) {
            if (inputList.size() - (step * i) < step) {
                outputList.add(inputList.subList(step * i, inputList.size()));
            } else {
                outputList.add(inputList.subList(step * i, step * (i + 1)));
            }
        }
        return outputList;
    }
}
