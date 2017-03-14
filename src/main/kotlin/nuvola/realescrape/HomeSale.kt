package nuvola.realescrape

import java.util.Date

data class HomeSale(val listingPrice: Int, val soldPrice: Int, val listingDate: Date, val pendingDate: Date,
                    val soldDate: Date, val nwmlsNumber: Int)
