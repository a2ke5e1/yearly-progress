package com.a3.yearlyprogess.screens

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.databinding.FragmentScreenListEventsBinding
import com.a3.yearlyprogess.eventManager.EventManagerActivity
import com.a3.yearlyprogess.eventManager.adapter.EventsListViewAdapter
import com.a3.yearlyprogess.eventManager.viewmodel.EventViewModel
import com.google.android.material.appbar.MaterialToolbar

class EventsListScreenFragment : Fragment() {


    private var _binding: FragmentScreenListEventsBinding? = null
    private val mEventViewModel: EventViewModel by viewModels()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        _binding = FragmentScreenListEventsBinding.inflate(inflater, container, false)
        // mEventViewModel = ViewModelProvider(this)[EventViewModel::class.java]

        val eventAdapter = EventsListViewAdapter(
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) {

        }
        binding.eventsRecyclerViewer.apply {
            adapter = eventAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        val toolbar = (activity as AppCompatActivity).supportActionBar
        mEventViewModel.readAllData.observe(viewLifecycleOwner) { events ->
            eventAdapter.setData(events)
            eventAdapter.selectedEventList.observe(viewLifecycleOwner) { selectedEvents ->
                val lengthItems = selectedEvents.size
                if (lengthItems > 0) {
                    toolbar?.title = "$lengthItems events selected"

                    val mt = (activity as AppCompatActivity).findViewById<MaterialToolbar>(R.id.toolbar)
                    mt.menu.clear()

                    mt.setNavigationIcon(R.drawable.ic_baseline_close_24)
                    mt.setNavigationOnClickListener {
                        eventAdapter.clearSelection()
                    }
                    mt.isTitleCentered = false
                    mt.inflateMenu(R.menu.selected_menu)
                    mt.setOnMenuItemClickListener { menuItem ->

                        when (menuItem.itemId) {

                            R.id.action_delete -> {
                                selectedEvents.forEach { event ->
                                    mEventViewModel.deleteEvent(event)
                                }
                                true
                            }

                            R.id.action_select_all -> {
                                eventAdapter.selectAll()
                                true
                            }

                            R.id.action_delete_all -> {
                                mEventViewModel.deleteAllEvent()
                                true
                            }

                            else -> true

                        }
                    }

                } else {
                    toolbar?.title = "Events"
                    val mt = (activity as AppCompatActivity).findViewById<MaterialToolbar>(R.id.toolbar)
                    mt.navigationIcon = null
                    mt.isTitleCentered = true
                    (activity as AppCompatActivity).setSupportActionBar(mt)
                }
            }
        }

        binding.addEventFab.setOnClickListener {
            val intent = Intent(it.context, EventManagerActivity::class.java)
            intent.putExtra("addMode", true)
            startActivity(intent)
        }

        binding.addEventFab.setOnLongClickListener {
            mEventViewModel.deleteAllEvent()
            true
        }

        return binding.root

    }


    override fun onDestroy() {
        val mt = (activity as AppCompatActivity).findViewById<MaterialToolbar>(R.id.toolbar)
        mt.navigationIcon = null
        mt.isTitleCentered = true
        (activity as AppCompatActivity).setSupportActionBar(mt)
        super.onDestroy()
        _binding = null
    }
}