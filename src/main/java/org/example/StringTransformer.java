package org.example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StringTransformer {
    private List<String> ECHAR = new ArrayList<>(Arrays.asList("\\", "\"", "\'", "\t", "\b", "\n", "\r", "\f"));
    private List<String> ECHAR2 = new ArrayList<>(Arrays.asList("", "", "", "t", "b", "n", "r", "f"));

    public String stringTrans(String str) {
        StringBuilder sb = new StringBuilder(str);
        for (int i = 0; i < ECHAR.size(); i++) {
            String keyword = ECHAR.get(i);
            String before;
            if (keyword.equals("\\") || keyword.equals("\"") || keyword.equals("\'"))
                before = "\\" + keyword;
            else
                before = "\\" + ECHAR2.get(i);
            int index = sb.indexOf(keyword);
            while (index != -1) {
                sb.delete(index, index + keyword.length());
                sb.insert(index, before);
                index = sb.indexOf(keyword, index + before.length());
            }
        }
        return sb.toString();
    }

    public String doubleTrans(String str) {
        String res = "";
        if (str.equals("Infinity"))
            res = "\"INF\"^^xsd:double";
        else if (str.equals("-Infinity"))
            res = "\"-INF\"^^xsd:double";
        else
            res = str;
        return res;
    }
}
