package RD2;

import RD2.apachejena.ApacheJena;
import RD2.gen.StatementGenerator;
import RD2.marklogic.MarkLogic;
import RD2.rdf4j.RDF4j;
import com.beust.jcommander.JCommander;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.jena.query.ResultSet;
import org.eclipse.rdf4j.query.BindingSet;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class Main {
    private static int gdbNum = 1;  //gdb count
    private static int queryNum = 100;  //queries per gdb

    private static final int maxN = 10;  //uri max count
    private static final int minN = 1;  //uri min count
    private static int N = (maxN + minN) / 2;  //uri count
    private static final int maxPropertyNum = 5;  //max property per resource
    private static final int minPropertyNum = 1;  //min property per resource

    private static Random r = new Random();
    private static Randomly rand = new Randomly();
    private static String regex = "^[\u4e00-\u9fa5]";  //predicate unicode

    private static List<String> URIs = new ArrayList<>();  //predicate URI
    private static List<String> URIsP = new ArrayList<>();  //predicate URI
    private static List<String> literalStrings = new ArrayList<>();  //string literal
    private static List<Integer> literalIntegers = new ArrayList<>();  //integer literal
    private static List<String> literalDoubles = new ArrayList<>();  //double literal
    private static List<Boolean> literalBooleans = new ArrayList<>();  //boolean literal

    private static int bNodeNum = 0;
    private static List<String> bNodePropertyLists = new ArrayList<>();
    private static List<String> jenaBNodeIDMap = new ArrayList<>();
    private static List<String> rdf4jBNodeIDMap = new ArrayList<>();
    private static List<String> markLogicBNodeIDMap = new ArrayList<>();

    private static ApacheJena jena = new ApacheJena();
    private static RDF4j rdf4j = new RDF4j();
    private static MarkLogic markLogic = new MarkLogic();
    private static StatementGenerator stGenerator = new StatementGenerator();
    private static File gdbfile;
    private static File logfile;
    private static FileWriter gdbFileWriter;
    private static FileWriter logFileWriter;
    private static StringTransformer strTrans = new StringTransformer();
    private static String dateTime;
    private static int bugInd = 1;

    private static int emptyRes = 0;
    private static int okRes = 0;
    private static int bugRes = 0;

    public static void main(String[] args) throws IOException {
//        if (args != null && args.length > 0) {
//            gdbNum = Integer.parseInt(args[0]);
//            queryNum = Integer.parseInt(args[1]);
//        }
//        System.out.println(System.getProperty("user.dir"));
        System.out.println("");
        Options options = new Options();
        JCommander jCmd = new JCommander();
        jCmd.addObject(options);
        jCmd.parse(args);
//        options.setDBMS(options.getDBMS().toUpperCase());
        markLogic = new MarkLogic(options);
        gdbNum = Integer.parseInt(options.getDbNum());
        queryNum = Integer.parseInt(options.getQueryNum());
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
        dateTime = formatter.format(date);
        String gdbPDirname = "./GeneratedGDB";
        File gdbPdir = new File(gdbPDirname);
        if (!gdbPdir.exists()) {
            gdbPdir.mkdir();
        }
        String gdbDirname = gdbPDirname + "/" + dateTime;
        File gdbDir = new File(gdbDirname);
        if (!gdbDir.exists()) {
            gdbDir.mkdir();
        }
        //generate log
        String logDirname = "./log";
        File logDir = new File(logDirname);
        if (!logDir.exists()) {
            logDir.mkdir();
        }
        String logFilename = logDirname + "/" + dateTime + ".txt";
        logfile = new File(logFilename);
        if (!logfile.exists()) {
            logfile.createNewFile();
        }
        logFileWriter = new FileWriter(logfile, true);
        //logFileWriter.write(dateTime + "\n\n");
        jena.init(dateTime);
        rdf4j.init(dateTime);
        markLogic.init(dateTime);
        //start test
        for (int i = 1; i <= gdbNum; i++) {
            //logFileWriter.write("--------------------\n");
            //System.out.print("--------------------");
            System.out.printf("Generated GDB-%d%n",i);
            if ((i / 10) % 10 == 1) {
                //logFileWriter.write("The " + i + "th generated graph database:\n");
                //System.out.print("The " + i + "th generated graph database:");
            } else {
                switch (i % 10) {
                    case 1:
                        //logFileWriter.write("The " + i + "st generated graph database:\n");
                        //System.out.print("The " + i + "st generated graph database:");
                        break;
                    case 2:
                        //logFileWriter.write("The " + i + "nd generated graph database:\n");
                        //System.out.print("The " + i + "nd generated graph database:");
                        break;
                    case 3:
                        //logFileWriter.write("The " + i + "rd generated graph database:\n");
                        //System.out.print("The " + i + "rd generated graph database:");
                        break;
                    default:
                        //logFileWriter.write("The " + i + "th generated graph database:\n");
                        //System.out.print("The " + i + "th generated graph database:");
                }
            }
            //logFileWriter.write("--------------------\n");
            //System.out.println("--------------------");
            URIs.clear();
            URIsP.clear();
            literalStrings.clear();
            literalIntegers.clear();
            literalDoubles.clear();
            literalBooleans.clear();
            bNodePropertyLists.clear();
            jenaBNodeIDMap.clear();
            rdf4jBNodeIDMap.clear();
            markLogicBNodeIDMap.clear();
            jena.clear();
            rdf4j.clear();
            markLogic.clear();
            bugInd = 1;
            //generate Turtle
            String gdbfilename = gdbDirname + "/gdb" + i + "-" + dateTime + ".ttl";
            gdbfile = new File(gdbfilename);
            if (!gdbfile.exists()) {
                gdbfile.createNewFile();
            }
            gdbFileWriter = new FileWriter(gdbfile, true);
            //generate gdb
            generateGDB();
            gdbFileWriter.close();
            //import GBDs
            rdf4j.createGDB(gdbfilename);
            jena.createGDB(gdbfilename);
            markLogic.createGDB(gdbfilename);
            genBNodeIDMap();
            //query test
            for (int j = 1; j <= queryNum; j++) {
                //logFileWriter.write("----------\n");
                //ystem.out.print("----------");
                System.out.println("Executed query number: " + j);
                if ((j / 10) % 10 == 1){
                    //logFileWriter.write("The " + j + "th generated query:\n");
                    //System.out.print("The " + j + "th generated query:");
                }
                else {
                    switch (j) {
                        case 1:
                            //logFileWriter.write("The " + j + "st generated query:\n");
                            //System.out.print("The " + j + "st generated query:");
                            break;
                        case 2:
                            //logFileWriter.write("The " + j + "nd generated query:\n");
                            //System.out.print("The " + j + "nd generated query:");
                            break;
                        case 3:
                            //logFileWriter.write("The " + j + "rd generated query:\n");
                            //System.out.print("The " + j + "rd generated query:");
                            break;
                        default:
                            //logFileWriter.write("The " + j + "th generated query:\n");
                            //System.out.print("The " + j + "th generated query:");
                    }
                }
                //logFileWriter.write("----------\n");
                //System.out.println("----------");
                //生成SparQL查询语句
                String queryStatement = generateQueryStatement();
                //差分测试
                differentialTesting(queryStatement, i, j);
            }
        }
        //logFileWriter.write("--------------------Test Result--------------------\n");
        //logFileWriter.write("Empty Results: " + emptyRes + ", " + 1.0 * emptyRes / gdbNum / queryNum * 100 + "%\n");
        //logFileWriter.write("Consistent Results " + okRes + ", " + 1.0 * okRes / gdbNum / queryNum * 100 + "%\n");
        //logFileWriter.write("Potential BUGs: " + bugRes + ", " + 1.0 * bugRes / gdbNum / queryNum * 100 + "%\n");

        System.out.println("--------------------Test Result--------------------");
        System.out.printf("Totally executed %d queries.%n", queryNum*gdbNum);
        System.out.println("Check generated data at gdb-*.ttl in generatedGDB");
        System.out.println("Empty Results: " + emptyRes + ", " + 1.0 * emptyRes / gdbNum / queryNum * 100 + "%");
        System.out.println("Consistent Results " + okRes + ", " + 1.0 * okRes / gdbNum / queryNum * 100 + "%");
        System.out.println("Potential BUGs: " + bugRes + ", " + 1.0 * bugRes / gdbNum / queryNum * 100 + "%");
    }

    public static void generateGDB() throws IOException {
        generateResources();
        generateStatements();
    }

    public static void generateResources() {
        N = r.nextInt(maxN - minN + 1) + minN;
        bNodeNum = (int) (N * r.nextDouble() * 0.4);
        Process proc;
        try {
            proc = Runtime.getRuntime().exec("python ./generateURI.py " + N);  //执行py文件
            BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line = null;
            while ((line = in.readLine()) != null) {
                URIs.add(line);
            }
            in.close();
            proc.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//        System.out.println(URIs);

        stGenerator.setURIs(URIs);
        stGenerator.setURIsP(URIsP);
    }

    public static void generateStatements() throws IOException {
        String prologue = "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n";
        gdbFileWriter.write(prologue);
        for (int i = 0; i < URIs.size(); i++) {
            int propertyNum = r.nextInt(maxPropertyNum - minPropertyNum + 1) + minPropertyNum;
            for (int j = 0; j < propertyNum; j++) {
                String triple = "<" + URIs.get(i) + ">";
                int propertyNameIndex = r.nextInt(N);
                String propertyName = URIs.get(propertyNameIndex);
                if (!URIsP.contains(propertyName))
                    URIsP.add(propertyName);
                triple += " <" + propertyName + ">";
                if (r.nextBoolean()) {
                    int resourceIndex = r.nextInt(N);
                    String resource = URIs.get(resourceIndex);
                    triple += " <" + resource + ">";
                } else {
                    String literalType = Randomly.fromOptions("STRING", "INT", "DOUBLE", "BOOLEAN");
                    switch (literalType) {
                        case "STRING":
                            String literalString = rand.getString();
                            while (literalString.matches(regex))
                                literalString = rand.getString();
                            literalString = strTrans.stringTrans(literalString);
                            literalStrings.add(literalString);
                            triple += " \"" + literalString + "\"";
                            break;
                        case "INT":
                            int literalInt = (int) rand.getInteger();
                            literalIntegers.add(literalInt);
                            triple += " " + literalInt;
                            break;
                        case "DOUBLE":
                            String literalDouble = Double.toString(rand.getDouble());
                            literalDouble = strTrans.doubleTrans(literalDouble.toString());
                            literalDoubles.add(literalDouble);
                            triple += " " + literalDouble;
                            break;
                        case "BOOLEAN":
                            Boolean literalBool = rand.getBoolean();
                            literalBooleans.add(literalBool);
                            triple += " " + literalBool;
                            break;
                    }
                }
                triple += " .\n";
                gdbFileWriter.write(triple);
                //logFileWriter.write(triple + "\n");
                //System.out.print(triple);
            }
        }
        for (int i = 0; i < bNodeNum; i++) {
            String propertyList = "";
            int propertyNum = r.nextInt(maxPropertyNum - minPropertyNum + 1) + minPropertyNum;
            for (int j = 0; j < propertyNum; j++) {
                int propertyNameIndex = r.nextInt(N);
                String propertyName = URIs.get(propertyNameIndex);
                if (!URIsP.contains(propertyName))
                    URIsP.add(propertyName);
                propertyList += " <" + propertyName + ">";
                if (r.nextBoolean()) {
                    int resourceIndex = r.nextInt(N);
                    String resource = URIs.get(resourceIndex);
                    propertyList += " <" + resource + ">";
                } else {
                    String literalType = Randomly.fromOptions("STRING", "INT", "DOUBLE", "BOOLEAN");
                    switch (literalType) {
                        case "STRING":
                            String literalString = rand.getString();
                            while (literalString.matches(regex))
                                literalString = rand.getString();
                            literalString = strTrans.stringTrans(literalString);
                            literalStrings.add(literalString);
                            propertyList += " \"" + literalString + "\"";
                            break;
                        case "INT":
                            int literalInt = (int) rand.getInteger();
                            literalIntegers.add(literalInt);
                            propertyList += " " + literalInt;
                            break;
                        case "DOUBLE":
                            String literalDouble = Double.toString(rand.getDouble());
                            literalDouble = strTrans.doubleTrans(literalDouble);
                            literalDoubles.add(literalDouble);
                            propertyList += " " + literalDouble;
                            break;
                        case "BOOLEAN":
                            Boolean literalBool = rand.getBoolean();
                            literalBooleans.add(literalBool);
                            propertyList += " " + literalBool;
                            break;
                    }
                }
                if (j != propertyNum - 1)
                    propertyList += " ;\n ";
            }
            bNodePropertyLists.add(propertyList);
            String triple = "[" + propertyList + " ] .\n";
            gdbFileWriter.write(triple);
            //logFileWriter.write(triple + "\n");
            //System.out.print(triple);
        }
//        jena.printStatements();
//        System.out.println();
//        rdf4j.printStatements();
//        System.out.println();
        stGenerator.setLiteral(literalStrings, literalIntegers, literalDoubles, literalBooleans);
        //logFileWriter.write("\n");
        //System.out.println();
    }

    public static void genBNodeIDMap() {
        for (int i = 0; i < bNodeNum; i++) {
            String queryStatement = "SELECT ?s\n";
            queryStatement += "WHERE { ?s " + bNodePropertyLists.get(i) + " . }";
            String jenaBNodeID = jena.queryBNodeID(queryStatement);
            jenaBNodeIDMap.add(jenaBNodeID);
            String rdf4jBNodeID = rdf4j.queryBNodeID(queryStatement);
            rdf4jBNodeIDMap.add(rdf4jBNodeID);
            String markLogicBNodeID = markLogic.queryBNodeID(queryStatement);
            markLogicBNodeIDMap.add(markLogicBNodeID);
        }
    }

    public static String generateQueryStatement() throws IOException {
        String queryStatement = stGenerator.genStatement();
        //logFileWriter.write(queryStatement + "\n");
        //logFileWriter.write("\n");
        //System.out.println(queryStatement);
        //System.out.println();

        return queryStatement;
    }

    public static void differentialTesting(String queryStatement, int gdbInd, int queryInd) throws IOException {
        //Apache Jena
        //logFileWriter.write("query results of Jena:\n");
        //System.out.println("query results of Jena:");
        ResultSet jenaResults = jena.query(queryStatement);
        List<String> jenaResList = new ArrayList<>();
        if (jenaResults != null) {  //没报错
            jenaResList = jena.formatter(jenaResults, jenaBNodeIDMap);
            Collections.sort(jenaResList);  //升序排序
//            System.out.println(jenaResList);
        }
        //RDF4J
        //logFileWriter.write("query results of RDF4j:\n");
        //System.out.println("query results of RDF4j:");
        List<BindingSet> rdf4jResults = rdf4j.query(queryStatement);
        List<String> rdf4jResList = new ArrayList<>();
        if (rdf4jResults != null) {  //没报错
            rdf4jResList = rdf4j.formatter(rdf4jResults, rdf4jBNodeIDMap);
            Collections.sort(rdf4jResList);  //升序排序
//            System.out.println(rdf4jResList);
        }
        //MarkLogic
        //logFileWriter.write("query results of MarkLogic:\n");
        //System.out.println("query results of MarkLogic:");
        JsonNode markLogicResults = markLogic.query(queryStatement);
        List<String> markLogicResList = new ArrayList<>();
        if (markLogicResults != null) {  //没报错
            markLogicResList = markLogic.formatter(markLogicResults, markLogicBNodeIDMap);
            Collections.sort(markLogicResList);  //升序排序
//            System.out.println(jenaResList);
        }
        if (jenaResults != null && rdf4jResults != null && markLogicResults != null
                && jenaResList.equals(rdf4jResList) && jenaResList.equals(markLogicResList)) {
            //logFileWriter.write("OK\n");
            //System.out.println("OK");
            okRes++;
            if (jenaResList.size() == 0)
                emptyRes++;
        } else {
            //logFileWriter.write("BUG!\n");
            //System.out.println("BUG!");
            bugRes++;
            String pDirname = "./BugReport";
            File pDir = new File(pDirname);
            if (!pDir.exists()) {
                pDir.mkdir();
            }
            String dirname = pDirname + "/" + dateTime;
            File dir = new File(dirname);
            if (!dir.exists()) {
                dir.mkdir();
            }
            String BugFileName = dirname + "/gbd" + gdbInd + "-" + dateTime + ".txt";
            File bugFile = new File(BugFileName);
            if (!bugFile.exists()) {
                bugFile.createNewFile();
            }
            OutputStream output = new FileOutputStream(bugFile, true);
            FileWriter fileWriter = new FileWriter(bugFile, true);
            fileWriter.write(dateTime + "\n\n");
            fileWriter.write("--------------------Bug" + bugInd + "--------------------\n");
            bugInd++;
            fileWriter.write("The data in Jena:\n");
            fileWriter.close();
//            output.close();
            jena.printStatements(output);
            fileWriter = new FileWriter(bugFile, true);
            fileWriter.write("\nThe data in RDF4j:\n");
            rdf4j.printStatements(fileWriter);
            fileWriter.write("\nThe data in MarkLogic:\n");
            markLogic.printStatements(fileWriter);
            fileWriter.write("\nThe query Statement:\n");
            fileWriter.write(queryStatement + "\n");
            fileWriter.write("\nThe query Results of Jena:\n");
            for (String string : jenaResList)
                fileWriter.write(string + "\n");
            //fileWriter.write(jenaResList + "\n");
            fileWriter.write("\nThe query Results of RDF4j:\n");
            for (String string : rdf4jResList)
                fileWriter.write(string + "\n");
            //fileWriter.write(rdf4jResList + "\n");
            fileWriter.write("\nThe query Results of MarkLogic:\n");
            for (String string : markLogicResList)
                fileWriter.write(string + "\n");
            //fileWriter.write(markLogicResList + "\n");

            fileWriter.write("\n");
            fileWriter.close();
        }
        //logFileWriter.write("\n");
        //System.out.println();
    }

}