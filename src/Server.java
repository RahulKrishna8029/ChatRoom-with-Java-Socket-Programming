import com.sun.jdi.event.MethodExitEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable{
    private ArrayList<ConnectionHandler> connections ;
    private ServerSocket server;
    private boolean done;
    private ExecutorService pool;


    public Server(){
        connections = new ArrayList<>();
        done = false;
    }

    @Override
    public void run() {
        try {
            ServerSocket server = new ServerSocket(9999);
            pool = Executors.newCachedThreadPool();
           while(!done) {
               Socket client = server.accept();
               ConnectionHandler clientHandler = new ConnectionHandler(client);
               connections.add(clientHandler);
               pool.execute(clientHandler);
           }
        } catch (Exception e) {
            shutDown();
        }
    }
    public void Broadcast(String message){
        for(ConnectionHandler ch : connections){
            if(ch!=null){
                ch.SendMessage(message);
            }
        }
    }

    public void shutDown() {
        try {
            done = true;
            if (!server.isClosed()) {
                server.close();
            }
            for (ConnectionHandler ch : connections){
                ch.shutDown();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    class ConnectionHandler implements Runnable{
        private Socket client;
        private BufferedReader in;
        private PrintWriter out;
        private String UserName;

        public ConnectionHandler(Socket client){
            this.client=client;
        }

        @Override
        public void run(){
            try {
                out = new PrintWriter(client.getOutputStream(),true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                out.println("Enter the username :");
                UserName=in.readLine();
                if(UserName.isEmpty()){
                    System.out.println("Enter a valid Username..");
                }
                else{
                    System.out.println(UserName+" is in chat");
                    Broadcast(UserName+" joined the room");
                    String Message;
                    while((Message=in.readLine())!=null){
                        if(Message.startsWith("/user")){
                            String[] messageSplit = Message.split(" ",2);
                            if(messageSplit.length==2){
                                Broadcast(UserName+" is changed to "+messageSplit[1]);
                                System.out.println(UserName+" is renamed to "+messageSplit[1]);
                                UserName=messageSplit[1];
                                out.println("Renamed successfully to :"+UserName);
                            }
                            else {
                                out.println("No Username is provided..");
                            }
                        }
                        else if(Message.startsWith("/exit")){
                            Broadcast(UserName+" is exited the room..");
                            shutDown();
                        }
                        else{
                            Broadcast(UserName+" : "+Message);
                        }
                    }
                }
            }catch (IOException e){
                shutDown();
            }
        }
        public void SendMessage(String message){
            out.println(message);
        }

        public void shutDown() {
            try {
                in.close();
                out.close();
                if (!client.isClosed()) {
                    client.close();
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.run();
    }
}
