package com.kamil.iwaniuk.datacollector

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import org.osmdroid.bonuspack.kml.KmlDocument
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.util.LocationUtils.getLastKnownLocation
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.FolderOverlay
import java.io.File
import java.io.FilenameFilter

class ViewLayersActivity : AppCompatActivity() {

    //val myFunctions = MyFunctions()
    var myMapView : MapView? = null
    var myMapViewController  = myMapView?.controller

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_layers)

        //umożliwienie OSM pobieranie map
        val ctx : Context = getApplicationContext()
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))

        //ustawienie menedzera lokalizacji
        val myLocationManager : LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        //ustawienie sluchacza lokalizacji
        val newLocationListener = object : LocationListener {
            override fun onLocationChanged(location: Location?) {
                printLocation(myLocationManager)
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
        myLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f,newLocationListener)

        //pasek akcji
        val myActionBar = supportActionBar
        myActionBar!!.title = getString(R.string.mainActivity_viewLayers)
        myActionBar.setDisplayHomeAsUpEnabled(true)

        //tworzenie mapy
        myMapView = findViewById(R.id.viewLayers_mapView)
        myMapView?.isClickable = true
        myMapView?.setMultiTouchControls(true)
        myMapView?.setUseDataConnection(true)
        myMapView?.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE)
        myMapView?.minZoomLevel = 8.0

        //tworzenie kontrolera mapy
        myMapViewController = myMapView?.controller
        myMapViewController?.setZoom(8.0)
        myMapViewController?.setCenter(GeoPoint(52.19109722, 19.35527778))

        val myCenterMapButton = findViewById<Button>(R.id.viewLayers_locationCenterButton)
        myCenterMapButton.setOnClickListener{
            val myLocation : Location? = getLastKnownLocation(myLocationManager)
            if (myLocation?.latitude != null && myLocation?.longitude != null){
                myMapViewController?.setCenter(GeoPoint(myLocation.latitude, myLocation.longitude))
            } else {
                Toast.makeText(this@ViewLayersActivity, R.string.viewLayersActivity_locationNotKnown, Toast.LENGTH_SHORT).show()
            }
        }

    }

    override fun onResume(){
        super.onResume()
        myMapView?.onResume()
    }

    override fun onPause(){
        super.onPause()
        myMapView?.onPause()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    //Tworzenie menu opcji w prawym gornym rogu
    override fun onCreateOptionsMenu(menu : Menu): Boolean {
        getMenuInflater().inflate(R.menu.view_layers, menu)
        return true
    }

    //Zdarzenia przy kliknięciu opcji w menu
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle presses on the action bar items
        // View v = findViewById(R.id.f);
        when (item.getItemId()) {
            R.id.view_layers_change_basemap ->
                chooseBasemap(myMapView)
            R.id.view_layers_add_layer ->
                addLayer(myMapView)
            R.id.view_layers_remove_layer ->
                removeLayer(myMapView)
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    fun printLocation(myLocationManager : LocationManager){
        val n : TextView = findViewById(R.id.viewLayers_latitudeValue)
        val e : TextView = findViewById(R.id.viewLayers_longitudeValue)
        val myLocation : Location? = getLastKnownLocation(myLocationManager)
        n.text = myLocation?.latitude.toString()
        e.text = myLocation?.longitude.toString()
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

    //funkcja dodajaca warstwe
    fun addLayer(mapView : MapView?){
        //utworzenie adaptera
        val kmlDocument = KmlDocument()
        val myFiles = kmlDocument.getDefaultPathForAndroid("")

        //utworzenie listy plikow
        val myFilesNamesList = arrayListOf<String>()
        val myFilesList = arrayListOf(myFiles.listFiles(object : FilenameFilter{
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

        var choosenFile : String = ""
        myMenuList.setOnItemClickListener { adapterView, view, position, id ->
            choosenFile = myFilesNamesList[position]
            //Toast.makeText(this@ViewLayersActivity,choosenFile,Toast.LENGTH_LONG).show()
            try {
                val localFile = kmlDocument.getDefaultPathForAndroid(choosenFile)
                kmlDocument.parseGeoJSON(localFile)
                val geoJsonOverlay = kmlDocument.mKmlRoot.buildOverlay(
                    mapView,
                    null,
                    null,
                    kmlDocument
                ) as FolderOverlay
                mapView?.overlays?.add(geoJsonOverlay)
                myMapView?.invalidate()
            } catch (e : Exception){
                Toast.makeText(this@ViewLayersActivity, R.string.viewLayersActivity_cannotAddLayer, Toast.LENGTH_SHORT).show()
            }
            myDialog.dismiss()
        }
    }

    //funkcja usuwajaca warstwe
    fun removeLayer(mapView : MapView?){

        val myLayersList = arrayListOf<String>()
        for(i in mapView!!.overlays){
            myLayersList.add(i.bounds.toString())
        }
        val myDialogBuilder = AlertDialog.Builder(this)
        val myInflater = this.layoutInflater
        val dialogView = myInflater.inflate(R.layout.dialog_view_layers_remove_layer, null)
        myDialogBuilder.setView(dialogView)

        //deklaracja przycisków dialogu i ich obsługa
        val myDialog = myDialogBuilder.create()
        //utworzenie listy menu
        val myMenuList : ListView = dialogView.findViewById(R.id.layers_to_remove)

        //utworzenie adaptera listy
        val myAdapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, myLayersList)

        //ustawienie adaptera
        myMenuList.adapter = myAdapter
        myDialog.show()

        myMenuList.setOnItemClickListener { adapterView, view, position, id ->
            mapView.overlays.removeAt(position)
            myMapView?.invalidate()
            myDialog.dismiss()
        }
    }

}
