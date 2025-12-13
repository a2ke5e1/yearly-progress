package com.a3.yearlyprogess.core.data.repository

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import com.a3.yearlyprogess.core.domain.model.CalendarInfo
import com.a3.yearlyprogess.core.domain.repository.CalendarRepository
import com.a3.yearlyprogess.core.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CalendarRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : CalendarRepository {

    override suspend fun hasCalendarPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED
    }

    override suspend fun getAvailableCalendars(): Result<List<CalendarInfo>> = withContext(Dispatchers.IO) {
        try {
            if (!hasCalendarPermission()) {
                return@withContext Result.failure(SecurityException("Calendar permission not granted"))
            }

            val calendars = getCalendarsDetails(context.contentResolver)
            Result.success(calendars)
        } catch (e: SecurityException) {
            Log.e("CalendarRepository", "Permission denied", e)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e("CalendarRepository", "Failed to fetch calendars", e)
            Result.failure(e)
        }
    }

    override suspend fun getSelectedCalendarDetails(calendarIds: Set<Long>): Result<List<CalendarInfo>> = withContext(Dispatchers.IO) {
        try {
            if (!hasCalendarPermission()) {
                return@withContext Result.failure(SecurityException("Calendar permission not granted"))
            }

            if (calendarIds.isEmpty()) {
                return@withContext Result.success(emptyList())
            }

            val calendars = getCalendarsByIds(context.contentResolver, calendarIds)
            Result.success(calendars)
        } catch (e: SecurityException) {
            Log.e("CalendarRepository", "Permission denied", e)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e("CalendarRepository", "Failed to fetch selected calendars", e)
            Result.failure(e)
        }
    }

    private fun getCalendarsDetails(contentResolver: ContentResolver): List<CalendarInfo> {
        val uri: Uri = CalendarContract.Calendars.CONTENT_URI
        val projection = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
            CalendarContract.Calendars.ACCOUNT_NAME
        )
        val calendars = mutableListOf<CalendarInfo>()

        contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(CalendarContract.Calendars._ID)
            val displayNameCol = cursor.getColumnIndexOrThrow(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME)
            val accountNameCol = cursor.getColumnIndexOrThrow(CalendarContract.Calendars.ACCOUNT_NAME)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val displayName = if (cursor.isNull(displayNameCol)) null else cursor.getString(displayNameCol)
                val accountName = if (cursor.isNull(accountNameCol)) null else cursor.getString(accountNameCol)

                if (displayName != null && accountName != null) {
                    calendars.add(CalendarInfo(id = id, displayName = displayName, accountName = accountName))
                }
            }
        }
        return calendars
    }

    private fun getCalendarsByIds(contentResolver: ContentResolver, calendarIds: Set<Long>): List<CalendarInfo> {
        val uri: Uri = CalendarContract.Calendars.CONTENT_URI
        val projection = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
            CalendarContract.Calendars.ACCOUNT_NAME
        )

        // Build selection for specific calendar IDs
        val placeholders = calendarIds.joinToString(",") { "?" }
        val selection = "${CalendarContract.Calendars._ID} IN ($placeholders)"
        val selectionArgs = calendarIds.map { it.toString() }.toTypedArray()

        val calendars = mutableListOf<CalendarInfo>()

        contentResolver.query(uri, projection, selection, selectionArgs, null)?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(CalendarContract.Calendars._ID)
            val displayNameCol = cursor.getColumnIndexOrThrow(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME)
            val accountNameCol = cursor.getColumnIndexOrThrow(CalendarContract.Calendars.ACCOUNT_NAME)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val displayName = if (cursor.isNull(displayNameCol)) null else cursor.getString(displayNameCol)
                val accountName = if (cursor.isNull(accountNameCol)) null else cursor.getString(accountNameCol)

                if (displayName != null && accountName != null) {
                    calendars.add(CalendarInfo(id = id, displayName = displayName, accountName = accountName))
                }
            }
        }
        return calendars
    }
}