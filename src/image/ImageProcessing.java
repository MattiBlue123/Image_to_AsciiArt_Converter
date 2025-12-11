package image;

import java.awt.*;

/**
 * A class responsible for processing an image: padding it to power of two dimensions,
 * splitting it into sub-images, and calculating the brightness of sub-images.
 *
 * @author Zohar Mattatia and Amit Tzur
 */
public class ImageProcessing {
    /* ================== Private Static Final Values ================== */
    /* Weights for calculating grey pixel value */
    /* red color weight */
    private static final double RED_WEIGHT = 0.2126;
    /* green color weight */
    private static final double GREEN_WEIGHT = 0.7152;
    /* blue color weight */
    private static final double BLUE_WEIGHT = 0.0722;
    /* maximum RGB value */
    private static final int MAX_RGB = 255;
    /* base for padding calculations (power of two) */
    private static final int BASE_FOR_PADDING_CALCULATIONS = 2;

    /* ================== Private Instance Variables ================== */
    /* padded image width */
    private int paddedWidth;
    /* padded image height */
    private int paddedHeight;
    /* the padded image pixel array */
    private final Color[][] paddedImage;
    /* the original image */
    private final Image originalImage;

    /**
     * Constructs an ImageProcessing instance with the specified original image.
     * Pads the image to the nearest power of two dimensions.
     *
     * @param originalImage The original image to be processed.
     */
    public ImageProcessing(Image originalImage) {
        this.originalImage = originalImage;
        setPaddingToPowerOfTwo();
        this.paddedImage = new Color[paddedHeight][paddedWidth];
        padImage();
    }

    /**
     * Sets the padded dimensions to the nearest power of two greater than or equal to
     * the original image dimensions.
     */
    private void setPaddingToPowerOfTwo() {
        int imageHeight = originalImage.getHeight();
        int imageWidth = originalImage.getWidth();

        int paddedHeight = 1;
        while (paddedHeight < imageHeight) {
            paddedHeight *= BASE_FOR_PADDING_CALCULATIONS;
        }
        this.paddedHeight = paddedHeight;

        int paddedWidth = 1;
        while (paddedWidth < imageWidth) {
            paddedWidth *= BASE_FOR_PADDING_CALCULATIONS;
        }
        this.paddedWidth = paddedWidth;
    }

    /**
     * Pads the original image into the paddedImage array with white pixels.
     * The original image is centered within the padded dimensions.
     */
    private void padImage() {
        // Calculate offsets to center the image (Symmetric Padding)
        int paddingTopLeftY = (paddedHeight - originalImage.getHeight()) / BASE_FOR_PADDING_CALCULATIONS;
        int paddingTopLeftX = (paddedWidth - originalImage.getWidth()) / BASE_FOR_PADDING_CALCULATIONS;

        int paddingBottomRightY = paddingTopLeftY + originalImage.getHeight();
        int paddingBottomRightX = paddingTopLeftX + originalImage.getWidth();

        for (int row = 0; row < paddedHeight; row++) {
            for (int col = 0; col < paddedWidth; col++) {

                // Check if current position is within the original image bounds
                if (row >= paddingTopLeftY && row < paddingBottomRightY &&
                        col >= paddingTopLeftX && col < paddingBottomRightX) {
                    // Copy pixel from original image
                    // subtracting offsets because original image coordinates are relative to (0,0)
                    this.paddedImage[row][col] =
                            originalImage.getPixel(row - paddingTopLeftY, col - paddingTopLeftX);
                } else {

                    // Set padding pixels to white
                    this.paddedImage[row][col] = Color.WHITE;
                }
            }
        }
    }

    /**
     * Splits the padded image into square sub-images based on the specified resolution.
     *
     * @param resolution The number of sub-images along the width of the padded image.
     * @return A 2D array of sub-images.
     */
    public Image[][] getSubImages(int resolution) {
        // Calculate the size of each square sub-image (in pixels)
        int subImageSize = paddedWidth / resolution;

        // Calculate how many rows of sub-images we will have
        int rows = paddedHeight / subImageSize;
        int cols = resolution; // columns are defined by resolution

        Image[][] subImages = new Image[rows][cols];

        // Iterate through the grid of sub-images
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {

                // Create the pixel array for the single sub-image
                Color[][] subImagePixels = new Color[subImageSize][subImageSize];

                // Calculate where this sub-image starts in the big padded image
                int startY = i * subImageSize;
                int startX = j * subImageSize;

                //Copy the pixels from the big image to the small square
                for (int y = 0; y < subImageSize; y++) {
                    for (int x = 0; x < subImageSize; x++) {
                        subImagePixels[y][x] = paddedImage[startY + y][startX + x];
                    }
                }

                // Create a new Image object for this square and store it
                subImages[i][j] = new Image(subImagePixels, subImageSize, subImageSize);
            }
        }
        return subImages;
    }

    /**
     * Calculates the average grey pixel value of a sub-image.
     *
     * @param subImage The sub-image to calculate the grey pixel value for.
     * @return The average grey pixel value of the sub-image, normalized between 0.0 and 1.0.
     */
    public double calcSubImageGreyPixel(Image subImage) {
        int width = subImage.getWidth();
        int height = subImage.getHeight();
        double totalBrightness = 0.0;

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                Color pixelColor = subImage.getPixel(col, row);
                // Calculate grey value using the formula
                double greyPixel = (pixelColor.getRed() * RED_WEIGHT +
                        pixelColor.getGreen() * GREEN_WEIGHT +
                        pixelColor.getBlue() * BLUE_WEIGHT);
                totalBrightness += greyPixel;
            }
        }

        // Return average brightness
        return totalBrightness / (width * height * MAX_RGB);
    }
}