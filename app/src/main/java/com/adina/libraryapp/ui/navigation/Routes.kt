package com.adina.libraryapp.ui.navigation

object Routes {
    const val Catalog = "catalog"
    const val Loans = "loans"
    const val Members = "members"
    const val ItemDetail = "item/{itemId}"

    fun itemDetail(itemId: Int): String = "item/$itemId"

    val bottomBarRoutes = setOf(Catalog, Loans, Members)
}
