package com.volvadvit.exchangerates

import android.R
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.volvadvit.exchangerates.databinding.ActivityMainBinding
import com.volvadvit.exchangerates.databinding.ListItemBinding
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements


class MainActivity : AppCompatActivity() {

    private val REGION_LIST: List<String> = listOf("Ростов-на-Дону","Москва","Екатеренбург","Санкт-Петербург","Россия")
    private val listUSD = ArrayList<Element>()
    private val listEUR = ArrayList<Element>()
    private val listGBP = ArrayList<Element>()
    private val listKZT = ArrayList<Element>()
    private val listCHF = ArrayList<Element>()
    private val listJPY = ArrayList<Element>()
    private val currencyList = listOf(listUSD, listEUR, listGBP, listKZT, listCHF, listJPY)

    private lateinit var doc: Document  // store html-page data, have fun to get it in String
    private lateinit var secThread: Thread
    private lateinit var runnable: Runnable
    private lateinit var binding: ActivityMainBinding
    private lateinit var bindingItem: ListItemBinding
    private lateinit var curTableElements: Elements
    private lateinit var mAdapter: RecyclerView.Adapter<CustomAdapter.ViewHolder>
    private var region: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        mAdapter = CustomAdapter(this, currencyList)
        binding.recyclerView.adapter = mAdapter

        // Make spinner and spinnerAdapter
        val adapter: ArrayAdapter<String> =
            ArrayAdapter<String>(this, R.layout.simple_spinner_item, REGION_LIST)
        // Определяем разметку для использования при выборе элемента
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        // Применяем адаптер к элементу spinner
        binding.spinner.adapter = adapter
        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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
            binding.tableDate.text = getTableColumnName(3).text()
            makeList()
        } catch (e: Exception) {
            println(e.message)
        }
    }

    private fun makeList() {
        var i: Int = 2
        for(list: MutableList<Element> in currencyList) {
            list.add(getCurrencyInfo(2,0))  // name
            list.add(getCurrencyInfo(2,1))  // byu
            if (region == "") {
                list.add(getBankName(true, i))
                list.add(getCityName(true, i))
                bindingItem.buyCityName.visibility = View.VISIBLE
            } else if (region != "") {
                list.add(getBankName(true, i))
                // TODO (закончить заполенение листа, загрузить его в адаптер и привязать инфо к ТекстВью)
            }
            list.add(getCurrencyInfo(2,2))  // trade
            list.add(getCurrencyInfo(2,3))  // CB RF
        }

        mAdapter.notifyDataSetChanged()
    }


    private fun getRegion():String =
        when(binding.spinner.selectedItem) {
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

    private fun getCurrencyInfo(currency: Int, section: Int): Element {
        /** currency: 2 - usd, 3 - eur, 4 - gbp, 5 - kzt, 6 - chf, 7 - jpy
         * index: 0 - name, 1 - byu cost, 2 - trade cost, 3 - CB RF */
        val usdRaw: Element = curTableElements[currency]
        return usdRaw.child(section)
    }

    private fun getBankName(trade: Boolean, currency: Int): Element {
        /** trade: true - Покупка, trade: false - Продажа
         * currency:    2 - usd, 3 - eur, 4 - gbp, 5 - kzt, 6 - chf, 7 - jpy
         */
        return curTableElements[currency].child( if (trade) 1 else 2).child(1)
    }
    private fun getCityName(trade: Boolean, currency: Int): Element {
        /** ONLY FOR REGION - Россия !
         * trade: true - Покупка, trade: false - Продажа
         * currency:    2 - usd, 3 - eur, 4 - gbp, 5 - kzt, 6 - chf, 7 - jpy
         */
        return curTableElements[currency].child( if (trade) 1 else 2).child(2)
    }
}
