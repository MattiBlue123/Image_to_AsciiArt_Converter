package ascii_art;

import image.Image;
import image.ImageProcessing;
import image_char_matching.SubImgCharMatcher;

import java.io.IOException;

public class AsciiArtAlgorithm {

    String inputImageFileName;
    char[] charset;
    int resolution;
    Image inputImage;
    char[][] outputCharArray;


    public AsciiArtAlgorithm(String inputImageFileName, char[] charset, int resolution) {
        this.inputImageFileName = inputImageFileName;
        this.charset = charset;
        this.resolution = resolution;
//        outputCharArray = new char[0][0];

    }


    public char[][] run() {
        try {
            inputImage = new Image(inputImageFileName);
        }

        catch (IOException e) {
            System.out.println("Error: could not read input image file "+inputImageFileName);
            throw new RuntimeException(e);
        }

        SubImgCharMatcher initialMatcher = new SubImgCharMatcher(this.charset);

        ImageProcessing processor = new ImageProcessing(inputImage);
        Image[][] subImagesArray = processor.getSubImages(resolution);
        double[][] subImagesBrightness = new double[subImagesArray.length * subImagesArray[0].length][1];

        for(int i=0; i<subImagesArray.length; i++) {
            for (int j=0; j<subImagesArray[0].length; j++) {
                subImagesBrightness[i*subImagesArray[0].length + j][0] =
                        processor.calcSubImageGreyPixel(subImagesArray[i][j]);
            }
        }

        for(int i=0; i<subImagesBrightness.length; i++) {
            for(int j=0; j<subImagesBrightness[0].length; j++) {
                outputCharArray[i][j] = initialMatcher.getCharByImageBrightness(subImagesBrightness[i][j]); // TODO!
        }








    }


    //    public static void main(String[] args) {
//        AsciiArtAlgorithm algorithm = new AsciiArtAlgorithm();
//        algorithm.run();
//    }
}