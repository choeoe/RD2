package RD2.gen;

import RD2.DataType;
import RD2.Randomly;
import RD2.StringHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ExpressionGenerator {
    private int maxExpressionDepth = 5;
    private int size = 10;

    private List<DataType> dataTypeList = new ArrayList<>(Arrays.asList(DataType.values()));

    public List<String> URIs = new ArrayList<>();
    public List<String> URIsP = new ArrayList<>();
    private static List<String> literalStrings = new ArrayList<>();
    private static List<Integer> literalIntegers = new ArrayList<>();
    private static List<String> literalDoubles = new ArrayList<>();
    private static List<Boolean> literalBooleans = new ArrayList<>();

//    private static String regex = "^[\u4e00-\u9fa5]";

    private Randomly rand = new Randomly();
    private StringHandler strHandler = new StringHandler();

    public void setURIs(List<String> URIs) {
        this.URIs = URIs;
//        System.out.println(this.URIs);
    }

    public void setURIsP(List<String> URIsP) {
        this.URIsP = URIsP;
    }

    public void setLiteral(List<String> literalStrings, List<Integer> literalIntegers, List<String> literalDoubles, List<Boolean> literalBooleans) {
        this.literalStrings = literalStrings;
//        System.out.println(literalStrings);
        this.literalIntegers = literalIntegers;
//        System.out.println(literalIntegers);
        this.literalDoubles = literalDoubles;
//        System.out.println(literalDoubles);
        this.literalBooleans = literalBooleans;
    }

    public String generateExpression() {
        DataType dataType;
        dataType = Randomly.fromList(DataType.ebvList());
        return genExpr(dataType);
    }

    public String genExpr(DataType dataType) {
        return genExpr(0, dataType);
    }


    public String genExpr(int depth, DataType originalDataType) {
        return "(" + genExprInternal(depth, originalDataType) + ")";
    }

    public String genExprInternal(int depth, DataType dataType) {
        if (depth > maxExpressionDepth) {
            switch (dataType) {
                case INTEGER:
                    if (!literalIntegers.isEmpty() && Randomly.getBoolean()) {
                        return "" + Randomly.fromList(literalIntegers);
                    }
                    break;
                case DOUBLE:
                    if (!literalDoubles.isEmpty() && Randomly.getBoolean()) {
                        return "" + Randomly.fromList(literalDoubles);
                    }
                    break;
                case BOOLEAN:
                    if (!literalBooleans.isEmpty() && Randomly.getBoolean()) {
                        return "" + Randomly.fromList(literalBooleans);
                    }
                    break;
                case STRING:
                    if (!literalStrings.isEmpty() && Randomly.getBoolean()) {
//                        System.out.println("---literal---");
                        String str = Randomly.fromList(literalStrings);
                        //str = strTrans.stringTrans(str);
                        return "\"" + str + "\"";
                    }
                    break;
            }
            return getRandomVal(dataType);
        }
        switch (dataType) {
            case INTEGER:
                return genIntExpr(depth);
            case DOUBLE:
                return genDoubleExpr(depth);
            case BOOLEAN:
                return genBooleanExpr(depth);
            case STRING:
                return genStringExpr(depth);
        }
        return "";
    }

    private String genBooleanExpr(int depth) {
        String expr, operator;
        DataType dataType;
        String op = Randomly.fromOptions("NOT", "AND_OR", "COMP");
        switch (op) {
            case "NOT":
                expr = "!" + genExpr(depth + 1, DataType.BOOLEAN);
                break;
            case "AND_OR":
                //?????????
                StringBuilder first = new StringBuilder(genExpr(depth + 1, DataType.BOOLEAN));
//                int nr = Randomly.smallNumber() + 1;
//                for (int i = 0; i < nr; i++) {
                operator = Randomly.fromOptions("&&", "||");
                first = new StringBuilder("(" + first + ") " + operator + " "
                        + genExpr(depth + 1, DataType.BOOLEAN));
//                }
                expr = first.toString();
                break;
            case "COMP":
                //dataType = getMeaningfulType();   //?????
                dataType = Randomly.fromList(dataTypeList);
                operator = Randomly.fromOptions("=", "!=", "<", "<=", ">", ">=");
                expr = genExpr(depth + 1, dataType) + " " + operator + " " + genExpr(depth + 1, dataType);
                break;
            default:
                throw new AssertionError();
        }
        return expr;
    }

    public String genIntExpr(int depth) {
        String expr, operator;
        String op = Randomly.fromOptions("UNARY", "BINARY");
        switch (op) {
            case "UNARY":
                operator = Randomly.fromOptions("-", "+");
                expr = operator + genExpr(depth + 1, DataType.INTEGER);
                break;
            case "BINARY":
                operator = Randomly.fromOptions("-", "+", "*", "/");
                expr = genExpr(depth + 1, DataType.INTEGER) + operator
                        + genExpr(depth + 1, DataType.INTEGER);
                break;
            default:
                throw new AssertionError();
        }
        return expr;
    }

    public String genStringExpr(int depth) {
        String expr;
        expr = genExpr(depth + 1, DataType.STRING);
        return expr;
    }

    public String genDoubleExpr(int depth) {
        String expr, operator;
        if (Randomly.getBoolean()) {
            expr = getRandomVal(DataType.DOUBLE);
            return expr;
        }
        String op = Randomly.fromOptions("UNARY", "BINARY");
        switch (op) {
            case "UNARY":
                operator = Randomly.fromOptions("-", "+");
                expr = operator + genExpr(depth + 1, DataType.DOUBLE);
                break;
            case "BINARY":
                operator = Randomly.fromOptions("-", "+", "*", "/");
                expr = genExpr(depth + 1, DataType.DOUBLE) + operator
                        + genExpr(depth + 1, DataType.DOUBLE);
                break;
            default:
                throw new AssertionError();
        }
        return expr;
    }

    public String getRandomVal(DataType dataType) {
        String val;
        switch (dataType) {
            case INTEGER:
                val = Long.toString(rand.getInteger(-100, 100));
                break;
            case DOUBLE:
                val = Double.toString(rand.getDouble());
                if (val.equals("Infinity"))
                    val = "\"INF\"^^xsd:double";
                else if (val.equals("-Infinity"))
                    val = "\"-INF\"^^xsd:double";
                break;
            case STRING:
                //if (size == 0) size = 20;
                String str = rand.getString();
                while (strHandler.hasChineseCharacter(str))
                    str = rand.getString();
                str = strHandler.stringTrans(str);
                //???
//                if (str.length() > size) {
//                    str = str.substring(0, size);
//                }
                val = "\"" + str + "\"";
                break;
            case BOOLEAN:
                val = Boolean.toString(Randomly.getBoolean());
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + this);
        }
        //appearedValues.add(val);  //??
        return val;
    }

}
