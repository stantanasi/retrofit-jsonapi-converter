package com.tanasi.jsonapi.example.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.tanasi.jsonapi.example.R
import com.tanasi.jsonapi.example.models.Article
import com.tanasi.jsonapi.example.models.People

class MainFragment : Fragment() {

    private lateinit var viewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        viewModel.getArticles()
        viewModel.getArticle("1")
        viewModel.createArticle(Article().also {
            it.title = "test"
            it.author = People(
                id = "2"
            )
        })
    }

}