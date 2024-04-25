import java.io.*;
import java.net.*;

class client{
    static int roomId;
    static char getchar(char c,char d){
        if(c=='1') return 'X';
        if(c=='2') return 'O';
        return d;
    }

    static void printBoard(String stats){
        System.out.println("Your Board");
        System.out.println(
            " "+getchar(stats.charAt(0), '1')+
            " | "+getchar(stats.charAt(1), '2')+
            " | "+getchar(stats.charAt(2), '3')+" \n"+
            "---+---+---\n"+
            " "+getchar(stats.charAt(3), '4')+
            " | "+getchar(stats.charAt(4), '5')+
            " | "+getchar(stats.charAt(5), '6')+" \n"+
            "---+---+---\n"+
            " "+getchar(stats.charAt(6), '7')+
            " | "+getchar(stats.charAt(7), '8')+
            " | "+getchar(stats.charAt(8), '9')+" \n"
        );
    }
    static int startGame(DataInputStream is,DataOutputStream os,BufferedReader br) throws IOException{
        String res=is.readUTF();
        printBoard(res);
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
                printBoard(stats);
            }else if(res.startsWith("yw")){
                stats=res.substring(2);
                printBoard(stats);
                System.out.println("YOU WIN !!");
                return 0;
            }else if(res.startsWith("yl")){
                stats=res.substring(2);
                printBoard(stats);
                System.out.println("YOU LOSE !!");
                return 0;
            }else if(res.startsWith("d")){
                stats=res.substring(1);
                printBoard(stats);
                System.out.println("DRAW !!");
                return 0;
            }else if(res.startsWith("pel")){
                System.out.println(res.substring(3)+" left");
                return 0;
            }else if(res.equals("rec")){
                System.out.println("Game closed due to Inactivity");
                return 1;
            }
            else
                System.out.println(res);
        }
        
    }
    public static void main(String[] args) throws Exception{
        System.out.println("Enter Your Name: ");
        BufferedReader br=new BufferedReader(new InputStreamReader(System.in));
        String name=br.readLine();
        
        Socket client=new Socket("localhost",4069);
        client.setSoTimeout(25000);
        DataOutputStream os=new DataOutputStream(client.getOutputStream());
        DataInputStream is=new DataInputStream(client.getInputStream());
        os.writeUTF(name);
        String res=is.readUTF();
        roomId=0;

        if(res.equals("#accepted")==false){
            System.out.println("Server didn't respond");
            return;
        }else{
            System.out.println("Welcome "+name+" to TIC-TAC-TOE");
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
                System.out.println("Exiting..");
                return;
            }
            if(c==1){
                os.writeUTF("c");
                res=is.readUTF();
                roomId=Integer.parseInt(res.substring(4));
                System.out.println("Your Room ID: "+roomId);
                
                while(true){
                    os.writeUTF("s"+roomId);
                    res=is.readUTF();
                    if(res.equals("stg")){
                        System.out.println("\nStarting the game..");   
                        int gameS=startGame(is,os,br);
                        if(gameS==1){
                            break;
                        }
                    }else if(res.startsWith("pe")){
                        if(res.charAt(2)=='l'){
                            System.out.println(res.substring(3)+" Left the room");
                            Thread.sleep(1000);
                            while(is.available()>0)
                                is.readUTF();
                        }
                        System.out.println("\nWaiting for other player to join..");
                        try{
                            res=is.readUTF();
                        }catch(Exception e){
                            System.out.println("\nClosing room for Inactivity..");
                            os.writeUTF("x"+roomId);
                            res=is.readUTF();
                            break;
                        }
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
                        System.out.println("\nRoom Closed");
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
                                System.out.println("\nStarting the game..");   
                                int gameS=startGame(is,os,br);
                                if(gameS==1){
                                    break;
                                }
                            }else if(res.equals("rc")){
                                System.out.println("\nRoom closed");
                                break;
                            }
                            System.out.println("Exit? (y/n)");
                            res=br.readLine();
                            if(res.equals("y")){
                                os.writeUTF("l"+roomId);
                                System.out.println("Exiting Room..");
                                break;
                            }
                    }
                }

            }
        }

    }
}

