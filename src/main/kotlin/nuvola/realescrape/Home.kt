package nuvola.realescrape

import com.google.gson.annotations.SerializedName
import org.lightcouch.Attachment
import java.io.File

data class Home(val address: String, val city: String, val zipCode: String, val redfinUrl: String) {
    @SerializedName("_id")
    private val id: String? = null
    @SerializedName("_rev")
    private val rev: String? = null
    @SerializedName("_attachments")
    private var attachments: MutableMap<String, Attachment>? = null

    private val sales = HashSet<String>()
    private val propertyRecords = HashSet<PropertyRecord>()
    var listed: PropertyRecord = PropertyRecord.EMPTY
    var pending: PropertyRecord = PropertyRecord.EMPTY
    var sold: PropertyRecord = PropertyRecord.EMPTY
    var timeOnMarket = -1L

    fun addSale(nwmlsNumber: String) {
        sales.add(nwmlsNumber)
    }

    fun getSales(): Set<String> {
        return sales
    }

    fun addPropertyRecord(propertyRecord: PropertyRecord) {
        propertyRecords.add(propertyRecord)
        analyze()
    }

    fun getPropertyRecords(): Set<PropertyRecord> {
        return propertyRecords
    }

    fun prepareDataFile() : File {
        val homeDataDirectory = File(RedfinDownloader.homeDataPath + zipCode)
        if (!homeDataDirectory.exists()) {
            homeDataDirectory.mkdirs()
        }
        return File(RedfinDownloader.homeDataPath + zipCode + File.separator + getCleanAddress())
    }

    internal fun getCleanAddress() : String {
        return address.replace(Regex("[: \\\\/]"), "_")
    }

    private fun analyze() {
        propertyRecords.forEach {
            when (it.type) {
                PropertyRecordType.LISTED  ->
                    if (listed.isEmpty() || listed.date < it.date) {
                        listed = it
                    }
                PropertyRecordType.PENDING ->
                    if (pending.isEmpty() || pending.date < it.date) {
                        pending = it
                    }
                PropertyRecordType.SOLD    ->
                    if (sold.isEmpty() || sold.date < it.date) {
                        sold = it
                    }
                else                       -> {
                }
            }
        }
        if (listed.isNotEmpty() && pending.isNotEmpty()) {
            timeOnMarket = ((pending.date.time - listed.date.time) / (1000*60*60*24))
        }
    }
}
