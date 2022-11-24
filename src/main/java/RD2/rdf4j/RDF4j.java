package RD2.rdf4j;

import org.eclipse.rdf4j.model.*;
import org.eclipse.rdf4j.model.util.Values;
import org.eclipse.rdf4j.model.vocabulary.XSD;
import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.query.resultio.text.tsv.SPARQLResultsTSVWriter;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class RDF4j {
    public Repository db = new SailRepository(new MemoryStore());
    public List<IRI> resources = new ArrayList<>();
    private List<String> bindingNames = new ArrayList<>();

    private static File logfile;
    private static FileWriter logFileWriter;
    private static OutputStream output;

    public void init(String dateTime) throws IOException {
        String logFilename = "./log/"+ dateTime + ".txt";
        logfile = new File(logFilename);
        logFileWriter = new FileWriter(logfile, true);
        output = new FileOutputStream(logfile, true);
    }

    public void createGDB(String fileName) throws IOException {
        File file = new File(fileName);
        // Open a connection to the database
        try (RepositoryConnection conn = db.getConnection()) {
//            conn.add(file, "", RDFFormat.TURTLE);
            try (InputStream input = new FileInputStream(fileName)) {
                // add the RDF data from the inputstream directly to our database
                conn.add(input, "", RDFFormat.TURTLE);
            }
            // let's check that our data is actually in the database
//            try (RepositoryResult<Statement> result = conn.getStatements(null, null, null)) {
//                for (Statement st : result) {
//                    System.out.println("db contains: " + st);
//                }
//            }
        }
    }

    public void createResources(List<String> URIs) {
        for (String URI : URIs) {
            IRI resource = Values.iri(URI);
            resources.add(resource);
        }
    }

    public void printResources() {
        for (IRI r : resources) {
            System.out.println(r.getNamespace() + r.getLocalName());
        }
    }

    public void addPropertyResource(int resourceIndex, String propertyName, String propertyValue) {
        try (RepositoryConnection conn = db.getConnection()) {
            IRI property = Values.iri(propertyName);
            IRI propertyResource = Values.iri(propertyValue);
            conn.add(resources.get(resourceIndex), property, propertyResource);
        }
    }

    public <T> void addPropertyLiteral(int resourceIndex, String propertyName, T literal) {
        try (RepositoryConnection conn = db.getConnection()) {
            IRI property = Values.iri(propertyName);
            if (literal instanceof Integer)
                conn.add(resources.get(resourceIndex), property, Values.literal(String.valueOf(literal), XSD.INTEGER));
            else
                conn.add(resources.get(resourceIndex), property, Values.literal(literal));
        }
    }

    public void printStatements(FileWriter fileWriter) {
        try (RepositoryConnection conn = db.getConnection()) {
            try (RepositoryResult<Statement> statements = conn.getStatements(null, null, null)) {
                for (Statement st : statements)
                    fileWriter.write(st + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public List<BindingSet> query(String queryString) {
        List<BindingSet> results;
        try (RepositoryConnection conn = db.getConnection()) {
            TupleQuery query = conn.prepareTupleQuery(queryString);

            //TupleQueryResultHandler tsvWriter = new SPARQLResultsTSVWriter(output);
            //TupleQueryResultHandler tsvWriter = new SPARQLResultsTSVWriter(System.out);
            //query.evaluate(tsvWriter);
            //logFileWriter.write("\n");
            //System.out.println("");

            TupleQueryResult queryResult = query.evaluate();
            bindingNames = queryResult.getBindingNames();
            results = QueryResults.asList(queryResult);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return results;
    }

    public String queryBNodeID(String queryString) {
        List<BindingSet> results;
        List<String> varNames = new ArrayList<>();
        String bNodeID;
        try (RepositoryConnection conn = db.getConnection()) {
            TupleQuery query = conn.prepareTupleQuery(queryString);
            TupleQueryResult queryResult = query.evaluate();
            varNames = queryResult.getBindingNames();
            results = QueryResults.asList(queryResult);

            BindingSet solution = results.get(0);
            String varName = varNames.get(0);
            Value value = solution.getValue(varName);
            BNode bNode = (BNode) value;
            bNodeID = bNode.getID();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return bNodeID;
    }

    public List<String> formatter(List<BindingSet> results, List<String> bNodeIDMap) {
        List<String> resList = new ArrayList<>();
        for (BindingSet solution : results) {
            String res = "";
            for (String bindingName : bindingNames) {
                Value value = solution.getValue(bindingName);
                if (value == null) {
                    res += "Null" + "#" + "null" + "#";
                } else if (value instanceof Literal) {
                    Literal literal = (Literal) value;
                    res += literal.getDatatype() + "#" + literal.getLabel() + "#";
                } else if (value instanceof IRI) {
                    IRI iri = (IRI) value;
                    String uri = iri.getNamespace() + iri.getLocalName();
                    res += "URI" + "#" + uri + "#";
                } else if (value instanceof BNode) {
                    BNode bNode = (BNode) value;
                    String id = bNode.getID();
                    int bNodeID = bNodeIDMap.indexOf(id);
                    res += "BlankNode" + "#" + bNodeID + "#";
                }
            }
            resList.add(res);
        }
        return resList;
    }

    public void clear() {
        resources.clear();
        db = new SailRepository(new MemoryStore());
    }

}
