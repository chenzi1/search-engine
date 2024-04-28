import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.uhighlight.UnifiedHighlighter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;

public class Searcher {

    public void searchFreeText(String indexPath, int numOfResults, String searchString) throws IOException, ParseException {
        // Create a query parser for the field "text"
        QueryParser parser = new QueryParser("text", new EnglishAnalyzer(EnglishAnalyzer.ENGLISH_STOP_WORDS_SET));
        Query query = parser.parse(searchString);

        // Open the index directory
        Directory indexDir = FSDirectory.open(Paths.get(indexPath));

        // Create an index searcher and execute the search
        try (DirectoryReader indexReader = DirectoryReader.open(indexDir)) {
//            StoredFields storedFields = indexReader.storedFields();
            IndexSearcher indexSearcher = new IndexSearcher(indexReader);
            TopDocs topDocs = indexSearcher.search(query, numOfResults); // limit to top n results

            // Initialize Highlighter
            UnifiedHighlighter unifiedHighlighter = UnifiedHighlighter.builder(indexSearcher, new EnglishAnalyzer(EnglishAnalyzer.ENGLISH_STOP_WORDS_SET)).build();

            String[] fragments = unifiedHighlighter.highlight("text", query, topDocs, 1);

            for(int i=0; i<fragments.length;i++) {
                // retrieve the corresponding Document
                Document doc = indexSearcher.doc(topDocs.scoreDocs[i].doc);
                String reviewId = doc.get("review_id"); // retrieve the review_id from Document
                String businessId = doc.get("business_id");
                String fragment = fragments[i];
                System.out.println("Review Id: " + reviewId
                        + "\nBusiness Id: " + businessId
                        + "\nSnippet: " + fragment + "\n");
            }

            // Iterate over the results and print the "text" field
//            for(ScoreDoc scoreDoc : topDocs.scoreDocs) {
//                Document resultDoc = storedFields.document(scoreDoc.doc);
//            }
        }
    }
}
