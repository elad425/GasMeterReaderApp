package com.example.gasmeterreader.utils;

import java.util.HashMap;
import java.util.Map;

public class ResultUtils {
    public static void addString(String input, HashMap<String, Integer> dic) {
        if (input.length() > 3) {
            dic.merge(input, 1, Integer::sum);
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
}
