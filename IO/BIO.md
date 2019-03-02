JAVA 中的BIO、NIO、AIO 理解为java语言对操作系统的各种模型的封装。程序员在使用这些API的时候，不需要关心操作系统层面的知识
，也不需要根据不同的操作系统编写不同的代码。只需要使用java中的API就可以了
基础概念：
    同步与异步
    同步：同步就是发起一个调用后，被调用者未处理完之前的请求之前，调用返回
    异步：异步就是发起一个调用后，立刻得到被调用者的回应表示已接收到请求，但是被调用者并没有返回结果，此时我们可以处理其
          它的请求，被调用者通常依靠事件，回调等机制通知调用者其返回结果
    同步与异步的区别最大在于异步的话调用者不需要等待处理结果，被调用者会通过回调等机制来通知调用者其返回的结果。
    
   阻塞与非阻塞
   阻塞：阻塞就是发起一个请求，调用者一直等待请求结果返回，也就是当前线程会被挂起，无法从事其他任务，只有当条件就绪才能继续
   非阻塞：非阻塞就是发起一个请求，调用者不用一直等着结果返回，可以先去干其他事情。
   
   
1、BIO（Blocking I/O）
   1.1 传统的BIO
   BIO 通信（一请求，一回答）模型图如下:
   ![images](https://github.com/CaoWenCool/JDK/blob/master/IO/image/BIO%E9%80%9A%E4%BF%A1%E6%A8%A1%E5%9E%8B%E5%9B%BE.jpg)
   采用BIO通信模型的服务端，通常由一个独立的Acceptor线程负责监听客户端的连接。我们一般通过在 while(true) 循环中服务端
   会调用accept()方法等待客户端的连接方式监听请求，请求一旦接收到一个连接请求，就可以建立通信套接字在这个通信套接字上
   进行读写操作，此时不能再接收其他客户端连接请求，只能等待同当前连接的客户端的操作执行完成，不过可以通过多线程来支持
   多个客户端的连接
   
   如果要让BIO通信模型能够在同时处理多个客户请求，就必须使用多线程（主要原因是：socket.accept(),socket.read()、
   socket().write() 涉及的三个主要函数同时同步阻塞的），也就是说在接收客户端连接请求之后为每个客户端创建一个新的线程进行
   链路处理，处理完成之后，通过输出流返回应答给客户端，线程销毁。这就是典型的一请求一应答通信模型。我们可以设想一下如果这个
   连接不做任何事情的haul，就会造成不必要的线程开销，不过可以通过线程池机制改善，线程池还可以让线程的创建和回收成本相对
   较低。使用FixedThreadPool 可以有效的控制了线程的最大数量，保证了系统有限的资源控制，实现了N（客户端请求数量）：M（
   处理客户端请求的线程数量）的伪异步I/O墨香（N可以远远大于M），
   
   设想一下当客户端并发访问量增加后这种模型会出现什么问题呢？
   在JAVA虚拟机中，线程是宝贵的资源，线程的创建和销毁成本很高。除此之外，编程的切换成本也很高，尤其在Linux这样的操作系统
   中，线程本质上就是一个进进程，创建和销毁线程都是重量级的系统函数。如果并发访问量增加会导致线程数量急剧膨胀可能会导致
   线程堆栈溢出，创建新线程失败等问题，最终导致进行宕机或者僵死，不能对外提供服务。
   
   1.2 伪异步IO
   为了解决同步阻塞I/O面临的一个链路需要一个线程处理的问题，后来又人对他的线程模型进行了优化--后端通过一个线程池来处理多个
   客户端的请求接入，形成客户端数量为M：线程池最大线程数N的比例关系，其中M可以远远大于N，通过线程池可以灵活地调配线程资源
   ，设置线程的最大值，防止由于海量并发接入导致线程耗尽。
   
   伪异步IO模型如下：
   ![images](https://github.com/CaoWenCool/JDK/blob/master/IO/image/%E4%BC%AA%E5%BC%82%E6%AD%A5IO%E6%A8%A1%E5%9E%8B%E5%9B%BE.jpg)
   采用线程池和任务队列可以实现一种叫做伪异步的I/O通信框架，它的模型如上图所示。当有新的客户端接入时，将客户端的Socket封装
   成一个Task（该任务实现java.lang.Runnable接口）投递到后端的线程池中进行处理，JDK的线程池维护一个消息队列和N个活跃线程。
   对消息队列中的任务进行处理。由于线程池可以设置队列的带下和最大线程数，因此他的资源占用是可控的，无论多少个客户端并发访问
   ，都不会导致医院的耗尽和宕机
   
   伪异步I/O通信框架采用了线程池实现，因此避免了为每个请求都创建一个独立线程造成的线程资源耗尽问题。不过因为它的底层任然是
   同步阻塞的BIO模型，因此无法从根本上解决问题。
   
   1.3 代码示例
   客户端
   
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

服务端：

    package IO;
    
    
    import java.io.IOException;
    import java.io.InputStream;
    import java.net.ServerSocket;
    import java.net.Socket;
    
    public class BIOServer {
        public static void main(String[] args) {
            //TODO服务端处理客户端连接请求
            try {
                ServerSocket serverSocket = new ServerSocket(1111);
    
                //接收到客户端连接请求之后为每个客户端创建一个新的线程进行链路处理
                new Thread(()->{
                    while (true){
                        try {
                            //阻塞方法获取新的连接
                            Socket socket = serverSocket.accept();
    
                            //为每一个新的连接都创建一个线程，负责读取数据
                            new Thread(()->{
                                int len;
                                byte[] data = new byte[1024];
                                try {
                                    InputStream inputStream = socket.getInputStream();
                                    //按照字节流方式读取数据
                                    while((len = inputStream.read(data))!= -1){
                                        System.out.println(new String(data,0,len));
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }).start();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
    
        }
    }

1.4 总结：
   在活动连接数不是特别高（小于单击1000）的情况下，这种模型是比较不错的，可以让每一个连接专注于自己的I/O并且编程模型
   简单，也不用过多考虑系统的过载和限流问题。线程池本身就是一个天然的漏斗，可以缓冲一些系统处理不了的连接或者请求。但
   是，当面对十万甚至百万级连接的时候，传统的BIO模型是万能为力的。因此我们需要一种更加高效的I/O处理模型来应对更高的并
   发量。    