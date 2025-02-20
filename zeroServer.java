import java.io.*;
import java.net.*;
import java.nio.channels.*;

public class zeroServer {
    public void copy(String from, OutputStream out) throws IOException {
        byte[] data = new byte[8 * 1024];
        try (FileInputStream fis = new FileInputStream(new File(from))) {
            int bytesRead;
            while ((bytesRead = fis.read(data)) != -1) {
                out.write(data, 0, bytesRead);
            }
        }
    }

    
    public void zeroCopy(String from, OutputStream out) throws IOException {
        try (FileChannel sourceChannel = new FileInputStream(new File(from)).getChannel();
             WritableByteChannel destChannel = Channels.newChannel(out)) {
            sourceChannel.transferTo(0, sourceChannel.size(), destChannel);
        }
    }

    
    public void listFilesInDirectory(String directoryPath, PrintWriter writer) {
        File dir = new File(directoryPath);
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        writer.println(file.getName());
                    }
                }
            }
        } else {
            writer.println("Directory not found.");
        }
        writer.println("END_OF_LIST"); 
    }

    
    public void startServer(int port) throws IOException {
        String serverFilePath = "/home/moji/Downloads";  
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Server is listening on port " + port);

        while (true) {
            
            Socket clientSocket = serverSocket.accept();
            System.out.println("Client connected");

            
            PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
            listFilesInDirectory(serverFilePath, writer);

            
            InputStream in = clientSocket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            
            String fileName = reader.readLine();
            String mode = reader.readLine();

            
            File file = new File(serverFilePath, fileName);
            if (!file.exists()) {
                writer.println("File not found: " + fileName);
                clientSocket.close();
                continue;
            }

            
            OutputStream out = clientSocket.getOutputStream();
            long start = System.currentTimeMillis();
            if ("1".equals(mode)) {
                System.out.println("Using Traditional I/O Copy");
                copy(file.getAbsolutePath(), out);
            } else if ("2".equals(mode)) {
                System.out.println("Using Zero-Copy I/O");
                zeroCopy(file.getAbsolutePath(), out);
            } else {
                System.out.println("Invalid mode received: " + mode);
            }

            long end = System.currentTimeMillis();
            System.out.println("File sent in " + (end - start) + " milliseconds");

            out.close();
            clientSocket.close();
        }
    }

    public static void main(String[] args) {
        try {
            zeroServer server = new zeroServer();
            server.startServer(12345);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}