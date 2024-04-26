import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
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

public class Indexer {

    ObjectMapper objectMapper = new ObjectMapper();
    JsonFactory jsonFactory = new JsonFactory();

    Document luceneDocument = new Document();

    Directory indexDirectory;
    IndexWriterConfig config;
    IndexWriter indexWriter;

    int counter = 0;

    int numOfCounter = 0;

    public void index() throws IOException {
        indexDirectory = FSDirectory.open(Paths.get("/Users/chenzi/desktop/yelp_dataset/index"));

        config = new IndexWriterConfig(new StandardAnalyzer(EnglishAnalyzer.ENGLISH_STOP_WORDS_SET));

        indexWriter = new IndexWriter(indexDirectory, config);

        try {

            JsonParser jsonParser = jsonFactory.createParser(new File("/Users/chenzi/desktop/yelp_dataset/yelp_academic_dataset_review.json"));

            // Advance to the first token
            jsonParser.nextToken();

            // Check if the first token is the start of an object
            if (jsonParser.getCurrentToken() == JsonToken.START_OBJECT) {
                // Iterate over the JSON object
                while (shouldProcess(jsonParser)) {
                    String fieldName = jsonParser.getCurrentName();
                    jsonParser.nextToken(); // Move to the value token
                    String fieldValue = jsonParser.getText(); // Assuming all values are strings
                    System.out.println();

                    if (fieldName.equalsIgnoreCase("text")) {
                        luceneDocument.add(new TextField(fieldName, fieldValue, Field.Store.YES));
                    } else {
                        luceneDocument.add(new StringField(fieldName, fieldValue, Field.Store.YES));
                    }
                    System.out.println(numOfCounter + " | " + ++counter);
                    if (counter >= 100000) {
                        indexWriter.addDocument(luceneDocument);
                        luceneDocument = new Document(); // clear the document
                        counter = 0; // reset the counter
                        numOfCounter++;
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
            System.out.println(++counter);
            if (jsonParser.nextToken() == JsonToken.START_OBJECT) {
                jsonParser.nextToken();
                return true;
            } else {
                indexWriter.addDocument(luceneDocument);
                return false;
            }
        } else {
            return true;
        }
    }
}
