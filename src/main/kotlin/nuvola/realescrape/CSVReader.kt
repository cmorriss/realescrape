package nuvola.realescrape

import java.io.File
import java.util.ArrayList
import java.util.Scanner

object CSVConstants {

    val DEFAULT_SEPARATOR = ','
    val DEFAULT_QUOTE = '"'
}

fun Scanner.parseLine(suppliedSeparators: Char = CSVConstants.DEFAULT_SEPARATOR,
                            suppliedCustomQuote: Char = CSVConstants.DEFAULT_QUOTE): List<String> {
    var separators = suppliedSeparators
    var customQuote = suppliedCustomQuote

    val result = ArrayList<String>()

    val csvLine = nextLine()
    //if empty, return!
    if (csvLine == null || csvLine.isEmpty()) {
        return result
    }

    if (customQuote == ' ') {
        customQuote = CSVConstants.DEFAULT_QUOTE
    }

    if (separators == ' ') {
        separators = CSVConstants.DEFAULT_SEPARATOR
    }

    var curVal = StringBuffer()
    var inQuotes = false
    var startCollectChar = false
    var doubleQuotesInColumn = false

    val chars = csvLine.toCharArray()

    for (ch in chars) {

        if (inQuotes) {
            startCollectChar = true
            if (ch == customQuote) {
                inQuotes = false
                doubleQuotesInColumn = false
            } else {

                //Fixed : allow "" in custom quote enclosed
                if (ch == '\"') {
                    if (!doubleQuotesInColumn) {
                        curVal.append(ch)
                        doubleQuotesInColumn = true
                    }
                } else {
                    curVal.append(ch)
                }

            }
        } else {
            if (ch == customQuote) {

                inQuotes = true

                //Fixed : allow "" in empty quote enclosed
                if (chars[0] != '"' && customQuote == '\"') {
                    curVal.append('"')
                }

                //double quotes in column will hit this!
                if (startCollectChar) {
                    curVal.append('"')
                }

            } else if (ch == separators) {

                result.add(curVal.toString())

                curVal = StringBuffer()
                startCollectChar = false

            } else if (ch == '\r') {
                //ignore LF characters
                continue
            } else if (ch == '\n') {
                //the end, break!
                break
            } else {
                curVal.append(ch)
            }
        }

    }

    result.add(curVal.toString())

    return result
}

fun loadCSV(csvFile: File): Set<Home> {
    val homes = HashSet<Home>()
    val scanner = Scanner(csvFile)
    scanner.parseLine()
    while (scanner.hasNext()) {
        val line = scanner.parseLine()
        if (line.size >= 22 && line[22].isNotEmpty()) {
            val url = line[20].replace(Regex("\\)"), "")
            val home = Home(line[3], line[4], line[6], url)
            home.addSale(line[22])
            homes.add(home)
        }
    }
    scanner.close()
    return homes
}

