import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client implements Runnable{
    private Socket client;
    private BufferedReader in;
    private PrintWriter out;
    private boolean done;

    @Override
    public void run() {
        try {
            Socket client = new Socket("127.0.0.1",9999);
            out = new PrintWriter(client.getOutputStream(),true);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));

            InputHandler inputHandler = new InputHandler();
            Thread t = new Thread(inputHandler);
            t.start();

            String inMessage;
            while ((inMessage = in.readLine())!=null){
                System.out.println(inMessage);
            }
        }catch (Exception e){
            shutDown();
        }
    }

    class InputHandler implements Runnable{

        @Override
        public void run() {
            try {
                BufferedReader intake = new BufferedReader(new InputStreamReader(System.in));
                while(!done){
                    String message = intake.readLine();
                    if(message.equals("/exit")){
                        intake.close();
                        shutDown();
                    }
                    else {
                        out.println(message);
                    }
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    public void shutDown() {
        done = true;
        try {
            out.close();
            in.close();
            if(!client.isClosed()){
                client.close();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }
}
