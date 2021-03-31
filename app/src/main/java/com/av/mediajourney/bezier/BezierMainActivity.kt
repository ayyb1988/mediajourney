package com.av.mediajourney.bezier

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.av.mediajourney.R

class BezierMainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bezier_main)
        val button = findViewById<Button>(R.id.button);
        val button2= findViewById<Button>(R.id.button2);



        button.setOnClickListener(fun(it: View) {
            val intent1 = Intent()
            //如果targetClass  targetClass::class.java入参
            intent1.setClass(this, BeizerAndroidAcitivity::class.java)
            startActivity(intent1)
        })

        button2.setOnClickListener(fun(it:View){
            val intent = Intent()
            intent.setClass(this,BezierActivity::class.java)
            startActivity(intent)
        })
    }
}