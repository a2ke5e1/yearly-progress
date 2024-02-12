package com.a3.yearlyprogess.components.dialogbox

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.CalendarContract
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.a3.yearlyprogess.MainActivity
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.databinding.DialogRestoreBackupBinding
import com.a3.yearlyprogess.eventManager.data.EventDatabase
import com.a3.yearlyprogess.eventManager.model.Event
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.raphaelebner.roomdatabasebackup.core.OnCompleteListener.Companion as RoomBackupCodes
import de.raphaelebner.roomdatabasebackup.core.RoomBackup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class BackupRestoreDialog(private val roomBackup: RoomBackup) : DialogFragment() {

    // Contract for reading events from calender
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission is granted. Continue the action or workflow in your app.
            readEventsFromCalender()
        } else {
            // Explain to the user that the feature is unavailable because the
            // features requires a permission that the user has denied. At the
            // same time, respect the user's decision. Don't link to system
            // settings in an effort to convince the user to change their decision.
        }
    }

    private fun readEventsFromCalender() {
        // TODO: Let user select which events to import
        lifecycleScope.launch(Dispatchers.IO) {

            val uri = CalendarContract.Events.CONTENT_URI
            val projection = arrayOf(
                CalendarContract.Events.TITLE,
                CalendarContract.Events.DESCRIPTION,
                CalendarContract.Events.DTSTART,
                CalendarContract.Events.DTEND
            )

            val cursor = requireActivity().contentResolver.query(
                uri,
                projection,
                null,
                null,
                "${CalendarContract.Events.DTSTART} ASC"
            )

            val eventList = mutableListOf<Event>()
            cursor?.use {
                val titleColumn = it.getColumnIndex(CalendarContract.Events.TITLE)
                val descriptionColumn = it.getColumnIndex(CalendarContract.Events.DESCRIPTION)
                val dtStartColumn = it.getColumnIndex(CalendarContract.Events.DTSTART)
                val dtEndColumn = it.getColumnIndex(CalendarContract.Events.DTEND)

                while (it.moveToNext()) {
                    val title = it.getString(titleColumn)
                    val description = it.getString(descriptionColumn)
                    val dtStart = it.getLong(dtStartColumn)
                    val dtEnd = it.getLong(dtEndColumn)

                    val event = Event(
                        id = 0,
                        eventTitle = title,
                        eventDescription = description,
                        eventStartTime = dtStart,
                        eventEndTime = dtEnd
                    )
                    eventList.add(event)
                }
            }

            cursor?.close()

            val eventDao = EventDatabase.getDatabase(requireContext()).eventDao()
            eventDao.insertAllEvents(eventList)

            dismiss()
            // Weird way to restart the app
            roomBackup.restartApp(Intent(requireContext(), MainActivity::class.java))
        }

    }

    @SuppressLint("SetTextI18n")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        roomBackup.database(EventDatabase.getDatabase(requireContext())).enableLogDebug(true)
            .backupLocation(RoomBackup.BACKUP_FILE_LOCATION_CUSTOM_DIALOG)
            .backupIsEncrypted(true)
            .customEncryptPassword("haha idc if you forgot")
            .apply {
                onCompleteListener { success, message, exitCode ->
                    when (exitCode) {
                        RoomBackupCodes.EXIT_CODE_ERROR_STORAGE_PERMISSONS_NOT_GRANTED -> {
                            this@BackupRestoreDialog.dismiss()
                            MaterialAlertDialogBuilder(context, R.style.CentralCard)
                                .setIcon(R.drawable.ic_storage)
                                .setTitle("Storage Permission")
                                .setMessage("To ensure a seamless experience with our backup & restore feature, we kindly request access to your device's storage. Granting this permission allows us to securely safeguard your data during backups and swiftly restore it when needed. Your privacy and data security are our utmost priorities. Thank you for entrusting us with the protection of your valuable information")
                                .setNeutralButton("Okay") { _, _ ->

                                }
                                .show()
                        }
                        else -> Log.d(
                            MainActivity.TAG,
                            "success: $success, message: $message, exitCode: $exitCode"
                        )
                    }
                    if (success) {
                        Toast.makeText(
                            requireContext(),
                            "Operation was successful.",
                            Toast.LENGTH_LONG
                        ).show()
                        restartApp(Intent(requireContext(), MainActivity::class.java))
                    }
                }
            }

        return activity?.let {
            val builder = MaterialAlertDialogBuilder(it)
            val inflater = requireActivity().layoutInflater
            val binding = DialogRestoreBackupBinding.inflate(inflater)

            binding.backupButton.setOnClickListener {
                backupDatabase()
            }

            binding.restoreButton.setOnClickListener {
                restoreDatabase()
            }

            binding.restoreGoogleCalenderButton.setOnClickListener {
                when {
                    ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.READ_CALENDAR
                    ) == PackageManager.PERMISSION_GRANTED -> {
                        // You can use the API that requires the permission.
                        readEventsFromCalender()
                    }
                    ActivityCompat.shouldShowRequestPermissionRationale(
                        requireActivity(), Manifest.permission.READ_CALENDAR) -> {
                        // In an educational UI, explain to the user why your app requires this
                        // permission for a specific feature to behave as expected, and what
                        // features are disabled if it's declined. In this UI, include a
                        // "cancel" or "no thanks" button that lets the user continue
                        // using your app without granting the permission.
                        // showInContextUI(...)
                    }
                    else -> {
                        // You can directly ask for the permission.
                        // The registered ActivityResultCallback gets the result of this request.
                        requestPermissionLauncher.launch(
                            Manifest.permission.READ_CALENDAR)
                    }
                }
            }

            binding.dismissButton.setOnClickListener {
                dismiss()
            }

            builder.setView(binding.root)
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun backupDatabase() {
        roomBackup.backup()
    }

    private fun restoreDatabase() {
        roomBackup.restore()
    }
}