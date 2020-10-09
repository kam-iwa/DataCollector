package com.kamil.iwaniuk.datacollector

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.preference.PreferenceManager
import org.osmdroid.bonuspack.kml.KmlDocument
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.util.LocationUtils
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.OverlayWithIW

class CreatePointLayersActivity : AppCompatActivity() {

    var myMapView: MapView? = null
    val myFunctions = MyFunctions()
    var myMapViewController = myMapView?.controller
    var layerName = ""
    var points = arrayListOf<OverlayWithIW>()
    var attributes = arrayListOf<String>("opis")
    var attributesValues = arrayListOf<String>("")

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_point_layers)

        val myListView: ListView = findViewById(R.id.createPointLayers_attributesList)
        val myAdapter = AttributeListAdapter(this, attributes, attributesValues)
        myListView.adapter = myAdapter

        //umożliwienie OSM pobieranie map
        val ctx: Context = getApplicationContext()
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))

        //ustawienie menedzera lokalizacji
        val myLocationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager

        //ustawienie sluchacza lokalizacji
        val newLocationListener = object : LocationListener {
            override fun onLocationChanged(location: Location?) {
                printLocation(myLocationManager)
                myMapViewController?.setCenter(GeoPoint(location!!.latitude, location!!.longitude))
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
                printLocation(myLocationManager)
            }

            override fun onProviderEnabled(provider: String?) {
                printLocation(myLocationManager)
            }

            override fun onProviderDisabled(provider: String?) {
                printLocation(myLocationManager)
            }
        }
        myLocationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            0,
            0f,
            newLocationListener
        )

        //pasek akcji
        val myActionBar = supportActionBar
        myActionBar!!.title = getString(R.string.mainActivity_pointLayer)
        myActionBar.setDisplayHomeAsUpEnabled(true)

        //tworzenie mapy
        myMapView = findViewById(R.id.createPointLayers_mapView)
        myMapView?.isClickable = true
        myMapView?.setMultiTouchControls(true)
        myMapView?.setUseDataConnection(true)
        myMapView?.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE)
        myMapView?.minZoomLevel = 8.0

        //tworzenie kontrolera mapy
        myMapViewController = myMapView?.controller
        myMapViewController?.setZoom(8.0)
        myMapViewController?.setCenter(GeoPoint(52.19109722, 19.35527778))

        //tworzenie przycisku do dodawania punktów
        val addPointButton: Button = findViewById(R.id.createPointLayers_addNewPoint)
        addPointButton.setOnClickListener {
            val myLocation: Location? = LocationUtils.getLastKnownLocation(myLocationManager)
            if (myLocation?.latitude != null && myLocation?.longitude != null) {
                val yourCoords = GeoPoint(myLocation.latitude, myLocation.longitude)
                val yourCoordsMarker = Marker(myMapView)
                yourCoordsMarker.position = yourCoords
                val layerAttributesText = StringBuilder()
                for ((counter, value) in attributesValues.withIndex()) {
                    if (counter == attributesValues.size - 1) {
                        layerAttributesText.append(attributes[counter] + "=" + value)
                    } else {
                        layerAttributesText.append(attributes[counter] + "=" + value + ";")
                    }
                }
                yourCoordsMarker.subDescription = layerAttributesText.toString()
                myMapView?.overlays?.add(yourCoordsMarker)
                points.add(yourCoordsMarker)
                myMapView?.invalidate()
            } else {
                Toast.makeText(
                    this@CreatePointLayersActivity,
                    R.string.viewLayersActivity_locationNotKnown,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    }

    override fun onResume() {
        super.onResume()
        myMapView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        myMapView?.onPause()
    }

    override fun onSupportNavigateUp(): Boolean {
        myFunctions.myBackDialog(this, this@CreatePointLayersActivity)
        return true
    }

    override fun onBackPressed() {
        myFunctions.myBackDialog(this, this@CreatePointLayersActivity)
    }

    //Tworzenie menu opcji w prawym gornym rogu
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        getMenuInflater().inflate(R.menu.create_layers, menu)
        return true
    }

    //Zdarzenia przy kliknięciu opcji w menu
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle presses on the action bar items
        // View v = findViewById(R.id.f);
        when (item.getItemId()) {
            R.id.create_layers_save_layer ->
                saveLayer(myMapView)
            R.id.create_layers_new_layer ->
                newLayer(myMapView)
            R.id.view_layers_change_basemap ->
                chooseBasemap(myMapView)
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    fun printLocation(myLocationManager: LocationManager) {
        val n: TextView = findViewById(R.id.createPointLayers_latitudeValue)
        val e: TextView = findViewById(R.id.createPointLayers_longitudeValue)
        val myLocation: Location? = LocationUtils.getLastKnownLocation(myLocationManager)
        n.text = myLocation?.latitude.toString()
        e.text = myLocation?.longitude.toString()
    }

    fun newLayer(mapView: MapView?) {
        val myDialogBuilder = AlertDialog.Builder(this)
        val myInflater = this.layoutInflater
        val dialogView = myInflater.inflate(R.layout.dialog_create_layers_new_layer, null)
        myDialogBuilder.setView(dialogView)

        //deklaracja przycisków dialogu i ich obsługa
        val myDialog = myDialogBuilder.create()
        val myAttributesName: EditText = dialogView.findViewById(R.id.new_layer_attributes)
        val myConfirmButton: Button = dialogView.findViewById(R.id.btn_confirm_create_layer)
        myConfirmButton.setOnClickListener {
            attributes = arrayListOf<String>()
            attributesValues = arrayListOf<String>()
            val attributesString = myAttributesName.text.toString()
            if (!(attributesString.length != 0 || attributesString.equals(",") || attributesString.endsWith(
                    ","
                ))
            ) {
                Toast.makeText(
                    this@CreatePointLayersActivity,
                    R.string.createLayersActivity_wrongAttributes,
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val attributesStringList = attributesString.split(',')
                for ((counter, i) in attributesStringList.withIndex()) {
                    attributes.add(i)
                    attributesValues.add("")
                }
                val myListView: ListView = findViewById(R.id.createPointLayers_attributesList)
                val myAdapter = AttributeListAdapter(this, attributes, attributesValues)
                myListView.adapter = myAdapter
                myDialog.dismiss()
            }
        }

        myDialog.show()
    }

    fun saveLayer(mapView: MapView?) {
        val myDialogBuilder = AlertDialog.Builder(this)
        val myInflater = this.layoutInflater
        val dialogView = myInflater.inflate(R.layout.dialog_create_layers_save_layer, null)
        myDialogBuilder.setView(dialogView)

        //deklaracja przycisków dialogu i ich obsługa
        val myDialog = myDialogBuilder.create()
        val myLayerName: EditText = dialogView.findViewById(R.id.save_layer_name)
        val myConfirmButton: Button = dialogView.findViewById(R.id.btn_confirm_save)
        myConfirmButton.setOnClickListener {
            val kmlDocument = KmlDocument()
            val localFile =
                kmlDocument.getDefaultPathForAndroid(myLayerName.text.toString() + ".geojson")
            kmlDocument.mKmlRoot.addOverlays(mapView!!.overlays, kmlDocument)
            for ((counter, i) in kmlDocument.mKmlRoot.mItems.withIndex()) {
                i.mId = counter.toString()
                val list1: ArrayList<String> = ArrayList(points[counter].subDescription.split(';'))
                val list2: ArrayList<ArrayList<String>> = arrayListOf()

                for ((counter_list, i) in list1.withIndex()) {
                    val temp = ArrayList(i.split("="))
                    list2.add(temp)
                }
                for ((counter_att, att) in list2.withIndex()) {
                    i.setExtendedData(list2[counter_att][0], list2[counter_att][1])
                }
            }
            kmlDocument.saveAsGeoJSON(localFile)
            myDialog.dismiss()
        }

        myDialog.show()
    }

    //funkcja zmieniajaca podklad
    fun chooseBasemap(mapView : MapView?){
        //tworzenie dialogu
        val myDialogBuilder = AlertDialog.Builder(this)
        val myInflater = this.layoutInflater
        val dialogView = myInflater.inflate(R.layout.dialog_view_layers_choose_basemap, null)
        myDialogBuilder.setView(dialogView)

        //deklaracja przycisków dialogu i ich obsługa
        val myDialog = myDialogBuilder.create()
        val button_osm : Button = dialogView.findViewById(R.id.btn_osm)
        val button_topo : Button = dialogView.findViewById(R.id.btn_topo)
        button_osm.setOnClickListener{
            mapView?.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE)
            mapView?.setUseDataConnection(true)
            myDialog.cancel()
        }
        button_topo.setOnClickListener{
            mapView?.setTileSource(TileSourceFactory.OpenTopo)
            mapView?.setUseDataConnection(true)
            myDialog.cancel()
        }
        myDialog.show()
    }
}
