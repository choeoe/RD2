package org.example.gen;

import org.example.Randomly;
import org.example.StringTransformer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class QueryGenerator {
    private final int maxPrologueNum = 2;
    private final int maxPropertyListPathNotEmptyNum = 2;
    private final int maxGPNTDepth = 3;

    private Randomly rand = new Randomly();
    private StringTransformer strTrans = new StringTransformer();
    public ExpressionGenerator exprGenerator = new ExpressionGenerator();

    public List<String> URIs = new ArrayList<>();
    public List<String> URIsP = new ArrayList<>();
    private static List<String> literalStrings = new ArrayList<>();
    private static List<Integer> literalIntegers = new ArrayList<>();
    private static List<String> literalDoubles = new ArrayList<>();
    private static List<Boolean> literalBooleans = new ArrayList<>();

    public int varIndex = 1;
    public List<String> vars = new ArrayList<>();

    private int gpntDepth = 0;

    private static String regex = "^[\u4e00-\u9fa5]";

//    private final String NUMBER = "0123456789";
//    private final String PN_CHARS_BASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
//    private final String PN_CHARS_U = PN_CHARS_BASE + "_";
//    private final String PN_CHARS = PN_CHARS_U + "-" + NUMBER;

    public void setURIs(List<String> URIs) {
        this.URIs = URIs;
//        System.out.println(this.URIs);
        exprGenerator.setURIs(URIs);
    }

    public void setURIsP(List<String> URIsP) {
        this.URIsP = URIsP;
        exprGenerator.setURIsP(URIsP);
    }

    public void setLiteral(List<String> literalStrings, List<Integer> literalIntegers, List<String> literalDoubles, List<Boolean> literalBooleans) {
        this.literalStrings = literalStrings;
        this.literalIntegers = literalIntegers;
        this.literalDoubles = literalDoubles;
        this.literalBooleans = literalBooleans;
        exprGenerator.setLiteral(literalStrings, literalIntegers, literalDoubles, literalBooleans);
    }

/*
PREFIX xsd
SELECT (DISTINCT)?
WHERE{
        TriplesBlock
        UNION
        OPTIONAL
        FILTER
//      MINUS
     }
//GROUP BY
//HAVING
ORDER BY
LIMIT
OFFSET
 */
    public String genQuery() {
        varIndex = 1;
        vars.clear();
        String query = genPrologue() + genSelectQuery();
        return query;
    }

    public String genPrologue() {
        String prologue = "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n";
        return prologue;
//        int num = rand.getInteger(0,maxPrologueNum);
//        String prologue = "";
//        return prologue;
    }

    public String genSelectQuery() {
        String selectQuery = genWhereClause() + genSolutionModifier();
        selectQuery = genSelectClause() + selectQuery;
        return selectQuery;
    }

    public String genSelectClause() {
        String distinct = "";
        if (rand.getBoolean())
            distinct = "DISTINCT ";
        if (!vars.isEmpty()) {
            List<String> selectedVars = Randomly.nonEmptySubset(vars);
            String selectClause = "SELECT " + distinct + String.join(" ", selectedVars) + "\n";
            return selectClause;
        } else {
            return "SELECT " + distinct + "*\n";
        }
    }

    public String genWhereClause() {
        String whereClause = "WHERE " + genGroupGraphPattern(false);
        return whereClause;
    }

    public String genGroupGraphPattern(boolean onlyTriplesBlock) {
        String str = "{ " + genGroupGraphPatternSub(onlyTriplesBlock) + " }\n";
        return str;
    }

    public String genGroupGraphPatternSub(boolean onlyTriplesBlock) {
        String str = "";
        str += genTriplesBlock();
//       if (gpntDepth >= maxGPNTDepth)
//            return str;
        if (!onlyTriplesBlock)
            str += genGraphPatternNotTriples();

//        int num = rand.getInteger(0, 5);
//        for (int i = 0; i < num; i++) {
//            gpntDepth++;
//            str += genGraphPatternNotTriples();
//            gpntDepth--;
//            if (rand.getBoolean())
//                str += genTriplesBlock();
//        }

        return str;
    }

    public String genTriplesBlock() {
        String str = genTriplesSameSubjectPath();
        if (rand.getBoolean()) {
            str += " .\n";
            if (rand.getBoolean()) {
                str += genTriplesBlock();
            }
        } else
            str += "\n";
        return str;
    }

    public String genTriplesSameSubjectPath() {
        String str = genVarOrResource() + " " + genPropertyListPathNotEmpty();
        return str;
    }

    public String genGraphPatternNotTriples() {
        String str = "";
        List<String> patterns = new ArrayList<>(Arrays.asList("GroupOrUnion", "OPTIONAL", "FILTER"));
        for (String pattern : patterns) {
            if (rand.getBoolean())
                continue;
            switch (pattern) {
                case "GroupOrUnion":
                    str += genGroupOrUnion();
                    break;
                case "OPTIONAL":
                    str += genOptional();
                    break;
                case "FILTER":
                    str += genFilter();
                    break;
            }
        }
        return str;
    }

    public String genGroupOrUnion() {
        String str = genGroupGraphPattern(true);
        int num = rand.getInteger(0, 2);
        for (int i = 0; i < num; i++) {
            str += "UNION\n" + genGroupGraphPattern(true);
        }
        return str;
    }

    public String genOptional() {
        String str = "OPTIONAL ";
        str += genGroupGraphPattern(true);
        return str;
    }

    public String genFilter() {
        String str = "FILTER ";
        str += "( " + exprGenerator.generateExpression() + " )" + "\n";
        return str;
    }

    public String genVarOrResource() {
        if (rand.getBoolean()) {
            return genVar();
        } else {
            String opName = Randomly.fromList(Arrays.asList("genIRI"));
            switch (opName) {
                case "genIRI":
                    return "<" + Randomly.fromList(URIs) + ">";
            }
        }
        return "";
    }

    public String genVarOrTerm() {
        if (rand.getBoolean()) {
            return genVar();
        } else {
            return genGraphTerm();
        }
    }

    public String genVar() {
        String str = "";
        if (rand.getBoolean()) {
            str = genVAR1();
        } else {
            str = genVAR2();
        }
        vars.add(str);
        return str;
    }

    public String genVAR1() {
        return "?" + genVARNAME();
    }

    public String genVAR2() {
        return "$" + genVARNAME();
    }

    public String genVARNAME() {
        String str = "var" + varIndex;
        varIndex++;
        return str;
    }

    public String genGraphTerm() {
        String opName = Randomly.fromList(Arrays.asList(
                "genIRI", "genRDFLiteral", "genIntegerLiteral", "genDoubleLiteral", "genBooleanLiteral"));
        switch (opName) {
            case "genIRI":
                return "<" + Randomly.fromList(URIs) + ">";
            case "genRDFLiteral":
//                if (rand.getBoolean()) {
                if (!literalStrings.isEmpty()) {
                    String str = Randomly.fromList(literalStrings);
                    //str = strTrans.stringTrans(str);
                    return "\"" + str + "\"";
                } else {
                    String str = rand.getString();
                    while (str.matches(regex))
                        str = rand.getString();
                    str = strTrans.stringTrans(str);
                    return "\"" + str + "\"";
                }

            case "genIntegerLiteral":
//                if (rand.getBoolean()) {
                if (!literalIntegers.isEmpty())
                    return "" + Randomly.fromList(literalIntegers);
                else
                    return Long.toString(rand.getInteger());
            case "genDoubleLiteral":
//                if (rand.getBoolean()) {
                if (!literalDoubles.isEmpty())
                    return "" + Randomly.fromList(literalDoubles);
                else {
                    String doubleLiteral = Double.toString(rand.getDouble());
                    doubleLiteral = strTrans.doubleTrans(doubleLiteral);
                    return doubleLiteral;
                }
            case "genBooleanLiteral":
                return Boolean.toString(rand.getBoolean());
//            case "genBlankNode":
//                return Randomly.fromList(blankNodesId);
//            case "genNIL":
        }
        return "";
    }

    public String genPropertyListPathNotEmpty() {
        String str = "";
        int num = rand.getInteger(1, maxPropertyListPathNotEmptyNum);
        for (int i = 0; i < num; i++) {
            if (i != 0)
                str += "; ";
            String op = Randomly.fromOptions("VAR", "IRI");
            switch (op) {
                case "VAR":
                    str += genVar();
                    break;
                case "IRI":
                    str += "<" + Randomly.fromList(URIsP) + ">";
                    break;
                case "A":
                    str += "a";
                    break;
            }
            str += " " + genObjectListPath();
        }
        return str;
    }

    public String genObjectListPath() {
        if (rand.getBoolean()) {
            return genVarOrTerm();
        } else {
            return genTriplesNodePath();
        }
    }

    public String genTriplesNodePath() {
        if (rand.getBoolean()) {
            return genVarOrTerm();
        } else {
            return genBlankNodePropertyListPath();
        }
    }

    public String genBlankNodePropertyListPath() {
        return "[ " + genPropertyListPathNotEmpty() + " ]";
    }

    public String genSolutionModifier() {
        String str = "";
        List<String> sms = new ArrayList<>(Arrays.asList("Group", "Having", "Order", "LimitOffset"));
        for (String sm : sms) {
            if (rand.getBoolean())
                continue;
            switch (sm) {
                case "Group":
                    str += genGroupClause();
                    break;
                case "Having":
                    str += genHavingClause();
                    break;
                case "Order":
                    str += genOrderClause();
                    break;
                case "LimitOffset":
                    str += genLimitOffsetClauses();
                    break;
            }
        }
        return str;
    }

    public String genGroupClause() {
        String str = "";
        return str;
    }

    public String genHavingClause() {
        String str = "";
        return str;
    }

    public String genOrderClause() {
        String str = "";
        if (vars.isEmpty()) {
            return str;
        }
        str = "ORDER BY ";
        String order = Randomly.fromList(Arrays.asList("ASC", "DESC"));
        str += order + " ";
        String var = Randomly.fromList(vars);
        str += "(" + var + ")\n";
        return str;
    }

    public String genLimitOffsetClauses() {
        String str = "";
        List<String> sms = new ArrayList<>(Arrays.asList("Limit", "Offset"));
        for (String sm : sms) {
            if (rand.getBoolean())
                continue;
            switch (sm) {
                case "Limit":
                    str += "LIMIT " + rand.getInteger(1, 51) + "\n";
                    break;
                case "Offset":
                    str += "OFFSET " + rand.getInteger(1, 6) + "\n";
                    break;
            }
        }
        return str;
    }

}
