package ascii_art;

import image.Image;
import image.ImageProcessing;
import image_char_matching.SubImgCharMatcher;

import java.util.Objects;
import java.util.Set;

/**
 * This class implements the core algorithm for converting an image to ASCII art.
 * It processes the input image, divides it into sub-images based on the specified resolution,
 * calculates the brightness of each sub-image, and maps these brightness values to characters
 * from the provided character set.
 * The algorithm supports caching of intermediate results to optimize performance
 * when the same image and resolution are used multiple times.
 * It also allows for reversing the brightness mapping to create different visual effects.
 *
 * @author Zohar Mattatia and Amit Tzur
 */
public class AsciiArtAlgorithm {
    /* ================== Static Cached Variables ================== */
    /* the resolution that was used in the last run */
    private static int savedResolution;
    /* the input image that was used in the last run */
    private static Image savedInputImage;
    /* the brightness matrix that was calculated in the last run - based on the image and resolution */
    private static double[][] savedBrightnessMatrix = null;
    /* the character matcher that was used in the last run - based on the charset */
    private static SubImgCharMatcher savedCharMatcher;
    /* the charset that was used in the last run */
    private static Set<Character> lastCharset = null;

    /* ================== Instance Private Variables ================== */
    /* the current input image */
    private final Image inputImage;
    /* the current charset */
    private final Set<Character> charSet;
    /* the current resolution */
    private final int resolution;
    /* whether to reverse the brightness mapping */
    private final boolean isReversed;

    /**
     * Constructs an AsciiArtAlgorithm instance with the specified parameters.
     *
     * @param inputImage The input image to be converted to ASCII art.
     * @param charSet    The set of characters to be used for mapping brightness values.
     * @param resolution The resolution for dividing the image into sub-images.
     * @param isReverse  Whether to reverse the brightness mapping.
     */
    public AsciiArtAlgorithm(Image inputImage, Set<Character> charSet,
                             int resolution, boolean isReverse) {
        this.inputImage = inputImage;
        this.charSet = charSet;
        this.resolution = resolution;
        this.isReversed = isReverse;
    }

    /**
     * Runs the ASCII art conversion algorithm.
     * Processes the input image into sub-images, calculates their brightness,
     * and maps these brightness values to characters from the character set.
     * Utilizes caching to optimize performance for repeated runs with the same parameters.
     *
     * @return A 2D array of characters representing the ASCII art.
     */
    public char[][] run() {
        // initialize char matcher - if charset changed, recreate it
        initCharMatcher();
        // calculate brightness matrix - if image or resolution changed, recalculate it
        // otherwise, use cached one
        calculateBrightnessMatrix();
        // map brightness matrix to an ascii art char array
        return mapBrightnessMatrixToCharArray();
    }

    /**
     * A helper method that initializes the character matcher based on the current character set.
     * If the character set has changed since the last run, a new SubImgCharMatcher
     * is created and cached for future use.
     */
    private void initCharMatcher() {
        if (savedCharMatcher == null || !Objects.equals(lastCharset, this.charSet)) {
            char[] charArrayRepresentation = new char[this.charSet.size()];
            int i = 0;
            for (Character c : this.charSet) {
                charArrayRepresentation[i++] = c;
            }
            savedCharMatcher = new SubImgCharMatcher(charArrayRepresentation);
            // update "last charset" cache for next time
            lastCharset = Set.copyOf(this.charSet);
        }
    }

    /**
     * Calculates the brightness matrix for the current input image and resolution.
     * If the input image or resolution has changed since the last run,
     * the brightness matrix is recalculated and cached for future use.
     */
    private void calculateBrightnessMatrix() {
        // calculate brightness matrix - if image or resolution changed, recalculate it
        // checking if image or resolution changed
        // image: by reference (same object or not), since in a specific run we upload only once
        // resolution: by value
        if (this.inputImage != savedInputImage || this.resolution != savedResolution) {
            savedInputImage = this.inputImage;
            savedResolution = this.resolution;

            // initialize image processor
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
    }

    /**
     * Maps the saved brightness matrix to a 2D array of characters using the saved character matcher.
     * If the reverse flag is set, the brightness values are inverted before mapping.
     *
     * @return A 2D array of characters representing the ASCII art.
     */
    private char[][] mapBrightnessMatrixToCharArray() {
        // create output char array based on brightness matrix and char matcher
        char[][] outputCharArray = new char[savedBrightnessMatrix.length][savedBrightnessMatrix[0].length];
        // map brightness to chars
        for (int i = 0; i < savedBrightnessMatrix.length; i++) {
            for (int j = 0; j < savedBrightnessMatrix[0].length; j++) {
                double brightness = savedBrightnessMatrix[i][j];
                // reverse brightness if needed. All brightness values are in range [0.0,1.0],
                // so reversing is done by subtracting from 1.0, so it's well-defined.
                if (isReversed) {
                    brightness = 1.0 - brightness;
                }
                outputCharArray[i][j] = savedCharMatcher.getCharByImageBrightness(brightness);
            }
        }

        return outputCharArray;
    }
}

















