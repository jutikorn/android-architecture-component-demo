package com.ben.boonya.architecturecomponentdemo.characterlist

import android.arch.lifecycle.LifecycleRegistry
import android.arch.lifecycle.LifecycleRegistryOwner
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.ben.boonya.architecturecomponentdemo.R
import com.ben.boonya.architecturecomponentdemo.extensions.onLoadMoreListener
import com.ben.boonya.architecturecomponentdemo.extensions.resetLoadMoreState
import kotlinx.android.synthetic.main.fragment_character_list.*

/**
 * Created by oozou on 6/21/2017 AD.
 */
class CharacterListFragment : Fragment(), LifecycleRegistryOwner, SwipeRefreshLayout.OnRefreshListener {
    private lateinit var viewmodel: CharacterListViewModel
    private lateinit var characterListAdapter: CharacterListAdapter
    private val registry = LifecycleRegistry(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewmodel = createViewModel()
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_character_list, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        characterListAdapter = CharacterListAdapter(viewmodel)

        swipeRefreshLayout.setOnRefreshListener(this)
        swipeRefreshLayout.setColorSchemeColors(R.color.colorPrimary)

        val linearLayoutManager = LinearLayoutManager(activity)
        rvCharacter.adapter = characterListAdapter
        rvCharacter.layoutManager = linearLayoutManager
        rvCharacter.onLoadMoreListener(startPage = 1){ currentPage -> viewmodel.getCharacterByPage(currentPage) }

        viewmodel.getCharacterByPage(1)
        attachObserver()
    }

    fun attachObserver() {
        viewmodel.isLoading.observe(this, Observer<Boolean> {
            it?.let {
                showLoadingDialog(it)
            }
        })

        viewmodel.characterResponse.observe(this, Observer {
            it?.let {
                if (!it.isEmpty()) {
                    characterListAdapter.notifyDataSetChanged()
                }
            }
        })

        viewmodel.throwable.observe(this, Observer {
            it?.message?.let {
                showMessage(it)
            }
        })
    }

    override fun getLifecycle(): LifecycleRegistry = registry


    override fun onRefresh() {
        rvCharacter.resetLoadMoreState()
        viewmodel.clearCharacterList()
        viewmodel.getCharacterByPage(1)
    }

    fun showLoadingDialog(isLoading: Boolean) {
        if (isLoading) {
            if (!swipeRefreshLayout.isRefreshing) {
                progressBar.visibility = View.VISIBLE
            }
        } else {
            progressBar.visibility = View.INVISIBLE
            if (swipeRefreshLayout.isRefreshing) {
                swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    fun showMessage(message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }

    private fun createViewModel() = ViewModelProviders.of(this).get(CharacterListViewModel::class.java)

    companion object {
        fun newInstance(): CharacterListFragment {
            return CharacterListFragment()
        }
    }
}