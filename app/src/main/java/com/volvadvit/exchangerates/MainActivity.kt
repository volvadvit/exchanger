package com.volvadvit.exchangerates

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements


class MainActivity : AppCompatActivity() {

    private val REGION_LIST: List<String> = listOf("Россия", "Ростов-на-Дону", "Москва", "Екатеренбург", "Санкт-Петербург")

    private val currencyList = arrayListOf(
            arrayListOf("", "", "", "", "", "", "", "", ""),
            arrayListOf("", "", "", "", "", "", "", "", ""),
            arrayListOf("", "", "", "", "", "", "", "", ""),
            arrayListOf("", "", "", "", "", "", "", "", ""),
            arrayListOf("", "", "", "", "", "", "", "", ""),
            arrayListOf("", "", "", "", "", "", "", "", "")
    )

    private lateinit var doc: Document  // store html-page data, have fun to get it in String
    private lateinit var secThread: Thread
    private lateinit var runnable: Runnable
    private lateinit var curTableElements: Elements
    private lateinit var mAdapter: RecyclerView.Adapter<CustomAdapter.ViewHolder>
    private var region: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize recycler
        mAdapter = CustomAdapter(this, currencyList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = mAdapter

        // Make spinner and spinnerAdapter
        val adapter: ArrayAdapter<String> =
            ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, REGION_LIST)
        // Определяем разметку для использования при выборе элемента
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        // Применяем адаптер к элементу spinner
        spinner.adapter = adapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
                init("")
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long){
                init(getRegion())
            }
        }
    }

    private fun init(region: String) {
        this.region = region
        // Need make parsing in new (not ui) thread
        runnable = Runnable() { getWeb() }
        secThread = Thread(runnable)
        secThread.start()
    }

    private fun getWeb() {
        try {
            doc = Jsoup.connect("https://${region}bankiros.ru/currency").get()
            curTableElements = getTableElements()  // get all elements from table
            tableDate.text = getTableColumnName(3).text()
            makeList()
        } catch (e: Exception) {
            println(e.message)
        }
    }

    private fun makeList() {
        var currencyIndex = 2
        for (list in currencyList) {
            // name
            list[0] = getCurrencyInfo(currencyIndex, 0)
            // byu info
            list[1] = getCurrencyInfo(currencyIndex, 1)  // byu value
            if (region == "") {
                list[2] = getBankName(false, currencyIndex)
                list[3] = getCityName(false, currencyIndex)
            } else {
                list[2] = getBankName(false, currencyIndex)
            }
            // trade info
            list[4] = getCurrencyInfo(currencyIndex, 2)  // trade value
            if (region == "") {
                list[5] = getBankName(true, currencyIndex)
                list[6] = getCityName(true, currencyIndex)
            } else {
                list[5] = getBankName(true, currencyIndex)
            }
            list[7] = getCurrencyInfo(currencyIndex, 3)  // CB value
            list[8] = getCBInfo(currencyIndex)  // CB info

            currencyIndex++
        }
        runOnUiThread {
            if (region == "") {
                findViewById<TextView>(R.id.buy_city_name).visibility = View.VISIBLE
                findViewById<TextView>(R.id.trade_city_name).visibility = View.VISIBLE
            } else {
                findViewById<TextView>(R.id.buy_city_name).visibility = View.INVISIBLE
                findViewById<TextView>(R.id.trade_city_name).visibility = View.INVISIBLE
            }
        }
        mAdapter.notifyDataSetChanged()// update recyclerView
        recyclerView.smoothScrollToPosition(mAdapter.itemCount - 1)
    }

    private fun getRegion():String =
        when(spinner.selectedItem) {
            "Ростов-на-Дону" -> "rostov-na-donu."
            "Москва" -> "moskva."
            "Екатеренбург" -> "ekaterinburg."
            "Санкт-Петербург" -> "spb."
            "Россия" -> ""
            else -> ""
        }


    private fun getTableElements(): Elements {
        val tables: Elements = doc.getElementsByTag("tbody")    // get array of all "tbody" elements on html-page
        val curTable: Element = tables[0]   // get by index, table that we need, now it's first table on the page
        /** 0 - title, 1 - table column name, 2 - usd, 3 - eur... */
        return curTable.children()    // get all elements from table
    }

    private fun getTableTitleElements(section: Int): Element {
        val tableTitle: Element = curTableElements[0]
        /** 0 - blank space, 1 - "Лучшие курсы", 2 - "Курсы ЦБ РФ" */
        return tableTitle.child(section)
    }

    private fun getTableColumnName(section: Int): Element {
        val tableColumnName: Element = curTableElements[1]
        /** 0 - blank space, 1 - "Покупка", 2 - "Продажа", 3 - current date */
        return tableColumnName.child(section)
    }

    private fun getCurrencyInfo(currency: Int, section: Int): String {
        /** currency: 2 - usd, 3 - eur, 4 - gbp, ... 7 -
         * section: 0 - name, 1 - byu cost, 2 - trade cost, 3 - CB RF */
        val usdRaw: Element = curTableElements[currency]
        return when(section) {
            0 -> usdRaw.child(0).text()
            1 -> usdRaw.child(section).child(0).text()
            2 -> usdRaw.child(section).child(0).text()
            3 -> usdRaw.child(section).child(0).text()
            else -> usdRaw.child(0).text()
        }
    }

    private fun getBankName(trade: Boolean, currency: Int): String {
        /** trade: true - Продажа, trade: false - Покупка
         *  currency: 2 - usd, 3 - eur, 4 - gbp, ... 7 -
        */
        return curTableElements[currency].child(if (!trade) 1 else 2).child(1).text()
    }

    private fun getCBInfo(currency: Int): String {
        return curTableElements[currency].child(3).child(1).text()
    }

    private fun getCityName(trade: Boolean, currency: Int): String {
        /** ONLY FOR REGION - Россия !
         * trade: true - Продажа, trade: false - Покупка
        * currency: 2 - usd, 3 - eur, 4 - gbp, ... 7 -
        */
        return curTableElements[currency].child(if (!trade) 1 else 2).child(2).text()
    }
}
