package nuvola.realescrape

import org.lightcouch.CouchDbClient
import java.io.File

class RedfinData {
    val soldHomesData by lazy {
        val homes = HashSet<Home>()
        File(RedfinDownloader.downloadPath).listFiles().filter { it.isFile }.forEach {
            homes.addAll(loadCSV(it))
        }
        homes
    }

    val processedSoldData by lazy {
        val dbClient = CouchDbClient()
        dbClient.view("_all_docs").includeDocs(true).query(Home::class.java)
    }

    val unprocessedSoldData by lazy {
        val soldHomes = HashSet<Home>()
        File(RedfinDownloader.homeDataPath).listFiles()?.forEach {

        }
    }
}
