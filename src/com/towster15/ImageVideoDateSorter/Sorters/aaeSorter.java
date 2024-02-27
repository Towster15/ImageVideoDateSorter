package com.towster15.ImageVideoDateSorter.Sorters;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class aaeSorter extends Sorter {
    private final Logger LOGGER;
    private final File destinationDir;
    private final List<File> aaes;

    public aaeSorter(Logger log, List<File> aaeList, File destinationDir) {
        super(destinationDir, false);
        this.LOGGER = log;
        this.destinationDir = destinationDir;
        aaes = aaeList;
    }

    public void run() {
        File looseAAEs = new File(destinationDir + "/AAEs");
        if (looseAAEs.mkdirs() || Files.exists(looseAAEs.toPath())) {
            for (File aae : aaes) {
                try {
                    moveToFolder(aae.toPath(), "AAEs");
                } catch (FileAlreadyExistsException fEx) {
                    LOGGER.log(Level.WARNING, "File already exists in AAE folder", aae);
                } catch (IOException ioEx) {
                    LOGGER.log(Level.WARNING, "Failed to make AAE folder, IOException");
                }
            }
        } else {
            LOGGER.warning("Failed to make AAE folder.");
        }
    }
}
