package com.towster15.ImageVideoDateSorter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import static javax.swing.GroupLayout.Alignment.CENTER;

public class Main extends JFrame implements ActionListener, ItemListener, PropertyChangeListener {
    private final static String SELECT_SOURCE = "1";
    private final static String SELECT_DEST = "2";
    private final static String START = "3";
    private final static String CANCEL = "4";
    private final JCheckBox sortImageCheckBox;
    private final JCheckBox separateBrokenImagesCheckBox;
    private final JCheckBox moveAAEsCheckBox;
    private final JCheckBox sortAAEsCheckBox;
    private final JCheckBox moveVideoCheckBox;
    private final JCheckBox sortVideoCheckBox;
    private final JCheckBox daySortCheckBox;
    private final JCheckBox OSCreateDateSortCheckBox;
    private final JCheckBox exitAfterSortCheckBox;
    private final JButton startSortingButton;
    private final JButton cancelButton;
    private final JLabel sourceDirLabel = new JLabel("No folder selected!");
    private final JLabel destinationDirLabel = new JLabel("No folder selected!");
    private final JLabel showSortingDisabledReasonLabel = new JLabel(
            "Two valid directories required to start sorting."
    );
    private File sourceDir = new File("");
    private File destinationDir = new File("");
    private boolean sortImages = true;
    private boolean separateBrokenImages = false;
    private boolean moveAAEs = true;
    private boolean sortAAEs = true;
    private boolean moveVideos = false;
    private boolean sortVideos = false;
    private boolean daySort = false;
    private boolean OSCreateDateSort = false;
    private boolean exitAfterSort = false;
    private Worker worker = new Worker();
    private Instant startTime;
    private boolean sortCancelled = false;
    private static final Logger LOGGER = Logger.getLogger("com.towster15.ImageDateSorter");
    private static final FileHandler FH;

    static {
        try {
            FH = new FileHandler("log.txt");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates the JFrame window
     */
    public Main() {
        SimpleFormatter format = new SimpleFormatter();
        FH.setFormatter(format);
        LOGGER.addHandler(FH);
        final Font FONT = new Font("Tahoma", Font.PLAIN, 14);
        final Font TITLEFONT = new Font("Tahoma", Font.BOLD, 16);
        final Font STATUSFONT = new Font("Tahoma", Font.PLAIN, 12);

        JLabel titleLabel = new JLabel("Image Sorter thing");
        titleLabel.setFont(TITLEFONT);

        final JButton sourceDirectorySelectButton = new JButton("Select a source directory");
        sourceDirectorySelectButton.setFont(FONT);
        sourceDirectorySelectButton.setActionCommand(SELECT_SOURCE);
        sourceDirectorySelectButton.addActionListener(this);

        JLabel sourceSelectedDirLabel = new JLabel("Selected directory:");
        sourceSelectedDirLabel.setFont(FONT);

        final JButton destinationDirectorySelectButton = new JButton(
                "Select a destination directory"
        );
        destinationDirectorySelectButton.setFont(FONT);
        destinationDirectorySelectButton.setActionCommand(SELECT_DEST);
        destinationDirectorySelectButton.addActionListener(this);

        JLabel destinationDirectoryLabel = new JLabel("Selected directory:");
        destinationDirectoryLabel.setFont(FONT);

        JSeparator sep1 = new JSeparator(SwingConstants.HORIZONTAL);

        sortImageCheckBox = new JCheckBox("Sort Images");
        sortImageCheckBox.doClick();
        sortImageCheckBox.setFont(FONT);
        sortImageCheckBox.addItemListener(this);

        separateBrokenImagesCheckBox = new JCheckBox("Separate Broken Images");
        separateBrokenImagesCheckBox.setToolTipText("Images with unreadable data (likely broken) " +
                " will be moved to a separate folder.");
        separateBrokenImagesCheckBox.setFont(FONT);
        separateBrokenImagesCheckBox.addItemListener(this);

        moveAAEsCheckBox = new JCheckBox("Move AAEs");
        moveAAEsCheckBox.setToolTipText("AAE files are typically created when exporting edited " +
                "images from iOS devices to Windows and they contain the edits to the image. " +
                "Generally you want to keep them together, although you can't recombine them.");
        moveAAEsCheckBox.setEnabled(true);
        moveAAEsCheckBox.setFont(FONT);
        moveAAEsCheckBox.addItemListener(this);

        sortAAEsCheckBox = new JCheckBox("Sort AAEs");
        sortAAEsCheckBox.setToolTipText("Sorts the AAE files to be with their matching images " +
                "where possible. This will hinder speed as it forces images to be sorted in a " +
                "single-threaded manner.");
        sortAAEsCheckBox.setFont(FONT);
        sortAAEsCheckBox.addItemListener(this);

        moveVideoCheckBox = new JCheckBox("Move Videos");
        moveVideoCheckBox.setToolTipText("Moves the videos from the source folder to a videos " +
                "folder within the destination folder.");
        moveVideoCheckBox.setFont(FONT);
        moveVideoCheckBox.addItemListener(this);

        sortVideoCheckBox = new JCheckBox("Sort Videos");
        sortVideoCheckBox.setToolTipText("Sort the videos using the same fallback date sorting as" +
                " images. Prevents videos being stored separately to images.");
        sortVideoCheckBox.setEnabled(false);
        sortVideoCheckBox.setFont(FONT);
        sortVideoCheckBox.addItemListener(this);

        JSeparator sep2 = new JSeparator(SwingConstants.HORIZONTAL);

        daySortCheckBox = new JCheckBox("Day Sorting");
        daySortCheckBox.setToolTipText("Enable sorting by year-month-day, versus year-month.");
        daySortCheckBox.setFont(FONT);
        daySortCheckBox.addItemListener(this);

        OSCreateDateSortCheckBox = new JCheckBox("Fallback date sorting");
        OSCreateDateSortCheckBox.setToolTipText("If the date can't be collected from the EXIF " +
                "data, use the date created recorded by the OS to sort the image. It's not as " +
                "accurate as using EXIF data, but allows a much larger amount of" +
                " images to be sorted.");
        OSCreateDateSortCheckBox.setFont(FONT);
        OSCreateDateSortCheckBox.addItemListener(this);

        exitAfterSortCheckBox = new JCheckBox("Exit after sort");
        exitAfterSortCheckBox.setFont(FONT);
        exitAfterSortCheckBox.addItemListener(this);

        JSeparator sep3 = new JSeparator(SwingConstants.HORIZONTAL);

        JLabel statusLabel = new JLabel("Status:");
        statusLabel.setFont(FONT);

        showSortingDisabledReasonLabel.setFont(STATUSFONT);

        startSortingButton = new JButton("Start");
        startSortingButton.setFont(FONT);
        startSortingButton.setActionCommand(START);
        startSortingButton.addActionListener(this);
        startSortingButton.setEnabled(false);

        cancelButton = new JButton("Cancel");
        cancelButton.setFont(FONT);
        cancelButton.setEnabled(false);
        cancelButton.setActionCommand(CANCEL);
        cancelButton.addActionListener(this);

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        getRootPane().putClientProperty("Window.documentModified", false);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(
                layout.createSequentialGroup().addGroup(
                        layout.createParallelGroup(CENTER)
                                .addComponent(titleLabel)
                                .addComponent(sourceDirectorySelectButton, 200, 275, 275)
                                .addComponent(sourceSelectedDirLabel)
                                .addComponent(sourceDirLabel)
                                .addComponent(destinationDirectorySelectButton, 200, 275, 275)
                                .addComponent(destinationDirectoryLabel)
                                .addComponent(destinationDirLabel)
                                .addComponent(sep1, 200, 275, 275)
                                .addGroup(layout.createSequentialGroup()
                                        .addComponent(sortImageCheckBox)
                                        .addComponent(separateBrokenImagesCheckBox)
                                )
                                .addGroup(layout.createSequentialGroup()
                                        .addComponent(moveAAEsCheckBox)
                                        .addComponent(sortAAEsCheckBox)
                                )
                                .addGroup(layout.createSequentialGroup()
                                        .addComponent(moveVideoCheckBox)
                                        .addComponent(sortVideoCheckBox)
                                )
                                .addComponent(sep2, 200, 275, 275)
                                .addGroup(layout.createSequentialGroup()
                                        .addComponent(daySortCheckBox)
                                        .addComponent(OSCreateDateSortCheckBox)
                                )
                                .addComponent(exitAfterSortCheckBox)
                                .addComponent(sep3, 200, 275, 275)
                                .addComponent(statusLabel)
                                .addComponent(showSortingDisabledReasonLabel)
                                .addGroup(layout.createSequentialGroup()
                                        .addComponent(startSortingButton, 50, 75, 100)
                                        .addComponent(cancelButton, 50, 75, 100)
                                )
                )
        );

        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addComponent(titleLabel)
                        .addComponent(sourceDirectorySelectButton)
                        .addComponent(sourceSelectedDirLabel)
                        .addComponent(sourceDirLabel)
                        .addComponent(destinationDirectorySelectButton)
                        .addComponent(destinationDirectoryLabel)
                        .addComponent(destinationDirLabel)
                        .addComponent(sep1)
                        .addGroup(layout.createParallelGroup()
                                .addComponent(sortImageCheckBox)
                                .addComponent(separateBrokenImagesCheckBox)
                        )
                        .addGroup(layout.createParallelGroup()
                                .addComponent(sortAAEsCheckBox)
                                .addComponent(moveAAEsCheckBox)
                        )
                        .addGroup(layout.createParallelGroup()
                                .addComponent(moveVideoCheckBox)
                                .addComponent(sortVideoCheckBox)
                        )
                        .addComponent(sep2)
                        .addGroup(layout.createParallelGroup()
                                .addComponent(daySortCheckBox)
                                .addComponent(OSCreateDateSortCheckBox)
                        )
                        .addComponent(exitAfterSortCheckBox)
                        .addComponent(sep3)
                        .addComponent(statusLabel)
                        .addComponent(showSortingDisabledReasonLabel)
                        .addGroup(layout.createParallelGroup()
                                .addComponent(startSortingButton)
                                .addComponent(cancelButton)
                        )
        );

        setTitle("Image Sorter");
        setResizable(false);
        pack();
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    /**
     * Listens for a button to be pressed, then determines what button
     * was pressed and performs the appropriate actions for each
     * button.
     *
     * @param e the event to be processed
     */
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case SELECT_SOURCE:
                sourceDir = getDirectory();
                sourceDirLabel.setText(sourceDir.getAbsolutePath());
                checkReadyToSort();
                break;
            case SELECT_DEST:
                destinationDir = getDirectory();
                destinationDirLabel.setText(destinationDir.getAbsolutePath());
                checkReadyToSort();
                break;
            case START:
                startSortingButton.setEnabled(false);
                startTime = Instant.now();

                worker = new Worker(
                        LOGGER, sourceDir, destinationDir, sortImages, separateBrokenImages,
                        moveAAEs, sortAAEs, moveVideos, sortVideos, daySort, OSCreateDateSort
                );
                worker.addPropertyChangeListener(this);
                worker.execute();

                if (exitAfterSort) {
                    dispose();
                }
                break;
            case CANCEL:
                sortCancelled = true;
                worker.cancel(true);
                break;
        }
    }

    /**
     * Listens for the state change of the checkboxes and updates
     * their associated variables.
     *
     * @param e the event to be processed
     */
    public void itemStateChanged(ItemEvent e) {
        if (e.getItemSelectable() == sortImageCheckBox) {
            sortImages = !(e.getStateChange() == ItemEvent.DESELECTED);
            checkReadyToSort();
            separateBrokenImagesCheckBox.setEnabled(sortImages);
            moveAAEsCheckBox.setEnabled(sortImages);
            sortAAEsCheckBox.setEnabled(sortImages && moveAAEs);
        } else if (e.getItemSelectable() == separateBrokenImagesCheckBox) {
            separateBrokenImages = !(e.getStateChange() == ItemEvent.DESELECTED);
        } else if (e.getItemSelectable() == moveAAEsCheckBox) {
            moveAAEs = (e.getStateChange() == ItemEvent.SELECTED);
            sortAAEsCheckBox.setEnabled(sortImages && moveAAEs);
        } else if (e.getItemSelectable() == sortAAEsCheckBox) {
            sortAAEs = (e.getStateChange() == ItemEvent.SELECTED);
        } else if (e.getItemSelectable() == moveVideoCheckBox) {
            moveVideos = !(e.getStateChange() == ItemEvent.DESELECTED);
            checkReadyToSort();
            sortVideoCheckBox.setEnabled(moveVideos);
        } else if (e.getItemSelectable() == sortVideoCheckBox) {
            sortVideos = !(e.getStateChange() == ItemEvent.DESELECTED);
        } else if (e.getItemSelectable() == daySortCheckBox) {
            daySort = !(e.getStateChange() == ItemEvent.DESELECTED);
        } else if (e.getItemSelectable() == exitAfterSortCheckBox) {
            exitAfterSort = !(e.getStateChange() == ItemEvent.DESELECTED);
        } else if (e.getItemSelectable() == OSCreateDateSortCheckBox) {
            OSCreateDateSort = !(e.getStateChange() == ItemEvent.DESELECTED);
        }
    }

    /**
     * This method gets called when a bound property is changed.
     *
     * @param evt A PropertyChangeEvent object describing the event source
     *            and the property that has changed.
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("state".equals(evt.getPropertyName())) {
            switch (worker.getState()) {
                case SwingWorker.StateValue.PENDING:
                    showSortingDisabledReasonLabel.setText("Ready.");
                    getRootPane().putClientProperty("Window.documentModified", false);
                    cancelButton.setEnabled(false);
                    break;
                case SwingWorker.StateValue.STARTED:
                    showSortingDisabledReasonLabel.setText("Sorting in progress... please wait.");
                    getRootPane().putClientProperty("Window.documentModified", true);
                    cancelButton.setEnabled(true);
                    break;
                case SwingWorker.StateValue.DONE:
                    showSortingDisabledReasonLabel.setText("Ready.");
                    getRootPane().putClientProperty("Window.documentModified", false);
                    cancelButton.setEnabled(false);
                    startSortingButton.setEnabled(true);
                    Instant endTime = Instant.now();
                    JOptionPane.showMessageDialog(
                            this, String.format(
                                    "Finished in %s seconds!",
                                    humanReadableDuration(Duration.between(startTime, endTime))
                            ),
                            sortCancelled ? "Cancelled" : "Success!",
                            sortCancelled ? JOptionPane.WARNING_MESSAGE :
                                    JOptionPane.INFORMATION_MESSAGE
                    );
                    break;
            }
        }
    }

    /**
     * Returns a File object that is the directory chosen by the user.
     * The file may not exist, if the user closed the JFileChooser or
     * managed
     *
     * @return directory chosen from the JFileChooser
     */
    private File getDirectory() {
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = fc.showOpenDialog(Main.this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            return fc.getSelectedFile();
        } else {
            return new File("null");
        }
    }

    /**
     * Enables or disables the start button (along with a message to
     * show the user why it's disabled), preventing the user from
     * being able to start the sort without having both directories
     * defined or without having any tasks selected.
     */
    private void checkReadyToSort() {
        if (sourceDir.exists() && destinationDir.exists() && (sortImages || moveVideos)) {
            startSortingButton.setEnabled(true);
            showSortingDisabledReasonLabel.setText("Ready.");
        } else if (sourceDir.exists() && destinationDir.exists()) {
            startSortingButton.setEnabled(false);
            showSortingDisabledReasonLabel.setText(
                    "No tasks selected."
            );
        } else {
            startSortingButton.setEnabled(false);
            showSortingDisabledReasonLabel.setText(
                    "Two valid directories required to start sorting."
            );
        }
    }

    /**
     * Returns a string that represents the given duration in an
     * easy-to-read format.
     *
     * @param duration the duration to be converted into a readable
     *                 format
     * @return the string representing the duration
     */
    private static String humanReadableDuration(Duration duration) {
        return duration.toString()
                .substring(2)
                .replaceAll("(\\d[HMS])(?!$)", "$1 ")
                .toLowerCase();
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (UnsupportedLookAndFeelException e) {
            // handle exception
            LOGGER.log(Level.INFO, "Failed to get system look and feel: unsupported");
        } catch (ClassNotFoundException e) {
            // handle exception
            LOGGER.log(Level.INFO, "Failed to get system look and feel: class not found");
        } catch (InstantiationException e) {
            // handle exception
            LOGGER.log(Level.INFO, "Failed to get system look and feel: instantiation ex");
        } catch (IllegalAccessException e) {
            // handle exception
            LOGGER.log(Level.INFO, "Failed to get system look and feel: illegal argument");
        }

        SwingUtilities.invokeLater( new Runnable() {
            public void run() {
                new Main().setVisible(true);
            }
        });
    }
}