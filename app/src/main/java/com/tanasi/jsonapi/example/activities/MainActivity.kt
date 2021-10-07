package com.tanasi.jsonapi.example.activities

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import android.os.Bundle
import com.tanasi.jsonapi.example.R
import com.tanasi.jsonapi.example.fragments.MainFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val ft: FragmentTransaction = supportFragmentManager.beginTransaction()
        ft.replace(R.id.frame_layout, MainFragment())
        ft.commit()
    }
}