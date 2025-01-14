package com.a3.yearlyprogess.components.dialogbox

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.a3.yearlyprogess.MainActivity
import com.a3.yearlyprogess.R
import com.a3.yearlyprogess.databinding.DialogRestoreBackupBinding
import com.a3.yearlyprogess.widgets.manager.eventManager.ImportEventCalendarActivity
import com.a3.yearlyprogess.widgets.manager.eventManager.data.EventDatabase
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import de.raphaelebner.roomdatabasebackup.core.OnCompleteListener.Companion as RoomBackupCodes
import de.raphaelebner.roomdatabasebackup.core.RoomBackup

class BackupRestoreDialog(private val roomBackup: RoomBackup) : DialogFragment() {
  @SuppressLint("SetTextI18n")
  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    roomBackup
        .database(EventDatabase.getDatabase(requireContext()))
        .enableLogDebug(true)
        .backupLocation(RoomBackup.BACKUP_FILE_LOCATION_CUSTOM_DIALOG)
        .backupIsEncrypted(true)
        .customEncryptPassword(
            "haha idc if you forgot",
        ) // DON'T CHANGE THIS, It's in the production version
        .apply {
          onCompleteListener { success, message, exitCode ->
            when (exitCode) {
              RoomBackupCodes.EXIT_CODE_ERROR_STORAGE_PERMISSONS_NOT_GRANTED -> {
                this@BackupRestoreDialog.dismiss()
                MaterialAlertDialogBuilder(context, R.style.CentralCard)
                    .setIcon(R.drawable.ic_storage)
                    .setTitle("Storage Permission")
                    .setMessage(
                        "To ensure a seamless experience with our backup & restore feature, we kindly request access to your device's storage. Granting this permission allows us to securely safeguard your data during backups and swiftly restore it when needed. Your privacy and data security are our utmost priorities. Thank you for entrusting us with the protection of your valuable information",
                    )
                    .setNeutralButton("Okay") { _, _ -> }
                    .show()
              }
              else -> {}
            // Log.d( MainActivity.TAG, "success: $success, message: $message, exitCode:
            // $exitCode",)
            }
            if (success) {
              Toast.makeText(requireContext(), "Operation was successful.", Toast.LENGTH_LONG)
                  .show()
              restartApp(Intent(requireContext(), MainActivity::class.java))
            }
          }
        }

    return activity?.let {
      val builder = MaterialAlertDialogBuilder(it)
      val inflater = requireActivity().layoutInflater
      val binding = DialogRestoreBackupBinding.inflate(inflater)

      binding.backupButton.setOnClickListener { backupDatabase() }

      binding.restoreButton.setOnClickListener { restoreDatabase() }

      binding.restoreGoogleCalenderButton.setOnClickListener {
        val intent = Intent(this.context, ImportEventCalendarActivity::class.java)
        startActivity(intent)
        dismiss()
      }

      binding.dismissButton.setOnClickListener { dismiss() }

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
