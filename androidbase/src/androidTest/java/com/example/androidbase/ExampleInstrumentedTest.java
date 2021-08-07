package com.example.androidbase;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.nio.ByteBuffer;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.example.androidbase.test", appContext.getPackageName());
    }

    @Test
    public  void testBuffer(String[] args) {

        System.out.println("--------- 创建一个15Byte大小的Buffer ---------");
        // 准备一个15字节大小的Buffer
        ByteBuffer buffer = ByteBuffer.allocate(15);
        System.out.println("position = " + buffer.position() + " limit = " + buffer.limit() + " capacity = " + buffer.capacity());
        System.out.println();

        System.out.println("--------- 写入10个byte ---------");
        // 写入10个字节数据
        for (int i = 1; i <= 10; i++) {
            buffer.put((byte)i);
        }
        System.out.println("position = " + buffer.position() + " limit = " + buffer.limit() + " capacity = " + buffer.capacity());
        System.out.println();

        System.out.println("--------- 写 --> 读 ---------");
        // 写 --> 读
        buffer.flip();
        System.out.println("position = " + buffer.position() + " limit = " + buffer.limit() + " capacity = " + buffer.capacity());
        System.out.println();

        System.out.println("--------- 读取5个byte ---------");
        // 读取5个字节数据
        for (int i = 0; i < 5; i++) {
            buffer.get();
        }
        System.out.println("position = " + buffer.position() + " limit = " + buffer.limit() + " capacity = " + buffer.capacity());
        System.out.println();

        System.out.println("--------- 读 --> 写 ---------");
        // 读 --> 写
        buffer.flip();
        System.out.println("position = " + buffer.position() + " limit = " + buffer.limit() + " capacity = " + buffer.capacity());
    }
}