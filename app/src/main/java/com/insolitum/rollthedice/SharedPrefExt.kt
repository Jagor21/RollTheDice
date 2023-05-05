package com.insolitum.rollthedice

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.SharedPreferences

private const val SP_NAME = "dice_sp"
const val INSTALL_ATTRIBUTION_SP_KEY = "install_attribution"

fun Application.getSharedPref(): SharedPreferences = getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
fun Activity.getSharedPref(): SharedPreferences = getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
