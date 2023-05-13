package com.example.noteapp

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Environment
import android.util.Log
import android.widget.RemoteViews
import android.widget.Toast
import com.example.noteapp.data.local.PreferenceDataStoreConstants
import com.example.noteapp.data.local.PreferenceDataStoreHelper
import io.noties.markwon.Markwon
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader


/**
 * Implementation of App Widget functionality.
 */
class NoteWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { appWidgetId ->
            updateAppWidget(context, appWidgetManager, appWidgetId)
            Toast.makeText(context, "Widget has been updated! ", Toast.LENGTH_SHORT).show();
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    var preferenceDataStoreHelper = PreferenceDataStoreHelper(context)
    val internalStoragePath = Environment.getExternalStorageDirectory().getAbsolutePath()
    val notesDir = File("$internalStoragePath/NoteApp")

    // Try to open last note
    GlobalScope.launch(Dispatchers.IO) {
        val fileName = preferenceDataStoreHelper!!.getFirstPreference(
            PreferenceDataStoreConstants.LAST_NOTE_NAME,
            ""
        )

        Log.d("Last file", fileName)

        val f = File(notesDir.toString() + "/" + fileName + ".txt")
        if (f.exists()) {
            val fileInputStream = FileInputStream(f)
            val inputReader = InputStreamReader(fileInputStream)

            val reader = BufferedReader(inputReader)
            val sb = StringBuilder()
            var line: String? = null
            while (reader.readLine().also { line = it } != null) {
                sb.append(line).append("\n")
            }
            reader.close()
            fileInputStream.close()

            var markwon = Markwon.create(context);
            var node = markwon.parse(sb.toString());
            var markdown = markwon.render(node);

            // Construct the RemoteViews object
            val views = RemoteViews(context.packageName, R.layout.widget_note)
            views.setTextViewText(R.id.appwidget_text, markdown)

            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}