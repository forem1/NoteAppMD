package com.example.noteapp

import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import com.example.noteapp.data.local.PreferenceDataStoreConstants.LAST_NOTE_NAME
import com.example.noteapp.data.local.PreferenceDataStoreHelper
import com.example.noteapp.databinding.ActivityEditorBinding
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader


class EditorActivity : AppCompatActivity() {

    var preferenceDataStoreHelper: PreferenceDataStoreHelper? = null

    val internalStoragePath = Environment.getExternalStorageDirectory().getAbsolutePath()
    val notesDir = File("$internalStoragePath/NoteApp")


    var mEdit: EditText? = null
    var binding: ActivityEditorBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            super.onCreate(savedInstanceState)

            preferenceDataStoreHelper = PreferenceDataStoreHelper(this)

            //window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
            binding = ActivityEditorBinding.inflate(layoutInflater)
            setContentView(binding!!.root)
            binding!!.edittext.setStylesBar(binding!!.stylesbar)

            mEdit = findViewById(R.id.editor_currentNoteName)

//        binding.edittext.text = SpannableStringBuilder(binding.edittext.getMD())
//        Select specific Styles to show
//        binding.stylesbar.stylesList = arrayOf(MarkdownEditText.TextStyle.BOLD, MarkdownEditText.TextStyle.ITALIC)

//        Timer().scheduleAtFixedRate(object : TimerTask() {
//            override fun run() {
//                Log.d("asd", binding.edittext.getMD())
//            }
//        }, 0, 5000) //put here time 1000 milliseconds=1 second


            //Create directory for notes
            if (!notesDir.exists()) {
                notesDir.mkdirs()
            }

            // Try to open last note
            lifecycleScope.launch {
                val fileName =  preferenceDataStoreHelper!!.getFirstPreference(LAST_NOTE_NAME,"")

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

                    mEdit?.setText(fileName)

                    binding!!.edittext.renderMD(sb.toString())
                }
            }

            val files: Array<File> = notesDir.listFiles()
            Log.d("Files", "Size: " + files.size)
            for (i in files.indices) {
                Log.d("Files", "FileName:" + files[i].name)
                Toast.makeText(applicationContext, files[i].name.toString(), Toast.LENGTH_SHORT)
            }

            val open = findViewById<ImageButton>(R.id.bottomNavigation_notesList)
            open.setOnClickListener {
                val editor_drawer_layout = findViewById<DrawerLayout>(R.id.editor_drawer_layout)
                if(!editor_drawer_layout.isDrawerOpen(GravityCompat.START)) editor_drawer_layout.openDrawer(GravityCompat.START);
                else editor_drawer_layout.closeDrawer(GravityCompat.END);

                Toast.makeText(applicationContext, "addNote", Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            e.printStackTrace();
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.editor_appbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        try {
            // Handle item selection
            return when (item.itemId) {
                R.id.editor_appbar_save -> {
                    val f = File(notesDir.toString() + "/" + mEdit?.getText().toString() + ".txt")

                    val fiStream = FileOutputStream(f)

                    fiStream.write(binding!!.edittext.getMD().toByteArray())
                    fiStream.close()

                    lifecycleScope.launch {
                        preferenceDataStoreHelper!!.putPreference(
                            LAST_NOTE_NAME,
                            mEdit?.getText().toString()
                        )
                    }

                    Toast.makeText(
                        applicationContext,
                        "Saved",
                        Toast.LENGTH_SHORT
                    ).show()

                    true
                }

                R.id.editor_appbar_open -> {
                    val f = File(notesDir.toString() + "/" + mEdit?.getText().toString() + ".txt")
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

                        binding!!.edittext.renderMD(sb.toString())

                        Toast.makeText(
                            applicationContext,
                            "Opened",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            applicationContext,
                            "Not found",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    true
                }

                else -> super.onOptionsItemSelected(item)
            }
        }catch (e: Exception) {
            e.printStackTrace();
        }

        return true
    }
}

//FIXME: java.lang.IndexOutOfBoundsException: Index: 2, Size: 1 when click a lot of to point list
//FIXME: SpannableStringBuilder   SPAN_EXCLUSIVE_EXCLUSIVE spans cannot have a zero length