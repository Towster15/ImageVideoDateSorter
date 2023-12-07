package com.towster15.ImageVideoDateSorter.Sorters;

import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ImageSorter extends Sorter {
    private final Logger LOGGER;
    private final boolean separateBroken;
    private final boolean OSCreateDateSort;
    private final HashMap<String, String> datedImages = new HashMap<>();

    /**
     * @param log              the logger to report events to
     * @param sourceDir        source directory file
     * @param destinationDir   destination directory file
     * @param separateBroken   boolean to enable or disable separating
     *                         broken images from the rest
     * @param daySort          boolean to enable or disable sorting by days
     * @param OSCreateDateSort boolean to enable or disable using the
     *                         OS's creation date, as a fallback opt.
     */
    public ImageSorter(
            Logger log,
            File sourceDir,
            File destinationDir,
            boolean separateBroken,
            boolean daySort,
            boolean OSCreateDateSort) {
        super(sourceDir, destinationDir, daySort);
        LOGGER = log;
        this.separateBroken = separateBroken;
        this.OSCreateDateSort = OSCreateDateSort;
    }

    /**
     * Run method for when an instance of this class, as a thread, is
     * called.
     */
    public void run() {
        sortImages();
    }

    /**
     * Sorts all the images in the source directory into dated folders
     * in the destination directory.
     */
    public void sortImages() {
        List<File> images = listImageFiles(sourceDir);
        File brokenImages = new File(destinationDir + "/Broken Images");
        if (!brokenImages.mkdirs()) {
            LOGGER.log(Level.WARNING, "Failed to make broken images folder, expect more errors!");
        }
        File looseAAEs = new File(destinationDir + "/Loose AAEs");
        if (!looseAAEs.mkdirs()) {
            LOGGER.log(Level.WARNING, "Failed to make loose AAEs folder, expect more errors!");
        }
        for (File image : images) {
            String date = getDateFromEXIF(image);
            if (date != null && !date.equals("null")) {
                datedImages.put(image.getName(), date);
                if (!checkDateFolderExists(date)) {
                    if (!makeDateFolder(date)) {
                        LOGGER.log(Level.WARNING, "Failed to make date folder", date);
                        continue;
                    }
                }
                try {
                    moveDatedFile(image.toPath(), date);
                } catch (FileAlreadyExistsException fEx) {
                    LOGGER.log(Level.WARNING, String.format("File already exists in %s folder",
                            date), image);
                } catch (IOException ioEx) {
                    LOGGER.log(Level.WARNING, "Failed to make date folder, IOException", date);
                }
            } else if (separateBroken) {
                datedImages.put(image.getName(), "null");
                try {
                    moveToFolder(image.toPath(), "Broken Images");
                } catch (IOException ioEx) {
                    LOGGER.log(Level.WARNING, "Failed to make folder, IOException", date);
                }
            } else {
                LOGGER.log(Level.WARNING, "File has no date data", image);
            }
        }

        // Deal with AAE files that should (ideally) be kept with
        // their corresponding JPG/PNG images
        // Seems like they used to be produced alongside PNGs, but now
        // everything seems to be exported as JPG
        List<File> aaeList = listAAEFiles(sourceDir);
        for (File aae : aaeList) {
            int fnlen = aae.getName().length();
            String pngKey = aae.getName().substring(0, fnlen - 4) + ".JPG";
            String jpgKey = aae.getName().substring(0, fnlen - 4) + ".PNG";

            try {
                if (datedImages.containsKey(jpgKey)) {
                    if (Objects.equals(datedImages.get(jpgKey), "null")) {
                        moveToFolder(aae.toPath(), "Broken Images");
                    } else {
                        moveDatedFile(aae.toPath(), datedImages.get(jpgKey));
                    }
                } else if (datedImages.containsKey(pngKey)) {
                    if (Objects.equals(datedImages.get(pngKey), "null")) {
                        moveToFolder(aae.toPath(), "Broken Images");
                    } else {
                        moveDatedFile(aae.toPath(), datedImages.get(pngKey));
                    }
                } else {
                    moveToFolder(aae.toPath(), "Loose AAEs");
                }
            } catch (FileAlreadyExistsException fEx) {
                LOGGER.log(Level.WARNING, "AAE already exists in folder", aae);
            } catch (IOException ioEx) {
                LOGGER.log(Level.WARNING, "Failed to make date folder, IOException");
            }
        }
    }

    /**
     * Checks through a list of all files in the folder and returns
     * only the image files within the list.
     * <p>
     * This uses the {@code listAllFiles} method to get the list of
     * all files initially.
     *
     * @param cwd the folder to scan, current working directory
     * @return the list of all images found
     */
    private static List<File> listImageFiles(File cwd) {
        List<File> returnImages = new ArrayList<>();
        List<File> thisFolderFileList = listAllFiles(cwd);
        for (File file : thisFolderFileList) {
            if (checkImageFile(file)) {
                returnImages.add(file);
            } else if (file.isDirectory()) {
                returnImages.addAll(listImageFiles(file));
            }
        }
        return returnImages;
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
     * Checks through a list of all files in the folder and returns
     * only the AAE files within the list.
     * <p>
     * This uses the {@code listAllFiles} method to get the list of
     * all files initially.
     *
     * @param cwd the folder to scan, current working directory
     * @return the list of all images found
     */
    private static List<File> listAAEFiles(File cwd) {
        List<File> returnImages = new ArrayList<>();
        List<File> thisFolderFileList = listAllFiles(cwd);
        for (File file : thisFolderFileList) {
            if (file.getName().toLowerCase().endsWith(".aae")) {
                returnImages.add(file);
            }
        }
        return returnImages;
    }

    /**
     * Returns the date an image was created, or null if the date
     * couldn't be retrieved.
     * <p>
     * If possible, gets the date that the image was originally taken.
     * If that's not available, use the date that the image was
     * created on the computer.
     * <p>
     * We always only take the first 10 characters before returning
     * because that leaves you with YYYY-MM-DD, without any time
     * information.
     * <p>
     * If something goes wrong reading an image and an error would be
     * thrown, null is returned instead of a date.
     *
     * @param file the image that we're reading EXIF data from
     * @return the taken/creation date of the image, or null if a
     * date could be retrieved
     */
    private String getDateFromEXIF(final File file) {
        try {
            final ImageMetadata metadata = Imaging.getMetadata(file);
            if (metadata instanceof JpegImageMetadata jpegMetadata) {
                if (jpegMetadata.getExif().getFieldValue(
                        ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL
                ) != null) {
                    return jpegMetadata.getExif().getFieldValue(
                            ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL
                    )[0].substring(0, 10);
                } else if (
                        jpegMetadata.getExif().getFieldValue(
                                ExifTagConstants.EXIF_TAG_DATE_TIME_DIGITIZED
                        ) != null) {
                    return jpegMetadata.getExif().getFieldValue(
                            ExifTagConstants.EXIF_TAG_DATE_TIME_DIGITIZED
                    )[0].substring(0, 10);
                }
            }
        } catch (IOException IOex) {
            // Failed to read image data
            LOGGER.log(Level.WARNING, "IOException reading metadata: " + file.getName(), IOex);
        } catch (ImageReadException imReadEx) {
            // Failed to read image metadata
            LOGGER.log(Level.WARNING, "Image read exception: " + file.getName());
            return null;
        } catch (IllegalArgumentException argEx) {
            LOGGER.log(Level.INFO, "Apache doesn't support image ext, using fallback");
        }
        if (OSCreateDateSort) {
            try {
                return getDate(file);
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "IOException reading file creation date", e);
                return null;
            }
        } else {
            return null;
        }
    }
}
