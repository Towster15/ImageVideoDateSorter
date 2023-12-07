# Simple Image and Video Sorter

A simple program to move your images from one folder to another, sorting them by date in the 
process.

The program traverses through all sub-folders within the source directory, searching for images. 
This is mainly because I find it useful to be able to drop all of my many folders of images into 
one folder, before letting the program deal with sorting them all.

**Any non-image files are left alone and untouched. No information about any files are sent over 
the internet - none of this program uses any networking functionality and everything is 
processed locally.**

Within the output folder, a separate folder is created for each year, with each year folder 
containing a corresponding folder for each month. Optionally, these month folders can then have 
folders for each day.

Other options include:
- Sorting only videos or only images
- Moving videos to a separate videos folder
- Using the operating system's "Date Created/Date Modified" as a fallback for when the date 
  taken is not available from the image's metadata, such as in a saved screenshot
- Separating potentially broken images into a separate folder

This project has mainly been a learning exercise for me as I'm quite new to Java, so I apologise 
in advance if there's any issues or bugs that haven't been accounted for. If you find something, 
please create an issue and I'll to my best to help and fix the issue.

## Testing

I've tested the program on my own image library, using photos taken on various devices (both 
digital cameras and phones). By no means is this project perfect, with issues being listed below,
but generally things have been very successful.

## Caveats and Known Issues

### Speed of file moving

The program used to be single threaded, but now it uses multiple. I'm almost certain that my 
current implementation isn't perfect by any stretch, as the GUI is still unresponsive during the 
sort. I'll fix it one day, maybe.

Generally it feels like the sort is a tad faster, however, I'm still being limited by my drives 
hitting 100% utilisation rather than the program.

### Failing to move AAE files

If your input image set contains multiple images with the same name
that an AAE file could be matched with, the program seems to give up
and not bother moving it.

Similarly, if you have multiple AAE files that should have had the
same name, for example `IMG4004 (1).AAE` and `IMG4004 (2).AAE`, they
won't be moved either.

## TODOs

1. Implement a way for the user to be notified of broken images or AAE files that either
   couldn't be moved or are broken

## Dependencies

- [Apache Commons Imaging](https://commons.apache.org/proper/commons-imaging)
  - Used to read image metadata

### Why not use Maven or Gradle to make it easier for others?

I tried, got frustrated and gave up. Sorry.