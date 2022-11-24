# RD<sup>2</sup>

## Required Environment

1. Windows 11 or Linux Ubuntu 20.04

2. JDK 11

3. Python 3.8.4

4. Install Faker

   ```bash
   pip install Faker
   ```

5. Maven 3.8.6
6. [MarkLogic 10](https://developer.marklogic.com/products/marklogic-server)
7. Apache Jena and RDF4J are integrated thus no need to be installed additionally

## Packing Method

```bash
mvn clean package
```

## Usage

1. Specifies the configuration of MarkLogic, the number of graph databases to be generated, and the number of SPARQL queries to be generated in each testing round.

```bash
java -jar RD2.jar --dbname Documents --host 127.0.0.1 --port 8000 --username root --password 123 --db-num 10 --query-num 100
```

2. Default: The configuration of MarkLogic, and the number of graph databases and SPARQL queries are the information in the above example.

```bash
java -jar RD2-1.0-SNAPSHOT.jar
```
## Result
You can verify test results in `BugReport` directory. The `txt` file will record potential bugs attached with corresponding data.
You can check full data generated in `GeneratedGDB` directory.
