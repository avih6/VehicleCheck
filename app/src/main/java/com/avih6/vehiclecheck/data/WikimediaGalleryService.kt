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
    val width: Int = 0,
    val height: Int = 0
)

object WikimediaGalleryService {

    suspend fun fetchCarImages(make: String, model: String, limit: Int = 16): List<CarGalleryImage> = withContext(Dispatchers.IO) {
        val query = "$make $model car"
        val encodedQuery = URLEncoder.encode(query, "UTF-8")
        val urlStr = "https://commons.wikimedia.org/w/api.php?action=query&generator=search&gsrnamespace=6&gsrsearch=$encodedQuery&gsrlimit=$limit&prop=imageinfo&iiprop=url|size&format=json&origin=*"

        try {
            val url = URL(urlStr)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 8000
            connection.readTimeout = 8000
            connection.setRequestProperty("User-Agent", "VehicleCheckApp/1.0 (Android; open-source)")

            if (connection.responseCode == 200) {
                val jsonText = connection.inputStream.bufferedReader().use { it.readText() }
                val root = JSONObject(jsonText)
                val queryObj = root.optJSONObject("query") ?: return@withContext emptyList()
                val pagesObj = queryObj.optJSONObject("pages") ?: return@withContext emptyList()

                val results = mutableListOf<CarGalleryImage>()
                val keys = pagesObj.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val page = pagesObj.getJSONObject(key)
                    val title = page.optString("title", "").replace("File:", "").replace(".jpg", "", ignoreCase = true).replace(".png", "", ignoreCase = true)
                    val imageInfoArr = page.optJSONArray("imageinfo")
                    if (imageInfoArr != null && imageInfoArr.length() > 0) {
                        val info = imageInfoArr.getJSONObject(0)
                        val imgUrl = info.optString("url")
                        val w = info.optInt("width", 0)
                        val h = info.optInt("height", 0)

                        if (imgUrl.isNotBlank() && (imgUrl.endsWith(".jpg", ignoreCase = true) || imgUrl.endsWith(".jpeg", ignoreCase = true) || imgUrl.endsWith(".png", ignoreCase = true))) {
                            results.add(CarGalleryImage(title, imgUrl, w, h))
                        }
                    }
                }
                results
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}