package ascii_art;

/**
 * An exception indicating incorrect usage of the ASCII art program.
 * Thrown when a user provides invalid input or uses a command incorrectly.
 * This covers format errors, boundary violations, and unknown commands.
 *
 * @author Zohar Mattatia and Amit Tzur
 */
public class UsageException extends Exception {

    /**
     * Constructs a UsageException with the specified detail message.
     *
     * @param message The detail message.
     */
    public UsageException(String message) {
        super(message);
    }
}
