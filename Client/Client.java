import java.io.*;
import java.net.*;


public class Client {

    public static DataInputStream in;
    public static DataOutputStream out;

    public static int handle_command(String line, BufferedReader command_reader){
        String[] commands = line.split(" ");
        String command = commands[0];
        if (command.equals("exit"))
            return 1;
        else if (command.equals("help")){
            System.out.println("Available commands:");
            System.out.println("\tlogin -> Authenticate yourself");
            System.out.println("\tch-pass -> Change your password");
            System.out.println("\tch-server-info -> Change server info");
            System.out.println("\tserver-ls -> Show files in the current server directory");
            System.out.println("\tserver-cd dir_name -> Change server directory to dir_name");
            System.out.println("\tclient-ls -> Show files in the current client directory");
            System.out.println("\tclient-cd dir_name -> Change client directory to dir_name");
            System.out.println("\tdownload file_name -> Download file_name from server");
            System.out.println("\tupload file_name -> Upload file_name to server");
            System.out.println("\texit -> Exit client application");
            System.out.println("\thelp -> Show command list");
        }
        else if(command.equals("login")){
            System.out.println("Enter username:");
            String username = command_reader.readLine();
            System.out.println("Enter password:");
            String password = command_reader.readLine();

            out.write(username);
            //encrypt the password
            out.write(password);
            out.flush();


        }
        else if(command.equals("ch-pass")){
            
        }
        else if(command.equals("ch-server-info")){
            
        }
        else if(command.equals("server-ls")){
            
        }
        else if(command.equals("client-ls")){
            
        }
        else if(command.equals("server-cd")){
            
        }
        else if(command.equals("client-cd")){
            
        }
        else if(command.equals("download")){
            
        }
        else if(command.equals("upload")){
            
        }
        return 0;
    } 
    public static void main(String[] args) {
        
        // read servers' info from config file

        String primary_server_name, secondary_server_name, primary_server_port, secondary_server_port; 

        try (FileReader fr = new FileReader("config")) {
            BufferedReader br = new BufferedReader(fr);
            String line = br.readLine();
            String[] info = line.split(" ");
            primary_server_name= commands[0];
            primary_server_port= commands[1];
            line = br.readLine();
            info = line.split(" ");
            secondary_server_name= commands[0];
            secondary_server_port= commands[1];

        } catch(FileNotFoundException f) {
            System.out.println("File not found: "+f.getMessage());
            f.printStackTrace();
        }
        
    
        Socket s = null;


        try{
            s = new Socket(primary_server_name, secondary_server_name);

            System.out.println("SOCKET=" + s);
            in = new DataInputStream(s.getInputStream());
	        out = new DataOutputStream(s.getOutputStream());


            String command = "";
            InputStreamReader input = new InputStreamReader(System.in);
            BufferedReader command_reader = new BufferedReader(input);

            System.out.println("Enter command: (help for more info)");
            while( true){
                try{
                    command= command_reader.readLine();
                    if (handle_command(command, command_reader))
                        break;
                    
                }catch (Exception e) {
		            System.out.println("message:" + e.getMessage());
                }
            }

            System.out.println("Exiting client");


        }catch (UnknownHostException e){
	        System.out.println("Sock:" + e.getMessage());

        } finally {
	    if (s != null){
            try {
                s.close();
            } catch (IOException e) {
                System.out.println("close:" + e.getMessage());
            }
		}
	}
    }
}