class test{
    public static void main(String[] args) {
        // System.out.println(" __ \n/  \\\n\\__/");
        // System.out.println("\\  /\n \\/ \n /\\ \n/  \\");
        // String s="000000000|-1|1";
        // String a[]=s.split("[|]");
        // for(String k:a)
        //     System.out.println(k);
        a thread=new a(Thread.currentThread());
        thread.start();
        while(true){
            System.out.println("main");
            try{
                Thread.sleep(1000);
                Thread.currentThread().wait();
            }
            catch(Exception e){}
        }
    }
}

class a extends Thread{
    Thread mainT;
    a(Thread t){
        mainT=t;
    }
    public void run(){
        while (true) {
            
            System.out.println("running thr");
            mainT.notify();
            try{
                Thread.sleep(10000);
            }catch(Exception e){}
        }
    }
}