package com.towster15.ImageVideoDateSorter.Sorters;

import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.ExifTagConstants;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ImageSorter extends Sorter {
    private final Logger LOGGER;
    private final List<File> images;
    private final List<File> aaeList;
    private final boolean sortAAEs;
    private final boolean dealwithAAEs;
    private final boolean separateBroken;
    private final boolean OSCreateDateSort;
    private final HashMap<String, String> datedImages = new HashMap<>();

    /**
     * @param log              the logger to report events to
     * @param imageList        list of images to sort
     * @param destinationDir   destination directory file
     * @param separateBroken   boolean to enable or disable separating
     *                         broken images from the rest
     * @param daySort          boolean to enable or disable sorting by days
     * @param OSCreateDateSort boolean to enable or disable using the
     *                         OS's creation date, as a fallback opt.
     */
    public ImageSorter(
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
        this.dealwithAAEs = false;
        this.separateBroken = separateBroken;
        this.OSCreateDateSort = OSCreateDateSort;
        System.out.println("without");
    }

    /**
     * @param log              the logger to report events to
     * @param imageList        list of images to sort
     * @param destinationDir   destination directory file
     * @param separateBroken   boolean to enable or disable separating
     *                         broken images from the rest
     * @param sortAAEs         boolean for sorting the AAEs by date
     * @param daySort          boolean to enable or disable sorting by days
     * @param OSCreateDateSort boolean to enable or disable using the
     *                         OS's creation date, as a fallback opt.
     */
    public ImageSorter(
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
        this.dealwithAAEs = true;
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
        File brokenImages = new File(destinationDir + "/Broken Images");
        if (!(brokenImages.mkdirs() || Files.exists(brokenImages.toPath()))) {
            LOGGER.log(Level.WARNING, "Failed to make broken images folder, expect more errors!");
        }
        if (sortAAEs) {
            File looseAAEs = new File(destinationDir + "/Loose AAEs");
            if (!(looseAAEs.mkdirs() || Files.exists(looseAAEs.toPath()))) {
                LOGGER.log(Level.WARNING, "Failed to make loose AAEs folder, expect more errors!");
            }
        } else {
            File looseAAEs = new File(destinationDir + "/AAEs");
            if (!(looseAAEs.mkdirs() || Files.exists(looseAAEs.toPath()))) {
                LOGGER.log(Level.WARNING, "Failed to make AAEs folder, expect more errors!");
            }
        }
        for (File image : images) {
            String date = getDateFromEXIF(image);
            if (date != null && !date.equals("null")) {
                if (sortAAEs) {
                    datedImages.put(image.getName(), date);
                }
                if (!checkDateFolderExists(date)) {
                    if (!makeDateFolder(date)) {
                        LOGGER.log(Level.WARNING, "Failed to make date folder", date);
                        continue;
                    }
                }
                try {
                    sortDatedFile(image.toPath(), date);
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

        if (dealwithAAEs) {
            // Deal with AAE files that should (ideally) be kept with
            // their corresponding JPG/PNG images
            // Seems like they used to be produced alongside PNGs, but now
            // everything seems to be exported as JPG
            for (File aae : aaeList) {
                int fnlen = aae.getName().length();
                String jpgKey = aae.getName().substring(0, fnlen - 4) + ".JPG";
                String pngKey = aae.getName().substring(0, fnlen - 4) + ".PNG";

                try {
                    if (datedImages.containsKey(jpgKey)) {
                        if (Objects.equals(datedImages.get(jpgKey), "null")) {
                            moveToFolder(aae.toPath(), "Broken Images");
                        } else {
                            sortDatedFile(aae.toPath(), datedImages.get(jpgKey));
                        }
                    } else if (datedImages.containsKey(pngKey)) {
                        if (Objects.equals(datedImages.get(pngKey), "null")) {
                            moveToFolder(aae.toPath(), "Broken Images");
                        } else {
                            sortDatedFile(aae.toPath(), datedImages.get(pngKey));
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
            if (metadata instanceof JpegImageMetadata) {
                final JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;

                if (jpegMetadata.getExif().getFieldValue(
                        ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL
                ) != null) {
                    String dtOriginal = jpegMetadata.getExif().getFieldValue(
                            ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL
                    )[0].substring(0, 10);
                    boolean success = true;
                    try {
                        Integer.parseInt(dtOriginal.substring(0, 4));
                        Integer.parseInt(dtOriginal.substring(5, 7));
                        Integer.parseInt(dtOriginal.substring(8, 10));
                    } catch (NumberFormatException nfe) {
                        success = false;
                    }
                    if (success) {
                    return jpegMetadata.getExif().getFieldValue(
                            ExifTagConstants.EXIF_TAG_DATE_TIME_ORIGINAL
                    )[0].substring(0, 10);}
                } if (
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
        catch (NullPointerException npx) {
            LOGGER.log(Level.WARNING, "Null returned for EXIF data: " + file.getName());
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
