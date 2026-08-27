package com.avih6.vehiclecheck.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
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

    private const val USER_AGENT = "VehicleCheckApp/1.0 (Android; open-source; https://github.com/avih6/VehicleCheck; admin@vehiclecheck.app)"

    suspend fun fetchCarImages(
        rawMake: String,
        rawModel: String = "",
        limit: Int = 30
    ): List<CarGalleryImage> = withContext(Dispatchers.IO) {
        fetchGalleryPage(rawMake, rawModel, offset = 0, limit = limit).images
    }

    suspend fun fetchCarImagesSpecific(
        make: String,
        model: String,
        year: Int?,
        colorHeb: String?,
        limit: Int = 12
    ): List<CarGalleryImage> = withContext(Dispatchers.IO) {
        val colorEn = translateColorToEnglish(colorHeb)
        val (makeEn, modelEn) = VehicleUtils.getEnglishMakeAndModel(make, model)
        val brand = if (makeEn != "car") makeEn else make
        val modelClean = if (modelEn != "car") modelEn else model

        val candidatesMap = mutableMapOf<String, CarGalleryImage>()

        // 1. Prepare multi-tier parallel queries
        val commonsQueries = listOfNotNull(
            if (!colorEn.isNullOrBlank() && year != null && brand.isNotBlank() && modelClean.isNotBlank()) "$brand $modelClean $year $colorEn" else null,
            if (!colorEn.isNullOrBlank() && brand.isNotBlank() && modelClean.isNotBlank()) "$brand $modelClean $colorEn" else null,
            if (year != null && brand.isNotBlank() && modelClean.isNotBlank()) "$brand $modelClean $year" else null,
            if (brand.isNotBlank() && modelClean.isNotBlank()) "$brand $modelClean" else null,
            if (brand.isNotBlank()) brand else null
        ).distinct()

        val wikiQueries = listOfNotNull(
            if (brand.isNotBlank() && modelClean.isNotBlank()) "$brand $modelClean" else null,
            if (brand.isNotBlank()) brand else null
        ).distinct()

        val categoryQueries = listOfNotNull(
            if (brand.isNotBlank() && modelClean.isNotBlank()) "Category:${brand.replace(" ", "_")}_${modelClean.replace(" ", "_")}" else null,
            if (brand.isNotBlank()) "Category:${brand.replace(" ", "_")}" else null
        ).distinct()

        val parallelResults = coroutineScope {
            val d1 = commonsQueries.map { q -> async { fetchCommonsSearch(q, limit = 20).images } }
            val d2 = wikiQueries.map { q -> async { fetchWikipediaSearch(q, limit = 8, isHebrew = false) } }
            val d3 = wikiQueries.map { q -> async { fetchWikipediaSearch(q, limit = 6, isHebrew = true) } }
            val d4 = categoryQueries.map { cat -> async { fetchCommonsCategoryMembers(cat, limit = 15) } }
            (d1 + d2 + d3 + d4).awaitAll()
        }

        for (list in parallelResults) {
            for (img in list) {
                if (!candidatesMap.containsKey(img.imageUrl) && !candidatesMap.containsKey(img.thumbUrl)) {
                    candidatesMap[img.imageUrl] = img
                }
            }
        }

        // Score and sort candidates
        val scored = candidatesMap.values.map { img ->
            val score = scoreImage(img, brand, modelClean, year, colorEn)
            img to score
        }.sortedByDescending { it.second }

        scored.map { it.first }.take(limit)
    }

    suspend fun fetchGalleryPage(
        rawMake: String,
        rawModel: String = "",
        offset: Int = 0,
        limit: Int = 40
    ): GalleryPageResult = withContext(Dispatchers.IO) {
        val query = buildSearchQuery(rawMake, rawModel)
        val commonsResult = fetchCommonsSearch(query, offset, limit)

        // For initial load (offset == 0), augment with Wikipedia & Category images for maximum richness
        if (offset == 0) {
            val (makeEn, modelEn) = VehicleUtils.getEnglishMakeAndModel(rawMake, rawModel)
            val brand = if (makeEn != "car") makeEn else rawMake
            val model = if (modelEn != "car") modelEn else rawModel

            val extraImages = coroutineScope {
                val dWikiEn = async {
                    if (brand.isNotBlank() && model.isNotBlank()) fetchWikipediaSearch("$brand $model", limit = 10, isHebrew = false)
                    else if (brand.isNotBlank()) fetchWikipediaSearch(brand, limit = 10, isHebrew = false)
                    else emptyList()
                }
                val dWikiHe = async {
                    if (rawMake.isNotBlank()) fetchWikipediaSearch("$rawMake $rawModel".trim(), limit = 6, isHebrew = true)
                    else emptyList()
                }
                val dCat = async {
                    if (brand.isNotBlank() && model.isNotBlank()) fetchCommonsCategoryMembers("Category:${brand.replace(" ", "_")}_${model.replace(" ", "_")}", limit = 15)
                    else if (brand.isNotBlank()) fetchCommonsCategoryMembers("Category:${brand.replace(" ", "_")}", limit = 15)
                    else emptyList()
                }
                dWikiEn.await() + dWikiHe.await() + dCat.await()
            }

            val merged = (commonsResult.images + extraImages).distinctBy { it.imageUrl }
            GalleryPageResult(merged, commonsResult.nextOffset)
        } else {
            commonsResult
        }
    }

    suspend fun fetchCommonsSearch(
        rawQuery: String,
        offset: Int = 0,
        limit: Int = 40
    ): GalleryPageResult = withContext(Dispatchers.IO) {
        val lightExclusions = " -logo -icon -diagram -flag -symbol -badge -map -drawing -blueprint"
        val fullQuery = "$rawQuery$lightExclusions"
        val encodedQuery = URLEncoder.encode(fullQuery, "UTF-8")
        val offsetParam = if (offset > 0) "&gsroffset=$offset" else ""
        val urlStr = "https://commons.wikimedia.org/w/api.php?action=query&generator=search&gsrnamespace=6&gsrsearch=$encodedQuery&gsrlimit=$limit$offsetParam&prop=imageinfo&iiprop=url|size|extmetadata&iiurlwidth=800&format=json&origin=*"

        try {
            val url = URL(urlStr)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 8000
            connection.readTimeout = 8000
            connection.setRequestProperty("User-Agent", USER_AGENT)

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
                        .replace(Regex("\\s+"), " ")
                        .trim()

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
                        val cleanArtist = rawArtist
                            .replace(Regex("<[^>]*>"), "")
                            .replace("\n", " ")
                            .replace("\r", " ")
                            .replace(Regex("\\s+"), " ")
                            .trim()

                        val rawDesc = extMetadata?.optJSONObject("ImageDescription")?.optString("value").orEmpty()
                        val cleanDesc = rawDesc.replace(Regex("<[^>]*>"), "").replace("\n", " ").trim()
                        val altText = if (cleanDesc.isNotBlank()) cleanDesc else "תמונת רכב $title"

                        val checkPath = fullUrl.substringBefore("?")
                        val isValidExtension = checkPath.endsWith(".jpg", ignoreCase = true) ||
                                              checkPath.endsWith(".jpeg", ignoreCase = true) ||
                                              checkPath.endsWith(".png", ignoreCase = true) ||
                                              checkPath.endsWith(".webp", ignoreCase = true)

                        val lowerTitle = title.lowercase()
                        val isJunk = lowerTitle.contains("logo") || lowerTitle.contains("icon") ||
                                     lowerTitle.contains("flag") || lowerTitle.contains("diagram") ||
                                     lowerTitle.contains("map") || lowerTitle.contains("badge") ||
                                     lowerTitle.contains("emblem") || lowerTitle.contains("symbol") ||
                                     lowerTitle.contains("interior") || lowerTitle.contains("dashboard") ||
                                     lowerTitle.contains("engine") || lowerTitle.contains("seats") ||
                                     lowerTitle.contains("steering") || lowerTitle.contains("wheel") ||
                                     lowerTitle.contains("part") || lowerTitle.contains("drawing") ||
                                     lowerTitle.contains("sketch") || lowerTitle.contains("blueprint") ||
                                     lowerTitle.contains("patent") || lowerTitle.contains("graph") ||
                                     lowerTitle.contains("chart") || lowerTitle.contains("table") ||
                                     lowerTitle.contains("stats") || lowerTitle.contains("infographic")

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
        } catch (_: Exception) {
            GalleryPageResult(emptyList(), null)
        }
    }

    suspend fun fetchWikipediaSearch(
        query: String,
        limit: Int = 10,
        isHebrew: Boolean = false
    ): List<CarGalleryImage> = withContext(Dispatchers.IO) {
        val host = if (isHebrew) "he.wikipedia.org" else "en.wikipedia.org"
        val encodedQuery = URLEncoder.encode(query, "UTF-8")
        val urlStr = "https://$host/w/api.php?action=query&generator=search&gsrsearch=$encodedQuery&gsrlimit=$limit&prop=pageimages&pithumbsize=800&format=json&origin=*"

        val results = mutableListOf<CarGalleryImage>()
        try {
            val url = URL(urlStr)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 8000
            connection.readTimeout = 8000
            connection.setRequestProperty("User-Agent", USER_AGENT)

            if (connection.responseCode == 200) {
                val jsonText = connection.inputStream.bufferedReader().use { it.readText() }
                val root = JSONObject(jsonText)
                val queryObj = root.optJSONObject("query") ?: return@withContext emptyList()
                val pagesObj = queryObj.optJSONObject("pages") ?: return@withContext emptyList()

                val keys = pagesObj.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val page = pagesObj.getJSONObject(key)
                    val title = page.optString("title", "")
                    val thumbObj = page.optJSONObject("thumbnail")
                    if (thumbObj != null) {
                        val thumbUrl = thumbObj.optString("source")
                        val w = thumbObj.optInt("width", 800)
                        val h = thumbObj.optInt("height", 600)
                        if (thumbUrl.isNotBlank() && !thumbUrl.contains("svg", ignoreCase = true)) {
                            val fullUrl = thumbUrl.replace(Regex("/thumb/"), "/")
                                .replace(Regex("/\\d+px-[^/]+$"), "")
                            val descUrl = "https://$host/wiki/${URLEncoder.encode(title.replace(" ", "_"), "UTF-8")}"
                            results.add(CarGalleryImage(
                                title = title,
                                imageUrl = fullUrl,
                                thumbUrl = thumbUrl,
                                descriptionUrl = descUrl,
                                license = "Creative Commons (Wikipedia)",
                                artist = "Wikipedia Contributor",
                                description = "תמונת ויקיפדיה רשמית של $title",
                                altText = title,
                                width = w,
                                height = h
                            ))
                        }
                    }
                }
            }
        } catch (_: Exception) {
            // Ignore Wikipedia query errors
        }
        results
    }

    suspend fun fetchCommonsCategoryMembers(
        categoryTitle: String,
        limit: Int = 20
    ): List<CarGalleryImage> = withContext(Dispatchers.IO) {
        val encodedCat = URLEncoder.encode(categoryTitle, "UTF-8")
        val urlStr = "https://commons.wikimedia.org/w/api.php?action=query&generator=categorymembers&gcmtitle=$encodedCat&gcmlimit=$limit&prop=imageinfo&iiprop=url|size&iiurlwidth=800&format=json&origin=*"

        val results = mutableListOf<CarGalleryImage>()
        try {
            val url = URL(urlStr)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 8000
            connection.readTimeout = 8000
            connection.setRequestProperty("User-Agent", USER_AGENT)

            if (connection.responseCode == 200) {
                val jsonText = connection.inputStream.bufferedReader().use { it.readText() }
                val root = JSONObject(jsonText)
                val queryObj = root.optJSONObject("query") ?: return@withContext emptyList()
                val pagesObj = queryObj.optJSONObject("pages") ?: return@withContext emptyList()

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
                        .replace(Regex("\\s+"), " ")
                        .trim()

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

                        val checkPath = fullUrl.substringBefore("?")
                        val isValidExtension = checkPath.endsWith(".jpg", ignoreCase = true) ||
                                              checkPath.endsWith(".jpeg", ignoreCase = true) ||
                                              checkPath.endsWith(".png", ignoreCase = true) ||
                                              checkPath.endsWith(".webp", ignoreCase = true)

                        val lowerTitle = title.lowercase()
                        val isJunk = lowerTitle.contains("logo") || lowerTitle.contains("icon") ||
                                     lowerTitle.contains("flag") || lowerTitle.contains("diagram") ||
                                     lowerTitle.contains("interior") || lowerTitle.contains("dashboard")

                        if (thumbUrl.isNotBlank() && isValidExtension && !isJunk) {
                            results.add(CarGalleryImage(
                                title = title,
                                imageUrl = fullUrl,
                                thumbUrl = thumbUrl,
                                descriptionUrl = descUrl,
                                license = "Creative Commons (Wikimedia Commons)",
                                artist = "Wikimedia Commons",
                                description = title,
                                altText = title,
                                width = w,
                                height = h
                            ))
                        }
                    }
                }
            }
        } catch (_: Exception) {
            // Ignore Category members query errors
        }
        results
    }

    fun translateColorToEnglish(colorHeb: String?): String? {
        if (colorHeb.isNullOrBlank()) return null
        val clean = colorHeb.trim()
        return when {
            clean.contains("כחול") -> "blue"
            clean.contains("אדום") -> "red"
            clean.contains("לבן") -> "white"
            clean.contains("שחור") -> "black"
            clean.contains("אפור") -> "grey"
            clean.contains("כסף") || clean.contains("סילבר") -> "silver"
            clean.contains("צהוב") -> "yellow"
            clean.contains("ירוק") -> "green"
            clean.contains("חום") -> "brown"
            clean.contains("כתום") -> "orange"
            clean.contains("זהב") -> "gold"
            clean.contains("בז'") || clean.contains("בז׳") -> "beige"
            clean.contains("טורקיז") -> "turquoise"
            clean.contains("סגול") -> "purple"
            clean.contains("תכלת") -> "light blue"
            clean.contains("ורוד") -> "pink"
            clean.contains("ברונזה") -> "bronze"
            else -> null
        }
    }

    fun scoreImage(
        image: CarGalleryImage,
        make: String,
        model: String,
        year: Int?,
        colorEn: String?
    ): Int {
        var score = 0
        val textToSearch = "${image.title} ${image.description}".lowercase()

        val makeLower = make.lowercase()
        if (makeLower.isNotBlank() && textToSearch.contains(makeLower)) {
            score += 1000
        }

        val modelLower = model.lowercase()
        if (modelLower.isNotBlank() && textToSearch.contains(modelLower)) {
            score += 500
        }

        if (year != null && textToSearch.contains(year.toString())) {
            score += 200
        } else if (year != null && (textToSearch.contains((year - 1).toString()) || textToSearch.contains((year + 1).toString()))) {
            score += 100
        }

        if (!colorEn.isNullOrBlank() && textToSearch.contains(colorEn.lowercase())) {
            score += 300
        }

        return score
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

        val isMachinery = listOf("komatsu", "caterpillar", "cat", "jcb", "bobcat", "deere", "excavator", "tractor", "צמ\"ה", "מחפר").any {
            brand.contains(it, ignoreCase = true) || model.contains(it, ignoreCase = true) || rawMake.contains(it, ignoreCase = true)
        }

        return when {
            isMachinery -> "$brand $model"
            model.isNotBlank() && !model.equals("car", ignoreCase = true) -> "$brand $model car"
            brand.isNotBlank() && !brand.equals("car", ignoreCase = true) -> "$brand car vehicle"
            else -> "automobiles passenger cars vehicle"
        }
    }
}