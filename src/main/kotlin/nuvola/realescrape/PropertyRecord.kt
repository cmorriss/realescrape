package nuvola.realescrape

import java.util.Date

data class PropertyRecord(val date: Date, val type: PropertyRecordType, val price: Int) {
    companion object {
        val EMPTY = PropertyRecord(Date(), PropertyRecordType.EMPTY, -1)
    }

    fun isEmpty() : Boolean {
        return type == PropertyRecordType.EMPTY
    }

    fun isNotEmpty() : Boolean {
        return !isEmpty()
    }
}
