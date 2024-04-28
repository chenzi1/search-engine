# search-engine

This project is created as part of the assignment for MH6301 Information Retrieval and Analysis.

Build
```commandline
mvn clean install
```

Run
```commandline
# for indexing
java -jar target/search-engine-1.0.jar index "{PATH-TO-INDEX-DIRECTORY}" "{PATH-TO-DATA-FILE}"

# for searching
java -jar target/search-engine-1.0jar search "{PATH-TO-INDEX-DIRECTORY}" "{TOP-N-NUMBER-OF-RESULTS} "{FREE-TEXT-QUERY}"
```