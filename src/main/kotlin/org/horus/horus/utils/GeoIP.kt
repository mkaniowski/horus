package org.horus.horus.utils

import java.io.File
import kotlin.random.Random

class GeoIP(private val filePath: String) {

    private var lines: List<String> = emptyList()
    private var header: List<String> = emptyList()

    // We'll assume the "geoname_id" column is what determines the country:
    private var geoNameIdIndex: Int = -1
    private var networkIndex: Int = -1

    // Hardcode the mapping from country ISO → GeoNameID.
    // Replace these numbers with the actual IDs relevant for your CSV data.
    private val countryGeoNameIdMap = mapOf(
        "RU" to "2017370",
        "CN" to "1814991",
        "BR" to "3469034",
        "IR" to "130758",
        "AF" to "1149361"
    )

    init {
        loadData(filePath)
    }

    private fun loadData(filePath: String) {
        lines = File(filePath).readLines()
        if (lines.size <= 1) {
            throw IllegalArgumentException("CSV file is empty or missing header")
        }

        // Parse the header row
        header = lines[0].split(",")

        // Find indexes of the relevant columns
        geoNameIdIndex = header.indexOf("geoname_id")
        networkIndex = header.indexOf("network")

        if (geoNameIdIndex == -1) {
            throw IllegalArgumentException("No 'geoname_id' column found in CSV file")
        }
        if (networkIndex == -1) {
            throw IllegalArgumentException("No 'network' column found in CSV file")
        }
    }

    /**
     * Returns a random IP from all rows (ignores country).
     */
    fun getRandomIp(): String {
        // Select a random row (excluding the header)
        val randomRow = lines[Random.nextInt(1, lines.size)]
        val rowValues = randomRow.split(",")

        // Extract the IP with the /XX suffix and strip it
        val ipWithSuffix = rowValues[networkIndex]
        return ipWithSuffix.split("/")[0]
    }

    /**
     * Returns a random IP whose geoname_id corresponds to the specified countryCode.
     * Only supports "RU", "CN", "BR", "IR", "AF".
     */
    fun getRandomIpByCountry(countryCode: String): String {
        // Ensure we have a GeoNameID mapping for this ISO country code
        val targetGeoNameId = countryGeoNameIdMap[countryCode]
            ?: throw IllegalArgumentException("Unsupported country code or missing geoname_id mapping: $countryCode")

        // Gather all lines (excluding the header) that match the target geoname_id
        val matchingLines = lines.drop(1).filter { line ->
            val columns = line.split(",")
            columns.size > geoNameIdIndex && columns[geoNameIdIndex] == targetGeoNameId
        }

        if (matchingLines.isEmpty()) {
            throw NoSuchElementException("No rows found for country code: $countryCode")
        }

        // Pick one at random
        val randomRow = matchingLines[Random.nextInt(matchingLines.size)]
        val rowValues = randomRow.split(",")
        val ipWithSuffix = rowValues[networkIndex]

        // Return just the IP portion
        return ipWithSuffix.split("/")[0]
    }
}
