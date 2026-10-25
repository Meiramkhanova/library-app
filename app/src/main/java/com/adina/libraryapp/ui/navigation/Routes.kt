package com.adina.libraryapp.ui.navigation

/**
 * Type-safe route names for Navigation Compose.
 * Bottom tabs stay on the graph root; [ItemDetail] is a stacked destination
 * that receives an item id as a navigation argument.
 */
object Routes {
    const val Catalog = "catalog"
    const val Loans = "loans"
    const val Members = "members"
    const val ItemDetail = "item/{itemId}"

    fun itemDetail(itemId: Int): String = "item/$itemId"

    val bottomBarRoutes = setOf(Catalog, Loans, Members)
}
