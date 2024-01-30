package com.a3.yearlyprogess.screens

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.a3.yearlyprogess.BuildConfig
import com.a3.yearlyprogess.MainActivity
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.databinding.DialogRestoreBackupBinding
import com.a3.yearlyprogess.eventManager.data.EventDatabase
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.raphaelebner.roomdatabasebackup.core.RoomBackup


class BackupRestoreDialog(private val  roomBackup: RoomBackup) : DialogFragment() {

    @SuppressLint("SetTextI18n")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
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

            binding.dismissButton.setOnClickListener {
                dismiss()
            }

            builder.setView(binding.root)
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun backupDatabase() {
        roomBackup.database(EventDatabase.getDatabase(requireContext())).enableLogDebug(true)
            .backupLocation(RoomBackup.BACKUP_FILE_LOCATION_CUSTOM_DIALOG)
            .maxFileCount(5)
            .apply {
                onCompleteListener { success, message, exitCode ->
                    Log.d(MainActivity.TAG, "success: $success, message: $message, exitCode: $exitCode")
                    if (success) {
                        restartApp(Intent(requireContext(), MainActivity::class.java))
                        Toast.makeText(
                            requireContext(),
                            "Backup was successful.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
            .backup()
    }

    private fun restoreDatabase() {
        roomBackup.database(EventDatabase.getDatabase(requireContext())).enableLogDebug(true)
            .backupLocation(RoomBackup.BACKUP_FILE_LOCATION_CUSTOM_DIALOG)
            .maxFileCount(5)
            .apply {
                onCompleteListener { success, message, exitCode ->
                    Log.d(MainActivity.TAG, "success: $success, message: $message, exitCode: $exitCode")
                    if (success) {
                        restartApp(Intent(requireContext(), MainActivity::class.java))
                        Toast.makeText(
                            requireContext(),
                            "Restore was successful.",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                }
            }
            .restore()
    }
}