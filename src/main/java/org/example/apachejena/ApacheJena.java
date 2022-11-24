package org.example.apachejena;

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.XSD;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ApacheJena {
    public Model model = ModelFactory.createDefaultModel();
    public List<Resource> resources = new ArrayList<>();

    private static File logfile;
    private static FileWriter logFileWriter;
    private static OutputStream output;

    public void init(String dateTime) throws IOException {
        String logFilename = "./log/" + dateTime + ".txt";
        logfile = new File(logFilename);
        logFileWriter = new FileWriter(logfile, true);
        output = new FileOutputStream(logfile, true);
    }

    public void createGDB(String fileName) {
        // use the RDFDataMgr to find the input file
        InputStream in = RDFDataMgr.open(fileName);
        if (in == null) {
            throw new IllegalArgumentException("File: " + fileName + " not found");
        }
        // read the RDF/TURTLE file
        RDFDataMgr.read(model, in, Lang.TURTLE);
        // write it to standard out
        //RDFDataMgr.write(System.out, model, Lang.TURTLE);
    }

    public void createResources(List<String> URIs) {
        for (String URI : URIs) {
            Resource resource = model.createResource(URI);
            resources.add(resource);
        }
    }

    public void printResources() throws IOException {
        for (Resource r : resources) {
            System.out.println(r.getURI());
        }
    }

    public void addPropertyResource(int resourceIndex, String propertyName, String propertyValue) {
        Property property = model.createProperty(propertyName);
        Resource propertyResource = model.createResource(propertyValue);
        resources.get(resourceIndex).addProperty(property, propertyResource);
    }

    public <T> void addPropertyLiteral(int resourceIndex, String propertyName, T literal) {
        Property property = model.createProperty(propertyName);
        if (literal instanceof Integer)
            resources.get(resourceIndex).addProperty(property, model.createTypedLiteral(String.valueOf(literal), XSD.integer.getURI()));
        else
            resources.get(resourceIndex).addProperty(property, model.createTypedLiteral(literal));
    }

    public void printStatements(OutputStream output) {
        model.write(output, "N-TRIPLE");
    }

    public ResultSet query(String queryString) throws IOException {
        Query query;
        try {
            query = QueryFactory.create(queryString);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        QueryExecution qexec = QueryExecutionFactory.create(query, model);
        ResultSet results = qexec.execSelect();

        QueryExecution qexec2 = QueryExecutionFactory.create(query, model);
        ResultSet results2 = qexec2.execSelect();
        results2 = ResultSetFactory.copyResults(results2);

        //ResultSetFormatter.out(output, results, query);
        //logFileWriter.write("\n");
        //ResultSetFormatter.out(System.out, results, query);
        //System.out.println();

        qexec.close();
        qexec2.close();
        return results2;
    }

    public String queryBNodeID(String queryString) {
        Query query;
        try {
            query = QueryFactory.create(queryString);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        QueryExecution qexec = QueryExecutionFactory.create(query, model);
        ResultSet results = qexec.execSelect();

        List<String> resultVars = results.getResultVars();
        QuerySolution soln = results.nextSolution();
        RDFNode n = soln.get(resultVars.get(0));
        Resource r = (Resource) n;
        AnonId id = r.getId();
        String bNodeID = id.toString();

        //qexec.close();

        return bNodeID;
    }

    public List<String> formatter(ResultSet results, List<String> bNodeIDMap) {
        List<String> resList = new ArrayList<>();
        List<String> resultVars = results.getResultVars();
        for (; results.hasNext(); ) {
            QuerySolution soln = results.nextSolution();
            String res = "";
            for (String var : resultVars) {
                RDFNode n = soln.get(var);  // Get a result variable by name.
                if (n == null) {
                    res += "Null" + "#" + "null" + "#";
                } else if (n.isLiteral()) {
                    Literal l = (Literal) n;
                    String dataTypeURI = l.getDatatypeURI();
                    String lexicalForm = l.getLexicalForm();
                    res += dataTypeURI + "#" + lexicalForm + "#";
                } else if (n.isResource()) {
                    Resource r = (Resource) n;
                    if (!r.isAnon()) {
                        String uri = r.getURI();
                        res += "URI" + "#" + uri + "#";
                    } else {
                        AnonId id = r.getId();
                        int bNodeID = bNodeIDMap.indexOf(id.toString());
                        res += "BlankNode" + "#" + bNodeID + "#";
                    }
                }
            }
            resList.add(res);
        }
        return resList;
    }

    public void clear() {
        resources.clear();
        model = ModelFactory.createDefaultModel();
    }

}
