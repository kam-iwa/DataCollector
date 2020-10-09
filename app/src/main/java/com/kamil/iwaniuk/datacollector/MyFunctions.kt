package com.kamil.iwaniuk.datacollector

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface

class MyFunctions {

    //okienka dialogowe

    fun myExitDialog(context: Context) {
        val myDialog = AlertDialog.Builder(context)
        myDialog.setTitle(R.string.myFunctions_exitApp)
        myDialog.setMessage(R.string.myFunctions_exitAppDesc)
        myDialog.setPositiveButton(R.string.text_yes) { _: DialogInterface, _: Int ->
            System.exit(0)
        }
        myDialog.setNegativeButton(R.string.text_no) { _: DialogInterface, _: Int -> }
        myDialog.show()
    }

    fun myBackDialog(context: Context, activity: Activity) {
        val myDialog = AlertDialog.Builder(context)
        myDialog.setTitle(R.string.myFunctions_goToMenu)
        myDialog.setMessage(R.string.myFunctions_goToMenuDesc)
        myDialog.setPositiveButton(R.string.text_yes) { _: DialogInterface, _: Int ->
            //activity.onBackPressed()
            activity.finish()
        }
        myDialog.setNegativeButton(R.string.text_no) { _: DialogInterface, _: Int -> }
        myDialog.show()
    }

}