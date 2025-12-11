package ascii_art;

import ascii_output.AsciiOutput;
import image.Image;
import image_char_matching.SubImgCharMatcher;

import java.io.IOException;

/**
 * A shell for interacting with the ASCII art generator.
 * Supports commands for modifying the character set,
 * changing resolution, setting output methods, and generating ASCII art.
 * The shell reads commands from standard input and executes them accordingly.
 * It's the main driver of the program.
 *
 * @author Zohar Mattatia and Amit Tzur
 */
public class Shell {
    /* ================== Static Final Variables ================== */
    /* used for resolution calculation (doubling/halving) */
    private static final int BASE_FOR_RESOLUTION_CALCULATION = 2;
    /* minimal size for charset to run the algorithm */
    private static final int MINIMAL_CHARSET_SIZE = 2;
    /* ASCII printable characters range */
    private static final int MIN_ASCII_VALUE = 32;
    /* ASCII printable characters range */
    private static final int MAX_ASCII_VALUE = 126;
    /* minimal number of arguments for commands that require at least one parameter */
    private static final int MIN_ARGS_LENGTH = 2;
    /* length of add/remove range command */
    private static final int ADD_RANGE_COMMAND_LENGTH = 3;
    /* character used to parse add/remove range command */
    private static final char ADD_RANGE_COMMAND_PARSER_CHAR = '-';
    /* default character set */
    private static final char[] DEFAULT_CHARSET = "0123456789".toCharArray();

    /* ================== Static Final Defaults ================== */
    /* default font for HTML output */
    private static final String DEFAULT_FONT = "Courier New";
    /* default HTML output file name */
    private static final String DEFAULT_HTML_OUTPUT_FILE_NAME = "out.html";
    /* shell prompt prefix */
    private static final String SHELL_PREFIX = ">>> ";
    /* regex for splitting by spaces */
    private static final String SPACES_REGEX = "\\s+";

    /* ================== Static Final Strings - Commands ================== */
    private static final String CHARS_COMMAND = "chars";
    /* exit command */
    private static final String EXIT_COMMAND = "exit";
    /* add command */
    private static final String ADD_COMMAND = "add";
    /* remove command */
    private static final String REMOVE_COMMAND = "remove";
    /* resolution command */
    private static final String RES_COMMAND = "res";
    /* reverse command */
    private static final String REVERSE_COMMAND = "reverse";
    /* output command */
    private static final String OUTPUT_COMMAND = "output";
    /* ascii art command */
    private static final String ASCII_ART_COMMAND = "asciiArt";
    /* all characters command */
    private static final String ALL_CHARS_COMMAND = "all";
    /* up resolution command */
    private static final String RES_UP_COMMAND = "up";
    /* down resolution command */
    private static final String RES_DOWN_COMMAND = "down";
    /* output to console command */
    private static final String OUTPUT_CONSOLE_COMMAND = "console";
    /* output to HTML command */
    private static final String OUTPUT_HTML_COMMAND = "html";
    /* space character command */
    private static final String SPACE_COMMAND = "space";
    /* prompt for resolution setting */
    private static final String RESOLUTION_SETTING_PROMPT = "Resolution set to ";

    /* ================== Static Final Strings - Error Messages ================== */
    /* input image error message */
    private static final String INPUT_IMAGE_ERROR = "Error: could not read input image file ";
    /* incorrect command error message */
    private static final String INCORRECT_COMMAND_ERROR_MESSAGE =
            "Did not execute due to incorrect command.";
    /* add command incorrect format error message */
    private static final String ADD_COMMAND_INCORRECT_FORMAT_ERROR =
            "Did not add due to incorrect format.";
    /* remove command incorrect format error message */
    private static final String REMOVE_COMMAND_INCORRECT_FORMAT_ERROR = "Did not remove due to incorrect " +
            "format.";
    /* resolution exceeding boundaries error message */
    private static final String RESOLUTION_EXCEEDING_BOUNDARIES_ERROR = "Did not change resolution due to " +
            "exceeding boundaries.";
    /* resolution command incorrect format error message */
    private static final String RES_COMMAND_INCORRECT_FORMAT_ERROR = "Did not change resolution due to " +
            "incorrect format.";
    /* output method command incorrect format error message */
    private static final String OUTPUT_METHOD_COMMAND_FORMAT_ERROR = "Did not change output method due to " +
            "incorrect format.";
    /* charset too small error message */
    private static final String CHARSET_SMALL_SIZE_ERROR = "Did not execute. Charset is too small.";
    /* unexpected error message */
    private static final String UNEXPECTED_ERROR = "An unexpected error occurred: ";

    /* ================== Instance Private Variables ================== */
    /* current resolution, initially set to 2 by default */
    private int resolution = BASE_FOR_RESOLUTION_CALCULATION;
    /* maximum resolution boundary */
    private int maxResolution;
    /* minimum resolution boundary */
    private int minResolution;
    /* whether the brightness mapping is reversed */
    private boolean isReversed = false;
    /* character matcher for managing the character set */
    private final SubImgCharMatcher matcher;
    /* the image to be processed */
    private Image image;
    /* the output method for ASCII art */
    private AsciiOutput outputMethod = new ascii_output.ConsoleAsciiOutput();


    /**
     * Constructs a Shell instance with the default character set.
     * Initializes the SubImgCharMatcher with the default character set.
     *
     */
    public Shell() {
        matcher = new SubImgCharMatcher(DEFAULT_CHARSET);
    }

    /**
     * Runs the shell with the specified image file.
     * Reads commands from standard input and executes them accordingly.
     * Handles errors related to image file reading.
     *
     * @param imageName The name of the image file to be processed.
     */
    public void run(String imageName) {
        try {
            image = new Image(imageName);
        } catch (IOException e) {
            System.out.println(INPUT_IMAGE_ERROR + imageName);
            return;
        }

        maxResolution = image.getWidth();
        minResolution = Math.max(1, image.getWidth() / image.getHeight());

        System.out.print(SHELL_PREFIX);
        String currInstruction = KeyboardInput.readLine();

        while (true) {
            // Splitting the instruction by whitespace,
            // and trimming to avoid leading/trailing spaces becoming empty args in the array
            String[] args = currInstruction.trim().split(SPACES_REGEX);

            // making sure there's at least one argument (the command)
            if (args.length > 0 && !args[0].isEmpty()) {
                try {
                    String command = args[0];

                    switch (command) {
                        case ADD_COMMAND:
                            removeAndAddOperation(args, true);
                            break;
                        case REMOVE_COMMAND:
                            removeAndAddOperation(args, false);
                            break;
                        case CHARS_COMMAND:
                            printCharset();
                            break;
                        case RES_COMMAND:
                            setResolution(args);
                            break;
                        case REVERSE_COMMAND:
                            setReversed();
                            break;
                        case OUTPUT_COMMAND:
                            setOutputType(args);
                            break;
                        case ASCII_ART_COMMAND:
                            runAsciiArt();
                            break;
                        case EXIT_COMMAND:
                            // exit the shell
                            return;
                        default:
                            throw new UsageException(INCORRECT_COMMAND_ERROR_MESSAGE);
                    }
                } catch (UsageException ue) {
                    System.out.println(ue.getMessage());
                } catch (Exception e) {
                    System.out.println(UNEXPECTED_ERROR + e.getMessage());
                    return;
                }


            }

            System.out.print(SHELL_PREFIX);
            currInstruction = KeyboardInput.readLine();
        }
    }


    /**
     * Removes or adds characters to the character set based on the provided arguments.
     * Characters can be removed or added individually, as ranges, or using special keywords.
     * They should be in the range of ASCII values from 32 to 126.
     * Excepted formats:
     * - Single character (e.g., "a")
     * - Range of characters (e.g., "a-z" or "z-a")
     * - Special keywords: "all" to add/remove all ASCII characters,
     * "space" to add/remove the space character
     * This method has been updated to use executeCharOperation and executeCharRangeOperation helper methods,
     * which encapsulate the logic for handling single characters, ranges, and special keywords,
     * and call the updateMatcher method to perform the actual addition or removal.
     *
     * @param args  - The command arguments, where args[1] specifies the characters to add/remove.
     * @param isAdd - True if adding characters, false if removing characters.
     * @throws UsageException if the command format is incorrect.
     */
    private void removeAndAddOperation(String[] args, boolean isAdd) throws UsageException {
        // Determine the correct error message based on the operation
        String errorMessage;
        if (isAdd) {
            errorMessage = ADD_COMMAND_INCORRECT_FORMAT_ERROR;
        } else {
            errorMessage = REMOVE_COMMAND_INCORRECT_FORMAT_ERROR;
        }

        if (args.length < MIN_ARGS_LENGTH) {
            throw new UsageException(errorMessage);
        }

        // args[0] is the command itself
        String param = args[1];
        executeCharOperation(param, isAdd, errorMessage);


    }

    /**
     * Prints the current character set used for ASCII art generation.
     * The characters are printed in sorted order.
     */
    private void printCharset() {
        // using TreeSet to sort the characters - O(n log n) time complexity
        for (char c : new java.util.TreeSet<>(matcher.getCharSet())) {
            System.out.print(c + " ");
        }
        System.out.println();
    }

    /**
     * Sets the resolution of the ASCII art based on the provided arguments.
     * The resolution can be increased or decreased by a factor of 2,
     * within the defined minimum and maximum boundaries.
     *
     * @param args - The command arguments, where args[1] specifies "up" or "down".
     * @throws UsageException if the command format is incorrect or if the resolution exceeds boundaries.
     */
    private void setResolution(String[] args) throws UsageException {
        if (args.length < MIN_ARGS_LENGTH) {
            System.out.println(RESOLUTION_SETTING_PROMPT + resolution + ".");
            return;
        }
        // args[0] is the command itself, args[1] is "up" or "down"
        switch (args[1]) {
            // if up
            case RES_UP_COMMAND:
                if (resolution * BASE_FOR_RESOLUTION_CALCULATION > maxResolution) {
                    throw new UsageException(RESOLUTION_EXCEEDING_BOUNDARIES_ERROR);
                }
                resolution *= BASE_FOR_RESOLUTION_CALCULATION;
                System.out.println(RESOLUTION_SETTING_PROMPT + resolution + ".");

                break;

            // if down
            case RES_DOWN_COMMAND:
                if (resolution / BASE_FOR_RESOLUTION_CALCULATION < minResolution) {
                    throw new UsageException(RESOLUTION_EXCEEDING_BOUNDARIES_ERROR);
                }
                resolution /= BASE_FOR_RESOLUTION_CALCULATION;
                System.out.println(RESOLUTION_SETTING_PROMPT + resolution);
                break;

            default:
                throw new UsageException(RES_COMMAND_INCORRECT_FORMAT_ERROR);
        }
    }

    /**
     * Toggles the reversed state for brightness mapping.
     * If currently reversed, it will be set to normal, and vice versa.
     */
    private void setReversed() {
        isReversed = !isReversed;
    }

    /**
     * Sets the output method for the ASCII art based on the provided arguments.
     * Supported output methods are "console" and "html".
     *
     * @param args - The command arguments, where args[1] specifies the output method.
     * @throws UsageException if the command format is incorrect.
     */
    private void setOutputType(String[] args) throws UsageException {
        if (args.length < MIN_ARGS_LENGTH) {
            throw new UsageException(OUTPUT_METHOD_COMMAND_FORMAT_ERROR);
        }
        // args[0] is the command itself, args[1] is the output method
        switch (args[1]) {
            // if console output
            case OUTPUT_CONSOLE_COMMAND:
                outputMethod = new ascii_output.ConsoleAsciiOutput();
                break;
            // if html output
            case OUTPUT_HTML_COMMAND:
                outputMethod =
                        new ascii_output.HtmlAsciiOutput(DEFAULT_HTML_OUTPUT_FILE_NAME, DEFAULT_FONT);
                break;
            default:
                throw new UsageException(OUTPUT_METHOD_COMMAND_FORMAT_ERROR);
        }
    }

    /**
     * Runs the ASCII art generation algorithm and outputs the result
     * using the specified output method.
     * Checks if the character set is valid before execution.
     *
     * @throws UsageException if the character set is too small.
     */
    private void runAsciiArt() throws UsageException {
        if (matcher.getCharSet().size() < MINIMAL_CHARSET_SIZE || image == null) {
            throw new UsageException(CHARSET_SMALL_SIZE_ERROR);
        }

        // initialize the ascii art algorithm with the current settings
        AsciiArtAlgorithm algorithm = new AsciiArtAlgorithm(
                image,
                matcher.getCharSet(),
                resolution,
                isReversed
        );

        // run the algorithm to get the ASCII art
        char[][] asciiArt = algorithm.run();
        // render the output using the specified method
        outputMethod.out(asciiArt);
    }

    /**
     * Executes the add/remove operation for a single character or special keywords.
     *
     * @param param        The character parameter or special keyword.
     * @param isAdd        True to add characters, false to remove them.
     * @param errorMessage The error message to use in case of incorrect format.
     * @throws UsageException if the command format is incorrect.
     */
    private void executeCharOperation(String param,
                                      boolean isAdd,
                                      String errorMessage) throws UsageException {

        switch (param) {
            case ALL_CHARS_COMMAND:
                for (char c = MIN_ASCII_VALUE; c <= MAX_ASCII_VALUE; c++) {
                    updateMatcher(c, isAdd);
                }
                break;

            case SPACE_COMMAND:
                updateMatcher(' ', isAdd);
                break;

            // handle single character or range input
            default:
                // single Char
                if (param.length() == 1) {
                    char c = param.charAt(0);
                    // making sure it's in the valid ASCII range
                    if (c < MIN_ASCII_VALUE || c > MAX_ASCII_VALUE) {
                        throw new UsageException(errorMessage);
                    }
                    updateMatcher(c, isAdd);
                }

                // Handle Range (e.g., a-z), it's always in the format <char>-<char>
                else if (param.length() == ADD_RANGE_COMMAND_LENGTH &&
                        param.charAt(1) == ADD_RANGE_COMMAND_PARSER_CHAR) {
                    executeCharRangeOperation(param, isAdd);
                    // if invalid format
                } else {
                    throw new UsageException(errorMessage);
                }
        }

    }

    /**
     * Executes the add/remove operation for a range of characters.
     *
     * @param param The range parameter in the format "a-z" or "z-a".
     * @param isAdd True to add characters, false to remove them.
     */
    private void executeCharRangeOperation(String param, boolean isAdd) {
        char start = param.charAt(0);
        char end = param.charAt(ADD_RANGE_COMMAND_LENGTH - 1);

        // here handling reverse range case (e.g., z-a)
        if (start > end) {
            char temp = start;
            start = end;
            end = temp;
        }

        for (char c = start; c <= end; c++) {
            updateMatcher(c, isAdd);
        }
    }

    /**
     * Updates the character matcher by adding or removing a character.
     *
     * @param c     The character to add or remove.
     * @param isAdd True to add the character, false to remove it.
     */
    private void updateMatcher(char c, boolean isAdd) {
        if (isAdd) {
            matcher.addChar(c);
        } else {
            matcher.removeChar(c);
        }
    }

    /**
     * The main method to start the ASCII art shell.
     * Expects an image file name as a command-line argument.
     *
     * @param args Command-line arguments, where args[0] is the image file name.
     */
    public static void main(String[] args) {
        Shell shell = new Shell();
        if (args.length > 0) {
            shell.run(args[0]);
        } else {
            System.out.println(INPUT_IMAGE_ERROR);
        }
    }
}