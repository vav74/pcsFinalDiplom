import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class BooleanSearchEngine implements SearchEngine {
    private final Map<String, List<PageEntry>> map = new HashMap<>();

    public BooleanSearchEngine(File pdfsDir) throws IOException {
        if (pdfsDir.isDirectory()) {
            for (File item : Objects.requireNonNull(pdfsDir.listFiles())) {
                if (item.isFile()) {
                    try (var doc = new PdfDocument(new PdfReader(item.getPath()))) {
                        int docNumOfPages = doc.getNumberOfPages();
                        for (int i = 1; i <= docNumOfPages; i++) {
                            Map<String, Integer> freqs = new HashMap<>();
                            PdfPage page = doc.getPage(i);
                            var text = PdfTextExtractor.getTextFromPage(page);
                            var words = text.split("\\P{IsAlphabetic}+");
                            words = Arrays.stream(words).map(String::toLowerCase).toArray(String[]::new);
                            for (var word : words) {
                                if (word.isEmpty()) {
                                    continue;
                                }
                                word = word.toLowerCase();
                                freqs.put(word, freqs.getOrDefault(word, 0) + 1);
                            }
                            for (Map.Entry<String, Integer> freqsSet : freqs.entrySet()) {
                                PageEntry pageEntry = new PageEntry(item.getName(), i, freqsSet.getValue());
                                if (!map.containsKey(freqsSet.getKey())) {
                                    map.put(freqsSet.getKey(), new ArrayList<>());
                                }
                                map.get(freqsSet.getKey()).add(pageEntry);
                            }
                        }
                    }
                }
            }
            for (Map.Entry<String, List<PageEntry>> set : map.entrySet()) {
                Collections.sort(map.get(set.getKey()));
            }
        }
    }

    @Override
    public List<PageEntry> search(String words) throws IOException {
        List<String> wordList = new LinkedList<>(Arrays.asList(words.trim().toLowerCase().split("\\s++")));
        List<String> stopWords = readStopWords();
        List<PageEntry> pageEntries = new ArrayList<>();
        try {
            wordList.removeAll(stopWords);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (wordList.size() == 1) {
            return map.getOrDefault(wordList.get(0), Collections.emptyList());
        } else {
            for (String s : wordList) {
                pageEntries.addAll(map.getOrDefault(s, Collections.emptyList()));
            }
            Set<Map.Entry<String, Map<Integer, Integer>>> pageEntriesGrouped = pageEntries.stream()
                    .collect(Collectors.groupingBy(PageEntry::getPdfName, Collectors.groupingBy(PageEntry::getPage, Collectors.summingInt(PageEntry::getCount))))
                    .entrySet();
            pageEntries.clear();
            for (Map.Entry<String, Map<Integer, Integer>> mapEntry : pageEntriesGrouped) {
                mapEntry.getValue().forEach((page, count) -> pageEntries.add(new PageEntry(mapEntry.getKey(), page, count)));
            }
            Collections.sort(pageEntries);
            return pageEntries;
        }
    }

    public List<String> readStopWords() throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader("stop-ru.txt"))) {
            return br.lines().collect(Collectors.toList());
        }
    }
}