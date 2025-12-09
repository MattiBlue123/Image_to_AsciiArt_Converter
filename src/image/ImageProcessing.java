package image;

import java.awt.*;

public class ImageProcessing {
    private static final double RED_WEIGHT = 0.2126;
    private static final double GREEN_WEIGHT = 0.7152;
    private static final double BLUE_WEIGHT = 0.0722;
    private static final int MAX_RGB = 255;

    private final Color[][] paddedImage;
    private int paddedWidth;
    private int paddedHeight;
    private final Image originalImage;

    public ImageProcessing(Image originalImage) {
        this.originalImage = originalImage;
        setPaddingToPowerOfTwo();
        this.paddedImage = new Color[paddedHeight][paddedWidth];
        padImage();
    }

    private void setPaddingToPowerOfTwo() {
        int imageHeight = originalImage.getHeight();
        int imageWidth = originalImage.getWidth();

        int paddedHeight = 1;
        while (paddedHeight < imageHeight) {
            paddedHeight *= 2;
        }
        this.paddedHeight = paddedHeight;

        int paddedWidth = 1;
        while (paddedWidth < imageWidth) {
            paddedWidth *= 2;
        }
        this.paddedWidth = paddedWidth;
    }
    
    private void padImage() {
        //Calculate offsets to center the image (Symmetric Padding)
        int paddingTopLeftY = (paddedHeight - originalImage.getHeight()) / 2;
        int paddingTopLeftX = (paddedWidth - originalImage.getWidth()) / 2;

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

    public double calcSubImageGreyPixel(Image subImage) {
        int width = subImage.getWidth();
        int height = subImage.getHeight();
        double totalBrightness = 0.0;

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                Color pixelColor = subImage.getPixel(col,row);
                // Calculate grey value using the formula
                double greyPixel =   (pixelColor.getRed()*RED_WEIGHT +
                                        pixelColor.getGreen()*GREEN_WEIGHT +
                                        pixelColor.getBlue()*BLUE_WEIGHT);
                totalBrightness += greyPixel;
            }

        }

        // Return average brightness
        return totalBrightness / (width * height* MAX_RGB);
    }


}