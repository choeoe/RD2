package org.example.marklogic;

import com.fasterxml.jackson.databind.JsonNode;
import com.marklogic.client.DatabaseClient;
import com.marklogic.client.DatabaseClientFactory;
import com.marklogic.client.io.FileHandle;
import com.marklogic.client.io.JacksonHandle;
import com.marklogic.client.io.ReaderHandle;
import com.marklogic.client.io.StringHandle;
import com.marklogic.client.semantics.*;
import org.example.Options;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class MarkLogic {
    static DatabaseClient client = DatabaseClientFactory.newClient(
            "localhost", 8000, "Documents",
            new DatabaseClientFactory.DigestAuthContext(
                    "root", "123"));
    static private GraphManager graphMgr = client.newGraphManager();
    static private String graphURI = GraphManager.DEFAULT_GRAPH;
    static private String format = RDFMimeTypes.TURTLE;

    private static File logfile;
    private static FileWriter logFileWriter;
    private static OutputStream output;

    public void init(String dateTime) throws IOException {
        String logFilename = "./log/" + dateTime + ".txt";
        logfile = new File(logFilename);
        logFileWriter = new FileWriter(logfile, true);
        output = new FileOutputStream(logfile, true);
    }

    public MarkLogic() {
    }

    public MarkLogic(Options options) {
        String host = options.getHost();
        int port = options.getPort();
        String database = options.getDbName();
        String user = options.getUserName();
        String password = options.getPassword();
        client = DatabaseClientFactory.newClient(
                host, port, database,
                new DatabaseClientFactory.DigestAuthContext(
                        user, password));
    }

    public static void createGDB(String filename) {
        //System.out.println("Creating graph " + graphURI);
        FileHandle tripleHandle =
                new FileHandle(new File(filename)).withMimetype(format);
        graphMgr.write(graphURI, tripleHandle);
    }

    public static void printStatements(FileWriter fileWriter) throws IOException {
        //System.out.println("Reading graph " + graphURI);
        StringHandle triples = graphMgr.read(graphURI, new StringHandle().withMimetype(format));
        fileWriter.write(String.valueOf(triples));
    }

    public static JsonNode query(String queryString) throws IOException {
        SPARQLQueryManager qm = client.newSPARQLQueryManager();
        SPARQLQueryDefinition query = qm.newQueryDefinition(queryString);
        JsonNode results;
        try {
            results = qm.executeSelect(query, new JacksonHandle()).get();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        //输出
        //ReaderHandle results2 = new ReaderHandle();
        //results2.setMimetype(SPARQLMimeTypes.SPARQL_CSV);
        //results2 = qm.executeSelect(query, results2);
        //results2.write(output);
        //logFileWriter.write("\n");
        //logFileWriter.write("\n");
        //results2.write(System.out);
        //System.out.println();
        //System.out.println();

        return results;
    }

    public String queryBNodeID(String queryString) {
        SPARQLQueryManager qm = client.newSPARQLQueryManager();
        SPARQLQueryDefinition query = qm.newQueryDefinition(queryString);
        JsonNode results = qm.executeSelect(query, new JacksonHandle()).get();
        JsonNode matches = results.path("results").path("bindings");
        JsonNode resultVars = results.path("head").path("vars");
        String var = resultVars.get(0).asText();
        String value = matches.get(0).path(var).path("value").asText();

        return value;
    }

    public List<String> formatter(JsonNode results, List<String> bNodeIDMap) {
        List<String> resList = new ArrayList<>();
        JsonNode resultVars = results.path("head").path("vars");
        JsonNode matches = results.path("results").path("bindings");
        for (int i = 0; i < matches.size(); i++) {
            String res = "";
            JsonNode match = matches.get(i);
            for (int j = 0; j < resultVars.size(); j++) {
                String var = resultVars.get(j).asText();
                JsonNode matchVar = match.path(var);
                if (matchVar.isEmpty()) {
                    res += "Null" + "#" + "null" + "#";
                } else {
                    String value = matches.get(i).path(var).path("value").asText();
                    String type = matches.get(i).path(var).path("type").asText();
                    switch (type) {
                        case "literal":
                            String datatype = matches.get(i).path(var).path("datatype").asText();
                            if (datatype.equals(""))
                                datatype = "http://www.w3.org/2001/XMLSchema#string";
                            if (datatype.equals("http://www.w3.org/2001/XMLSchema#double")) {
                                if (!value.contains(".")) {
                                    int len = value.length();
                                    int e = len - 1;
                                    int offset = 1;
                                    StringBuilder sb = new StringBuilder(value);
                                    if (value.charAt(0) == '-'){
                                        e--;
                                        offset = 2;
                                    }
                                    sb.insert(offset, ".");
                                    value = sb.toString();
                                    while(value.endsWith("0")){
                                        value = value.substring(0, value.length() - 1);
                                    }
                                    value += "E" + e;
                                }
                            }
                            res += datatype + "#" + value + "#";
                            break;
                        case "uri":
                            res += "URI" + "#" + value + "#";
                            break;
                        case "bnode":
                            int bNodeID = bNodeIDMap.indexOf(value);
                            res += "BlankNode" + "#" + bNodeID + "#";
                            break;
                    }
                }
            }
            resList.add(res);
        }
        return resList;
    }

    public void clear() {
        graphMgr.delete(graphURI);
    }

}
