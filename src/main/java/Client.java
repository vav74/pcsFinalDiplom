import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    public static void main(String[] args) throws IOException {
        String host = "localhost";
        int port = 8989;
        try (Socket clientSocket = new Socket(host, port);
             BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            out.println("бизнес Менеджер");
            String json = in.readLine();
            String[] str = json.split("(?<=},)");
            for (String st : str) {
                System.out.println(st);
            }
        }
    }
}