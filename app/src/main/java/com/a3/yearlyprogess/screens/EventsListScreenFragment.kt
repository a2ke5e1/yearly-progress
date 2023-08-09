package com.a3.yearlyprogess.screens

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.a3.yearlyprogess.databinding.FragmentScreenListEventsBinding
import com.a3.yearlyprogess.eventManager.EventManagerActivity
import com.a3.yearlyprogess.eventManager.adapter.EventsListViewAdapter
import com.a3.yearlyprogess.eventManager.viewmodel.EventViewModel

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
            AppWidgetManager.INVALID_APPWIDGET_ID) {

        }
        binding.eventsRecyclerViewer.apply {
            adapter = eventAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
        mEventViewModel.readAllData.observe(viewLifecycleOwner) { events ->
            eventAdapter.setData(events)
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
}