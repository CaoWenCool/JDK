1、ArrayList简介
2、ArrayList核心源码
3、ArrayList源码分析
    （1）、System.arraycopy()和Arrays.copyOf()方法
        两者的区别
    （2）、ArrayList核心扩容技术
    （3）、内部类
4、ArrayList经典Demo

1、ArrayList简介
    ArrayList的底层是数组队列，相当于动态数组。与Java中的数组相比，他的容量能动态增长。在添加大量元素之前，
应用程序可以使用ensureCapacity操作来增加ArrayList实例的容量。这样可以减少递增式再分配的数量。
它继承于AbstractList，实现了List、RandomAccess、Cloneable、java.io.Serializable这些接口。
在我们学习数据结构的事就知道了线性表的顺序存储，插入删除元素的时间复杂度为o(n),求表长以及增加元素，取第i元素
的时间复杂度为o(1).

ArrayList继承了AbstractList，实现了List。它是一个数组队列，提供了相关的添加、删除、修改、遍历等功能。

ArrayList实现了RandomAccess接口，提供了随机访问功能。RandomAccess是Java中用来被List实现，为List提供快速
访问功能的。在ArrayList中，我们即可以通过元素的序号快速获取对象，这就是快速随机访问。

ArrayList实现了Cloneable接口，即覆盖了函数clone（）,能被克隆

ArrayList是想了java.io.Serializable接口，这意味着ArrayList支持序列化，能通过序列化去传输。

和Vector不同，ArrayList中的操作不是线程安全的！所以，建议在单线程中才使用ArrayList，而在多线程中可以选择
Vector或者CopyOnWriteArrayList。

2、ArrayList的核心源码


public class ArrayList<E> extends AbstractList<E> implements List<E>,RandomAccess,Cloneable,java.io.Serializable {

    /**
     * 默认初始化容量大小
     */
    private static final int DEFAULT_CAPACITY = 10;

    /**
     * 空数组（用于空实例）
     */
    private static final Object[] EMPTY_ELEMENTDATA = {};

    /**
     * 用于默认大小空实例的共享空数组实例
     * 我们把它从EMPTY_ELEMENTDATA数组中区分出来，以知道在添加第一个元素时容量需要增加多少
     */
    private static final Object[] DEFAULTCAPACITY_EMPTY_ELEMENTDATA = {};

    /**
     * 保存ArrayList数据的数组
     */
    transient Object[] elementData;

    /**
     * ArrayList所包含的元素个数
     */
    private int size;

    /**
     * 带初始量参数的构造函数，用户需要自己指定容量
     *
     * @param initialCapacity
     */
    public ArrayList(int initialCapacity) {
        if (initialCapacity > 0) {
            //创建initialCapacity大小的数组
            this.elementData = EMPTY_ELEMENTDATA;
        } else if (initialCapacity == 0) {
            //创建空数组
            this.elementData = EMPTY_ELEMENTDATA;
        } else {
            throw new IllegalArgumentException("Illegal Capacity" + initialCapacity);
        }
    }

    /**
     * 默认构造函数，DEFAULTCAPACITY_EMPTY_ELEMENTDATA为0，初始化10，也就是说初始其实是空数组 当添加第一个元素的时候数组容量才变成10
     */
    public ArrayList() {
        this.elementData = DEFAULTCAPACITY_EMPTY_ELEMENTDATA;
    }

    public ArrayList(Collection<? extends E> c) {

        elementData = c.toArray();
        //如果指定元素个数不为0
        if ((size = elementData.length) != 0) {
            //c.toArray 可能返回的不是Object类型的数组所以加上下面的语句用于判断
            //这里用到了反射里面的getClass方法
            if (elementData.getClass() != Object[].class) {
                elementData = Arrays.copyOf(elementData, size, Object[].class);
            }
        } else {
            //用空数组代替
            this.elementData = EMPTY_ELEMENTDATA;
        }
    }

    /**
     * 修改这个ArrayList实例的容量是列表的当前大小，应用程序可以使用此操作来最小化ArrayList实例的存储
     */
    public void trimToSize() {
        modCount++;
        if (size < elementData.length) {
            elementData = (size == 0) ? EMPTY_ELEMENTDATA : Arrays.copyOf(elementData, size);
        }
    }

    /**
     * 下面是ArrayList的扩容机制
     * ArrayList的扩容机制提高了性能，如果每次值扩充一个，
     * 那么频繁的插入会导致频繁的拷贝，降低性能，而ArrayList的扩容机制避免了这种情况
     * 如果有必要，增加此ArrayList实例的容量，以确保它至少能容纳元素的数量
     *
     * @param minCapacity 所需的最小容量
     */
    public void ensureCapacity(int minCapacity) {
        int minExpand = (elementData != DEFAULTCAPACITY_EMPTY_ELEMENTDATA)
                //any size if not default element table
                ? 0

                : DEFAULT_CAPACITY;
        if (minCapacity > minExpand) {
            ensureExplicitCapacity(minCapacity);
        }
    }

    /**
     * 得到最小容量
     */
    private void ensureCapacityInternal(int minCapacity) {
        if (elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA) {
            //获取默认的容量和传入参数的较大值
            minCapacity = Math.max(DEFAULT_CAPACITY, minCapacity);
        }
        ensureExplicitCapacity(minCapacity);
    }

    public void ensureExplicitCapacity(int minCapacity) {
        modCount++;

        if (minCapacity - elementData.length > 0) {
            //调用grow方法进行扩容，调用此方法代表已经开始了扩容了
            grow(minCapacity);
        }
    }

    /**
     * 要分配的最大数组的大小
     */
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;

    /**
     * ArrayList扩容的核心方法
     *
     * @param minCapacity
     */
    private void grow(int minCapacity) {
        //oldCapacity为旧容量，newCapaCity为新容量
        int oldCapacity = elementData.length;
        //将oldCapacity右移一位，其效果相当于oldCapacity/2
        //我们知道位运算速度远远快于整除运算，整句运算式的结果就是新容量更新为原来的1.5倍
        int newCapacity = oldCapacity + (oldCapacity >> 1);
        if (newCapacity - minCapacity < 0) {
            newCapacity = minCapacity;
        }
        //再检查新容量是否超出了ArrayList所定义的最大容量
        //若超出了，则调用hugeCapacity()来比较minCapacity和MAX_ARRAY_SIZE;
        //如果minCapacity大于MAX_ARRAY_SIZE,则新容量则为Integer.MAX_VALUE，否则，新容量大小为MAX_ARRAY_SIZE。
        if (newCapacity - MAX_ARRAY_SIZE > 0) {
            newCapacity = hugeCapacity(minCapacity);
        }
        elementData = Arrays.copyOf(elementData, newCapacity);
    }

    /**
     * 比较minCapacity和MAX_ARRAY_SIZE
     *
     * @param minCapacity
     * @return
     */
    private static int hugeCapacity(int minCapacity) {

        if (minCapacity < 0) throw new OutOfMemoryError();

        return (minCapacity > MAX_ARRAY_SIZE) ?
                Integer.MAX_VALUE :
                MAX_ARRAY_SIZE;
    }

    /**
     * 返回此列表中元素数
     *
     * @return
     */
    @Override
    public int size() {
        return size;
    }

    /**
     * 如果此列表不包含元素，则返回true
     *
     * @return
     */
    public boolean isEmpty() {
        //注意=和==好的区别
        return size == 0;
    }

    /**
     * 如果此列表包含指定的元素，返回true
     *
     * @param o
     * @return
     */
    public boolean contains(Object o) {
        //indexOf()方法：返回此列表中指定元素的首次出现的索引，如果此列表不包含此元素，则为-1
        return indexOf(o) >= 0;
    }

    public int indexOf(Object o) {
        if (o == null) {
            for (int i = 0; i < size; i++) {
                if (elementData[i] == null) {
                    return i;
                }
            }
        } else {
            for (int i = 0; i < size; i++) {
                if (o.equals(elementData[i])) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * 返回列表中指定元素的最后一次出现的索引，如果此列表中不包含次元素，则返回-1
     *
     * @param o
     * @return
     */
    public int lastIndexOf(Object o) {
        if (o == null) {
            for (int i = size - 1; i >= 0; i--) {
                if (elementData[i] == null) {
                    return i;
                }
            }
        } else {
            for (int i = size - 1; i >= 0; i--) {
                if (o.equals(elementData[i])) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * 返回ArrayList实例的浅拷贝。（元素本身不被赋值）
     *
     * @return
     */
    public Object clone() {
        try {
            ArrayList<?> v = (ArrayList<?>) super.clone();
            v.elementData = Arrays.copyOf(elementData, size);
            v.modCount = 0;
            return v;
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }

    /**
     * 以正确的顺序（从第一个元素到最后一个元素）返回一个包含此列表中所有元素的数组
     * 返回的数组将是安全的，因为该列表中不保留对他的引用。（换句话说这个方法必须分配一个新的数组）
     * 因此，调用者可以自由的修改返回的数组，此方法充当基于阵列和基于集合的API之间的桥梁
     *
     * @return
     */
    public Object[] toArray() {
        return Arrays.copyOf(elementData, size);
    }


    /**
     * 以正确的顺序返回一个包含此列表中所有元素的数组（从第一个到最后一个元素）
     * 返回的数组的运行时类型是指定数组的运行时类型。如果列表适合指定的数组，则返回其中
     * 否则，将为指定数组的运行时类型和此列表的大小分配一个新的数组
     * 如果此列表适用于指定的数组，其余空间（即数组的列表数量多于此元素），则紧跟在集合结束后的数组中的元素设置为null
     * 这仅在调用者知道列表不包含任何空元素的情况下才能确定列表的长度
     *
     * @param a
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        if (a.length < size) {
            //新建一个运行时类型的数组，但是ArrayList数组的内容
            return (T[]) Arrays.copyOf(elementData, size, a.getClass());
            //调用System提供的Arraycopy()方法实现数组之间的复制
        }
        System.arraycopy(elementData, 0, a, 0, size);
        if (a.length > size) {
            a[size] = null;
        }
        return a;
    }

    @SuppressWarnings("unchecked")
    E elementData(int idnex) {
        return (E) elementData[idnex];
    }

    /**
     * 返回此列表中指定位置的元素
     *
     * @param index
     * @return
     */
    public E get(int index) {
        rangeCheck(index);

        return elementData(index);
    }

    /**
     * 将指定元素追加到此列表的末尾
     *
     * @param e
     * @return
     */
    public boolean add(E e) {
        ensureCapacityInternal(size + 1);
        //这里可以看到ArrayList添加元素的实质相当于为数组赋值
        elementData[size + 1] = e;
        return true;
    }


    /**
     * 在此列表中指定位置插入指定的元素。
     * 先调用rangCheckForApp 对index进行界限检查，如果用ensureCapacityInternal 方法保证capacity足够大
     * 在将从index开始之后的所有成员后移一个位置，将element插入index位置，最后size+1
     *
     * @param index
     * @param element
     */
    public void add(int index, E element) {
        rangeCheckForAdd(index);

        ensureCapacityInternal(size + 1);
        //arraycopy()这个实现数组之间复制的方法一定要看一下，下面就用到了arraycopy()方法实现数组自己复制自己
        System.arraycopy(elementData, index, elementData, index + 1, size - index);

        elementData[index] = element;
        size++;
    }

    /**
     * 删除该列表中指定位置的元素，将任何后续元素移动到左侧（从其索引中减去一个元素）
     *
     * @param index
     * @return
     */
    public E remove(int index) {
        rangeCheck(index);

        modCount++;
        E oldValue = elementData(index);

        int numMoved = size - index - 1;
        if (numMoved > 0) {
            System.arraycopy(elementData, index + 1, elementData, index,
                    numMoved);
        }
        elementData[--size] = null;
        //从列表中删除元素
        return oldValue;
    }

    /**
     * 从列表中删除指定元素的第一个出现（如果存在），如果列表中不包含该元素，则它不会更改
     * 返回true，如果此列表中包含此元素
     *
     * @param o
     * @return
     */
    public boolean remove(Object o) {
        if (o == null) {
            for (int index = 0; index < size; index++) {
                if (elementData[index] == null) {
                    fastRemove(index);
                    return true;
                }
            }
        } else {
            for (int index = 0; index < size; index++) {
                if (o.equals(elementData[index])) {
                    return true;
                }
            }
        }
        return false;
    }

    private void fastRemove(int index) {
        modCount++;
        int numMoved = size - index - 1;
        if (numMoved > 0) {
            System.arraycopy(elementData, index + 1, elementData, index,
                    numMoved);
        }
        elementData[--size] = null;
    }

    /**
     * 从列表中删除所有元素
     */
    public void clear() {
        modCount++;
        //把数组中所有的元素的值设为null
        for (int i = 0; i < size; i++) {
            elementData[i] = null;
        }
        size = 0;
    }

    /**
     * 按照指定集合的Iterator返回的顺序将指定集合中的所有元素追加到此列表中的末尾
     *
     * @param c
     * @return
     */
    public boolean addAll(Collection<? extends E> c) {
        Object[] a = c.toArray();
        int numNew = a.length;
        ensureCapacityInternal(size + numNew);
        System.arraycopy(a, 0, elementData, size, numNew);
        size += numNew;
        return numNew != 0;
    }

    /**
     * 将指定集合中的所有元素插入到此列表中，从指定的位置开始
     *
     * @param index
     * @param c
     * @return
     */
    public boolean addAll(int index, Collection<? extends E> c) {
        rangeCheckForAdd(index);

        Object[] a = c.toArray();
        int numNew = a.length;
        ensureCapacityInternal(size + numNew);

        int numMoved = size - index;
        if (numMoved > 0) {
            System.arraycopy(elementData, index, elementData, index + numNew, numMoved);
        }

        System.arraycopy(a, 0, elementData, index, numNew);
        size += numNew;
        return numNew != 0;
    }


    /**
     * 从列表中删除所有索引为fromIndex含 和toIndex之间的元素
     * 将任何后续元素移动到左侧（减少其索引）
     *
     * @param fromIndex
     * @param toIndex
     */
    protected void removeRange(int fromIndex, int toIndex) {
        modCount++;
        int numMoved = size - toIndex;
        System.arraycopy(elementData, toIndex, elementData, fromIndex, numMoved);

        int newSize = size - (toIndex - fromIndex);
        for (int i = newSize; i < size; i++) {
            elementData[i] = null;
        }

        size = newSize;
    }


    /**
     * 检查给定的索引是否在范围内
     *
     * @param index
     */
    private void rangeCheck(int index) {
        if (index > size || index < 0) {
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
        }
    }

    /**
     * add 和 addAll 使用的rangeCheck的一个版本
     *
     * @param index
     */
    private void rangeCheckForAdd(int index) {
        if (index > size || index < 0) {
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
        }
    }

    /**
     * 返回IndexOutOfBoundsExceptions细节信息
     *
     * @param index
     * @return
     */
    private String outOfBoundsMsg(int index) {
        return "Index:" + index + ",size:" + size;
    }

    /**
     * 从此列表中删除指定集合中包含的所有元素
     *
     * @param c
     * @return
     */
    public boolean removeAll(Collection<?> c) {
        Objects.requireNonNull(c);
        //如果此列表中被修改则返回true
        boolean modified = false;
        Iterator<?> it = iterator();
        while (it.hasNext()) {
            if (c.contains(it.next())) {
                it.remove();
                modified = true;
            }
        }
        return modified;
    }

    /**
     * 仅保留此列表中包含在指定集合中的元素
     * 换句话说，从此列表中删除其中不包含在指定集合中的所有元素
     *
     * @param c
     * @return
     */
    public boolean retainAll(Collection<?> c) {
        Objects.requireNonNull(c);
        boolean modified = false;
        Iterator<?> it = iterator();
        while (it.hasNext()) {
            if (c.contains(it.next())) {
                it.remove();
                modified = true;
            }
        }
        return modified;
    }
}

3、ArrayList源码分析

通过上面的源码我们发现这两个实现数组复制的方法被广泛使用而且很多地方特别的巧妙。
比如下面的add(int index,E element)方法就很巧妙的用到了arrayCopy（）方法让数组自己复制自己实现让index开始之后的所有
位置后移一个位置
    
        /**
         * 在此列表中指定位置插入指定的元素。
         * 先调用rangCheckForApp 对index进行界限检查，如果用ensureCapacityInternal 方法保证capacity足够大
         * 在将从index开始之后的所有成员后移一个位置，将element插入index位置，最后size+1
         *
         * @param index
         * @param element
         */
        public void add(int index, E element) {
            rangeCheckForAdd(index);
    
            ensureCapacityInternal(size + 1);
            //arraycopy()这个实现数组之间复制的方法一定要看一下，下面就用到了arraycopy()方法实现数组自己复制自己
            System.arraycopy(elementData, index, elementData, index + 1, size - index);
    
            elementData[index] = element;
            size++;
        }
 
 又如toArray（）方法中用到了copyOf()方法
    
        /**
         * 以正确的顺序（从第一个元素到最后一个元素）返回一个包含此列表中所有元素的数组
         * 返回的数组将是安全的，因为该列表中不保留对他的引用。（换句话说这个方法必须分配一个新的数组）
         * 因此，调用者可以自由的修改返回的数组，此方法充当基于阵列和基于集合的API之间的桥梁
         *
         * @return
         */
        public Object[] toArray() {
            //elementData：要复制的数组，size：要复制的长度
            return Arrays.copyOf(elementData, size);
        }

两者的联系与区别
   联系：看两者源代码可以发现copyOf()内部调用了System.arraycopy()方法
   区别：1、arraycopy()需要目标数组，将原数组拷贝到你定义的数组里面，而且可以选择拷贝的起点和长度以及放入新数组中的位置
    2、copyOf()是系统自动在内部新建一个数组，并返回该数组
    
4、ArrayList核心扩容技术


    /**
     * 下面是ArrayList的扩容机制
     * ArrayList的扩容机制提高了性能，如果每次值扩充一个，
     * 那么频繁的插入会导致频繁的拷贝，降低性能，而ArrayList的扩容机制避免了这种情况
     * 如果有必要，增加此ArrayList实例的容量，以确保它至少能容纳元素的数量
     *
     * @param minCapacity 所需的最小容量
     */
    public void ensureCapacity(int minCapacity) {
        int minExpand = (elementData != DEFAULTCAPACITY_EMPTY_ELEMENTDATA)
                //any size if not default element table
                ? 0

                : DEFAULT_CAPACITY;
        if (minCapacity > minExpand) {
            ensureExplicitCapacity(minCapacity);
        }
    }

    /**
     * 得到最小容量
     */
    private void ensureCapacityInternal(int minCapacity) {
        if (elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA) {
            //获取默认的容量和传入参数的较大值
            minCapacity = Math.max(DEFAULT_CAPACITY, minCapacity);
        }
        ensureExplicitCapacity(minCapacity);
    }

    //判断是否需要扩容，上面两个方法都要调用
    public void ensureExplicitCapacity(int minCapacity) {
        modCount++;

        if (minCapacity - elementData.length > 0) {
            //调用grow方法进行扩容，调用此方法代表已经开始了扩容了
            grow(minCapacity);
        }
    }
   
    /**
         * ArrayList扩容的核心方法
         *
         * @param minCapacity
         */
        private void grow(int minCapacity) {
            //oldCapacity为旧容量，newCapaCity为新容量
            int oldCapacity = elementData.length;
            //将oldCapacity右移一位，其效果相当于oldCapacity/2
            //我们知道位运算速度远远快于整除运算，整句运算式的结果就是新容量更新为原来的1.5倍
            int newCapacity = oldCapacity + (oldCapacity >> 1);
            if (newCapacity - minCapacity < 0) {
                newCapacity = minCapacity;
            }
            //再检查新容量是否超出了ArrayList所定义的最大容量
            //若超出了，则调用hugeCapacity()来比较minCapacity和MAX_ARRAY_SIZE;
            //如果minCapacity大于MAX_ARRAY_SIZE,则新容量则为Integer.MAX_VALUE，否则，新容量大小为MAX_ARRAY_SIZE。
            if (newCapacity - MAX_ARRAY_SIZE > 0) {
                newCapacity = hugeCapacity(minCapacity);
            }
            elementData = Arrays.copyOf(elementData, newCapacity);
        }
        
        
扩容机制代码已经做了详细的解释。另外值得注意的是大家最容易忽略的一个运算符：移位运算符
移位运算符：是在二进制的基础上对数字进行平移。按照平移的方向和填充数字的规则分为三种： << 左移， >> 右移  和 >>>>无符号右移
。
作用：对于大数据的2进制运算，位移运比那些普通的运算符的运算要快很多，因为程序仅仅移动一下而已，不去计算，这样提高了效率，节省了
资源。
另外注意：
1、java中length属性针对数组说的。
2、java中的length()方法是针对字符串String说的
3、java中的size()方法是针对泛型集合说的，

内部类：
（1）、private class Itr implements Iterator<E>
该类实现了Iterator接口，同时重写了里面的hashNext(),next(),remove()等方法
(2)、 private class ListItr implements ListIterator<E>
该类实现了ListIterator接口，同时重写了hasPrevious(),previousIndex().previous(),Set(E e),add(E e)等方法
所以这也可以看出Iterator和ListIterator的区别：  ListIterator在Iterator的基础上增加了添加对象，修改对象，逆向遍历等方法。
（3）、private class SubList extends AbstractList<E> implements RandomAccess
(4)、static final class ArrayListSpliterator<E> implements Spliterator<E>

5、ArrayList的经典Demo
import java.util.Iterator;

/**
 * Created by 曹文 on 2019/2/27.
 */
public class ArrayListDemo {
    public static void main(String[] srgs){
        ArrayList<Integer> arrayList = new ArrayList<Integer>();

        System.out.printf("Before add:arrayList.size() = %d\n",arrayList.size());

        arrayList.add(1);
        arrayList.add(3);
        arrayList.add(5);
        arrayList.add(7);
        arrayList.add(9);
        System.out.printf("After add:arrayList.size() = %d\n",arrayList.size());

        System.out.println("Printing elements of arrayList");
        // 三种遍历方式打印元素
        // 第一种：通过迭代器遍历
        System.out.print("通过迭代器遍历:");
        Iterator<Integer> it = arrayList.iterator();
        while(it.hasNext()){
            System.out.print(it.next() + " ");
        }
        System.out.println();

        // 第二种：通过索引值遍历
        System.out.print("通过索引值遍历:");
        for(int i = 0; i < arrayList.size(); i++){
            System.out.print(arrayList.get(i) + " ");
        }
        System.out.println();

        // 第三种：for循环遍历
        System.out.print("for循环遍历:");
        for(Integer number : arrayList){
            System.out.print(number + " ");
        }

        // toArray用法
        // 第一种方式(最常用)
        Integer[] integer = arrayList.toArray(new Integer[0]);

        // 第二种方式(容易理解)
        Integer[] integer1 = new Integer[arrayList.size()];
        arrayList.toArray(integer1);

        // 抛出异常，java不支持向下转型
        //Integer[] integer2 = new Integer[arrayList.size()];
        //integer2 = arrayList.toArray();
        System.out.println();

        // 在指定位置添加元素
        arrayList.add(2,2);
        // 删除指定位置上的元素
        arrayList.remove(2);
        // 删除指定元素
        arrayList.remove((Object)3);
        // 判断arrayList是否包含5
        System.out.println("ArrayList contains 5 is: " + arrayList.contains(5));

        // 清空ArrayList
        arrayList.clear();
        // 判断ArrayList是否为空
        System.out.println("ArrayList is empty: " + arrayList.isEmpty());
    }
}
6、查看ArrayList的类图

