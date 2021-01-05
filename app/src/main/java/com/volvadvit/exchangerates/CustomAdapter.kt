package com.volvadvit.exchangerates

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CustomAdapter(context: Context, currencyList: ArrayList<ArrayList<String>>): RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

    private var inflater: LayoutInflater = LayoutInflater.from(context)
    private val valueList = currencyList

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val currency_name: TextView = view.findViewById(R.id.currency_name)
        val buy_value: TextView = view.findViewById(R.id.buy_value)
        val buy_bank_name: TextView = view.findViewById(R.id.buy_bank_name)
        val buy_city_name: TextView = view.findViewById(R.id.buy_city_name)
        val trade_value: TextView = view.findViewById(R.id.trade_value)
        val trade_bank_name: TextView = view.findViewById(R.id.trade_bank_name)
        val trade_city_name: TextView = view.findViewById(R.id.trade_city_name)
        val CB_value: TextView = view.findViewById(R.id.CB_value)
        val CB_info: TextView = view.findViewById(R.id.CB_info)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = inflater.inflate(R.layout.list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val list = valueList[position]
        holder.currency_name.text = list[0]
        holder.buy_value.text = list[1]
        holder.buy_bank_name.text = list[2]
        holder.buy_city_name.text = list[3]
        holder.trade_value.text = list[4]
        holder.trade_bank_name.text = list[5]
        holder.trade_city_name.text = list[6]
        holder.CB_value.text = list[7]
        holder.CB_info.text = list[8]
    }

    override fun getItemCount(): Int = valueList.size
}