import java.io.IOException;
import java.util.List;

public interface SearchEngine {
    List<PageEntry> search(String word) throws IOException;
}
