package com.volvadvit.exchangerates

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.jsoup.nodes.Element

class CustomAdapter(context: Context, currencyList: List<List<Element>>): RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

    private var inflater: LayoutInflater = LayoutInflater.from(context)

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // here initialize textView from list_item
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        listUSD: List<Element>,
        listEUR: List<Element>,
        listGBP: List<Element>,
        listKZT: List<Element>,
        listCHF: List<Element>,
        listJPY: List<Element>
        val view: View = inflater.inflate(R.layout.list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        val message: String = messageList.get(position)
//        holder.messageHolder.text = message
    }

    override fun getItemCount(): Int {
        //messageList.size
        return 0
    }
}