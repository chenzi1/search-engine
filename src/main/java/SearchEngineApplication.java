import org.apache.lucene.queryparser.classic.ParseException;

import java.io.IOException;

public class SearchEngineApplication {

    public static void main(String[] args) {
        switch (args[0]) {
            case "index":
                Indexer indexer = new Indexer();
                try {
                    indexer.index(args[1], args[2]);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;
            case "search":
                Searcher searcher = new Searcher();
                try {
                    searcher.searchFreeText(args[1], Integer.parseInt(args[2]), args[3]);
                } catch (IOException | ParseException e) {
                    throw new RuntimeException(e);
                }
                break;
            case "searchid":
                Searcher searcherId = new Searcher();
                try {
                    searcherId.searchById(args[1], args[2]);
                } catch (ParseException | IOException e) {
                    throw new RuntimeException(e);
                }
        }
    }
}
