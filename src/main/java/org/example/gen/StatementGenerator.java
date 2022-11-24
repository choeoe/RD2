package org.example.gen;

import java.util.ArrayList;
import java.util.List;

public class StatementGenerator {
    public List<String> URIs = new ArrayList<>();
    public List<String> URIsP = new ArrayList<>();
    private static List<String> literalStrings = new ArrayList<>();
    private static List<Integer> literalIntegers = new ArrayList<>();
    private static List<String> literalDoubles = new ArrayList<>();
    private static List<Boolean> literalBooleans = new ArrayList<>();

    QueryGenerator queryGenerator = new QueryGenerator();

    public void setURIs(List<String> URIs) {
        this.URIs = URIs;
//        System.out.println(this.URIs);
        queryGenerator.setURIs(URIs);
    }

    public void setURIsP(List<String> URIsP) {
        this.URIsP = URIsP;
        queryGenerator.setURIsP(URIsP);
    }

    public void setLiteral(List<String> literalStrings, List<Integer> literalIntegers, List<String> literalDoubles, List<Boolean> literalBooleans) {
        this.literalStrings = literalStrings;
        this.literalIntegers = literalIntegers;
        this.literalDoubles = literalDoubles;
        this.literalBooleans = literalBooleans;
        queryGenerator.setLiteral(literalStrings, literalIntegers, literalDoubles, literalBooleans);
    }

    public String genStatement() {
        return queryGenerator.genQuery();
    }
}
