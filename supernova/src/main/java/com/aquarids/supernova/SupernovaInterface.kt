package com.aquarids.supernova

interface SupernovaInterface {

    fun openLink(link: String)
    fun close()
    fun toast(msg: String)
    fun setTitle(title: String)
    fun setNavigationColor(color: String)
    fun showNavigation(show: Boolean)
    fun showLoadingDialog(show: Boolean)
    fun setNavigationTextColor(color: String)
}