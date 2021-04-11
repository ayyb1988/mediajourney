package com.av.mediajourney.skybox

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class SkyBox {

    // Kotlin 中该如何使用呢？需要一个将变量和方法包含在 companion object 域中
    companion object{
        val BYTES_PER_FLOAT = 4
        val POSITION_COMPONENT_COUNT = 3 // xyz
    }



    lateinit var  indexArrayBuffer :ByteBuffer
    lateinit var  vertexArrayBuffer :FloatBuffer

    constructor(){
        //立方体 的8个顶点
        val vertexData = floatArrayOf(
                -1f, 1f, 1f,     // (0) Top-left near
                1f, 1f, 1f,     // (1) Top-right near
                -1f, -1f, 1f,     // (2) Bottom-left near
                1f, -1f, 1f,     // (3) Bottom-right near
                -1f, 1f, -1f,     // (4) Top-left far
                1f, 1f, -1f,     // (5) Top-right far
                -1f, -1f, -1f,     // (6) Bottom-left far
                1f, -1f, -1f     // (7) Bottom-right far
        )

        vertexArrayBuffer = ByteBuffer
                .allocateDirect(vertexData.size * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertexData)
        vertexArrayBuffer.position(0)


        // 立方体6个面，每个面2个三角形，对应的index如下
        indexArrayBuffer = ByteBuffer.allocateDirect(6 * 6)
                .put(byteArrayOf( // Front
                        1, 3, 0,
                        0, 3, 2,  // Back
                        4, 6, 5,
                        5, 6, 7,  // Left
                        0, 2, 4,
                        4, 2, 6,  // Right
                        5, 7, 1,
                        1, 7, 3,  // Top
                        5, 1, 4,
                        4, 1, 0,  // Bottom
                        6, 2, 7,
                        7, 2, 3
                ))
        indexArrayBuffer.position(0)
    }
}