package RD2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StringHandler {
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

    public boolean hasChineseCharacter(String str) {
        for (char c : str.toCharArray()) {
            if (c >= 0x4E00 && c <= 0x9FFF)
                return true;
            else if (c >= 0x3400 && c <= 0x4DBF)
                return true;
        }
        return false;
    }

}
