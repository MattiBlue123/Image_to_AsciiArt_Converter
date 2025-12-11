package image_char_matching;


import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Set;
import java.util.TreeSet;
import java.util.Collections;

/**
 * This class matches characters to brightness values based on their visual representation.
 * It calculates the brightness of each character in the provided charset and allows for
 * retrieving the character that best matches a given brightness value.
 *
 * @author Zohar Mattatia and Amit Tzur
 */
public class SubImgCharMatcher {
    /* ================== Private Static Final - Cached Data ================== */
    /* Cache for character brightness data to avoid redundant calculations */
    private static final Map<Character, Double> savedCharBrightnessData = new HashMap<>();
    /* ================== Private Static Final - Constant values ================== */
    /* Dimensions of the pixel grid for character representation */
    private static final int PIXELS_GRID_DIMENSIONS_FOR_CHARACTERS = 16;
    /* Total number of pixels in the grid */
    private static final int PIXELS_IN_GRID = 256;

    /* ================== Private Instance Variables ================== */
    /* Mapping of characters to their brightness values */
    private final HashMap<Character, Double> charToBrightnessMap;
    /* Mapping of normalized brightness values to sets of characters */
    private final TreeMap<Double, TreeSet<Character>> brightnessMap;
    /* Minimum and maximum brightness values among the characters */
    private double maxBrightness = Double.NEGATIVE_INFINITY;
    /* Minimum brightness value among the characters */
    private double minBrightness = Double.POSITIVE_INFINITY;

    /**
     * Constructs a SubImgCharMatcher with the specified character set.
     * Calculates the brightness of each character and initializes the char to brightness map,
     * Max value and Min value.
     * Initializes the brightness map according to normalized brightness values.
     *
     * @param charset The set of characters to be used for matching brightness values.
     */
    public SubImgCharMatcher(char[] charset) {
        charToBrightnessMap = new HashMap<>();
        for (char c : charset) {
            double charBrightness = calculateCharBrightness(c);
            // update min and max brightness values if needed
            maxBrightness = Math.max(maxBrightness, charBrightness);
            minBrightness = Math.min(minBrightness, charBrightness);
            // add to charToBrightnessMap which holds all characters and their ORIGINAL brightness values
            // (before normalization)
            charToBrightnessMap.put(c, charBrightness);
        }
        // create brightnessMap which maps according to normalized brightness values
        brightnessMap = new TreeMap<>();
        updateBrightnessMap();
    }

    /**
     * Retrieves the character that best matches the given brightness value.
     *
     * @param brightness The brightness value to match.
     * @return The character that best matches the given brightness value.
     */
    public char getCharByImageBrightness(double brightness) {

        // If the brightness value exists in the map, return the first character in the set.
        if (brightnessMap.containsKey(brightness)) {
            return brightnessMap.get(brightness).first();
        } else {
            // if the brightness value is outside the range of keys in the map,
            // return the character corresponding to the closest key.
            if (brightness < brightnessMap.firstKey()) {
                return brightnessMap.get(brightnessMap.firstKey()).first();

            } else if (brightness > brightnessMap.lastKey()) {
                return brightnessMap.get(brightnessMap.lastKey()).first();

            } else {
                // find the closest keys (lower and higher) to the given brightness value
                // floorKey method returns the closest key (a double) which is
                // less than or equal to the given value
                Double lowerKey = brightnessMap.floorKey(brightness);
                // same for ceilingKey method, but greater than or equal to the given value
                Double higherKey = brightnessMap.ceilingKey(brightness);
                // according to the instructions,
                // if absolut value is the same, return the lower brightness character
                if ((brightness - lowerKey) <= (higherKey - brightness)) {
                    return brightnessMap.get(lowerKey).first();
                } else {
                    return brightnessMap.get(higherKey).first();
                }
            }
        }
    }

    /**
     * Adds a character to the matcher, calculating its brightness.
     * Updates the brightness map if necessary and the charToBrightnessMap which holds all characters and
     * their brightness values (before normalization).
     *
     * @param c The character to be added.
     */
    public void addChar(char c) {
        // if character already exists - do nothing and exit the method
        if (charToBrightnessMap.containsKey(c)) {
            return;
        }

        // Calculate brightness
        double charBrightness = calculateCharBrightness(c);

        // Add to charToBrightnessMap - the data structure that holds all characters available
        // and their original brightness values
        charToBrightnessMap.put(c, charBrightness);

        // Check if we need to update min/max brightness. If so, update the entire brightness map.
        if (charBrightness > maxBrightness || charBrightness < minBrightness) { //full rebuild
            maxBrightness = Math.max(maxBrightness, charBrightness);
            minBrightness = Math.min(minBrightness, charBrightness);
            updateBrightnessMap();
            return;
        }

        // Else, just add to brightnessMap - after normalizing the brightness value
        // so that we can find it in the map
        charBrightness = normBrightness(charBrightness);
        // Add to brightnessMap
        brightnessMap.computeIfAbsent(charBrightness, k -> new TreeSet<>()).add(c);
    }

    /**
     * Removes a character from the matcher.
     * Updates the brightness map and min/max brightness values if necessary.
     *
     * @param c The character to be removed.
     */
    public void removeChar(char c) {
        // Check if character exists
        if (!charToBrightnessMap.containsKey(c)) {
            return;
        }
        // Get original brightness
        double charBrightness = charToBrightnessMap.get(c);

        if (charBrightness < maxBrightness && charBrightness > minBrightness) {
            // Remove from charToBrightnessMap
            charToBrightnessMap.remove(c);
            // remove from brightnessMap according to normalized brightness
            brightnessMap.get(normBrightness(charBrightness)).remove(c);
            return;
        }

        // Remove from charToBrightnessMap
        charToBrightnessMap.remove(c);
        // if the removed char was the last char in the charset, set min and max to infinity/-infinity
        // and clear brightnessMap completely
        if (charToBrightnessMap.isEmpty()) {
            brightnessMap.clear();
            minBrightness = Double.POSITIVE_INFINITY;
            maxBrightness = Double.NEGATIVE_INFINITY;
            return;
        }
        // Recalculate min and max brightness values - since that char was one of them
        minBrightness = Collections.min(charToBrightnessMap.values());
        maxBrightness = Collections.max(charToBrightnessMap.values());
        // Rebuild brightnessMap completely - all normalized brightness values have changed
        updateBrightnessMap();
    }

    /**
     * Retrieves the set of characters currently in the matcher.
     *
     * @return A set of characters in the matcher.
     */
    public Set<Character> getCharSet() {
        return charToBrightnessMap.keySet();
    }

    /**
     * Updates the brightness map based on the current character to brightness mapping.
     * This method is called when min or max brightness values change.
     */
    private void updateBrightnessMap() {
        if (brightnessMap != null) {
            brightnessMap.clear();
        }
        for (Character c : charToBrightnessMap.keySet()) {
            double charBrightness = normBrightness(charToBrightnessMap.get(c));
            if (brightnessMap != null) {
                brightnessMap.computeIfAbsent(charBrightness, k -> new TreeSet<>()).add(c);
            }
        }
    }

    /**
     * Calculates the brightness of a character based on its visual representation.
     * The brightness is defined as the ratio of "on" pixels to the total number of pixels
     * in a 16x16 representation of the character.
     *
     * @param c The character whose brightness is to be calculated.
     * @return The brightness value of the character.
     */
    private double calculateCharBrightness(char c) {
        if (savedCharBrightnessData.containsKey(c)) {
            return savedCharBrightnessData.get(c);
        }
        boolean[][] converted = CharConverter.convertToBoolArray(c);
        int counter = 0;
        for (int i = 0; i < PIXELS_GRID_DIMENSIONS_FOR_CHARACTERS; i++) {
            for (int j = 0; j < PIXELS_GRID_DIMENSIONS_FOR_CHARACTERS; j++) {
                if (converted[i][j]) {
                    counter++;
                }
            }
        }

        // normalize brightness to [0,1] by dividing by the number of pixels in the grid (16x16=256)
        double charBrightness = (double) counter / PIXELS_IN_GRID;
        savedCharBrightnessData.put(c, charBrightness);
        return charBrightness;
    }

    /**
     * Normalizes a brightness value to the range [0, 1] based on the current min and max brightness.
     * They are defined according to the characters currently in the charset.
     * This algorithm doesn't run for a charset with a single character
     * (in which case all brightness map to that char).
     * That means that the formula is well-defined (denominator is not zero).
     *
     * @param val The brightness value to normalize.
     * @return The normalized brightness value.
     */
    private double normBrightness(double val) {
        if (maxBrightness == minBrightness) {
            return 0.0;
        }
        return (val - minBrightness) / (maxBrightness - minBrightness);

    }
}


