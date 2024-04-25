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
            try{
                res=is.readUTF();
            }catch(Exception e){continue;}
            
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
            }else if(res.startsWith("d")){
                System.out.println(res);
                System.out.println("Draw");
                return;
            }else if(res.startsWith("pel")){
                System.out.println(res.substring(3)+" left");
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
        client.setSoTimeout(10000);
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
        
        while(true){
            Thread.sleep(1000);
            while(is.available()>0)
                is.readUTF();
            System.out.println("\nMENU:\n1.Create Room\n2.Join room\n3.Exit\nEnter Choice: ");
            int c=0;
            try{
                c=Integer.parseInt(br.readLine());
            }catch(Exception e){
                System.out.println("Incorrect Input");
                continue;
            }
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
                
                while(true){
                    os.writeUTF("s"+roomId);
                    res=is.readUTF();
                    if(res.equals("stg")){
                        System.out.println("starting");   
                        startGame(is,os,br);
                    }else if(res.startsWith("pe")){
                        if(res.charAt(2)=='l'){
                            System.out.println(res.substring(3)+" left");
                            Thread.sleep(1000);
                            while(is.available()>0)
                                is.readUTF();
                        }
                        System.out.println("Waiting for other player to join..");
                        try{
                            res=is.readUTF();
                        }catch(Exception e){
                            System.out.println("closing room for inactivity..");
                            os.writeUTF("x"+roomId);
                            res=is.readUTF();
                            break;
                        }
                        // System.err.println(res);
                        if(res.startsWith("cn")){
                            System.out.println(res.substring(2)+" Connected ");
                        }
                        continue;
                    }
                    System.out.println("Exit? (y/n)");
                    res=br.readLine();
                    if(res.equals("y")){
                        os.writeUTF("x"+roomId);
                        res=is.readUTF();
                        System.out.println("room closed");
                        break;
                    }

                }
            }
            else if(c==2){
                os.writeUTF("r");
                res=is.readUTF();
                if(res.equals("")){
                    System.out.println("No open rooms");
                    continue;
                }
                System.out.println("Available rooms: \n"+res);
                System.out.println("Enter roomId: ");
                int id=0;
                try{
                    id=Integer.parseInt(br.readLine());
                }catch(Exception e){
                    System.out.println("Incorrect input");
                    continue;
                }
                os.writeUTF("j"+id);
                res=is.readUTF();
                System.out.println(res);
                if(res.equals("Connected")==false){
                    System.out.println("Not connected");
                }else{
                    roomId=id;
                    
                    while(true){
                            System.out.println("Waiting for host...");
                            try{
                                res=is.readUTF();
                            }catch(Exception e){
                                System.out.println("Unresponsive host..exiting room");
                                os.writeUTF("l"+roomId);
                                break;
                            }
                            if(res.equals("stg")){
                                System.out.println("starting");   
                                startGame(is,os,br);
                            }else if(res.equals("rc")){
                                System.out.println("room closed");
                                break;
                            }
                            System.out.println("Exit? (y/n)");
                            res=br.readLine();
                            if(res.equals("y")){
                                os.writeUTF("l"+roomId);
                                System.out.println("exiting room");
                                break;
                            }
                    }
                }

            }
        }

    }
}