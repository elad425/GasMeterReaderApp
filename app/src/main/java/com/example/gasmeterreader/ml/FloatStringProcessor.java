package com.example.gasmeterreader.ml;
import android.util.Pair;

public class FloatStringProcessor {
    private static final int UPPER_BOUND_NORMAL = 30;
    private static final int LOWER_BOUND_NORMAL = 0;
    private static final int UPPER_BOUND_ERROR = 60;
    private static final int LOWER_BOUND_ERROR = -20;
    private static final int MAX_DOT_PLACE = 3;

    public static Pair<String, Integer> fixData(String input, String check) {
        if (input == null || check == null) {
            return new Pair<>("None", 0);
        }
        input = sanitizeInput(input);

        if (!input.contains(".")) {
            return handleNoDotCase(input, check);
        } else if (!hasMoreThanOneDot(input)) {
            return handleSingleDotCase(input, check);
        }
        return new Pair<>("None", 0);
    }

    private static String insertDot(String str, int positionFromRight) {
        if (str.length() <= positionFromRight) return str;
        return str.substring(0, str.length() - positionFromRight) + "." +
                str.substring(str.length() - positionFromRight);
    }

    private static boolean hasMoreThanOneDot(String str) {
        return str.indexOf('.') != str.lastIndexOf('.');
    }

    private static String sanitizeInput(String input) {
        input = input.replace("dot", ".");
        return input.replaceFirst("^0+", "0");
    }

    private static Pair<String, Integer> handleNoDotCase(String input, String check) {
        double checkValue;
        try {
            checkValue = Double.parseDouble(check);
        } catch (NumberFormatException e) {
            return new Pair<>("None", 0);
        }
        for (int i = MAX_DOT_PLACE; i >= 1; i--) {
            String modifiedInput = insertDot(input, i);

            try {
                double inputValue = Double.parseDouble(modifiedInput);
                if (isWithinRange(inputValue, checkValue, UPPER_BOUND_NORMAL, LOWER_BOUND_NORMAL)) {
                    return adjustResult(modifiedInput, i, 1);
                } else if (isWithinRange(inputValue, checkValue, UPPER_BOUND_ERROR, LOWER_BOUND_ERROR)) {
                    return adjustResult(modifiedInput, i, 0);
                }
            } catch (NumberFormatException ignored) {}
        }

        return new Pair<>("None", 0);
    }

    private static Pair<String, Integer> handleSingleDotCase(String input, String check) {
        double inputValue, checkValue;
        try {
            inputValue = Double.parseDouble(input);
            checkValue = Double.parseDouble(check);
        } catch (NumberFormatException e) {
            return new Pair<>("None", 0);
        }
        boolean hasExtraDigit = input.indexOf('.') == input.length() - 1 - MAX_DOT_PLACE;

        if (isWithinRange(inputValue, checkValue, UPPER_BOUND_NORMAL, LOWER_BOUND_NORMAL)) {
            return adjustResult(input, hasExtraDigit ? MAX_DOT_PLACE : -1, 1);
        } else if (isWithinRange(inputValue, checkValue, UPPER_BOUND_ERROR, LOWER_BOUND_ERROR)) {
            return adjustResult(input, hasExtraDigit ? MAX_DOT_PLACE : -1, 0);
        }
        return new Pair<>("None", 0);
    }

    private static boolean isWithinRange(double value, double target, double upperBound, double lowerBound) {
        return value >= target + lowerBound && value <= target + upperBound;
    }

    private static Pair<String, Integer> adjustResult(String input, int dotPosition, int status) {
        if (dotPosition == MAX_DOT_PLACE) {
            return new Pair<>(normalizeDecimal(input.substring(0, input.length() - 1)), status);
        }
        return new Pair<>(normalizeDecimal(input), status);
    }

    private static String normalizeDecimal(String input) {
        if (input.matches("0\\.[0-9]+")) {
            return input;
        } else if (input.matches("0+([1-9][0-9]*\\.?[0-9]*|\\.\\d+)")) {
            return input.replaceFirst("^0+(?=\\d)", "");
        }
        return input;
    }
}
