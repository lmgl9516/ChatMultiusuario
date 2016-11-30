
/**
 *
 * @author lmgl9
 */


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.ServerSocket;
import java.nio.channels.FileChannel;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Servidor {

  // The server socket.
  private static ServerSocket serverSocket = null;
  // The client socket.
  private static Socket clientSocket = null;
  // This chat server can accept up to maxClientsCount clients' connections.
  private static final int maxClientsCount = 10;
  private static final clientThread[] threads = new clientThread[maxClientsCount];

  public static void main(String args[]) {

    // The default port number.
    int portNumber = 2222;
    if (args.length < 1) {
      System.out.println("Multiusuario servidor por LEO inc. <portNumber>\n"
          + "Now using port number=" + portNumber);
    } else {
      portNumber = Integer.valueOf(args[0]).intValue();
    }

    /*
     * Open a server socket on the portNumber (default 2222). Note that we can
     * not choose a port less than 1023 if we are not privileged users (root).
     */
    try {
      serverSocket = new ServerSocket(portNumber);
    } catch (IOException e) {
      System.out.println(e);
    }

    /*
     * Create a client socket for each connection and pass it to a new client
     * thread.
     */
    while (true) {
      try {
        clientSocket = serverSocket.accept();
        int i = 0;
        for (i = 0; i < maxClientsCount; i++) {
          if (threads[i] == null) {
            (threads[i] = new clientThread(clientSocket, threads)).start();
            System.out.println("cliente conectado");
            break;
          }
        }
        if (i == maxClientsCount) {
          PrintStream os = new PrintStream(clientSocket.getOutputStream());
          os.println("Server too busy. Try later.");
          os.close();
          clientSocket.close();
        }
      } catch (IOException e) {
        System.out.println(e);
      }
    }
  }
}

/*
 * The chat client thread. This client thread opens the input and the output
 * streams for a particular client, ask the client's name, informs all the
 * clients connected to the server about the fact that a new client has joined
 * the chat room, and as long as it receive data, echos that data back to all
 * other clients. The thread broadcast the incoming messages to all clients and
 * routes the private message to the particular client. When a client leaves the
 * chat room this thread informs also all the clients about that and terminates.
 */
class clientThread extends Thread {

  private String clientName = null;
  private String llavePublica = null;
  private DataInputStream is = null;
  private DataOutputStream os= null;
  private Socket clientSocket = null;
  private final clientThread[] threads;
  private int maxClientsCount;

  public clientThread(Socket clientSocket, clientThread[] threads) {
    this.clientSocket = clientSocket;
    this.threads = threads;
    maxClientsCount = threads.length;
  }

  public void run() {
    int maxClientsCount = this.maxClientsCount;
    clientThread[] threads = this.threads;

    try {
      /*
       * Create input and output streams for this client.
       */
      is = new DataInputStream(clientSocket.getInputStream());
      os = new DataOutputStream(clientSocket.getOutputStream());
      String name;
      String respuesta;
    while (true) {
        try{
            os.writeUTF("-.-///Ya tienes usuario? s/n");
        }catch(Exception e){
        }
        respuesta = is.readUTF().trim();
        if ("s".equals(respuesta)) {
            while (true) {
                try{
                    os.writeUTF("-.-///Ingresa tu nombre de usuario.");
                }catch(Exception e){
                }
                name = is.readUTF().trim();
                System.out.println("nombre es "+name);
                int clA=1,clB=2,seA=1,seB=2;
                try{
                     BufferedReader cl1 = new BufferedReader(new FileReader(name+"/"+name+".cer"));
                     BufferedReader cl2 = new BufferedReader(new FileReader("servidor/"+name+".cer"));   
                     BufferedReader se1 = new BufferedReader(new FileReader(name+"/servidor.cer"));   
                     BufferedReader se2 = new BufferedReader(new FileReader("servidor/servidor.cer")); 
                     String sCurrentLine;
                     try{
                        os.writeUTF("-.-///leyendo "+name+"/"+name+".cer");
                    }catch(Exception e){
                    }
                    while ((sCurrentLine = cl1.readLine()) != null) {
                            clA=Integer.valueOf(sCurrentLine);
                    }
                    try{
                        os.writeUTF("-.-///leyendo "+"servidor/"+name+".cer");
                    }catch(Exception e){
                    }
                    while ((sCurrentLine = cl2.readLine()) != null) {
                            clB=Integer.valueOf(sCurrentLine);
                    }
                    try{
                         os.writeUTF("-.-///leyendo "+name+"/servidor.cer");
                    }catch(Exception e){
                    }
                    while ((sCurrentLine = se1.readLine()) != null) {
                            seA=Integer.valueOf(sCurrentLine);
                    }
                    try{
                        os.writeUTF("-.-///leyendo "+"servidor/servidor.cer");
                    }catch(Exception e){
                    }
                    while ((sCurrentLine = se2.readLine()) != null) {
                            seB=Integer.valueOf(sCurrentLine);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (clA==clB&&seA==seB){
                try{
                    os.writeUTF("-.-///CONEXION AUTENTICADA.");
                    os.writeUTF("=?=?=?listo");
                    try{
                        os.writeUTF("-.-///Bienvenido " + name
                        + " al chat de LEO.\n Para salir ingrese /quit en una nueva linea.");
                    }catch(Exception e){
                    }
                    synchronized (this) {
                        for (int i = 0; i < maxClientsCount; i++) {
                            if (threads[i] != null && threads[i] == this) {
                            clientName = "@" + name;
                            break;
                            }
                        }
                        for (int i = 0; i < maxClientsCount; i++) {
                            if (threads[i] != null && threads[i] != this) {  
                            threads[i].os.writeUTF("-.-///*** Un nuevo usuario " + name + " Entro a la aplicacion !!! ***");
                            }
                        }
                    }
                    for (int i = 0; i < maxClientsCount; i++) {
                        if (threads[i] != null && threads[i] != this) {
                             FileChannel inputChannel = null;
                             FileChannel outputChannel = null;
                            try {
                                inputChannel = new FileInputStream(name+"/"+name+".cer").getChannel();
                                outputChannel = new FileOutputStream(threads[i].clientName.substring(1,threads[i].clientName.length())+"/"+name+".cer").getChannel();
                                outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            threads[i].os.writeUTF("-.-///*** certificado " + name+ ".cer recibido ***");
                            try {
                                TimeUnit.SECONDS.sleep(1);
                            } catch (InterruptedException ex) {
                            Logger.getLogger(clientThread.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            threads[i].os.writeUTF("%&/()="+name);
                        }
                    }
                    os.writeUTF("-.-///certificados compartidos.");
                    break;
                }catch(Exception e){
                }
                }
                else{
                    try{
                        os.writeUTF("-.-///!!!!!fallo en autenticacion!!!!!.");
                    }catch(Exception e){
                    }  
                }
            }
        } else {
            while (true) {
                try{
                os.writeUTF("-.-///Ingresa tu nombre.");
                 }catch(Exception e){
                 }
                name = is.readUTF().trim();
                System.out.println("nombre es "+name);
                if (name.indexOf('@') == -1) {
                    break;
                } else {
                os.writeUTF("-.-///El nombre no debe de contener el caracter '@'");
                }
            }
            try{
                os.writeUTF("-.-///Bienvenido " + name
                + " al chat de LEO.\n Para salir ingrese /quit en una nueva linea.");
            }catch(Exception e){
            }
            synchronized (this) {
                for (int i = 0; i < maxClientsCount; i++) {
                    if (threads[i] != null && threads[i] == this) {
                        clientName = "@" + name;
                        break;
                    }
                }
                for (int i = 0; i < maxClientsCount; i++) {
                    if (threads[i] != null && threads[i] != this) {  
                        threads[i].os.writeUTF("-.-///*** Un nuevo usuario " + name + " Entro a la aplicacion !!! ***");
                    }
                }
            }
            try{
                File theDir = new File(name);
                // if the directory does not exist, create it
            if (!theDir.exists()) {
                try{
                    theDir.mkdir();
                } catch(SecurityException se){}        
            }
            os.writeUTF("+*-+*-Configuracion finalizada");
            }catch(Exception e){
            }
            try{
                File file = new File(name+"/servidor.cer");
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write("5994");
            bw.close();
            }catch (IOException e) {
                e.printStackTrace();
            }
            while (true) {
                String line = is.readUTF().trim();
                System.out.println(line);
                if (line.startsWith("/quit")) {
                    break;
                }
                if ("@@++--".equals(line.substring(0,6))){
                    llavePublica=line.substring(6,line.length());
                    System.out.println(llavePublica);
                    try{
                        File file = new File(name+"/"+name+".cer");
                        if (!file.exists()) {
                            file.createNewFile();
                        }
                        FileWriter fw = new FileWriter(file.getAbsoluteFile());
                        BufferedWriter bw = new BufferedWriter(fw);
                        bw.write(llavePublica);
                        bw.close();
                        }catch (IOException e) {
                            e.printStackTrace();
                        }
                        try{
                            File file = new File("servidor/"+name+".cer");
                            if (!file.exists()) {
                                file.createNewFile();
                            }
                            FileWriter fw = new FileWriter(file.getAbsoluteFile());
                            BufferedWriter bw = new BufferedWriter(fw);
                            bw.write(llavePublica);
                            bw.close();
                        }catch (IOException e) {
                            e.printStackTrace();
                        }
                        os.writeUTF("-.-///Certificado: "+clientName.substring(1,clientName.length())+".cer fue generado exitosamente");
                        for (int i = 0; i < maxClientsCount; i++) {
                            if (threads[i] != null && threads[i] != this) {
                                try{
                                    File file = new File(threads[i].clientName.substring(1,threads[i].clientName.length())+"/"+name+".cer");
                                    if (!file.exists()) {
                                        file.createNewFile();
                                    }
                                    FileWriter fw = new FileWriter(file.getAbsoluteFile());
                                    BufferedWriter bw = new BufferedWriter(fw);
                                    bw.write(llavePublica);
                                    bw.close();
                                }catch (IOException e) {
                                    e.printStackTrace();
                                }
                                threads[i].os.writeUTF("-.-///*** certificado " + name+ ".cer recibido ***");
                                try {
                                    TimeUnit.SECONDS.sleep(1);
                                } catch (InterruptedException ex) {
                                Logger.getLogger(clientThread.class.getName()).log(Level.SEVERE, null, ex);
                                }
                                threads[i].os.writeUTF("%&/()="+name);
                            }
                        }
                        break;  
                }
            }  
        }
        break;
    }
      /* Start the conversation. */  
    while (true) {
        String line = is.readUTF().trim();
        System.out.println(line);
        if (line.startsWith("/quit")) {
            break;
        }
        /* If the message is private sent it to the given client. */
        if (line.startsWith("@")) {
            String[] words = line.split("\\s", 2);
            if (words.length > 1 && words[1] != null) {
                words[1] = words[1].trim();
            if (!words[1].isEmpty()) {
                synchronized (this) {
                for (int i = 0; i < maxClientsCount; i++) {
                    if (threads[i] != null && threads[i] != this
                        && threads[i].clientName != null
                        && threads[i].clientName.equals(words[0])) {
                            threads[i].os.writeUTF("#$%#$%"+this.clientName);  
                            try {
                                TimeUnit.SECONDS.sleep(1);
                            } catch (InterruptedException ex) {
                                Logger.getLogger(clientThread.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            threads[i].os.writeUTF(words[1]);
                            /*
                             * Echo this message to let the client know the private
                             * message was sent.
                             */
                            this.os.writeUTF("-.-///**enviado exitosamente");
                            break;
                    }
                }
                }
            }
            }
        } else {
          /* The message is public, broadcast it to all other clients. */
            synchronized (this) {
                for (int i = 0; i < maxClientsCount; i++) {
                    if (threads[i] != null && threads[i].clientName != null) {
                        threads[i].os.writeUTF("<" + name + "> " + line);
                    }
                }
            }
        }
    }
    synchronized (this) {
        for (int i = 0; i < maxClientsCount; i++) {
            if (threads[i] != null && threads[i] != this
              && threads[i].clientName != null) {
                threads[i].os.writeUTF("-.-///*** The user " + name + " is leaving the chat room !!! ***");
            }
        }
    }
    os.writeUTF("-.-///*** Bye " + name + " ***");

      /*
       * Clean up. Set the current thread variable to null so that a new client
       * could be accepted by the server.
       */
    synchronized (this) {
        for (int i = 0; i < maxClientsCount; i++) {
            if (threads[i] == this) {
                threads[i] = null;
            }
        }
    }
      /*
       * Close the output stream, close the input stream, close the socket.
       */
    is.close();
    os.close();
    clientSocket.close();
    } catch (IOException e) {
    }
  }
}
