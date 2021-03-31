package com.av.mediajourney.bezier

import android.graphics.Path
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.av.mediajourney.R

class BeizerAndroidAcitivity : AppCompatActivity()
{
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_beizer_android)
        val view = findViewById<BeizerView>(R.id.view);

    }

}