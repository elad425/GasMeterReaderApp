package com.example.gasmeterreader.utils;

import java.util.HashMap;
import java.util.Map;

public class ResultUtils {
    public static void addString(String input, HashMap<String, Integer> dic) {
        if(input.length() > 3) {
            if (dic.containsKey(input)) {
                dic.put(input, dic.get(input) + 1);
            } else {
                dic.put(input, 1);
            }
        }
    }

    public static int getMaxCount(HashMap<String, Integer> dic) {
        int maxCount = 0;
        for (Map.Entry<String, Integer> entry : dic.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
            }
        }
        return maxCount;
    }

    public static String getMostFrequentString(HashMap<String, Integer> dic) {
        String mostFrequent = null;
        int maxCount = 0;
        for (Map.Entry<String, Integer> entry : dic.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                mostFrequent = entry.getKey();
            }
        }
        if(mostFrequent == null){
            return "מחפש..";
        } else{
            return mostFrequent;
        }
    }

    public static String insertDot(String str, int positionFromRight) {
        if (str.length() <= positionFromRight) return str;
        return str.substring(0, str.length() - positionFromRight) + "." + str.substring(str.length() - positionFromRight);
    }

    public static boolean hasMoreThanOneDot(String str) {
        int firstDot = str.indexOf('.');
        if (firstDot == -1) {
            return false;
        }
        int secondDot = str.indexOf('.', firstDot + 1);
        return secondDot != -1;
    }

    public static String fixData(String input, String check) {
        if (input == null) {
            return "";
        }
        input = input.replace("dot", ".");
        input = input.replaceFirst("^0+", "");

        if(!input.contains(".")) {
            double checkValue = Double.parseDouble(check);
            for (int i = 3; i >= 1; i--) {
                try {
                    String modifiedInput = insertDot(input, i);
                    try {
                        double inputValue = Double.parseDouble(modifiedInput);
                        if (checkInRange(inputValue, checkValue)) {
                            if (i == 3) {
                                return modifiedInput.substring(0, modifiedInput.length() - 1);
                            } else return modifiedInput;
                        }
                    } catch (NumberFormatException ignored) {
                    }
                } catch (NumberFormatException e) {
                    return "None";
                }
            }
        } else if(!hasMoreThanOneDot(input) ){
            try {
                double inputDouble = Double.parseDouble(input);
                double checkDouble = Double.parseDouble(check);
                if (checkInRange(inputDouble, checkDouble)) {
                    if (input.indexOf('.') == input.length() - 4){
                        return input.substring(0, input.length() - 1);
                    } else return input;
                }
            } catch (NumberFormatException e) {
                return "None";
            }
        }
        return "None";
    }

    public static boolean checkInRange(double inputDouble, double checkDouble){
        return inputDouble >= checkDouble && inputDouble - checkDouble <= 30;
    }

}
