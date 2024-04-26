import java.io.IOException;

public class SearchEngineApplication {

    public static void main(String[] args) {
        Indexer indexer = new Indexer();
        try {
            indexer.index();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
