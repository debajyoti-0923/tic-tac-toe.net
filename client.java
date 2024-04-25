import java.io.*;
import java.net.*;

class client{
    static int roomId;
    static void startGame(DataInputStream is,DataOutputStream os,BufferedReader br) throws IOException{
        String res=is.readUTF();
        System.out.println(res);
        String msg;
        String stats;
        while(true){
            res=is.readUTF();
            if(res.equals("yc")){
                while(true){
                    System.out.println("Enter your move: ");
                    msg=br.readLine();
                    if(msg.matches("[0-9]")){
                        os.writeUTF("m"+msg+roomId);
                        break;
                    }else{
                        System.out.println("Incorrect Input");
                    }
                }
            }else if(res.startsWith("gb")){
                // stats=res.split("[|]");
                // stats[0]=stats[0].substring(2);
                // System.out.println("gb "+stats[0]);
                // System.out.println(stats[1]);
                // System.out.println(stats[2]);

                // if(stats[1].equals("-1")==false){

                // }
                stats=res.substring(2);
                System.out.println(stats);
            }else if(res.startsWith("yw")){
                System.out.println(res);
                System.out.println("you win");
                return;
            }else if(res.startsWith("yl")){
                System.out.println(res);
                System.out.println("you Lose");
                return;
            }
            else
                System.out.println(res);
        }
        
    }
    public static void main(String[] args) throws Exception{

        BufferedReader br=new BufferedReader(new InputStreamReader(System.in));
        String name=br.readLine();
        
        Socket client=new Socket("localhost",4069);
        DataOutputStream os=new DataOutputStream(client.getOutputStream());
        DataInputStream is=new DataInputStream(client.getInputStream());
        os.writeUTF(name);
        String res=is.readUTF();
        System.out.println(res);
        roomId=0;

        if(res.equals("#accepted")==false){
            System.out.println("Server didn't respond");
            return;
        }
        // MyThreadRead read=new MyThreadRead(is);
        // MyThreadWrite wrt=new MyThreadWrite(os);
        // wrt.start();
        // read.start();
        // wrt.join();
        // read.join();
        
        while(true){
            System.out.println("1.Create Room\n2.Join room\n3.Exit\nEnter Choice: ");
            int c=Integer.parseInt(br.readLine());
            if(c==3){
                System.out.println("close");
                return;
            }
            if(c==1){
                os.writeUTF("c");
                res=is.readUTF();
                System.out.println(res);
                roomId=Integer.parseInt(res.substring(4));
                System.out.println(roomId);
                System.out.println("Waiting for other player to join..");
                res=is.readUTF();
                if(res.startsWith("cn")){
                    System.out.println(res.substring(2)+" Connected ");
                }
                while(true){
                    System.out.println("1.Start\n2.Exit\nEnter Choice: ");
                    c=Integer.parseInt(br.readLine());
                    if(c==2){
                        os.writeUTF("x"+roomId);
                    }else if(c==1){
                        os.writeUTF("s"+roomId);
                    }else{
                        System.out.println("Incorrect Input");
                        continue;
                    }
                    res=is.readUTF();
                    if(res.equals("stg")){
                        System.out.println("starting");   
                        startGame(is,os,br);
                    }else if(res.equals("nep")){
                        System.out.println("Not enough players");
                    }else if(res.equals("rc")){
                        System.out.println("room closed");
                        break;
                    }
                    // MyThreadRead read=new MyThreadRead(is,br,os);
                    // read.start();
                    // read.join();
                }
            }
            else if(c==2){
                os.writeUTF("r");
                res=is.readUTF();
                System.out.println("Available rooms: \n"+res);
                System.out.println("Enter roomId: ");
                int id=Integer.parseInt(br.readLine());
                os.writeUTF("j"+id);
                res=is.readUTF();
                System.out.println(res);
                if(res.equals("Connected")==false){
                    System.out.println("Not connected");
                }else{
                    roomId=id;
                    while(true){
                        System.out.println("1.Ready\n2.Exit\nEnter Choice: ");
                        c=Integer.parseInt(br.readLine());
                        if(c==2){
                            os.writeUTF("l"+roomId);
                            System.out.println("Exiting room");
                            break;
                        }else if(c==1){
                            System.out.println("Waiting for host...");
                            res=is.readUTF();
                            if(res.equals("stg")){
                                System.out.println("starting");   
                                startGame(is,os,br);
                            }else if(res.equals("rc")){
                                System.out.println("room closed");
                                break;
                            }
                        }else{
                            System.out.println("Incorrect Input");
                        }
                    }
                }

            }
        }


        // System.out.println("1.Create Room\n2.Join room\n3.Exit\nEnter Choice: ");
        // int c=Integer.parseInt(br.readLine());
        // if(c==3){
        //     System.out.println("close");
        //     // read.interrupt();
        //     return;
        // }
        // if(c==1){
        //     os.writeUTF("c");
        //     res=is.readUTF();
        //     System.out.println(res);
        //     roomId=Integer.parseInt(res.substring(4));
        //     System.out.println(roomId);

        //     while(true){
        //         System.out.println("1.Start\n2.Exit\nEnter Choice: ");
        //         c=Integer.parseInt(br.readLine());
        //         if(c==2){
        //             System.out.println("close");
        //             return;
        //         }
        //         os.writeUTF("s"+roomId);
        //         System.out.println(is.readUTF());
        //         while(true){
                    
        //         }
                
        //     }

        // }
        // if(c==2){
        //     os.writeUTF("r");
        //     res=is.readUTF();
        //     System.out.println("Available rooms: \n"+res);
        //     System.out.println("Enter roomId: ");
        //     int id=Integer.parseInt(br.readLine());
        //     os.writeUTF("j"+id);
        //     res=is.readUTF();
        //     System.out.println(res);
        //     if(res.equals("Connected")){
        //         roomId=id;
        //     }else{
        //         System.out.println("Not connected");
        //     }
        // }


    }
}

class MyThreadRead extends Thread{
    DataInputStream is;
    DataOutputStream os;
    BufferedReader br;
    public MyThreadRead(DataInputStream i,BufferedReader b,DataOutputStream o){
        is=i;
        br=b;
        os=o;
    }
    public void run(){
        try{
            String msg=null;
            while ( true) {
                msg=is.readUTF();
                if(msg!=null){
                    System.out.println(msg);
                    if(msg.startsWith("rmid")){
                        client.roomId=Integer.parseInt(msg.substring(4));
                        System.out.println(client.roomId);
                    }
                    if(msg.startsWith("yc")){
                        System.out.println("Enter your choice");
                        msg=br.readLine();
                        os.writeUTF(msg);
                    }
                }
                msg=null;
            }
        }catch(Exception e){
            System.out.println("prob");
        }
    }
}
class MyThreadWrite extends Thread{
    private DataOutputStream os;
    public BufferedReader br;
    public String clname="@client0";
    public MyThreadWrite(DataOutputStream o){
        os=o;
        try{
            InputStreamReader isr=new InputStreamReader(System.in);
            br=new BufferedReader(isr);
        }
        catch(Exception e){
            System.out.println("prob2");
        }
    }
    public void run(){
        try{
            while (true) {
                String msg=br.readLine();
                os.writeUTF(msg);
            }
        }catch(Exception e){System.out.println("prob3");}
    }
}