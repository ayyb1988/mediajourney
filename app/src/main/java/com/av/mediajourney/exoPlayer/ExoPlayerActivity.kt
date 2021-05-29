package com.av.mediajourney.exoPlayer

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.av.mediajourney.R
import com.mediajourney.myplayer.ExoBaseUserActivity
import com.mediajourney.myplayer.audio.ExoSimpleAudioPlayerActivity

class ExoPlayerActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exoplayer_main)

        val btExoPlayerBasic = findViewById<View>(R.id.bt_exoplayer_basic);
        btExoPlayerBasic.setOnClickListener(this)

        val btExoPlayerAudio = findViewById<View>(R.id.bt_exoplayer_audio);
        btExoPlayerAudio.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        val id = v.id;
        val intent = Intent()
        when (id) {
            R.id.bt_exoplayer_basic -> intent.setClass(this, ExoBaseUserActivity::class.java)
            R.id.bt_exoplayer_audio -> intent.setClass(this, ExoSimpleAudioPlayerActivity::class.java)
        }
        startActivity(intent)
    }

}