package IO;

import java.io.IOException;
import java.net.Socket;
import java.util.Date;

public class BIOClient {

    public static void main(String[] args) {
        //TODO创建多个线程，模型多个客户端连接服务端
        new Thread(()->{
            try {
                Socket socket = new Socket("127.0.0.1",1111);
                while (true){
                    try {
                        socket.getOutputStream().write((new Date()+"：hello ").getBytes());
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
