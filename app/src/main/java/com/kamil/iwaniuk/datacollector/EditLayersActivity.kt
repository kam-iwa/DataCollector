package com.kamil.iwaniuk.datacollector

import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import org.osmdroid.bonuspack.kml.*
import org.osmdroid.util.GeoPoint
import java.io.File
import java.io.FilenameFilter
import java.lang.NullPointerException
import java.lang.StringBuilder

class EditLayersActivity : AppCompatActivity() {

    var geoFeatures = arrayListOf<String>()
    var choosenFile: String = ""
    var features = ArrayList<KmlFeature>()
    val myFunctions = MyFunctions()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_layers)
        
        val myActionBar = supportActionBar
        myActionBar!!.title = getString(R.string.mainActivity_editLayers)
        myActionBar.setDisplayHomeAsUpEnabled(true)

        //tworzenie adaptera listy dla obiektow z wybranego pliku
        val myListView: ListView = findViewById(R.id.editLayers_featuresList)
        val myAdapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, geoFeatures)
        myListView.adapter = myAdapter

        //przy kliknieciu w element listy nazwany od id obiektu
        myListView.setOnItemClickListener { adapterView, view, position, id ->
            val kmlDocument = KmlDocument()
            val localFile = kmlDocument.getDefaultPathForAndroid(choosenFile)
            kmlDocument.parseGeoJSON(localFile)
            var feature = features[position] as KmlPlacemark

            val myId: TextView = findViewById(R.id.editLayers_featureId)
            myId.text = feature.mId
            val myGeom: TextView = findViewById(R.id.editLayers_geometry)
            myGeom.text = feature.boundingBox.toString()

            var attributes = arrayListOf<String>()
            var attributesValues = arrayListOf<String>()
            val myAttributesListView: ListView = findViewById(R.id.editLayers_attributesList)

            try {
                val attributesStringList = feature.mExtendedData.keys
                for (i in attributesStringList) {
                    attributes.add(i)
                    attributesValues.add(feature.mExtendedData[i]!!)
                }
                val myAdapter = AttributeListAdapter(this, attributes, attributesValues)
                myAttributesListView.adapter = myAdapter
            } catch (e: NullPointerException) {
                val attributes: Array<String> = arrayOf<String>()
                val myAdapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,attributes)
                myAttributesListView.adapter = myAdapter
            }
            val myConfirmButton: Button = findViewById(R.id.editLayers_confirmChanges)

            //zapisanie zmian w obiekcie, ale nie w pliku
            myConfirmButton.setOnClickListener {
                feature.mId = myId.text.toString()
                for(counter in 0 until myAttributesListView.count){
                    val myView = myAttributesListView.adapter.getView(counter, null, myAttributesListView)
                    val myEditText: EditText = myView.findViewById(R.id.input_attribute_value)
                    feature.mExtendedData.set(myEditText.hint.toString(), myEditText.text.toString())
                }
                refreshList(kmlDocument, features)
            }
            val myGeometryEditButton: Button = findViewById(R.id.editLayers_editGeometry)
            myGeometryEditButton.setOnClickListener {
                if(feature.mGeometry is KmlPoint){
                    editPointGeometry(feature)
                } else if(feature.mGeometry is KmlLineString){
                    editLineGeometry(feature)
                } else if(feature.mGeometry is KmlPolygon){
                    editPolygonGeometry(feature)
                } else {
                    Toast.makeText(this@EditLayersActivity, R.string.editLayersActivity_geometryTypeNotSupported, Toast.LENGTH_SHORT).show()
                }
            }

            //Przyciski tworzenia i usuwania obiektow i atrybutow
            val myAddFeatureButton: Button = findViewById(R.id.editLayers_addFeature)
            myAddFeatureButton.setOnClickListener {
                addFeature(features)
            }
            val myRemoveFeatureButton: Button = findViewById(R.id.editLayers_removeFeature)
            myRemoveFeatureButton.setOnClickListener {
                removeFeature(position, features)
            }
            val myAddAttributeButton: Button = findViewById(R.id.editLayers_addAttribute)
            myAddAttributeButton.setOnClickListener {
                addAttribute(features)
            }
            val myRemoveAttributeButton: Button = findViewById(R.id.editLayers_removeAttribute)
            myRemoveAttributeButton.setOnClickListener {
                removeAttribute(position, features)
            }
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        myFunctions.myBackDialog(this, this@EditLayersActivity)
        return true
    }

    override fun onBackPressed() {
        myFunctions.myBackDialog(this, this@EditLayersActivity)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        getMenuInflater().inflate(R.menu.edit_layers, menu)
        return true
    }

    //Zdarzenia przy kliknięciu opcji w menu
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle presses on the action bar items
        // View v = findViewById(R.id.f);
        when (item.getItemId()) {
            R.id.edit_layers_load_layer ->
                loadLayer()
            //Toast.makeText(this@ViewLayersActivity, getString(R.string.view_layers_add_layer), Toast.LENGTH_SHORT).show()
            R.id.edit_layers_save_layer ->
                saveLayer()
            //Toast.makeText(this@ViewLayersActivity, getString(R.string.view_layers_remove_layer), Toast.LENGTH_SHORT).show()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    fun loadLayer(){
        //utworzenie adaptera
        val kmlDocument = KmlDocument()
        val myFiles = kmlDocument.getDefaultPathForAndroid("")

        val myFilesNamesList = arrayListOf<String>()
        val myFilesList = arrayListOf(myFiles.listFiles(object : FilenameFilter {
            override fun accept(dir: File, name: String): Boolean {
                if (name.endsWith(".geojson")){
                    myFilesNamesList.add(name)
                }
                return name.endsWith(".geojson")
            }
        }))

        //budowanie wlasnego okienka dialogowego
        val myDialogBuilder = AlertDialog.Builder(this)
        val myInflater = this.layoutInflater
        val dialogView = myInflater.inflate(R.layout.dialog_view_layers_add_layer, null)
        myDialogBuilder.setView(dialogView)

        //deklaracja przycisków dialogu i ich obsługa
        val myDialog = myDialogBuilder.create()
        //utworzenie listy menu
        val myDirName : TextView = dialogView.findViewById(R.id.layers_to_add_txt)
        myDirName.text = myFiles.toString()
        val myMenuList : ListView = dialogView.findViewById(R.id.layers_to_add)

        //utworzenie adaptera listy
        val myAdapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, myFilesNamesList)

        //ustawienie adaptera
        myMenuList.adapter = myAdapter
        myDialog.show()

        //wybor pliku z listy plikow
        choosenFile = ""
        geoFeatures = arrayListOf<String>()
        myMenuList.setOnItemClickListener { _, _, position, _ ->
            choosenFile = myFilesNamesList[position]
            try {
                val localFile = kmlDocument.getDefaultPathForAndroid(choosenFile)
                kmlDocument.parseGeoJSON(localFile)
                features = kmlDocument.mKmlRoot.mItems
                for(i in kmlDocument.mKmlRoot.mItems){
                    if (i.mId == null) {
                        geoFeatures.add("Feature without ID")
                    } else {
                        geoFeatures.add(i.mId)
                    }
                }
                val myListView : ListView = findViewById(R.id.editLayers_featuresList)
                val myFeatureAdapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, geoFeatures)
                myListView.adapter = myFeatureAdapter
            } catch (e : Exception){
                Toast.makeText(this@EditLayersActivity, R.string.editLayersActivity_cannotLoadLayer, Toast.LENGTH_SHORT).show()
            }
            myDialog.dismiss()
        }
    }

    //zapis danych w pliku
    fun saveLayer(){
        try{
            val kmlDocument = KmlDocument()
            val localFile = kmlDocument.getDefaultPathForAndroid(choosenFile)
            kmlDocument.parseGeoJSON(localFile)
            kmlDocument.mKmlRoot.mItems = features
            kmlDocument.saveAsGeoJSON(localFile)
            Toast.makeText(this@EditLayersActivity, R.string.editLayersActivity_layerSaved, Toast.LENGTH_SHORT).show()
        } catch (err: Exception) {
            Toast.makeText(this@EditLayersActivity, R.string.editLayersActivity_cannotSaveLayer, Toast.LENGTH_SHORT).show()
        }
    }

    //edycja geometrii - do dorobienia linie, poligony
    fun editPointGeometry(feature : KmlPlacemark){
        val myDialogBuilder = AlertDialog.Builder(this)
        val myInflater = this.layoutInflater
        val dialogView = myInflater.inflate(R.layout.dialog_edit_point_geometry, null)
        myDialogBuilder.setView(dialogView)
        val myDialog = myDialogBuilder.create()

        val lat: EditText = dialogView.findViewById(R.id.editPointGeometry_latitude)
        lat.setText(feature.mGeometry.mCoordinates[0].latitude.toString())
        val lon: EditText = dialogView.findViewById(R.id.editPointGeometry_longitude)
        lon.setText(feature.mGeometry.mCoordinates[0].longitude.toString())

        val myGeometryConfirm: Button = dialogView.findViewById(R.id.editPointGeometry_confirm)
        myGeometryConfirm.setOnClickListener {
            if(lat.text.toString() == "") {
                feature.mGeometry.mCoordinates[0].latitude = 0.0
            } else {
                feature.mGeometry.mCoordinates[0].latitude = lat.text.toString().toDouble()
            }

            if(lon.text.toString() == ""){
                feature.mGeometry.mCoordinates[0].longitude = 0.0
            } else {
                feature.mGeometry.mCoordinates[0].longitude = lon.text.toString().toDouble()
            }
            myDialog.dismiss()
        }
        myDialog.show()
    }

    fun editLineGeometry(feature : KmlPlacemark){
        val myDialogBuilder = AlertDialog.Builder(this)
        val myInflater = this.layoutInflater
        val dialogView = myInflater.inflate(R.layout.dialog_edit_line_geometry, null)
        myDialogBuilder.setView(dialogView)
        val myDialog = myDialogBuilder.create()

        val coords: EditText = dialogView.findViewById(R.id.editLineGeometry_geometryValue)
        val coordsString = StringBuilder()
        for((counter, coord) in feature.mGeometry.mCoordinates.withIndex()) {
            if(counter != feature.mGeometry.mCoordinates.size - 1){
                coordsString.append(coord.latitude.toString() + ',' + coord.longitude.toString() + "\n")
            } else {
                coordsString.append(coord.latitude.toString() + ',' + coord.longitude.toString())
            }
        }
        coords.setText(coordsString)

        val myGeometryConfirm: Button = dialogView.findViewById(R.id.editLineGeometry_confirmGeometry)
        myGeometryConfirm.setOnClickListener {
            val list1 = ArrayList(coords.text.toString().split("\n"))
            val list2 = ArrayList<ArrayList<String>>()
            for(coord in list1){
                val coordinates = ArrayList(coord.split(','))
                list2.add(coordinates)
            }
            val coordinatesList = ArrayList<GeoPoint>()
            try {
                for (coordinate in list2) {
                    val convertedCoordinate = ArrayList<Double>()
                    for (coordinateElement in coordinate) {
                        convertedCoordinate.add(coordinateElement.toDouble())
                    }
                    coordinatesList.add(GeoPoint(convertedCoordinate[0], convertedCoordinate[1]))
                }
                if(coordinatesList.size >= 2){
                    feature.mGeometry.mCoordinates = coordinatesList
                } else {
                    Toast.makeText(this@EditLayersActivity, R.string.editLayersActivity_notEnoughVertices, Toast.LENGTH_SHORT).show()
                }
            } catch (err:NumberFormatException) {
                Toast.makeText(this@EditLayersActivity, "Nie można zapisać geometrii - niepoprawnie zapisane liczby.", Toast.LENGTH_SHORT).show()
            }

            myDialog.dismiss()
        }
        myDialog.show()
    }

    fun editPolygonGeometry(feature : KmlPlacemark){
        val myDialogBuilder = AlertDialog.Builder(this)
        val myInflater = this.layoutInflater
        val dialogView = myInflater.inflate(R.layout.dialog_edit_polygon_geometry, null)
        myDialogBuilder.setView(dialogView)
        val myDialog = myDialogBuilder.create()

        val coords: EditText = dialogView.findViewById(R.id.editPolygonGeometry_geometryValue)
        val coordsString = StringBuilder()
        for((counter, coord) in feature.mGeometry.mCoordinates.withIndex()) {
            if(counter != feature.mGeometry.mCoordinates.size - 1){
                coordsString.append(coord.latitude.toString() + ',' + coord.longitude.toString() + "\n")
            } else {
                coordsString.append(coord.latitude.toString() + ',' + coord.longitude.toString())
            }
        }
        coords.setText(coordsString)

        val myGeometryConfirm: Button = dialogView.findViewById(R.id.editPolygonGeometry_confirmGeometry)
        myGeometryConfirm.setOnClickListener {
            val list1 = ArrayList(coords.text.toString().split("\n"))
            val list2 = ArrayList<ArrayList<String>>()
            for(coord in list1){
                val coordinates = ArrayList(coord.split(','))
                list2.add(coordinates)
            }
            val coordinatesList = ArrayList<GeoPoint>()
            try {
                for (coordinate in list2) {
                    val convertedCoordinate = ArrayList<Double>()
                    for (coordinateElement in coordinate) {
                        convertedCoordinate.add(coordinateElement.toDouble())
                    }
                    coordinatesList.add(GeoPoint(convertedCoordinate[0], convertedCoordinate[1]))
                }
                if(coordinatesList.size >= 3){
                    coordinatesList.add(coordinatesList[0])
                    feature.mGeometry.mCoordinates = coordinatesList
                } else {
                    Toast.makeText(this@EditLayersActivity, R.string.editLayersActivity_notEnoughVertices, Toast.LENGTH_SHORT).show()
                }
            } catch (err:NumberFormatException) {
                Toast.makeText(this@EditLayersActivity, R.string.editLayersActivity_wrongNumberFormat, Toast.LENGTH_SHORT).show()
            }

            myDialog.dismiss()
        }
        myDialog.show()
    }

    fun refreshList(kmlDocument: KmlDocument, features: ArrayList<KmlFeature>){
        geoFeatures = arrayListOf<String>()
        for(i in features){
            if (i.mId == null) {
                geoFeatures.add("Feature without ID")
            } else {
                geoFeatures.add(i.mId)
            }
        }
        val myListView : ListView = findViewById(R.id.editLayers_featuresList)
        val myFeatureAdapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, geoFeatures)
        myListView.adapter = myFeatureAdapter
    }

    fun addFeature(features: ArrayList<KmlFeature>){
        val myDialogBuilder = AlertDialog.Builder(this)
        val myInflater = this.layoutInflater
        val dialogView = myInflater.inflate(R.layout.dialog_add_feature, null)
        myDialogBuilder.setView(dialogView)
        val myDialog = myDialogBuilder.create()

        val id: EditText = dialogView.findViewById(R.id.addFeature_id)
        val confirm: Button = dialogView.findViewById(R.id.addFeature_confirm)
        confirm.setOnClickListener {
            if(features.size >= 1){
                features.add(features[features.size - 1].clone())
                features[features.size - 1].mId = id.text.toString()
                for(key in features[features.size - 1].mExtendedData.keys){
                    features[features.size - 1].mExtendedData[key] = ""
                }
            } else {
                Toast.makeText(this@EditLayersActivity, R.string.editLayersActivity_emptyLayer, Toast.LENGTH_SHORT).show()
            }
            val kmlDocument = KmlDocument()
            refreshList(kmlDocument, features)
            myDialog.dismiss()
        }
        myDialog.show()

    }

    fun removeFeature(position: Int, features: ArrayList<KmlFeature>){
        try{
            if(features.size > 1){
                features.removeAt(position)
                val kmlDocument = KmlDocument()
                refreshList(kmlDocument, features)
            } else {
                Toast.makeText(this@EditLayersActivity, R.string.editLayersActivity_lastFeature, Toast.LENGTH_SHORT).show()
            }
        } catch (err: Exception){
            Toast.makeText(this@EditLayersActivity, R.string.editLayersActivity_cannotRemoveFeature, Toast.LENGTH_SHORT).show()
        }
    }

    fun addAttribute(features: ArrayList<KmlFeature>){
        val myDialogBuilder = AlertDialog.Builder(this)
        val myInflater = this.layoutInflater
        val dialogView = myInflater.inflate(R.layout.dialog_add_attribute, null)
        myDialogBuilder.setView(dialogView)
        val myDialog = myDialogBuilder.create()

        val attributeName: EditText = dialogView.findViewById(R.id.addAttribute_key)
        val defaultValue: EditText = dialogView.findViewById(R.id.addAttribute_defaultValue)
        val confirm: Button = dialogView.findViewById(R.id.addAttribute_confirm)
        confirm.setOnClickListener{
            if(attributeName.text.toString().isNotBlank()){
                for(feat in features){
                    feat.mExtendedData[attributeName.text.toString()] = defaultValue.text.toString()
                }
                val kmlDocument = KmlDocument()
                refreshList(kmlDocument, features)
            } else {
                Toast.makeText(this@EditLayersActivity, R.string.editLayersActivity_wrongColumnName, Toast.LENGTH_SHORT).show()
            }
            myDialog.dismiss()
        }
        myDialog.show()
    }

    fun removeAttribute(position: Int, features: ArrayList<KmlFeature>){
        val myDialogBuilder = AlertDialog.Builder(this)
        val myInflater = this.layoutInflater
        val dialogView = myInflater.inflate(R.layout.dialog_remove_attribute, null)
        myDialogBuilder.setView(dialogView)
        val myDialog = myDialogBuilder.create()

        val myMenuList : ListView = dialogView.findViewById(R.id.removeAttribute_list)
        val myAttributesList = features[position].mExtendedData.keys.toTypedArray()
        val myAdapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, myAttributesList)

        //ustawienie adaptera
        myMenuList.adapter = myAdapter
        myMenuList.setOnItemClickListener { adapterView, view, position2, id ->
            for(feat in features){
                feat.mExtendedData.remove(myAttributesList[position2])
            }
            val kmlDocument = KmlDocument()
            refreshList(kmlDocument, features)
            myDialog.dismiss()
        }
        myDialog.show()
    }
}
