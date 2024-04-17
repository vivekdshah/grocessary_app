package com.hartapps.hwesushi_kitchen

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.recyclerview.widget.RecyclerView

class PrinterAdapter(
    private val printerList: List<PrinterModel>,
    private val onPrinterSelectedListener: OnPrinterSelectedListener
) : RecyclerView.Adapter<PrinterAdapter.ViewHolder>() {

    interface OnPrinterSelectedListener {
        fun onPrinterSelected(printer: PrinterModel)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val radioButton: RadioButton = itemView.findViewById(R.id.radioButton)

        init {
            radioButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onPrinterSelectedListener.onPrinterSelected(printerList[position])
                    clearSelections()
                    printerList[position].isSelected = true
                    notifyDataSetChanged()
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_printer, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val printer = printerList[position]
        holder.radioButton.text = printer.interfaceType + "-" + printer.identifier
        holder.radioButton.isChecked = printer.isSelected
    }

    override fun getItemCount(): Int = printerList.size

    private fun clearSelections() {
        for (printer in printerList) {
            printer.isSelected = false
        }
    }
}
