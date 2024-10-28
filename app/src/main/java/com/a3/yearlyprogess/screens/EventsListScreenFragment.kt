package com.a3.yearlyprogess.screens

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.databinding.FragmentScreenListEventsBinding
import com.a3.yearlyprogess.widgets.manager.eventManager.EventEditorActivity
import com.a3.yearlyprogess.widgets.manager.eventManager.adapter.EventsListViewAdapter
import com.a3.yearlyprogess.widgets.manager.eventManager.adapter.ImportEventItemKeyProvider
import com.a3.yearlyprogess.widgets.manager.eventManager.adapter.MyItemDetailsLookup
import com.a3.yearlyprogess.widgets.manager.eventManager.model.Event
import com.a3.yearlyprogess.widgets.manager.eventManager.viewmodel.EventViewModel
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// TODO: Redo event list screen with better UI and functionality
//          stability and performance

// TODO: Implement a search feature
class EventsListScreenFragment : Fragment() {

  private var _binding: FragmentScreenListEventsBinding? = null
  private val mEventViewModel: EventViewModel by viewModels()

  // This property is only valid between onCreateView and
  // onDestroyView.
  private val binding
    get() = _binding!!

  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?
  ): View {

    _binding = FragmentScreenListEventsBinding.inflate(inflater, container, false)

    manageEventAddButton()

    val eventAdapter = EventsListViewAdapter(AppWidgetManager.INVALID_APPWIDGET_ID) {}

    binding.eventsRecyclerViewer.apply {
      adapter = eventAdapter
      layoutManager = LinearLayoutManager(requireContext())
    }

    tracker =
        SelectionTracker.Builder<Long>(
                "mySelection",
                binding.eventsRecyclerViewer,
                ImportEventItemKeyProvider(binding.eventsRecyclerViewer),
                MyItemDetailsLookup(binding.eventsRecyclerViewer),
                StorageStrategy.createLongStorage())
            .withSelectionPredicate(SelectionPredicates.createSelectAnything())
            .build()

    eventAdapter.setTracker(tracker!!)

    tracker?.addObserver(
        object : SelectionTracker.SelectionObserver<Long>() {
          override fun onSelectionChanged() {
            super.onSelectionChanged()
            val lengthItems = tracker!!.selection.size()
            // Log.d("TAG", "onCreateView: ${tracker!!.selection}")

            if (lengthItems != 0) {

              val mt = (activity as AppCompatActivity).findViewById<MaterialToolbar>(R.id.toolbar)
              mt.title = getString(R.string.no_events_selected, lengthItems)
              mt.menu.clear()

              mt.setNavigationIcon(R.drawable.ic_baseline_close_24)
              mt.setNavigationOnClickListener { tracker!!.clearSelection() }
              mt.isTitleCentered = false
              mt.inflateMenu(R.menu.selected_menu)
              mt.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                  R.id.action_delete -> {
                    val events = eventAdapter.getSelectedEvents()
                    showDeleteConfirmationDialog(events)
                    true
                  }

                  R.id.action_select_all -> {
                    eventAdapter.selectAll()
                    true
                  }

                  R.id.action_delete_all -> {
                    showDeleteConfirmationDialog()
                    true
                  }

                  else -> true
                }
              }
            } else {
              val mt = (activity as AppCompatActivity).findViewById<MaterialToolbar>(R.id.toolbar)
              mt.title = resources.getString(R.string.events)
              mt.navigationIcon = null
              mt.isTitleCentered = true
              (activity as AppCompatActivity).setSupportActionBar(mt)

              lifecycleScope.launch(Dispatchers.Main) { eventAdapter.notifyDataSetChanged() }
            }
          }
        })

    mEventViewModel.readAllData.observe(viewLifecycleOwner) { events ->
      if (events.isEmpty()) {
        binding.noEvents.visibility = View.VISIBLE
        binding.eventsRecyclerViewer.visibility = View.GONE
      } else {
        binding.noEvents.visibility = View.GONE
        binding.eventsRecyclerViewer.visibility = View.VISIBLE
      }

      eventAdapter.setData(events)
    }

    return binding.root
  }

  // Adds the floating action button to add events
  // and hides it while scrolling
  private fun manageEventAddButton() {

    binding.addEventFab.setOnClickListener {
      val intent = Intent(it.context, EventEditorActivity::class.java)
      intent.putExtra("addMode", true)
      startActivity(intent)
    }

    binding.eventsRecyclerViewer.addOnScrollListener(
        object : RecyclerView.OnScrollListener() {
          override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            if (dy > 0 && binding.addEventFab.visibility == View.VISIBLE) {
              binding.addEventFab.hide()
            } else if (dy < 0 && binding.addEventFab.visibility != View.VISIBLE) {
              binding.addEventFab.show()
            }
          }
        })
  }

  private var tracker: SelectionTracker<Long>? = null

  override fun onDestroy() {
    val mt = (activity as AppCompatActivity).findViewById<MaterialToolbar>(R.id.toolbar)
    mt.navigationIcon = null
    mt.isTitleCentered = true
    (activity as AppCompatActivity).setSupportActionBar(mt)
    super.onDestroy()
    _binding = null
  }

  private fun showDeleteConfirmationDialog(events: List<Event> = emptyList()) {
    val count = events.size
    var title = getString(R.string.delete_selected_events, count)
    var message = getString(R.string.delete_the_selected_events_message)

    if (count == 0) {
      title = getString(R.string.delete_all_events)
      message = getString(R.string.delete_all_events_message)
    }

    val dialog =
        MaterialAlertDialogBuilder(requireContext(), R.style.CentralCard)
            .setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.outline_delete_24))
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
              if (count == 0) {
                mEventViewModel.deleteAllEvent()
              } else {
                events.forEach { event -> mEventViewModel.deleteEvent(event) }
              }
              tracker?.clearSelection()
            }
            .setNegativeButton(getString(R.string.no)) { _, _ -> }
            .create()
    dialog.show()
  }
}
