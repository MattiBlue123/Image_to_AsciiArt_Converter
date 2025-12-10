package ascii_art;

import ascii_output.AsciiOutput;
import image.Image;
import image_char_matching.SubImgCharMatcher;

import java.io.IOException;

public class Shell {
    private static final char[] DEFAULT_CHARSET = "0123456789".toCharArray();
    private static final String DEFAULT_FONT = "Courier New";

    private Image image;
    private final SubImgCharMatcher matcher;
    private int resolution = 2;
    private int maxResolution;
    private int minResolution;
    private boolean isReversed = false;
    private AsciiOutput outputMethod = new ascii_output.ConsoleAsciiOutput();


    public Shell() {
        matcher = new SubImgCharMatcher(DEFAULT_CHARSET);
    }

    public void run(String imageName) {
        try {
            image = new Image(imageName);
        } catch (IOException e) {
            System.err.println("Error: could not read input image file " + imageName);
            return;
        }

        maxResolution = image.getWidth();
        minResolution = Math.max(1, image.getWidth() / image.getHeight());

        System.out.print(">>> ");
        String currInstruction = KeyboardInput.readLine();

        // Check if input is not null and continue until "exit" command
        while (true) {
            // 1. Trim and Split the instruction by whitespace
            // This handles multiple spaces between words automatically
            String[] args = currInstruction.trim().split("\\s+");

            // 2. Check if the line is not empty
            if (args.length > 0 && !args[0].isEmpty()) {
                String command = args[0];

                switch (command) {
                    case "chars":
                        // Logic: Simply ignores args[1], args[2]... if they exist
                        printCharset();
                        break;
                    case "exit":
                        // Logic: Breaks the loop regardless of any extra text
                        return;
                    case "add":
                        addChars(args);
                        break;
                    case "remove":
                        removeChars(args);
                        break;
                    case "res":
                        setResolution(args);
                        break;
                    case "reverse":
                        setReversed();
                        break;
                    case "output":
                        setOutputType(args);
                        break;
                    case "asciiArt":
                        runAsciiArt();
                        break;
                    default:
                        System.out.println("Did not execute due to incorrect command.");
                }
            }

            System.out.print(">>> ");
            currInstruction = KeyboardInput.readLine();
        }
    }

    private void addChars(String[] args) {
        if (args.length < 2) {
            System.out.println("Did not add due to incorrect format.");
            return;
        }
        // args[0] is the "add" command itself
        String charsToAdd = args[1];

        switch (charsToAdd) {
            case "all": //
                for (char c = 32; c <= 126; c++) {
                    matcher.addChar(c);
                }
                break;

            case "space": //
                matcher.addChar(' ');
                break;

            default:
                // handle single char or range input
                if (charsToAdd.length() == 1) { //
                    matcher.addChar(charsToAdd.charAt(0));

                    // a range is ALWAYS in the format <char>-<char>
                } else if (charsToAdd.length() == 3 && charsToAdd.charAt(1) == '-') { //
                    char start = charsToAdd.charAt(0);
                    char end = charsToAdd.charAt(2);

                    // here handling reverse range case
                    if (start > end) { //
                        char temp = start;
                        start = end;
                        end = temp;
                    }

                    for (char c = start; c <= end; c++) {
                        matcher.addChar(c);
                    }

                } else {
                    System.out.println("Did not add due to incorrect format."); //
                }
        }
    }

    private void removeChars(String[] args) {
        // 1. Validate argument count
        if (args.length < 2) {
            System.out.println("Did not remove due to incorrect format.");
            return;
        }
        String param = args[1];

        switch (param) {
            case "all":
                for (char c = 32; c <= 126; c++) {
                    matcher.removeChar(c);
                }
                break;

            case "space":
                matcher.removeChar(' ');
                break;

            default:
                // Handle single char
                if (param.length() == 1) {
                    matcher.removeChar(param.charAt(0));
                }
                // Handle Range (e.g., a-z or z-a)
                else if (param.length() == 3 && param.charAt(1) == '-') {
                    char start = param.charAt(0);
                    char end = param.charAt(2);

                    // Handle reverse range (e.g., p-m)
                    if (start > end) {
                        char temp = start;
                        start = end;
                        end = temp;
                    }

                    for (char c = start; c <= end; c++) {
                        matcher.removeChar(c);
                    }
                } else {
                    System.out.println("Did not remove due to incorrect format.");
                }
        }
    }

    private void setResolution(String[] args) {
        if (args.length < 2) {
            System.out.println("Resolution set to " + resolution);
            return;
        }

        switch (args[1]) {
            case ("up"):
                if (resolution * 2 > maxResolution) {
                    System.out.println("Did not change resolution due to exceeding boundaries.");
                    return;
                }
                resolution *= 2;
                System.out.println("Resolution set to " + resolution);

                break;
            case ("down"):
                if (resolution / 2 < minResolution) {
                    System.out.println("Did not change resolution due to exceeding boundaries.");
                    return;
                }
                resolution /= 2;
                System.out.println("Resolution set to " + resolution);
                break;

            default:
                System.out.println("Did not change resolution due to incorrect format.");
        }
    }

    private void printCharset() {
        ;
        for (char c : new java.util.TreeSet<>(matcher.getCharSet())) {
            System.out.print(c + " ");
        }
        System.out.println();
    }

    private void setReversed() {
        isReversed = !isReversed;
    }

    private void setOutputType(String[] args) {
        if (args.length < 2) {
            System.out.println("Did not change output method due to incorrect format.");
            return;
        }
        switch (args[1]) {
            case ("console"):
                outputMethod = new ascii_output.ConsoleAsciiOutput();
                break;
            case ("html"):
                outputMethod = new ascii_output.HtmlAsciiOutput("out.html", DEFAULT_FONT);
                break;
            default:
                System.out.println("Did not change output method due to incorrect format.");
        }
    }

    private void runAsciiArt() {
        if (matcher.getCharSet().size() < 2 || image == null) {
            System.out.println("Did not execute. Charset is too small.");
            return;
        }

        AsciiArtAlgorithm algorithm = new AsciiArtAlgorithm(
                image,
                matcher.getCharSet(),
                resolution,
                isReversed
        );

        char[][] asciiArt = algorithm.run();
        if (outputMethod != null) {
            outputMethod.out(asciiArt);
        } else {
            System.out.println("Output method not set. Use the 'output' command to set it.");
        }
    }

    public static void main(String[] args) {
        Shell shell = new Shell();
        if (args.length > 0) {
            shell.run(args[0]);
        } else {
            System.out.println("Please provide an image file name as an argument.");
        }
    }
}