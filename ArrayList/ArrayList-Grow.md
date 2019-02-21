一、ArrayList与有三种初始化方式，构造方案源码如下：
    
    /**
     * 默认初始容量大小
     */
    private static final int DEFAULT_CAPACITY = 10;
    
    /**
    * 默认初始化内容
    */
    private static final Object[] DEFAULTCAPACITY_EMPTY_ELEMENTDATA = {};
    
    /**
    * 空数组的数据内容
    */
    private static final Object[] EMPTY_ELEMENTDATA = {};
    
    /**
    * 默认的构造函数
    */
    public ArrayList(){
        this.elementData = DEFAULTCAPACITY_EMPTY_ELEMENTDATA;   
    }
    
    /**
    * 数组内容
    */
    transient Object[] elementData; 
    
    /**
    * 待初始化容量参数的构造函数（用户自己制定容量）
    */
    public ArrayList(int initialCapacity){
        if(initialCapacity > 0){//初始容量大于0
            //创建initialCapacity大小的数据
            this.elementData = new Object[initialCapacity];
        }else if(initialCapacity == 0){
            //创建空数组
            this.elementData = EMPTY_ELEMENTDATA;
        }else{
            throw new IllegalArgumentExeption("Illegal Capacity:"+initialCity);
        }
    }
    
    /**
    * 构造包含指定collention 元素的列表，这些元素利用该集合的迭代器按照顺序返回
    * 如果指定的集合为null，throws NullPointerException
    */
    public ArrayList(Collention<? extends E> c){
        elementData = c.toArray();
        if((size = elementData.length)!=0){
            //c.toArray might (incorrectly) not return Object[](see 6200652)
            if(elementData.getClass() != Object[].class){
                elementData  = Arrays.copyOf(elementData,size,Object[].class);
            }
        }else{
            //replace with empty array
            this.elementData = EMPTY_ELEMENTDATA;
        }
    }
    
    
  以无参数构造创建ArrayList时，实际上初始化赋值的是一个空数组，当真正对数组进行添加元素操作时，才真正分配容量。即向数组中添加
  第一个元素时，数组容量扩为10.
二、分析ArrayList的扩容机制
这里以无参构造函数创建的ArrayList为例进行分析

1、add 方法
    
    /**
    * 将指定的元素追加到此列表的末尾
    **/
    public boolean add(E e){
        //添加元素之前，先调用ensureCapacityInternal方法
        ensureCapacityInternal(size + 1);
        //这里看到的ArrayList添加元素的实质就相当于数组赋值
        elementData[size++] = e;
        return true;
    }

2、ensureCapacityInternal()方法
可以看到add方法首先调用了ensureCapacityInternal（size+1）

    //得到最小扩容量
    private void ensureCapacityInternal(int minCapacity){
        if(elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA){
            //获取默认的容量和传入参数的较大值
            minCapacity  = Math.max(DEFAULT_CAPACITY,minCapacity);
        }
        
        ensureExplicitCa[acity(minCapacity)
    }
    
   当要add进第一个元素时，minCapacity为1，在Math.max()方法比较后，minCapacity为10
   
3、ensureExplicitCapacity()方法
如果调用ensureCapacityInternal()方法就一定会讲过（执行）这个方法，下面我们来研究一下这个方法的源码

    private void ensureExplicitCapacity(int minCapacity){
        modCount++;
       
       //overflow-conscious code
       if(minCapacity - elementData.length > 0){
            //调用grow方法进行扩容，调用此方法代表已经开始了扩容
            grow(minCapacity);
       }
    }
    
仔细分析一下：
    （1）、当我们要add进第1个元素到ArrayList时，elementData.length为0（因为还是一个空的list），因为执行了
    ensureCapacityInternal()方法，所以minCapacity此时为10。此时，minCapacity - elementData.length > 0 成立，所以会进入
    grow(minCapacity)方法
    （2）、当add第二个元素时，minCapacity为2，此时elementData.length(容量)在添加第一个元素后扩容成10了，此时
    minCapacity - elementData.length > 0不成立，所以不会进入（执行）grow(minCapacity)方法。
    （3）、添加第3、4……到第10个元素时，依然不会执行grow方法，数组容量都为10;
    （4）、直到添加第11个元素时，minCapacity（为11）比elementData.length(为10)要大。进入grow方法进行扩容
    
4、grow()方法
    
    /**
    * 要分配的最大数组大小 
    */
    public static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;
    
    /**
    * ArrayList 扩容的核心方法
    */
    private void grow(){
        //oldCapacity为旧容量，newCapacity为新容量
        int oldCapacity = elementData.length;
        //将oldCapacity右移一位，其效果相当于oldCapacity/2
        //我们知道位运算的速度远大于整除运算，整句运算式的结果就是将新容量更新为旧容量的1.5倍
        int newCapacity = oldCapacity + (oldCapacity >> 1);
        //然后检查新容量是否大于最小容量，若还是小于最小需要的容量，那么就把最小需要容量当做数组的新容量，
        if(newCapacity - minCapacity < 0){
            newCapacity = minCapacity;
        }
        //如果新容量大于MAX_ARRAY_SIZE,进入执行‘hugeCapacity()’方法来比较 minCapacity 和 MAX_ARRAY_SIZE,
        //如果minCapacity大于最大容量，则新容量则为 Integer.MAX_VALUE 否则，新容量大小为MAX_ARRAY_SIZE.即为Integer.MAX_VALUE-8.
        if(newCapacity - MAX_ARRAY_SIZE > 0){
            newCapacity = hugeCapacity(minCapacity);
        }
        //minCapacity is usually close to size,so this is a win
        elementData = Arrays.copyOf(elementData,newCapacity);
    }
    
   int newCapacity= oldCapacity + (oldCapacity >> 1),所以ArrayList每次扩容之后容量都会变为原来的1.5倍！
   “>>”(移位运算符)： >>1 右移一位相当于除2，右移n位相当于除以2的n次方，这里oldCapacity冥想右移了一位相当于oldCapacity/2.对
   于大数据的2进制运算，位移运算符比那些普通的运算符的运算速度快很多，因为程序仅仅移动一下而已，不去计算，这样提高了效率，节省了
   资源
   
   我们探讨一下grow（）方法
   （1）、当add第一元素时，oldCapcity为0，经比较后第一个if按断成立，newCapacity = minCapacity（为10）。但是第二个if判断不会
   成立，即newCapacity不比MAX_ARRAY_SIZE大，则不会进入hugeCapacity 方法。数组容量为10，add方法中return true,size 变为1
   （2）、当add第11个方法进入grow（）方法时，newCapacity为15，比minCapacity（为11）大，第一个if判断不成立。新容量没有大于
   数组组大size，不会进入hugeCapacity方法。数组容量扩为15，add方法中return true,size增为11。
   （3）、以此类推
   
5、hugeCapacity（）方法
    从上面grow()方法的源码我们知道：如果新容量大于MAX_ARRAY_SIZE,进入执行 hugeCapacity() 方法比较 minCapacity和
    MAX_ARRAY_SIZE，如果minCapacity大于最大容量，则新容量则Integer.MAX_VALUE,否则，新容量大小则为MAX_ARRAY_SIZE,
    即为Integer.MAX_VALUE - 8
    
    private static int hugeCapacity(int minCapacity){
        
        if(minCapacity < 0) 
            throw new OutOfMemoryError();
        //对minCapacity 和MAX_ARRAY_SIZE进行比较
        //若minCapacity大，将Integer.MAX_VALUE作为新数组的大小
        //若MAX_ARRAY_SIZE大，将MAX_ARRAY_SIZE作为新数组的大小
        //MAX_ARRAY_SIZE = Intger.MAX_VALUE - 8;
        return (minCapacity > MAX_ARRAY_VALUE) ?
            Integer.MAX_VALUE:
            MAX_ARRAY_SIZE;
    }   
   
三、System.arraycopy()和Arrays.copyOf()方法
阅读源码，我们就会发现ArrayList中大量引用了这两个方法，比如我们上面将的扩容操作以及add(int index,Element)、toArray（）等方法
1、System.arraycopy()方法
    
    /**
    * 在此列表中的指定位置插入指定的元素
    * 先调用rangeCheckForAdd 对index 进行界限检查，然后调用ensureCapacityInternal方法保证capacity足够大
    * 在将从index 开始之后的所有成员后移一个位置，将element插入index位置；最后size加1
    */
    public void add(int index,E element){
        rangeCheckForAdd(index);
        
        ensureCapacityInternal(size + 1);
        //arraycopy()方法实现数组自己复制自己
        //elementdata:源数组；index：源数组中的起始位置；elementData:目标数组；index+1:目标数组的起始位置；
        //size - index 要复制数组的元素的数量
        System.arraycopy(elementData,index,elementData,index+1,size-index);
        elementData[index] = element;
        size++;
    }
    
我们写一个简单的方法测试以下：
    
    public class ArraycopyTest{
        public static void main(String[] args){
            int[] a = new int[10];
            a[0]=0;
            a[1]=1;
            a[2]=2;
            a[3]=3;
            System.arrayCopy(a,2,a,3,3);
            a[2] = 99;
            for(int i=0; i<a.length;i++){
                System.out.println(a[i]);
            }
        }
    }
    
结果：
    0 1 99 2 3 0 0 0 0 0

2、Arrays.copyOf()方法

    /**
    * 以正确的顺序返回一个包含此列表中所有元素的数组（从第一个元素到最后一个元素）；返回数组的运行时类型是指定数组的运行时类型
    */
    public Object[] toArray(){
        //elementData:要复制的数组，size:要复制的长度
        return Arrays.copyOf(elementData,size);
    }
    
Arrays.copyOf()方法主要是为了给原有的数组进行扩容。
    
    public class ArrayscopyOfTest(){
        public static void main(String args[]){
            int[] a = new int[3];
            a[0] = 0;
            a[1] = 1;
            a[2] = 2;
            int[] b = Arrays.copyOf(a,10);
            System.out.print("b.length"b.length);
        }
    }
    
结果：
    10
    
    
3、两者的联系与区别
    联系：
    看两者源代码可以发现，copyOf()方法实际调用了System.arraycopy()方法
    区别：
    arrayCopy()需要目标数组，将原数组拷贝到你自己定义的数组里或者原数组，而且可以拷贝的起点和长度以及放入新数组中的位置。
    copyOf()是系统自动在内部新建一个数组，并返回该数组
    
四、ensureCapacity()方法
    ArrayList源码中，有一个ensureCapacity方法，这个方法ArrayList内部没有被调用过，所有很显然是提供给用户调用的，
    
    
    /**
    * 如果有必要，早呢更加此ArrayList实例的容量，以确保它至少可以容纳由minimum capacity参数指定的元素数
    * @param minCapacity 所需的最小的容量
    */
    public void ensureCapacity(int minCapacity){
        int minExpand = (elementData != DEFAULTCAPACITY_EMPTY_ELEMENTDATA)
        //any size if not default element table
        ? 0
        //larger than default for default empty table.It's already
        //supposed to be at default size
        : DEFAULT_CAPACITY;
       
        if(minCapacity > minExpand){
            ensureExplicitCapacity(minCapacity);
        }
    }
    
add大量元素之前用ensureCapacity方法，以减少增量重新分配的次数
测试方法如下：
    
    public class EnsureCapacityTest{
        public static void main(String[] args){
            ArrayList<Object> list = new ArrayList<Object>();
            final int N = 10000000;
            long startTime = System.currentTimeMillis();
            for(int i = 0;i< N;i++){
                list.add(i);
            }   
            long endTime = System.currentTimeMillis();
            System.out.print("使用ensureCapacity方法前"+(endTime - startTime));
            
            list = new ArrayList<Object>();
            long startTime1 = System.currentTimeMills();
            list.ensureCapacity(N);
            for(int i= 0 ;i<N;i++){
                list.add(i);
            }
            long endTime1 = System.currentTimeMills();
            System.out.print("使用ensureCapacity方法后"+(endTime1 - startTime1));
        }
    }
    
运行结果：
    使用ensureCapacity方法前：4637
    使用ensureCapacity方法后：241
    
通过运行结果，我们可以明显的看出ArrayList添加大量元素之前，最好先用ensureCapacity方法，以减少增量重新分配的次数

引用自：https://github.com/Snailclimb/JavaGuide/blob/master/Java%E7%9B%B8%E5%85%B3/ArrayList-Grow.md