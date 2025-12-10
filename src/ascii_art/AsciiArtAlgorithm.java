package ascii_art;

import ascii_output.HtmlAsciiOutput;
import image.Image;
import image.ImageProcessing;
import image_char_matching.SubImgCharMatcher;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

public class AsciiArtAlgorithm {

    private static String savedInputImageFileName = "";
    private static int savedResolution;
    private static Image savedInputImage;
    private static double[][] savedBrightnessMatrix = null;
    private static SubImgCharMatcher savedCharMatcher;
    private static char[] lastCharset = null;

    private final String inputImageFileName;
    private final char[] charset;

    private boolean isSameResolution = false;
    private boolean isSameImage = false;

    public AsciiArtAlgorithm(String inputImageFileName, char[] charset, int resolution) {
        this.inputImageFileName = inputImageFileName;
        this.charset = charset;
        if (AsciiArtAlgorithm.savedResolution == resolution) {
            isSameResolution = true;
        } else {
            AsciiArtAlgorithm.savedResolution = resolution;
        }

    }

    public char[][] run() {
        // try to open the input image file
        if (Objects.equals(AsciiArtAlgorithm.savedInputImageFileName, this.inputImageFileName)) {
            isSameImage = true;
        } else {
            AsciiArtAlgorithm.savedInputImageFileName = this.inputImageFileName;
            loadInputImage();
        }
        return processImage();
    }

    private void loadInputImage() {
        try {
            savedInputImage = new Image(inputImageFileName);
        } catch (IOException e) {
            System.err.println("Error: could not read input image file " + inputImageFileName);
            throw new RuntimeException(e);
        }
    }

    private char[][] processImage() {
        // initialize the char matcher
        if (savedCharMatcher == null || !Arrays.equals(lastCharset, this.charset)) {
            savedCharMatcher = new SubImgCharMatcher(this.charset);
        }

        // initialize image processor
        if (!isSameResolution || !isSameImage || savedBrightnessMatrix == null) {
            ImageProcessing processor = new ImageProcessing(savedInputImage);
            // get sub-images
            Image[][] subImagesArray = processor.getSubImages(savedResolution);

            // initialize output char array
            int rows = subImagesArray.length;
            int cols = subImagesArray[0].length;

            savedBrightnessMatrix = new double[rows][cols];
            // calculate brightness of each sub-image and map to char
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    savedBrightnessMatrix[i][j] = processor.calcSubImageGreyPixel(subImagesArray[i][j]);

                }
            }
        }
        char[][] outputCharArray = new char[savedBrightnessMatrix.length][savedBrightnessMatrix[0].length];

        for (int i = 0; i < savedBrightnessMatrix.length; i++) {
            for (int j = 0; j < savedBrightnessMatrix[0].length; j++) {
                outputCharArray[i][j] =
                        savedCharMatcher.getCharByImageBrightness(savedBrightnessMatrix[i][j]);
            }
        }
        lastCharset = Arrays.copyOf(this.charset, this.charset.length);
        return outputCharArray;
    }

}

















