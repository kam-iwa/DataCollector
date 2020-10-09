package com.kamil.iwaniuk.datacollector

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


class MainActivity : AppCompatActivity() {

    val myFunctions = MyFunctions()
    val myPermissions = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //zapytanie o uprawnienia
        requestNecessaryPermissions(myPermissions)

        //lista wartosci menu
        val myMenuValues = arrayListOf(
            getString(R.string.mainActivity_viewLayers),
            getString(R.string.mainActivity_createLayers),
            getString(R.string.mainActivity_editLayers),
            getString(R.string.mainActivity_exitApp)
        )

        //utworzenie listy menu
        val myMenuList: ListView = findViewById(R.id.menu)

        //utworzenie adaptera listy
        val myAdapter =
            ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, myMenuValues)

        //ustawienie adaptera
        myMenuList.adapter = myAdapter

        //obsluga klikniec elementow listy
        myMenuList.setOnItemClickListener { adapterView, view, position, id ->
            if (position == 0) {
                if(returnMissingPermissionsMessage(myPermissions)){
                    startActivity(Intent(this@MainActivity, ViewLayersActivity::class.java))
                } else {
                    Toast.makeText(this@MainActivity, R.string.mainActivity_permissionsRequired, Toast.LENGTH_SHORT).show()
                }
            }
            if (position == 1) {
                if(returnMissingPermissionsMessage(myPermissions)) {
                    chooseGeometryType()
                } else {
                    Toast.makeText(this@MainActivity, R.string.mainActivity_permissionsRequired, Toast.LENGTH_SHORT).show()
                }
            }
            if (position == 2) {
                startActivity(Intent(this@MainActivity, EditLayersActivity::class.java))
            }
            if (position == 3) {
                myFunctions.myExitDialog(this)
            }
        }
    }

    //wykonuje sie przy nacisnieciu przycisku cofniecia
    override fun onBackPressed() {
        myFunctions.myExitDialog(this)
    }

    private fun requestNecessaryPermissions(permissions: Array<String>) {
        val permissionsToRequest: ArrayList<String> = ArrayList()
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED
            ) {
                // gdy uprawnienia nie sa przyznane
                permissionsToRequest.add(permission)
            }
        }
        if (permissionsToRequest.size > 0) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toArray(arrayOfNulls(0)),
                1
            )
        }
    }

    private fun returnMissingPermissionsMessage(permissions: Array<String>): Boolean{
        val permissionsToRequest: ArrayList<String> = ArrayList()
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED
            ) {
                // gdy uprawnienia nie sa przyznane
                permissionsToRequest.add(permission)
            }
        }
        return permissionsToRequest.size <= 0
    }

    fun chooseGeometryType(){
        val myDialogBuilder = AlertDialog.Builder(this)
        val myInflater = this.layoutInflater
        val myDialogView = myInflater.inflate(R.layout.dialog_main_activity_choose_type, null)
        myDialogBuilder.setView(myDialogView)
        val myDialog = myDialogBuilder.create()

        //deklaracja przycisków dialogu i ich obsługa
        val myPointButton: Button = myDialogView.findViewById(R.id.mainActivity_geomType_pointButton)
        val myLineButton: Button = myDialogView.findViewById(R.id.mainActivity_geomType_lineButton)
        val myPolygonButton: Button = myDialogView.findViewById(R.id.mainActivity_geomType_polygonButton)

        myPointButton.setOnClickListener {
            startActivity(Intent(this@MainActivity, CreatePointLayersActivity::class.java))
            myDialog.dismiss()
        }
        myLineButton.setOnClickListener {
            startActivity(Intent(this@MainActivity, CreateLineLayersActivity::class.java))
            myDialog.dismiss()
        }
        myPolygonButton.setOnClickListener {
            startActivity(Intent(this@MainActivity, CreatePolygonLayersActivity::class.java))
            myDialog.dismiss()
        }

        myDialog.show()
    }


}
