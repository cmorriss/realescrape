package nuvola.realescrape

import org.lightcouch.CouchDbClient

class RedfinQuery {
    val dbHomes = loadDB()

    fun loadDB() : Set<Home> {
        val client = CouchDbClient()
        return client.view("_all_docs").includeDocs(true).query(Home::class.java).toSet()
    }

    fun percentOver() {
        val zipHomes = HashSet<Home>()
        var totalNumOver = 0
        var totalNumUnder = 0
        var totalAmountOver = 0
        var totalAmountUnder = 0
        var cityHomesOver = HashSet<String>()
        dbHomes.forEach {
                if (it.listed.isNotEmpty() && it.sold.isNotEmpty()) {
                    val miss = it.sold.price - it.listed.price
                    if (miss <= 0) {
                        totalAmountUnder += -miss
                        totalNumUnder++
                    } else {
                        cityHomesOver.add(it.city)
                        totalAmountOver += miss
                        totalNumOver++
                    }
                }
        }

        val averageUnder = if (totalNumUnder == 0) 0 else totalAmountUnder / totalNumUnder
        val averageOver = if (totalNumOver == 0) 0 else totalAmountOver / totalNumOver
        println("num under: $totalNumUnder")
        println("average under: $averageUnder")
        println("num over: $totalNumOver")
        println("average over: $averageOver")
        println("Cities over:")
        cityHomesOver.forEach(::println)
    }
}

fun main(args: Array<String>) {
    val query = RedfinQuery()
    query.percentOver()
}
