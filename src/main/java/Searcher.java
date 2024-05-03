import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.uhighlight.UnifiedHighlighter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;

public class Searcher {

    public void searchFreeText(String indexPath, int numOfResults, String searchString) throws IOException, ParseException {
        Instant start = Instant.now();
        // Create a query parser for the field "text"
        QueryParser parser = new QueryParser(
                "text",
                new EnglishAnalyzer(
                        EnglishAnalyzer.ENGLISH_STOP_WORDS_SET));
        Query query = parser.parse(searchString);

        // Open the index directory
        Directory indexDir = FSDirectory.open(Paths.get(indexPath));

        // Create an index searcher and execute the search
        try (DirectoryReader indexReader = DirectoryReader.open(indexDir)) {
            IndexSearcher indexSearcher = new IndexSearcher(indexReader);
            TopDocs topDocs = indexSearcher.search(query, numOfResults); // limit to top n results

            // Initialize Highlighter
            UnifiedHighlighter unifiedHighlighter = UnifiedHighlighter.builder(indexSearcher, new EnglishAnalyzer(EnglishAnalyzer.ENGLISH_STOP_WORDS_SET)).build();

            String[] fragments = unifiedHighlighter.highlight("text", query, topDocs, 1);

            for(int i=0; i<fragments.length;i++) {
                // retrieve the corresponding Document
                Document doc = indexSearcher.doc(topDocs.scoreDocs[i].doc);
                float score = topDocs.scoreDocs[i].score;
                int docId = topDocs.scoreDocs[i].doc;
                String reviewId = doc.get("review_id"); // retrieve the review_id from Document
                String businessId = doc.get("business_id");
                String fragment = fragments[i];
                System.out.println("Score: " + score
                        + "\nDocument ID: " + docId
                        + "\nReview Id: " + reviewId
                        + "\nBusiness Id: " + businessId
                        + "\nSnippet: " + fragment + "\n");
            }
        }
        System.out.println("Free text query time taken: " + Duration.between(start,Instant.now()).toMillis());
    }

    public void searchById(String indexPath, String reviewId) throws ParseException, IOException {
        Instant start = Instant.now();
        Query query = new TermQuery(new Term("review_id", reviewId));

        // Open the index directory
        Directory indexDir = FSDirectory.open(Paths.get(indexPath));

        try (DirectoryReader indexReader = DirectoryReader.open(indexDir)) {
            IndexSearcher indexSearcher = new IndexSearcher(indexReader);
            TopDocs topDocs = indexSearcher.search(query, 1); // limit to 1 result

            Document doc = indexSearcher.doc(topDocs.scoreDocs[0].doc);
            String businessId = doc.get("business_id");
            String reviewText = doc.get("text");
            System.out.println("Review Id: " + reviewId
                    + "\nBusiness Id: " + businessId
                    + "\nReview: " + reviewText + "\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Review ID query time taken: " + Duration.between(start,Instant.now()).toMillis());
    }
}
