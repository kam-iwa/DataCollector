package com.kamil.iwaniuk.datacollector

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class FeatureListAdapter (private val myContext : Context, private val myGeoFeatures : ArrayList<String>) :
    ArrayAdapter<String>(myContext, R.layout.element_feature, myGeoFeatures) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater : LayoutInflater = context
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val rowView = inflater.inflate(R.layout.element_feature, parent, false)

        val textView = rowView.findViewById(R.id.input_feature_value) as TextView

        textView.setText(myGeoFeatures[position])

        return rowView
    }


}
