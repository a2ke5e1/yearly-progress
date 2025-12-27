package com.a3.yearlyprogess.feature.settings.util


import android.content.Context
import com.a3.yearlyprogess.core.util.Log
import com.a3.yearlyprogess.domain.model.City
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

object CityDataParser {

    suspend fun loadCitiesFromAssets(context: Context, fileName: String = "worldcities.csv"): List<City> =
        withContext(Dispatchers.IO) {
            val cities = mutableListOf<City>()

            try {
                context.assets.open(fileName).use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream)).use { reader ->
                        // Skip header
                        reader.readLine()

                        reader.lineSequence().forEach { line ->
                            try {
                                val city = parseCsvLine(line)
                                if (city != null) {
                                    cities.add(city)
                                }
                            } catch (e: Exception) {
                                // Skip malformed lines
                                Log.e("CityDataParser", "Error parsing line: $line", e)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("CityDataParser", "Error loading cities from assets", e)
            }

            cities
        }

    private fun parseCsvLine(line: String): City? {
        // Handle CSV with quoted fields
        val fields = mutableListOf<String>()
        var currentField = StringBuilder()
        var inQuotes = false

        for (char in line) {
            when {
                char == '"' -> inQuotes = !inQuotes
                char == ',' && !inQuotes -> {
                    fields.add(currentField.toString())
                    currentField.clear()
                }
                else -> currentField.append(char)
            }
        }
        fields.add(currentField.toString())

        if (fields.size < 11) return null

        return try {
            City(
                name = fields[0],
                nameAscii = fields[1],
                latitude = fields[2].toDouble(),
                longitude = fields[3].toDouble(),
                country = fields[4],
                iso2 = fields[5],
                iso3 = fields[6],
                adminName = fields[7],
                population = fields[9].toLongOrNull()
            )
        } catch (e: Exception) {
            null
        }
    }
}