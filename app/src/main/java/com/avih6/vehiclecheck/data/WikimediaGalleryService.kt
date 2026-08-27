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

        val queries = mutableListOf<String>()
        // 1. Most specific: Make + Model + Year + Color
        if (!colorEn.isNullOrBlank() && year != null) {
            queries.add("$brand $modelClean $year $colorEn")
        }
        // 2. Make + Model + Color
        if (!colorEn.isNullOrBlank()) {
            queries.add("$brand $modelClean $colorEn")
        }
        // 3. Make + Model + Year
        if (year != null) {
            queries.add("$brand $modelClean $year")
        }
        // 4. Make + Model
        if (brand.isNotBlank() && modelClean.isNotBlank()) {
            queries.add("$brand $modelClean")
        }

        val candidatesMap = mutableMapOf<String, CarGalleryImage>()
        for (q in queries) {
            val page = fetchGalleryPageByRawQuery(q, limit = 24)
            for (img in page.images) {
                if (!candidatesMap.containsKey(img.imageUrl)) {
                    candidatesMap[img.imageUrl] = img
                }
            }
            if (candidatesMap.size >= 40) break
        }

        // Fallback to brand if we got absolutely nothing
        if (candidatesMap.isEmpty() && brand.isNotBlank()) {
            val page = fetchGalleryPageByRawQuery(brand, limit = 20)
            for (img in page.images) {
                if (!candidatesMap.containsKey(img.imageUrl)) {
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
    ): GalleryPageResult {
        val query = buildSearchQuery(rawMake, rawModel)
        return fetchGalleryPageByRawQuery(query, offset, limit)
    }

    suspend fun fetchGalleryPageByRawQuery(
        rawQuery: String,
        offset: Int = 0,
        limit: Int = 40
    ): GalleryPageResult = withContext(Dispatchers.IO) {
        val baseExclusions = " -logo -icon -badge -flag -diagram -map -emblem -symbol -drawing -blueprint -sketch -dashboard -interior -seats -engine -steering -graph -chart -table -plot -ranking -stats -curve -infographic -factory -plant -dealership -showroom -workshop -garage -office -building -facade -advertisement -ad -poster -exhibit -fair -production -assembly"
        val fullQuery = "$rawQuery$baseExclusions"
        val encodedQuery = URLEncoder.encode(fullQuery, "UTF-8")
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
                                     lowerTitle.contains("underneath") || lowerTitle.contains("chassis") ||
                                     lowerTitle.contains("part") || lowerTitle.contains("drawing") ||
                                     lowerTitle.contains("sketch") || lowerTitle.contains("blueprint") ||
                                     lowerTitle.contains("patent") || lowerTitle.contains("graph") ||
                                     lowerTitle.contains("chart") || lowerTitle.contains("table") ||
                                     lowerTitle.contains("plot") || lowerTitle.contains("rank") ||
                                     lowerTitle.contains("stats") || lowerTitle.contains("curve") ||
                                     lowerTitle.contains("infographic") || lowerTitle.contains("factory") ||
                                     lowerTitle.contains("plant") || lowerTitle.contains("dealership") ||
                                     lowerTitle.contains("showroom") || lowerTitle.contains("workshop") ||
                                     lowerTitle.contains("garage") || lowerTitle.contains("building") ||
                                     lowerTitle.contains("facade") || lowerTitle.contains("office") ||
                                     lowerTitle.contains("advertisement") || lowerTitle.contains("poster") ||
                                     lowerTitle.contains("exhibit") || lowerTitle.contains("fair") ||
                                     lowerTitle.contains("assembly line") || lowerTitle.contains("assembly-line") ||
                                     lowerTitle.contains("production line")

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
        
        val baseExclusions = " -logo -icon -badge -flag -diagram -map -emblem -symbol -drawing -blueprint -sketch -dashboard -interior -seats -engine -steering -graph -chart -table -plot -ranking -stats -curve -infographic -factory -plant -dealership -showroom -workshop -garage -office -building -facade -advertisement -ad -poster -exhibit -fair -production -assembly"
        val ageExclusions = " -vintage -antique -classic -pre-war -museum -bundesarchiv -historical -history -historic"

        if (trimmedMake.isBlank() || trimmedMake == "הכל" || trimmedMake.equals("all", ignoreCase = true)) {
            // For general gallery showcase, keep it modern by applying age exclusions
            val fullExclusions = baseExclusions + ageExclusions
            return if (trimmedModel.isNotBlank()) {
                "$trimmedModel car vehicle$fullExclusions"
            } else {
                "automobiles passenger cars vehicle modern$fullExclusions"
            }
        }

        // For specific vehicle make searches, do not apply ageExclusions to avoid filtering classic/old vehicles
        val exclusions = baseExclusions
        val (makeEn, modelEn) = VehicleUtils.getEnglishMakeAndModel(trimmedMake, trimmedModel)
        val brand = if (makeEn != "car") makeEn else trimmedMake
        val model = if (modelEn != "car") modelEn else trimmedModel

        val isMachinery = listOf("komatsu", "caterpillar", "cat", "jcb", "bobcat", "deere", "excavator", "tractor", "צמ\"ה", "מחפר").any {
            brand.contains(it, ignoreCase = true) || model.contains(it, ignoreCase = true) || rawMake.contains(it, ignoreCase = true)
        }

        return when {
            isMachinery -> "$brand $model$exclusions"
            model.isNotBlank() && !model.equals("car", ignoreCase = true) -> "$brand $model car$exclusions"
            brand.isNotBlank() && !brand.equals("car", ignoreCase = true) -> "$brand car vehicle$exclusions"
            else -> "automobiles passenger cars vehicle$exclusions"
        }
    }
}