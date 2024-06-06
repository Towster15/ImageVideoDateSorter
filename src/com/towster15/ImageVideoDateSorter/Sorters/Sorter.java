package com.towster15.ImageVideoDateSorter.Sorters;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;

public class Sorter extends Thread {
    protected final File destinationDir;
    private final HashMap<String, String> monthMap = new HashMap<>();
    protected final boolean daySort;
    protected final boolean copyInsteadOfMove;

    /**
     * @param destinationDir destination directory File
     * @param daySort        boolean to enable or disable sorting by days
     */
    public Sorter(File destinationDir, boolean daySort, boolean copyInsteadOfMove) {
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

        this.destinationDir = destinationDir;
        this.daySort = daySort;
        this.copyInsteadOfMove = copyInsteadOfMove;
    }

    /**
     * Get the date that the file was created on the/a computer.
     *
     * @param file the date the file was created
     * @return a string of the creation date in the format YYYY-MM-DD
     */
    protected static String getDate(File file) throws IOException {
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

    protected void sortDatedFile(Path filePath, String date) throws IOException {
        if (copyInsteadOfMove) {
            copyDatedFile(filePath, date);
        } else {
            moveDatedFile(filePath, date);
        }
    }

    /**
     * Moves the provided file to the corresponding date folder.
     *
     * @param filePath the path of the image to be moved
     * @param date     the date the image was taken/created
     */
    protected void moveDatedFile(Path filePath, String date) throws IOException {
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

    /**
     * Moves the provided file to the corresponding date folder.
     *
     * @param filePath the path of the image to be moved
     * @param date     the date the image was taken/created
     */
    protected void copyDatedFile(Path filePath, String date) throws IOException {
        String year = date.substring(0, 4);
        String month = monthMap.get(date.substring(5, 7));
        String day = date.substring(8, 10);

        try {
            if (daySort) {
                Files.copy(
                        filePath,
                        Path.of(
                                destinationDir.getAbsolutePath(), year, month, day,
                                filePath.getFileName().toString()
                        )
                );
            } else {
                Files.copy(
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
                        Files.copy(
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
                        Files.copy(
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

    /**
     * Moves the provided file to the provided folder.
     *
     * @param filePath   the path of the image to be moved
     * @param folderName the date the image was taken/created
     */
    protected void moveToFolder(Path filePath, String folderName) throws IOException {
        try {
            Files.move(
                    filePath,
                    Path.of(
                            destinationDir.getAbsolutePath(), folderName,
                            filePath.getFileName().toString()
                    )
            );
        } catch (FileAlreadyExistsException e) {
            boolean successful_move = false;
            int duplicate_num = 1;

            int i = filePath.toString().lastIndexOf('.');
            String extension = filePath.toString().substring(i);
            int b = filePath.getFileName().toString().length();

            while (!successful_move) {
                try {
                    successful_move = true;
                    Files.move(
                            filePath,
                            Path.of(
                                    destinationDir.getAbsolutePath(),
                                    folderName,
                                    filePath.getFileName().toString().substring(
                                            0, b - extension.length()
                                    ) + String.format("(%d)%s", duplicate_num, extension)
                            )
                    );
                } catch (FileAlreadyExistsException ex) {
                    successful_move = false;
                    duplicate_num++;
                }
            }
        }
    }
}
