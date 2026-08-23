package com.avih6.vehiclecheck.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

data class CarGalleryImage(
    val title: String,
    val imageUrl: String,
    val thumbUrl: String,
    val descriptionUrl: String = "",
    val license: String = "Creative Commons (Wikimedia Commons)",
    val artist: String = "",
    val description: String = "",
    val altText: String = "",
    val width: Int = 0,
    val height: Int = 0
)

data class GalleryPageResult(
    val images: List<CarGalleryImage>,
    val nextOffset: Int?
)

object WikimediaGalleryService {

    suspend fun fetchCarImages(
        rawMake: String,
        rawModel: String = "",
        limit: Int = 30
    ): List<CarGalleryImage> = withContext(Dispatchers.IO) {
        fetchGalleryPage(rawMake, rawModel, offset = 0, limit = limit).images
    }

    suspend fun fetchGalleryPage(
        rawMake: String,
        rawModel: String = "",
        offset: Int = 0,
        limit: Int = 40
    ): GalleryPageResult = withContext(Dispatchers.IO) {
        val query = buildSearchQuery(rawMake, rawModel)
        val encodedQuery = URLEncoder.encode(query, "UTF-8")
        val offsetParam = if (offset > 0) "&gsroffset=$offset" else ""
        val urlStr = "https://commons.wikimedia.org/w/api.php?action=query&generator=search&gsrnamespace=6&gsrsearch=$encodedQuery&gsrlimit=$limit$offsetParam&prop=imageinfo&iiprop=url|size|extmetadata&iiurlwidth=800&format=json&origin=*"

        try {
            val url = URL(urlStr)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 8000
            connection.readTimeout = 8000
            connection.setRequestProperty("User-Agent", "VehicleCheckApp/1.0 (Android; open-source; https://github.com/avih6/VehicleCheck; admin@vehiclecheck.app)")

            if (connection.responseCode == 200) {
                val jsonText = connection.inputStream.bufferedReader().use { it.readText() }
                val root = JSONObject(jsonText)
                
                val nextOffset = root.optJSONObject("continue")?.optInt("gsroffset")
                val queryObj = root.optJSONObject("query") ?: return@withContext GalleryPageResult(emptyList(), null)
                val pagesObj = queryObj.optJSONObject("pages") ?: return@withContext GalleryPageResult(emptyList(), null)

                val results = mutableListOf<CarGalleryImage>()
                val keys = pagesObj.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val page = pagesObj.getJSONObject(key)
                    val rawTitle = page.optString("title", "")
                    val title = rawTitle
                        .replace("File:", "")
                        .replace(".jpg", "", ignoreCase = true)
                        .replace(".jpeg", "", ignoreCase = true)
                        .replace(".png", "", ignoreCase = true)
                        .replace(".webp", "", ignoreCase = true)
                        .replace("_", " ")

                    val imageInfoArr = page.optJSONArray("imageinfo")
                    if (imageInfoArr != null && imageInfoArr.length() > 0) {
                        val info = imageInfoArr.getJSONObject(0)
                        val fullUrl = info.optString("url")
                        val thumbUrl = info.optString("thumburl").ifBlank { fullUrl }
                        val descUrl = info.optString("descriptionurl").ifBlank {
                            "https://commons.wikimedia.org/wiki/${URLEncoder.encode(rawTitle, "UTF-8")}"
                        }
                        val w = info.optInt("width", 0)
                        val h = info.optInt("height", 0)

                        val extMetadata = info.optJSONObject("extmetadata")
                        val licName = extMetadata?.optJSONObject("LicenseShortName")?.optString("value")
                        val license = if (!licName.isNullOrBlank()) licName else "Creative Commons (Wikimedia Commons)"

                        val rawArtist = extMetadata?.optJSONObject("Artist")?.optString("value").orEmpty()
                        val cleanArtist = rawArtist.replace(Regex("<[^>]*>"), "").trim()

                        val rawDesc = extMetadata?.optJSONObject("ImageDescription")?.optString("value").orEmpty()
                        val cleanDesc = rawDesc.replace(Regex("<[^>]*>"), "").replace("\n", " ").trim()
                        val altText = if (cleanDesc.isNotBlank()) cleanDesc else "תמונת רכב $title"

                        val checkPath = fullUrl.substringBefore("?")
                        val isValidExtension = checkPath.endsWith(".jpg", ignoreCase = true) ||
                                              checkPath.endsWith(".jpeg", ignoreCase = true) ||
                                              checkPath.endsWith(".png", ignoreCase = true) ||
                                              checkPath.endsWith(".webp", ignoreCase = true)

                        // Filter out icon, logo, map, diagram, flag SVGs/PNGs
                        val lowerTitle = title.lowercase()
                        val isJunk = lowerTitle.contains("logo") || lowerTitle.contains("icon") ||
                                     lowerTitle.contains("flag") || lowerTitle.contains("diagram") ||
                                     lowerTitle.contains("map") || lowerTitle.contains("badge") ||
                                     lowerTitle.contains("emblem") || lowerTitle.contains("symbol")

                        if (thumbUrl.isNotBlank() && isValidExtension && !isJunk) {
                            results.add(CarGalleryImage(
                                title = title,
                                imageUrl = fullUrl,
                                thumbUrl = thumbUrl,
                                descriptionUrl = descUrl,
                                license = license,
                                artist = cleanArtist,
                                description = cleanDesc,
                                altText = altText,
                                width = w,
                                height = h
                            ))
                        }
                    }
                }
                GalleryPageResult(results, nextOffset)
            } else {
                GalleryPageResult(emptyList(), null)
            }
        } catch (e: Exception) {
            GalleryPageResult(emptyList(), null)
        }
    }

    private fun buildSearchQuery(rawMake: String, rawModel: String): String {
        val trimmedMake = rawMake.trim()
        val trimmedModel = rawModel.trim()

        if (trimmedMake.isBlank() || trimmedMake == "הכל" || trimmedMake.equals("all", ignoreCase = true)) {
            return if (trimmedModel.isNotBlank()) {
                "$trimmedModel car vehicle"
            } else {
                "automobiles passenger cars vehicle modern"
            }
        }

        val (makeEn, modelEn) = VehicleUtils.getEnglishMakeAndModel(trimmedMake, trimmedModel)
        val brand = if (makeEn != "car") makeEn else trimmedMake
        val model = if (modelEn != "car") modelEn else trimmedModel

        return when {
            model.isNotBlank() && !model.equals("car", ignoreCase = true) -> "$brand $model car"
            brand.isNotBlank() && !brand.equals("car", ignoreCase = true) -> "$brand car vehicle"
            else -> "automobiles passenger cars vehicle"
        }
    }
}