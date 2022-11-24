package RD2;

import com.beust.jcommander.Parameter;
import lombok.Data;

@Data
public class Options {

//    @Parameter(names = {"--dbms"}, description = "Specifies the target DBMS")
//    private String DBMS = "marklogic";

    @Parameter(names = {"--dbname"}, description = "Specifies the test database")
    private String dbName = "Documents";

    @Parameter(names = "--host", description = "The host used to log into the DBMS")
    private String host = "127.0.0.1";

    @Parameter(names = "--port", description = "The port used to log into the DBMS")
    private int port = 8000;

    @Parameter(names = "--username", description = "The user name used to log into the DBMS")
    private String userName = "root";

    @Parameter(names = "--password", description = "The password used to log into the DBMS")
    private String password = "123";

    @Parameter(names = "--test-time", description = "Specifies the testing time")
    private String testTime = "5";

    @Parameter(names = "--query-num", description = "The num of queries each db executes")
    private String queryNum = "100";

    @Parameter(names = "--db-num", description = "Specifies the num of generated gdb")
    private String dbNum = "10";

    /*
    @Parameter(names = {"--single-query"}, description = "Whether just test a single query")
    private boolean singleQuery = false;

    @Parameter(names = {"--set-case"}, description = "Whether use a specified case")
    private boolean setCase = false;

    @Parameter(names = {"--case-file"}, description = "Specifies the input file of the specified case")
    private String caseFile = "";

    //@Parameter(names = { "--table" }, description = "Specifies the test table")
    //private String tableName = "troi";

    @Parameter(names = "--random-string-generation", description = "Select the random-string generation approach")
    private Randomly.StringGenerationStrategy randomStringGenerationStrategy = Randomly.StringGenerationStrategy.SOPHISTICATED;

    @Parameter(names = "--string-constant-max-length", description = "Specify the maximum-length of generated string constants")
    private int maxStringConstantLength = 10;

    @Parameter(names = "--use-constant-caching", description = "Specifies whether constants should be cached and re-used with a certain probability", arity = 1)
    private boolean useConstantCaching = true;

    @Parameter(names = "--constant-cache-size", description = "Specifies the size of the constant cache. This option only takes effect when constant caching is enabled")
    private int constantCacheSize = 100;
     */
}
