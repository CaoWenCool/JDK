NIO(New I/O)
1.1 简介
    NIO是一种同步非阻塞的I/O模型，在java1.4中引入了NIO框架，对应java.io包，提供了Channel，Selector、Buffer等抽象
    
   NIO中的N 可以理解为Non-blocking，不单纯是New 。它支持面向缓冲的，基于通道的I/O操作方法。NIO提供了与传统BIO模型中的Socket
   和ServerSocket想对象的SocketChannel和ServerSocketChannel两种不同的套接字通道实现，两种通道都支持阻塞和非阻塞两种模式。
   阻塞模式使用像传统中的支持一样，比较简单，但是性能和可靠性都不好；非阻塞模式正好与之相反。对于低负载、低并发的应用程序，可以
   使用同步阻塞I/O来提升开发速率和更好的维护性；对于高负载、高并发的网络应用，应使用NIO的非阻塞模式来开发。
   
   2.2 NIO的特性/NIO与IO的区别
   NIO流式非阻塞IO而IO流式阻塞IO：可以从NIO的3个核心组件/特性为NIO带来的一些改进来分析。
   （1） Non-blocking IO（非阻塞IO）
   IO流式阻塞的，NIO流式不阻塞的。
   
   JAVA NIO 使我们可以进行非阻塞IO操作。比如说，单线程中从通道读取数据到buffer，同时可以继续做别的事情，当数据读取到buffer中后，
   线程再继续处理数据。写数据也是一样的，另外非阻塞也是如此。一个线程请求写入一些数据到某通道，但不需要等待它完全写入，这个线程
   同时可以去做别的事情，
   
   Java IO 的各种流式阻塞的。这意味着，当一个线程调用read()或者write（）时，该线程被阻塞，直到有一些数据被读取，或数据完全写入
   。该线程在此期间不能再做任何事情了。
   
   （2）、Buffer缓冲区
    IO 面向流（Stream oriented），而NIO面向缓冲区（Buffer oriented）
    
   Buffer是一个对象，它包含一些要写入或者要读出的数据。在NIO类库中加入Buffer对象，体现了新库与原I/O的一个重要区别。在面向流的
   I/O中，可以将数据直接写入或者将数据直接读到Stream对象中。虽然Stream中也有Buffer开头的扩展类，但只是流的包装类，还是从流读到
   缓冲区，而NIO却是直接读到Buffer中进行操作。
   
   在NIO库中，所有数据都是用缓冲区处理的。在读取数据时，它是直接读到缓冲区中的。在写入数据时，写入到缓冲区中。任何时候访问NIO中
   的数据，都是通过缓冲区进行操作。
   
   最常用的缓冲区是ByteBuffer，一个ByteBuffer提供了一组功能用于操作Byte数组。除了ByteBuffer，还有其他的一些缓冲区，事实上，每
   一种java基本类型（除了Boolean类型）都对应一种缓冲区。
   
   （3）、Channel通道
   NIO通过channel通道进行读写。
    
   通道是双向的，可读也可写，而流的读写是单向的。无论读写，通道只能和Buffer交互，因为Buffer，通道可以异步读写。
   
   （4）、Selector(选择器)
   NIO有选择器，IO没有选择器
   
   选择器用于使用单个线程处理多个通道。因此，它需要较少的线程来处理这些通道。线程之间的切换对于操作系统来说是昂贵的。因此，为了
   提高系统效率选择器是有用的。
   
  如下图：
  
  
  2.3、NIO读数据和写数据
  通常来说NIO中的所有IO都是从Channel通道开始的
  (1) 从通道进行数据读取：创建一个缓冲区，然后请求通道读取数据
 （2）从通道进行数据写入：创建一个缓冲区，填充数据，并要求通道写入数据
 数据读取和写入操作图示：
 
 2.4 NIO核心组件简单介绍
 NIO包含下面几个核心的组件：
 （1） Channel通道
 （2） Buffer缓冲区
 （3） Selector选择器
 整个NIO体系包含的类远远不止这三个，只能说这三个是NIO体系的核心API
 
 2.5 代码示例
    
    package IO;
    
    import java.io.IOException;
    import java.net.InetSocketAddress;
    import java.nio.ByteBuffer;
    import java.nio.channels.SelectionKey;
    import java.nio.channels.Selector;
    import java.nio.channels.ServerSocketChannel;
    import java.nio.channels.SocketChannel;
    import java.nio.charset.Charset;
    import java.util.Iterator;
    import java.util.Set;
    
    public class NIOClient {
        public static void main(String[] args) {
            //1 serverSelector负责轮询是否有新的连接，服务端监测到新的连接之后，不在创建一个新的线程而是直接将新的连接绑定到
            //clientSelector上，这样就不用IO模型中1W个while循环在死等
            try {
                Selector serverSelector = Selector.open();
            //2 clientSelector 负责轮询连接是否有数据可读
                Selector clientSelector = Selector.open();
    
                new Thread(()->{
                    try {
                        ServerSocketChannel listenerChannel = ServerSocketChannel.open();
    
                        listenerChannel.socket().bind(new InetSocketAddress(1111));
                        listenerChannel.configureBlocking(false);
                        listenerChannel.register(serverSelector, SelectionKey.OP_ACCEPT);
    
                        while(true){
                            //监测是否有新的连接，这里的1指的是阻塞的时间为1ms
                            if(serverSelector.select(1) > 0){
                                Set<SelectionKey> set = serverSelector.selectedKeys();
                                Iterator<SelectionKey> keyIterator = set.iterator();
    
                                while (keyIterator.hasNext()){
                                    SelectionKey key = keyIterator.next();
    
                                    if(key.isAcceptable()){
                                        try{
                                            //每一个新连接，不需要创建一个线程，而是直接注册到clientSelector
                                            SocketChannel clientChannel =((ServerSocketChannel)key.channel()).accept();
                                            clientChannel.configureBlocking(false);
                                            clientChannel.register(clientSelector,SelectionKey.OP_READ);
                                        }finally {
                                            keyIterator.remove();
                                        }
                                    }
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
                new Thread(()->{
                    while (true){
                        //2 批量轮询是否有哪些连接有数据可读，这里的1指的是阻塞的时间为1ms
                        try {
                            if(clientSelector.select(1)>0){
                                Set<SelectionKey> set = clientSelector.selectedKeys();
                                Iterator<SelectionKey> keyIterator = set.iterator();
    
                                while (keyIterator.hasNext()){
                                    SelectionKey key = keyIterator.next();
    
                                    if(key.isReadable()){
                                        try{
                                            SocketChannel clientChannel = (SocketChannel) key.channel();
                                            ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
    
                                            //面向Buffer
                                            clientChannel.read(byteBuffer);
                                            byteBuffer.flip();
                                            System.out.println(Charset.defaultCharset().newDecoder().decode(byteBuffer).toString());
                                        }finally {
                                            keyIterator.remove();
                                            key.interestOps(SelectionKey.OP_READ);
                                        }
                                    }
                                }
                            }
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

JDK的NIO底层由epoll实现，该实现饱受诟病的空轮询bug对导致cpu飙升100%
项目庞大后，自行实现的NIO很容易出现各类bug，维护成本较高。

Netty的出现在很大程度上改善了JDK原生NIO所存在的一些让人难以忍受的问题。



 
 