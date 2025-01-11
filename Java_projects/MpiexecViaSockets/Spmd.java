import java.io.*;
import java.net.*;  
import java.util.concurrent.Executors; 
import java.util.concurrent.ExecutorService;

abstract class ExecutableFile { 
    public static final String startSearchFromDir = "E:\\APD"; 
    public static final String parametersOfCommand = "/R"; //parameters of the 'WHERE' command (this means recursion in the search)

    public static String findAbsoluthPath(String executableProgram) { 
        System.out.println("Searching for absoluth_path of the executable program..."); //show a message because can take a while

        try { 
            ProcessBuilder processBuilder = new ProcessBuilder("where", parametersOfCommand, startSearchFromDir, executableProgram); //create a process builder with the command to find the path of the executable program
            processBuilder.redirectErrorStream(true); // merge the error stream with the output stream, so we can read feedback from created processes

            Process process = processBuilder.start(); //start the process
            try (BufferedReader processOutput = new BufferedReader(new InputStreamReader(process.getInputStream()))) { 
                String absoluthPath = processOutput.readLine(); //read the output of the process
                
                if (absoluthPath == null || absoluthPath.startsWith("INFO") || absoluthPath.trim().isEmpty()) { //if the output is null or starts with INFO(means wrong command/file_not_found) or is empty
                    return null; 
                } else { 
                    return absoluthPath; //return the absoluth path of the executable program
                }
            } 
        } catch (IOException exception) { 
            System.out.println("Error executing process: " + exception.getMessage()); 
            return null; 
        }
    }
}

public class Spmd {  
    private static class ClientHandler implements Runnable { 
        private final Socket clientSocket;  
        private final int serverPortNumber; //we remember the port number of the server for using it in the command of executing recieved executable_file

        public ClientHandler(Socket socket, int portNumber) { 
            this.clientSocket = socket;  
            this.serverPortNumber = portNumber;
        }

        @Override
        public void run() { 
            try (PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true); 
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {

                String executableProgram = in.readLine(); //contains name of the executable program that client wants to run(already checked by client if it exists and is executable)
                System.out.println("Client requested to run: " + executableProgram); 

                String executableAbsoluthPath = ExecutableFile.findAbsoluthPath(executableProgram); //find the absoluth_path for each given host(can be different in every case, this is why we need call it N times) 
                if (executableAbsoluthPath == null) {  
                    System.out.println(String.format("Executable '%s' NOT founded in start_searching_dir: %s !!", executableProgram, ExecutableFile.startSearchFromDir)); //print error message in server console
                    out.println(String.format("Executable '%s' NOT founded in start_searching_dir: %s !!", executableProgram, ExecutableFile.startSearchFromDir)); //print error message in client console
                    return; 
                } else { 
                    System.out.println(String.format("Executable '%s' was founded at: %s ", executableProgram, executableAbsoluthPath)); //print status in server console
                }

                try {                  
                    String serverPortAsString = String.valueOf(this.serverPortNumber); //convert the port number of the server to string, to can pass it to the executable_program command
                    ProcessBuilder processBuilder = new ProcessBuilder(executableAbsoluthPath, serverPortAsString); //create a process builder with the command and the port_nuber of server to can pass it to executable(ProcessBuilder needs [String, String] as arguments)
                    processBuilder.redirectErrorStream(true); // merge the error stream with the output stream, so we can read feedback from created processes
                    
                    System.out.println("Start of execution..."); 
                    Process process = processBuilder.start(); 


                    try (BufferedReader processOutput = new BufferedReader(new InputStreamReader(process.getInputStream()))) { 
                        String outputLine; 
                        while ((outputLine = processOutput.readLine()) != null) { //keep reading at the output_stream of the process until it is empty
                            out.println(outputLine); //send the output to the client by writing it to the output_stream of the client
                        }
                    } 
                    
                    process.waitFor(); //wait for the process to finish
                    System.out.println("Execution finished successfully !"); 

                } catch (IOException | InterruptedException exception) { 
                    System.out.println("Error executing process: " + exception.getMessage());  
                    out.println("Server_Error: " + exception.getMessage());
                }  
            
            } catch (IOException exception) { 
                System.out.println("Error handling client: " + exception.getMessage());
            
            } finally { 
                System.out.println(String.format("Closing connection with client '%s\n", clientSocket.getInetAddress()));
                
                try { 
                    clientSocket.close(); // close the client connection
                
                } catch (IOException e) {
                    System.out.println(String.format("Error closing client '%s' !!", clientSocket.getInetAddress())); 
                    System.out.println(e.getMessage());
                } 
            }
                       
        }
    } 

    public static void main(String[] args) throws IOException{ 
        if (args.length != 2) {
            System.err.println("Usage: java Smpd <port number> <max_nr_of_clients>");
            System.exit(1);
        }

        int portNumber = Integer.parseInt(args[0]);  
        int maxSimultaneousClients = Integer.parseInt(args[1]);

        ExecutorService threadPool = Executors.newFixedThreadPool(maxSimultaneousClients);  //we create a limited 'bucket' of workers to handle the client requests

        try (ServerSocket serverSocket = new ServerSocket(portNumber)) { 
            System.out.println("Server started on port: " + portNumber + "\n"); 

            while (true) { //use a loop to accept multiple client requests, until server is stopped with CTRL+C
                try { 
                    Socket clientSocket = serverSocket.accept(); //accept a new connection
                    System.out.println("New client connected: " + clientSocket.getInetAddress());
                
                    threadPool.submit(new ClientHandler(clientSocket, portNumber)); //submit the client handler to the thread pool, so it can be executed by a worker
                
                } catch (IOException exception) { 
                    System.out.println("Error accepting client: " + exception.getMessage());
                }
            }
        } catch (IOException exception) { 
            System.out.println("Error: " + exception.getMessage());
        } 
    }
}