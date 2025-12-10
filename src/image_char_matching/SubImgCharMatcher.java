package image_char_matching;

import java.util.*;

public class SubImgCharMatcher {
    private static final Map<Character, Double> savedCharBrightnessData = new HashMap<>();

    private final HashMap<Character, Double> charToBrightnessMap;
    private final TreeMap<Double, TreeSet<Character>> brightnessMap;
    private double maxBrightness = Double.NEGATIVE_INFINITY;
    private double minBrightness = Double.POSITIVE_INFINITY;

    public SubImgCharMatcher(char[] charset) {
        charToBrightnessMap = new HashMap<>();
        for (char c : charset) {
            double charBrightness = calculateCharBrightness(c);
            maxBrightness = Math.max(maxBrightness, charBrightness);
            minBrightness = Math.min(minBrightness, charBrightness);
            charToBrightnessMap.put(c, charBrightness);
        }
        brightnessMap = new TreeMap<>();
        updateBrightnessMap();
    }


    public char getCharByImageBrightness(double brightness) {

        if (brightnessMap.containsKey(brightness)) {
            return brightnessMap.get(brightness).first();
        } else {
            if (brightness < brightnessMap.firstKey()) {
                return brightnessMap.get(brightnessMap.firstKey()).first();
            } else if (brightness > brightnessMap.lastKey()) {
                return brightnessMap.get(brightnessMap.lastKey()).first();
            } else {
                Double lowerKey = brightnessMap.floorKey(brightness);
                Double higherKey = brightnessMap.ceilingKey(brightness);
                if ((brightness - lowerKey) <= (higherKey - brightness)) {
                    return brightnessMap.get(lowerKey).first();
                } else {
                    return brightnessMap.get(higherKey).first();
                }
            }
        }
    }

    public void addChar(char c) {
        // Check if character already exists
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
            // TODO: check if this is correct
            brightnessMap.get(normBrightness(charBrightness)).remove(c);
            return;
        }
        // so c->brightness is either max or min
        // Remove from charToBrightnessMap
        charToBrightnessMap.remove(c);
        minBrightness = Collections.min(charToBrightnessMap.values());
        maxBrightness = Collections.max(charToBrightnessMap.values());
        updateBrightnessMap();
    }


    private void updateBrightnessMap() {
        if(brightnessMap != null) {
            brightnessMap.clear();
        }
        for (Character c : charToBrightnessMap.keySet()) {
            double charBrightness = normBrightness(charToBrightnessMap.get(c));
            brightnessMap.computeIfAbsent(charBrightness, k -> new TreeSet<>()).add(c);
        }
    }

    private double calculateCharBrightness(char c) {
        if(savedCharBrightnessData.containsKey(c)) {
            return savedCharBrightnessData.get(c);
        }
        boolean[][] converted = CharConverter.convertToBoolArray(c);
        int counter = 0;
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {
                if (converted[i][j]) {
                    counter++;
                }
            }
        }
        double charBrightness = (double) counter / 256;
        savedCharBrightnessData.put(c, charBrightness);
        return charBrightness;


    }

    private double normBrightness(double var) {
        return (var - minBrightness) / (maxBrightness - minBrightness);

    }
}


