package nuvola.realescrape

enum class PropertyRecordType {
    SOLD, LISTED, PENDING, EMPTY;
    companion object {
        fun from(stringValue: String): PropertyRecordType {
            if (stringValue.contains("Listed")) return LISTED
            if (stringValue.contains("Pending")) return PENDING
            if (stringValue.contains("Sold")) return SOLD
            throw IllegalArgumentException("Could not understand record type $stringValue")
        }
    }
}
