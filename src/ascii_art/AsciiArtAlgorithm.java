package ascii_art;

import image.Image;
import image.ImageProcessing;
import image_char_matching.SubImgCharMatcher;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

public class AsciiArtAlgorithm {

    private static int savedResolution;
    private static Image savedInputImage;
    private static double[][] savedBrightnessMatrix = null;
    private static SubImgCharMatcher savedCharMatcher;
    private static Set<Character> lastCharset = null;

    private final Image inputImage;
    private final Set<Character> charSet;
    private final int resolution;
    private final boolean isReversed;


    public AsciiArtAlgorithm(Image inputImage, Set<Character> charSet,
                             int resolution, boolean isReverse) {
        this.inputImage = inputImage;
        this.charSet = charSet;
        this.resolution = resolution;
        this.isReversed = isReverse;
    }

    public char[][] run() {
        // initialize the char matcher
        if (savedCharMatcher == null || !Objects.equals(lastCharset, this.charSet)) {
            char[] charArrayRepresentation = new char[this.charSet.size()];
            int i = 0;
            for (Character c : this.charSet) {
                charArrayRepresentation[i++] = c;
            }
            savedCharMatcher = new SubImgCharMatcher(charArrayRepresentation);
            lastCharset = Set.copyOf(this.charSet);
        }

        // initialize image processor
        if (this.inputImage != savedInputImage || this.resolution != savedResolution) {
            savedInputImage = this.inputImage;
            savedResolution = this.resolution;

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
                double brightness = savedBrightnessMatrix[i][j];
                if (isReversed) {
                    brightness = 1.0 - brightness;
                }
                outputCharArray[i][j] = savedCharMatcher.getCharByImageBrightness(brightness);
            }
        }


        return outputCharArray;
    }
}

















