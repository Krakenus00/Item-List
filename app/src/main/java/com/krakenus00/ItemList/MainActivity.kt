package com.krakenus00.ItemList

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.JsonReader
import android.util.JsonWriter
import android.util.Log
import android.view.*
import android.widget.*
import java.io.*
import java.lang.Exception
import java.nio.charset.Charset

class MainActivity : AppCompatActivity() {
    private val TAG: String = "MainActivity"
    private val INTERVAL: Long = 1000 /*ms*/ * 60 /*s*/ * 2 /*min*/

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var adapter: ItemListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val list = findViewById<ListView>(R.id.items_list)
        adapter = ItemListAdapter(readData(), LayoutInflater.from(applicationContext))
        list.adapter = adapter
        val btn = findViewById<ImageButton>(R.id.add_item_button)
        val txt = findViewById<EditText>(R.id.add_item_text)
        btn.setOnClickListener {
            if (TextUtils.isEmpty(txt.text.toString()))
                txt.error = "Name field was empty!"
            else
            {
                adapter.addItem(txt.text.toString())
                txt.setText("")
            }
        }
        startRepeatedTask()
    }

    override fun onResume() {
        startRepeatedTask()
        super.onResume()
    }

    override fun onPause() {
        stopRepeatedTask()
        backupData()
        super.onPause()
    }

    private fun readData(): ArrayList<String> {
        val path = File(applicationContext.filesDir.absolutePath + "/item_list/list.txt")
        var arr = ArrayList<String>()
        if (path.exists()) {
            try {
                val txt = path.readText(Charset.defaultCharset())
                arr = jsonToArray(txt)
            }
            catch (e: Exception) { Log.e(TAG, "Error while reading the data.", e) }
        }
        return arr
    }

    private fun backupData() {
        val main_dir = File(applicationContext.filesDir.absolutePath + "/item_list")
        var success = true
        if (!main_dir.exists())
            success = main_dir.mkdir()
        if (success) {
            val file = File(main_dir, "list.txt")
            val txt = arrayToJson(adapter.items)
            try { file.writeText(txt, Charset.defaultCharset()) }
            catch (e: Exception) { Log.e(TAG, "Error while saving the data.", e) }
        }
    }

    private fun arrayToJson(arr: ArrayList<String>): String {
        if (arr.isEmpty()) return ""
        val sw = StringWriter()
        val jw = JsonWriter(sw)
        jw.use { jw ->
            jw.beginArray()
            arr.forEach{ jw.value(it) }
            jw.endArray()
        }
        val txt = sw.toString()
        sw.close()
        return txt
    }

    private fun jsonToArray(json: String): ArrayList<String> {
        var arr = ArrayList<String>()
        val sr = StringReader(json)
        val jr = JsonReader(sr)
        jr.use { jr ->
            jr.beginArray()
            while(jr.hasNext())
                arr.add(jr.nextString())
            jr.endArray()
        }
        return arr
    }

    private val task = object: Runnable {
        override fun run() {
            backupData()
            handler.postDelayed(this, INTERVAL)
        }
    }

    private fun startRepeatedTask() {
        task.run()
    }

    private fun stopRepeatedTask() {
        handler.removeCallbacks(task)
    }

    private class ItemListAdapter(data: ArrayList<String>, li: LayoutInflater) : BaseAdapter() {
        val items = data
        private val inflater = li

        fun addItem(item: String?) {
            if (item!=null) {
                items.add(item)
                notifyDataSetChanged()
            }
        }

        fun removeItem(position: Int) {
            items.removeAt(position)
            notifyDataSetChanged()
        }

        override fun isEmpty(): Boolean {
            return items.isEmpty()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var v = convertView
            if (v == null)
                v = inflater.inflate(R.layout.list_layout, null)
            val element = getItem(position) as String
            val txt = v!!.findViewById<TextView>(R.id.list_item_text)
            txt.text = element
            val btn = v.findViewById<ImageButton>(R.id.list_item_button)
            btn.setOnClickListener { removeItem(position) }
            return v
        }

        override fun getItem(position: Int): Any {
            return items[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getCount(): Int {
            return items.size
        }
    }
}