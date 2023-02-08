package RD2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum DataType {
    INTEGER,

//    DECIMAL,

//    FLOAT,
    DOUBLE,

    BOOLEAN,

    STRING,

//    DATETIME
    ;

    public static DataType getRandomDataType() {
        return Randomly.fromOptions(INTEGER, STRING, BOOLEAN, DOUBLE);
    }

    public boolean isNumeric() {
        return Arrays.asList(INTEGER, DOUBLE).contains(this);
    }

    public boolean isString() {
        return Arrays.asList(STRING).contains(this);
    }

    public boolean hasLen() {
        return this == STRING;
    }

    public static List<DataType> ebvList() {
        List<DataType> dataTypeListEBV = new ArrayList<>(Arrays.asList(BOOLEAN, INTEGER, DOUBLE, STRING));
        return dataTypeListEBV;
    }

}
