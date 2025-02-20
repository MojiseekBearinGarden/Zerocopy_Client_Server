
import java.io.*;
import java.net.*;
import java.util.Scanner;

public class zeroClient {

    public static void main(String[] args) {
        String serverAddress = "localhost";
        int port = 12345;

        try (Socket socket = new Socket(serverAddress, port)) {

            InputStream in = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            System.out.println("Available files on server:");
            String fileName;

            while (!(fileName = reader.readLine()).equals("END_OF_LIST")) {
                System.out.println(fileName);
            }

            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter the file name you want to download: ");
            String selectedFile = scanner.nextLine();

            System.out.print("Enter copy mode (1 for Traditional I/O, 2 for Zero-Copy I/O): ");
            String mode = scanner.nextLine();

            writer.println(selectedFile);
            writer.println(mode);

            String clientFilePath = "/home/moji/pic" + selectedFile;
            File outputFile = new File(clientFilePath);

            File parentDir = outputFile.getParentFile();
            if (!parentDir.exists()) {
                parentDir.mkdirs();
            }

            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                byte[] buffer = new byte[8 * 1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
            }

            System.out.println("File downloaded successfully to " + clientFilePath);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}