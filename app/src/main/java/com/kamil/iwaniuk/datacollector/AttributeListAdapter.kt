package com.kamil.iwaniuk.datacollector

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.TextView


class AttributeListAdapter(private val myContext : Context, private val myFields : ArrayList<String>, private val myValues : ArrayList<String>) :
    ArrayAdapter<String>(myContext, R.layout.element_attribute, myValues) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater : LayoutInflater = context
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val rowView = inflater.inflate(R.layout.element_attribute, parent, false)

        val editText = rowView.findViewById(R.id.input_attribute_value) as EditText
        val textView = rowView.findViewById(R.id.input_attribute_desc) as TextView

        editText.setText(myValues[position])
        editText.hint = myFields[position]
        textView.text = myFields[position]

        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable) {
                myValues[position] = editText.text.toString()
            }
        })

        return rowView
    }
}