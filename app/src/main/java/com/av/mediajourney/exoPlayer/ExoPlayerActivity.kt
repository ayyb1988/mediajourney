package com.av.mediajourney.exoPlayer

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.av.mediajourney.R
import com.example.myplayer.ExoBaseUserActivity

class ExoPlayerActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exoplayer_main)

        val btExoPlayerBasic = findViewById<View>(R.id.bt_exoplayer_basic);
        btExoPlayerBasic.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        val id = v.id;
        val initent = Intent()
        when (id) {
            R.id.bt_exoplayer_basic -> {
                initent.setClass(this, ExoBaseUserActivity::class.java)
                startActivity(initent)
            }


        }
    }

}