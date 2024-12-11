package com.example.gasmeterreader.utils;

import java.util.HashMap;
import java.util.Map;

public class StringsUtils {
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
            return "not detected";
        } else{
            return mostFrequent;
        }
    }

    public static String fixID(String input) {
        int hyphenIndex = input.indexOf('e');
        if (hyphenIndex != -1) {
            return input.substring(hyphenIndex + 1);
        }
        return input;
    }
}
