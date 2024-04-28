import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;

public class Indexer {

    ObjectMapper objectMapper = new ObjectMapper();
    JsonFactory jsonFactory = new JsonFactory();

    Document luceneDocument = new Document();

    Directory indexDirectory;
    IndexWriterConfig config;
    IndexWriter indexWriter;

    int counter = 0;

    int numOfCounter = 0;

    Instant start;

    public void index(String indexPath, String filePath) throws IOException {
        indexDirectory = FSDirectory.open(Paths.get(indexPath));

        config = new IndexWriterConfig(new EnglishAnalyzer(EnglishAnalyzer.ENGLISH_STOP_WORDS_SET));

        indexWriter = new IndexWriter(indexDirectory, config);

        try {

            JsonParser jsonParser = jsonFactory.createParser(new File(filePath));

            // Advance to the first token
            jsonParser.nextToken();

            // Check if the first token is the start of an object
            if (jsonParser.getCurrentToken() == JsonToken.START_OBJECT) {
                start = Instant.now();
                // Iterate over the JSON object
                while (shouldProcess(jsonParser)) {
                    String fieldName = jsonParser.currentName();
                    jsonParser.nextToken(); // Move to the value token
                    String fieldValue = jsonParser.getText(); // Assuming all values are strings
                    if (fieldName.equalsIgnoreCase("text")) {
                        luceneDocument.add(new TextField(fieldName, fieldValue, Field.Store.YES));
                    } else {
                        luceneDocument.add(new StringField(fieldName, fieldValue, Field.Store.YES));
                    }
                    counter++;
                    if (counter >= 100000) {
                        indexWriter.addDocument(luceneDocument);
                        luceneDocument = new Document(); // clear the document
                        counter = 0; // reset the counter
                        System.out.println(++numOfCounter * 100000);
                    }
                }
            }
            jsonParser.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            indexWriter.close(); // Ensure indexWriter gets closed
        }
        System.out.println("Successfully indexed document!");
    }

    private boolean shouldProcess(JsonParser jsonParser) throws IOException {
        if (jsonParser.nextToken() == JsonToken.END_OBJECT) {
            if (jsonParser.nextToken() == JsonToken.START_OBJECT) {
                jsonParser.nextToken();
                return true;
            } else {
                indexWriter.addDocument(luceneDocument);
                Instant finish = Instant.now();
                System.out.println("Total number of records processed: " + (numOfCounter * 100000 + counter) + "\n"
                        + "Total time elapsed: " + Duration.between(start, finish).toMillis());
                return false;
            }
        } else {
            return true;
        }
    }
}
