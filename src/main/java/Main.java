import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class Main {
    private static final int PORT = 8989;

    public static void main(String[] args) throws IOException {
        System.out.println("Starting server at " + PORT + "...");
        BooleanSearchEngine engine = new BooleanSearchEngine(new File("pdfs"));
        System.out.println("Listening at " + PORT + ".");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                try (
                        Socket socket = serverSocket.accept();
                        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
                ) {
                    String words = in.readLine();
                    Gson gson = new GsonBuilder().create();
                    List<PageEntry> searchResult = engine.search(words);
                    if (searchResult.size() >= 1) {
                        out.println(gson.toJson(searchResult));
                    } else {
                        out.println("Ничего не найдено");
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Не могу стартовать сервер");
            e.printStackTrace();
        }
    }
}