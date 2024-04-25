import java.io.*;
import java.net.*;
import java.util.*;
class gameData{
    Socket x=null;
    Socket y=null;
    int board[]=new int[]{0,0,0,0,0,0,0,0,0}; 
    int cp=0;
    String left=null;
    // gameData(int x_,int y_){
    //     x=x_;
    //     y=y_;
    // }

    boolean addP(Socket p){
        if(y==null){
            y=p;
        }else if(x==null){
            x=p;
        }else{
            return false;
        }

        if(x!=null && y!=null){
            cp=1;
        }
        return true;
    }
    // 036 -1
    // 012 -2
    // 048 -3
    // 147 -4
    // 258 -5
    // 246 -6
    // 345 -7
    // 678 -8
    int check(){
        if(board[0]==board[3] && board[3]==board[6] && board[6]!=0) return 1*10+board[6];
        if(board[0]==board[1] && board[1]==board[2] && board[2]!=0) return 2*10+board[2];
        if(board[0]==board[4] && board[4]==board[8] && board[8]!=0) return 3*10+board[8];
        if(board[1]==board[4] && board[4]==board[7] && board[7]!=0) return 4*10+board[7];
        if(board[2]==board[5] && board[5]==board[8] && board[8]!=0) return 5*10+board[8];
        if(board[2]==board[4] && board[4]==board[6] && board[6]!=0) return 6*10+board[6];
        if(board[3]==board[4] && board[4]==board[5] && board[5]!=0) return 7*10+board[5];
        if(board[6]==board[7] && board[7]==board[8] && board[8]!=0) return 8*10+board[8];
        for(int i=0;i<9;i++){
            if(board[i]==0) return -1;
        }
        return 10;
    }
    String update(int ind,Socket p){
        int t=0;
        if(p==x){
            t=1;
        }else if(p==y){
            t=2;
        }else{
            return "f2";
        }
        if(cp==0) return "f1";
        if(ind<0 || ind>8 || cp!=t ||board[ind]!=0){
            return "f2";
        }
        board[ind]=t;
        cp=(cp==1)?2:1;
        return stats();
    }

    String parseBoard(){
        String s="";
        for(int i=0;i<9;i++){
            s+=board[i];
        }
        return s;
    }

    String stats(){
        String res=parseBoard();
        int r=check();
        res=res+"|"+r+"|"+((r==-1)?cp:-1);
        return res;
    }

    void reset(){
        for(int i=0;i<9;i++){
            board[i]=0;
        }
        Socket temp=x;
        x=y;
        y=temp;
        cp=1;
    }
}
class Server{
    public static void main(String[] args){
        Server a =new Server();
        // gameData g=new gameData();
        // g.addP(1, 1);
        // System.out.println(g.update(0, 1));
        // g.addP(2, 2);
        // System.out.println(g.update(0, 1));
        // System.out.println(g.update(4, 2));
        // System.out.println(g.update(6, 1));
        // System.out.println(g.update(5, 2));
        // System.out.println(g.update(3, 1));
        // System.out.println(g.parseBoard());
        // g.reset();
        // System.out.println(g.stats());
        a.doConnections();
        
    }
    public void doConnections(){
        try{
            ServerSocket server=new ServerSocket(4069);
            HandleCallThread msgrouterThread=new HandleCallThread();
            msgrouterThread.start();
            while(true){
                Socket client=server.accept();
                if(client!=null){
                    DataOutputStream os=new DataOutputStream(client.getOutputStream());
                    DataInputStream is=new DataInputStream(client.getInputStream());
                    String requestedClname=is.readUTF();
                    os.writeUTF("#accepted");
                    msgrouterThread.clientList.put(requestedClname,client);
                }
            }
        }
        catch(Exception e){
            System.out.println("Error Occured oops"+e.getMessage());
        }
    }
}
class HandleCallThread extends Thread{
    public HashMap<String,Socket> clientList=new HashMap<String,Socket>();
    public DataInputStream is=null;
    public DataOutputStream os=null;

    HashMap<Integer,gameData> rooms=new HashMap<Integer,gameData>();
    
    int globalRoomId=1000;

    public void run(){
        String msg="";
        System.out.println("Server running");

        while(true){
            try{
                if(clientList.isEmpty()==false){
                    for(String key:clientList.keySet()){
                        Socket playerSocket=clientList.get(key);

                        is=new DataInputStream(playerSocket.getInputStream());
                        if(is.available()>0){
                            msg=is.readUTF();
                            System.out.println(msg);
                            if(msg.charAt(0)=='c'){
                                createRoom(playerSocket);
                            }else if(msg.charAt(0)=='j'){
                                joinRoom(playerSocket,Integer.parseInt(msg.substring(1)),key);
                            }else if(msg.charAt(0)=='r'){
                                String res="";
                                for(int k:rooms.keySet()){
                                    gameData g=rooms.get(k);
                                    if(g.cp==0)
                                        res+=k+"\n";
                                }
                                send(playerSocket, res,0);
                            }else if(msg.charAt(0)=='s'){
                                int roomId_=Integer.parseInt(msg.substring(1));
                                start_(playerSocket,roomId_);
                            }else if(msg.charAt(0)=='m'){
                                int roomId_=Integer.parseInt(msg.substring(2));
                                int mv=msg.charAt(1)-49;
                                move(playerSocket,mv,roomId_);
                            }else if(msg.charAt(0)=='x'){
                                int roomId_=Integer.parseInt(msg.substring(1));
                                closeRoom(playerSocket, roomId_);
                            }else if(msg.charAt(0)=='l'){
                                int roomId_=Integer.parseInt(msg.substring(1));
                                exitRoom(playerSocket,roomId_,key);
                            }
                        }
                    }
                }
                Thread.sleep(1);
            }catch(Exception e){
                System.out.println(e.getMessage());
            }
        }
    }

    void start_(Socket p,int rid) throws IOException{
        gameData g=rooms.get(rid);
        if(g.x==null || g.y==null){
            // if(g.left!=null){
            //     send(p,"pel"+g.left,rid);
            // }else
                send(p,"pen",rid);
            return;
        }
        g.reset();
        String res_=g.stats();
        sendBoth(g, "stg",rid);
        sendBoth(g, res_.substring(0,9),rid);
        send(g.x,"yc",rid);
    }
    void sendBoth(gameData g,String data,int rid) throws IOException{
        DataOutputStream os1;
        DataOutputStream os2;
        try{
            if(g.x!=null){
                os1=new DataOutputStream(g.x.getOutputStream());
                os1.writeUTF(data);
            }
            if(g.y!=null){
                os2=new DataOutputStream(g.y.getOutputStream());
                os2.writeUTF(data);
            }
        }catch(Exception e){
            if(e.getMessage().equals("Connection reset by peer")){
                rooms.remove(rid);
            }
        }
    }
    void send(Socket p,String data,int rid) throws IOException{
        try{
            DataOutputStream os=new DataOutputStream(p.getOutputStream());
            os.writeUTF(data);
        }catch(Exception e){
            if(e.getMessage().equals("Connection reset by peer")){
                rooms.remove(rid);
            }
        }
    }

    void createRoom(Socket p)throws IOException{
        gameData g=new gameData();
        g.addP(p);
        rooms.put(globalRoomId, g);
        //send to socket room,ridID
        String res="rmid"+globalRoomId;
        System.out.println(res);
        send(p,res,0);
        globalRoomId+=1;
    }
    void joinRoom(Socket p,int rid,String n) throws IOException{
        if(rooms.containsKey(rid)==false){
            System.out.println(rid);
            send(p,"Incorrect room ID",rid);
            return;
        }
        gameData g=rooms.get(rid);
        if(g.addP(p)==false){
            send(p,"Room full",rid);
        }else{
            send(g.y,"cn"+n,rid);
            send(p,"Connected",rid);
        }
    }
    void closeRoom(Socket p,int rid) throws IOException{
        gameData g=rooms.get(rid);
        sendBoth(g,"rc",rid);
        rooms.remove(rid);
    }
    void exitRoom(Socket p,int rid,String n) throws IOException{
         gameData g=rooms.get(rid);
         if(g==null) return;
         if(g.x==p){
            g.x=null;
         }else if(g.y==p){
            g.y=g.x;
            g.x=null;
         }
         g.left=n;
         g.cp=0;
         send(g.y,"pel"+n,rid);
         System.out.println(n+" left");
    }
    void move(Socket p,int m,int rid) throws IOException{
        gameData g=rooms.get(rid);
        String res=g.update(m, p);
        if(res.equals("f1")){
            send(p, "Game not started yet",rid);
            send(p, "yc",rid);
            return;
        }else if(res.equals("f2")){
            send(p, "Incorrect Input",rid);
            send(p, "yc",rid);
            return;
        }
        String r[]=res.split("[|]");
        sendBoth(g, "gb"+r[0],rid);

        if(r[1].equals("-1")==false){
            if(r[1].charAt(1)=='1'){
                send(g.x,"yw"+r[0]+r[1].charAt(0),rid);
                send(g.y,"yl"+r[0]+r[1].charAt(0),rid);
            }else if(r[1].charAt(1)=='2'){
                send(g.x,"yl"+r[0]+r[1].charAt(0),rid);
                send(g.y,"yw"+r[0]+r[1].charAt(0),rid);
            }else{
                send(g.x,"d"+r[0],rid);
                send(g.y,"d"+r[0],rid);
            }
            return;
        }
        if(r[2].equals("1"))
            send(g.x,"yc",rid);
        else if(r[2].equals("2"))
            send(g.y,"yc",rid);
    }


}

