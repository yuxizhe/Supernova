package com.aquarids.sample

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import com.aquarids.supernova.Supernova
import com.aquarids.supernova.toUri

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        val url = "https://www.baidu.com"
        val edt = findViewById<EditText>(R.id.edt_url)
        val open = findViewById<Button>(R.id.btn_open)

        open.setOnClickListener {
            val url = edt.text.toString()
            url.toUri()?.host?.let {
                Supernova.launchUrl(this, url, it)
            }
        }
    }
}
