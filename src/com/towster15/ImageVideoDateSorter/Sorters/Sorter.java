package com.towster15.ImageVideoDateSorter.Sorters;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Sorter {
    protected final File sourceDir;
    protected final File destinationDir;
    private final HashMap<String, String> monthMap = new HashMap<>();
    protected final boolean daySort;

    /**
     * @param sourceDir source directory File
     * @param destinationDir destination directory File
     * @param daySort boolean to enable or disable sorting by days
     */
    public Sorter(File sourceDir, File destinationDir, boolean daySort) {
        monthMap.put("01", "01 January");
        monthMap.put("02", "02 February");
        monthMap.put("03", "03 March");
        monthMap.put("04", "04 April");
        monthMap.put("05", "05 May");
        monthMap.put("06", "06 June");
        monthMap.put("07", "07 July");
        monthMap.put("08", "08 August");
        monthMap.put("09", "09 September");
        monthMap.put("10", "10 October");
        monthMap.put("11", "11 November");
        monthMap.put("12", "12 December");

        this.sourceDir = sourceDir;
        this.destinationDir = destinationDir;
        this.daySort = daySort;
    }

    /**
     * Traverses through a folder and its sub-folders to find all
     * files and adds them to a list. Uses recursion to check
     * sub-folders.
     *
     * @param cwd the folder to scan, current working directory
     * @return the list of all images found
     */
    protected static List<File> listAllFiles(File cwd) {
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

    /**
     * Get the date that the file was created on the/a computer.
     *
     * @param file the date the file was created
     * @return a string of the creation date in the format YYYY-MM-DD
     */
    protected static String getDate(File file) throws IOException{
        return Files.readAttributes(
                Paths.get(file.getAbsolutePath()),
                BasicFileAttributes.class
        ).creationTime().toString().substring(0, 10);
    }

    /**
     * Checks whether the folder corresponding to the given date
     * exists.
     *
     * @param date the date the image was taken/created
     * @return true if the folder exists, otherwise returns false.
     */
    protected boolean checkDateFolderExists(String date) {
        String year = date.substring(0, 4);
        String month = monthMap.get(date.substring(5, 7));
        String day = date.substring(8, 10);

        File file;
        if (daySort) {
            file = new File(
                    destinationDir + "/" + year + "/" + month + "/" + day
            );
        } else {
            file = new File(destinationDir + "/" + year + "/" + month);
        }
        return file.exists() && file.isDirectory();
    }

    /**
     * Creates the necessary folders to copy an image to the correct
     * sorted location.
     *
     * @param date the date the image was taken/created
     * @return true if the folder creation was a success,
     * otherwise returns false.
     */
    protected boolean makeDateFolder(String date) {
        String year = date.substring(0, 4);
        String month = monthMap.get(date.substring(5, 7));
        String day = date.substring(8, 10);

        File file;
        if (daySort) {
            file = new File(
                    destinationDir + "/" + year + "/" + month + "/" + day
            );
        } else {
            file = new File(destinationDir + "/" + year + "/" + month);
        }
        return file.mkdirs();
    }

    /**
     * Moves the provided file to the corresponding date folder.
     *
     * @param filePath the path of the image to be moved
     * @param date the date the image was taken/created
     */
    protected void moveFile(Path filePath, String date) throws IOException {
        String year = date.substring(0, 4);
        String month = monthMap.get(date.substring(5, 7));
        String day = date.substring(8, 10);

        try {
            if (daySort) {
                Files.move(
                        filePath,
                        Path.of(
                                destinationDir.getAbsolutePath(), year, month, day,
                                filePath.getFileName().toString()
                        )
                );
            } else {
                Files.move(
                        filePath,
                        Path.of(
                                destinationDir.getAbsolutePath(), year, month,
                                filePath.getFileName().toString()
                        )
                );
            }
        } catch (FileAlreadyExistsException e) {
            boolean successful_move = false;
            int duplicate_num = 1;

            int i = filePath.toString().lastIndexOf('.');
            String extension = filePath.toString().substring(i);
            int b = filePath.getFileName().toString().length();

            while (!successful_move) {
                try {
                    successful_move = true;
                    if (daySort) {
                        Files.move(
                                filePath,
                                Path.of(
                                        destinationDir.getAbsolutePath(),
                                        year, month, day,
                                        filePath.getFileName().toString().substring(
                                                0, b - extension.length()
                                        ) + String.format("(%d)%s", duplicate_num, extension)
                                )
                        );
                    } else {
                        Files.move(
                                filePath,
                                Path.of(
                                        destinationDir.getAbsolutePath(),
                                        year, month,
                                        filePath.getFileName().toString().substring(
                                                0, b - extension.length()
                                        ) + String.format("(%d)%s", duplicate_num, extension)
                                )
                        );
                    }
                } catch (FileAlreadyExistsException ex) {
                    successful_move = false;
                    duplicate_num++;
                }
            }
        }
    }
}
