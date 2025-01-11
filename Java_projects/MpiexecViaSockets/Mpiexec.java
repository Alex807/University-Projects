import java.io.*;
import java.net.*; 
import java.util.List; 
import java.util.ArrayList;

abstract class Execute { 
    public static final String host = "localhost"; 

    public static void onHosts(String currentHost, int comunicatingPort, String executableProgram) { 
        
        try (Socket socket = new Socket(currentHost, comunicatingPort); //we connect to the host with an UNIQUE port_number for each host
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true); //for sending input to the hosts
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) { //for reading the output from the hosts
            
            out.println(executableProgram); //send the name of the executable program to the host via soket
            
            String serverResponse; 
            while ((serverResponse = in.readLine()) != null) { 
                System.out.println(String.format("Server_Output for IP-Adress[%s]: %s", currentHost, serverResponse)); //print the response from the server
            }
        
        } catch (IOException exception) { 
            System.out.println("Error connecting to host: " + currentHost); 
            System.out.println(exception.getMessage());
        }
    }

    public static void onProcesses(String currentPort, String executableProgram) {   
        
        try (Socket socket = new Socket("localhost", Integer.parseInt(currentPort)); //we connect with given port number to the localhost
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true); 
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            
            out.println(executableProgram); //send the name of the executable program to the host via soket
            
            String serverResponse; 
            while ((serverResponse = in.readLine()) != null) { 
                System.out.println(String.format("Server_Output on localhost_port[%s]: %s", currentPort, serverResponse)); //print the response from the server
            }
        
        } catch (IOException exception) { 
            System.out.println("Error connecting to port: " + currentPort); 
            System.out.println(exception.getMessage());
        }
    }

    public static boolean isExecutableValid(String executableProgram) {  
        if (executableProgram == null || executableProgram.trim().isEmpty() || (!executableProgram.endsWith(".exe") && !executableProgram.endsWith(".class"))) { //check if the executable program is sintactically correct, we check if exists in server part of project
            return false; 
        }

        return true;
    }
}

public class Mpiexec {
    public static void main(String[] args) throws IOException {
        if (args.length < 4 || (!args[0].equals("-hosts") && !args[0].equals("-processes"))) {
            System.err.println("Usage:");
            System.err.println("  java Mpiexec -hosts N IPADDRESS_1 IPADDRESS_2 ...  IPADRESS_N program.exe");
            System.err.println("  java Mpiexec -processes N port_1 port_2 ... port_N program.exe");
            System.exit(1); //exit the program if the arguments are not correct
        }

        boolean executeOnHosts = args[0].equals("-hosts"); //we check if the first argument is -hosts or -processes

        int totalWorkers = Integer.parseInt(args[1]); 
        if (totalWorkers != args.length - 3) { //we check if N reflects the number of IP addresses/ports given in line args
            System.err.println(String.format("Error: Number of processes/hosts does NOT match given total_number '%d' !!", totalWorkers));
            System.exit(1);
        }

        String executableProgram = args[args.length - 1]; //the last argument is the executable program that we want to run on the workers 
        if (!Execute.isExecutableValid(executableProgram)) { //we check if the executable program is not empty and has a valid extension
            System.err.println("Error: Executable program is missing or don't have '.exe'/'.class' extension !!");
            System.exit(1);
        }

        List<String> adressesOfWorkers = new ArrayList<String>(totalWorkers); //we store the IP_addresses/hosts of the workers to call needed function to execute executable
        for (int i = 2; i < (args.length - 1); i++) { 
            adressesOfWorkers.add(args[i]); //we start from the 3rd argument
        }

        List<Thread> threadsList = new ArrayList<Thread>(totalWorkers); //we store the threads that we will create to execute concurrently the executable program (one thread for each worker) 
        int comunicatingPort = 50505; //we initialize the port number to 50505, to be out of 'well-known' ports range
        for (String currentWorker : adressesOfWorkers) { 
            Thread thread; 
            String local_worker = currentWorker; //we store the current worker in a local variable to avoid concurrency issues
            int local_port = comunicatingPort; //we store the current port in a local variable to avoid concurrency issues
            if (executeOnHosts) { //create threads by the command that we used
                thread = new Thread(() -> Execute.onHosts(local_worker, local_port, executableProgram)); 
            
            } else { 
                thread = new Thread(() -> Execute.onProcesses(local_worker, executableProgram)); 
            }

            comunicatingPort++; //increment the port number for each worker
            threadsList.add(thread); //add thread in list to join them later 
            thread.start(); //start thread execution
        } 
        
        for (Thread thread : threadsList) { 
            try { 
                thread.join(); //wait for all threads to finish
            
            } catch (InterruptedException exception) { 
                System.out.println("Error joining threads: " + exception.getMessage());
            }
        }

        System.out.println("All processes are done."); //print a status message when all threads have finished their work
    }
}