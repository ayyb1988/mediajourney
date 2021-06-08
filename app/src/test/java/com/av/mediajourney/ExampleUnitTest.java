package com.av.mediajourney;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    private static ThreadLocal<String> sStrThreadlocal = new ThreadLocal<>();
    private static ThreadLocal<Integer> sIntegerThreadLocal = new ThreadLocal<>();
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testThreadLocal(){

        //在主线程给Threadlocal赋值，请取出输出

        sStrThreadlocal.set("aaa");
        String value = sStrThreadlocal.get();

        sIntegerThreadLocal.set(1);
        Integer intValue = sIntegerThreadLocal.get();
        System.out.println("111 curThreadId="+Thread.currentThread()+" strthreadLocalValue="+value
        +" intThreadLocalValue="+intValue);

        //创建两个线程，分别给ThreadLocal赋不同的值
        new Thread(new Runnable() {
            @Override
            public void run() {
                sStrThreadlocal.set("bbb");
                String value = sStrThreadlocal.get();

                sIntegerThreadLocal.set(2);
                Integer intValue = sIntegerThreadLocal.get();
                System.out.println("222 curThreadId="+Thread.currentThread()+" strthreadLocalValue="+value
                        +" intThreadLocalValue="+intValue);

                sStrThreadlocal.remove();
                sIntegerThreadLocal.remove();
                value = sStrThreadlocal.get();
                intValue = sIntegerThreadLocal.get();
                System.out.println("after remove 222 curThreadId="+Thread.currentThread()+" strthreadLocalValue="+value
                        +" intThreadLocalValue="+intValue);

            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                String value = sStrThreadlocal.get();

                Integer intValue = sIntegerThreadLocal.get();

                System.out.println("333 curThreadId="+Thread.currentThread()+" strthreadLocalValue="+value
                        +" intThreadLocalValue="+intValue);

                sStrThreadlocal.remove();
                sIntegerThreadLocal.remove();
                value = sStrThreadlocal.get();
                intValue = sIntegerThreadLocal.get();
                System.out.println("after remove 333 curThreadId="+Thread.currentThread()+" strthreadLocalValue="+value
                        +" intThreadLocalValue="+intValue);

            }
        }).start();

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //最后在输出下主线程的ThreadLocal值
        value = sStrThreadlocal.get();
        intValue = sIntegerThreadLocal.get();
        System.out.println("444 curThreadId="+Thread.currentThread()+" strthreadLocalValue="+value
                +" intThreadLocalValue="+intValue);

        //用完之后记得remove，虽然，ThreadLocalMap.Entry的key是ThreadLocal的弱引用，但是value占用的内存空间还在。
        //被回收的场景有两个，再次调用set或者get方法是会坚持map中是否有key为空的Entry（由于key是弱引用，外部强引用依赖断开后，gc时就会回收该key，
        // 回收后即可null），如果有则清除。

        //但是不知道什么时候再次调用set或者get，这种被动的方式只能说是做了个保证，如果没有调用set/get，就可能引发内存泄露
        //ThreadLocal提供了一个remove方法，由我们主动清除ThreadLocal对应的ThreadLocalMap.Entry中的堆内存
        sStrThreadlocal.remove();
        sIntegerThreadLocal.remove();
        value = sStrThreadlocal.get();
        intValue = sIntegerThreadLocal.get();
        System.out.println("after remove 444 curThreadId="+Thread.currentThread()+" strthreadLocalValue="+value
                +" intThreadLocalValue="+intValue);
    }
}