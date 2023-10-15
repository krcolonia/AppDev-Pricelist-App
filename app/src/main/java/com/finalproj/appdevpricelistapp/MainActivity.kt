package com.finalproj.appdevpricelistapp

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.finalproj.appdevpricelistapp.databinding.ActivityMainBinding
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.text.DecimalFormat

class MainActivity : AppCompatActivity() {

    private lateinit var mainBinding: ActivityMainBinding
    private var df: DecimalFormat = DecimalFormat("###,###,###.00")

    private lateinit var listView: ListView
    private lateinit var searchView: SearchView
    private var priceList = ArrayList<String>()
    private val fileName = "listinfo.dat"
    private var currListSize:Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)

        listView = mainBinding.listView
        searchView = mainBinding.searchBar

        mainBinding.saveItemBtn.setOnClickListener {
            val itemName = mainBinding.itemNameInp.text
            val itemPrice = mainBinding.itemPriceInp.text
            addToList(itemName, itemPrice)
            currListSize++
        }

        loadPriceList()
        updateListView()

        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterListView(newText)
                return true
            }
        })

        listView.setOnItemClickListener {
            _, _, position, _ ->
            delDialog(position)
        }

    }

    private fun addToList(itemName: Editable, itemPrice: Editable) {
        val item = itemName.toString().trim().replaceFirstChar {it.uppercase()}
        val price = df.format(itemPrice.toString().toFloat()).trim()

        val savedProd = "Name: $item \nPrice: PHP $price"

        priceList.add(currListSize, savedProd)

        itemName.clear()
        itemPrice.clear()

        updateListView()
        savePriceList()
    }

    private fun filterListView(query: String?) {
        if(query == null)
            return

        val filterList = priceList.filter { it.contains(query, true) }

        if(filterList.isEmpty())
            Toast.makeText(applicationContext,"No Item Found", Toast.LENGTH_LONG).show()
        else
            updateFilteredListView(filterList)
    }

    private fun updateListView() {
        val updateAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, priceList)
        listView.adapter = updateAdapter
    }

    private fun updateFilteredListView(filterList: List<String>) {
        val updateAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, filterList)
        listView.adapter = updateAdapter
    }

    private fun delDialog(position: Int) {

        var posToDel = 0

        for(i in 0..priceList.size){
            if (listView.getItemAtPosition(position).toString() == priceList[i]) {
                break
            }
            posToDel++
        }

        val alert = AlertDialog.Builder(this)
        alert.setMessage("Would you like to delete this item?")
        alert.setPositiveButton("Yes") {
            _ , _ ->
            priceList.removeAt(posToDel)
            currListSize--
            updateListView()
            savePriceList()
        }
        alert.setNegativeButton("No") {
            dialog, _ ->
            dialog.cancel()
        }
        alert.create().show()
    }

    private fun savePriceList() {
        val outputStream: FileOutputStream
        try {
            outputStream = openFileOutput(fileName, Context.MODE_PRIVATE)
            val objOutputStream = ObjectOutputStream(outputStream)
            objOutputStream.writeObject(priceList)
            objOutputStream.close()
            outputStream.close()
        }
        catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun loadPriceList() {
        val inputStream: FileInputStream
        try {
            inputStream = openFileInput(fileName)
            val objInputStream = ObjectInputStream(inputStream)
            val list = objInputStream.readObject() as ArrayList<*>
            priceList.addAll(list.filterIsInstance<String>())
            currListSize = priceList.size
            objInputStream.close()
            inputStream.close()
        }
        catch(e: IOException) {
            e.printStackTrace()
        }
    }
}