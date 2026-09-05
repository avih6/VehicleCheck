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
        trimLevel: String? = null,
        category: String? = null,
        limit: Int = 12
    ): List<CarGalleryImage> = withContext(Dispatchers.IO) {
        val colorEn = translateColorToEnglish(colorHeb)
        val (makeEn, modelEn) = VehicleUtils.getEnglishMakeAndModel(make, model, trimLevel, category)
        val brand = if (makeEn != "car") makeEn else make
        val modelClean = if (modelEn != "car") modelEn else model

        val trimLower = trimLevel.orEmpty().lowercase()
        val catLower = category.orEmpty().lowercase()
        val isAmbulance = catLower.contains("אמבולנס") || trimLower.contains("אמבולנס") || modelClean.equals("ambulance", ignoreCase = true) || trimLower.contains("הצלה")
        val isBus = catLower.contains("אוטובוס") || trimLower.contains("אוטובוס") || modelClean.equals("bus", ignoreCase = true) || trimLower.contains("404") || trimLower.contains("405")
        val isPolice = catLower.contains("משטרה") || trimLower.contains("משטרה") || catLower.contains("סיור") || catLower.contains("ביטחון")
        val isIsraeliPreferred = isAmbulance || isBus || isPolice

        val candidatesMap = mutableMapOf<String, CarGalleryImage>()

        // 1. Prepare multi-tier parallel queries with Israeli preference
        val commonsQueries = mutableListOf<String>()

        if (isAmbulance) {
            commonsQueries.add("Magen David Adom ambulance")
            commonsQueries.add("MDA ambulance Israel")
            commonsQueries.add("Israel ambulance $brand")
            commonsQueries.add("$brand ambulance Israel")
            commonsQueries.add("$brand Sprinter ambulance")
            commonsQueries.add("$brand Savana ambulance")
            commonsQueries.add("$brand ambulance")
        }

        if (isBus) {
            commonsQueries.add("Israel bus $brand")
            commonsQueries.add("$brand $modelClean bus")
            commonsQueries.add("$brand bus")
        }

        if (isPolice) {
            commonsQueries.add("Israel Police $brand")
            commonsQueries.add("Israeli police vehicle $brand")
            commonsQueries.add("Israel Police car")
        }

        if (brand.equals("dodge", ignoreCase = true) && (modelClean.contains("500") || model.contains("די 500") || model.contains("500"))) {
            commonsQueries.add("Dodge D series truck")
            commonsQueries.add("Dodge D500 truck")
            commonsQueries.add("Dodge D500")
            commonsQueries.add("Dodge truck 1969")
        }

        if (!colorEn.isNullOrBlank() && year != null && brand.isNotBlank() && modelClean.isNotBlank()) {
            commonsQueries.add("$brand $modelClean $year $colorEn car")
        }
        if (!colorEn.isNullOrBlank() && brand.isNotBlank() && modelClean.isNotBlank()) {
            commonsQueries.add("$brand $modelClean $colorEn car")
        }
        if (year != null && brand.isNotBlank() && modelClean.isNotBlank()) {
            commonsQueries.add("$brand $modelClean $year")
            commonsQueries.add("$brand $modelClean $year car")
        }
        if (brand.isNotBlank() && modelClean.isNotBlank()) {
            commonsQueries.add("$brand $modelClean")
            commonsQueries.add("$brand $modelClean car")
        }
        if (brand.isNotBlank()) {
            commonsQueries.add("$brand automobile")
            commonsQueries.add(brand)
        }

        val wikiQueries = listOfNotNull(
            if (isAmbulance) "Magen David Adom" else null,
            if (isBus && brand.isNotBlank()) "$brand bus" else null,
            if (brand.isNotBlank() && modelClean.isNotBlank()) "$brand $modelClean" else null,
            if (brand.isNotBlank()) brand else null
        ).distinct()

        val categoryQueries = mutableListOf<String>()
        if (isAmbulance) {
            categoryQueries.add("Category:Ambulances_in_Israel")
            categoryQueries.add("Category:Magen_David_Adom_vehicles")
        }
        if (isBus && brand.isNotBlank()) {
            categoryQueries.add("Category:${brand.replace(" ", "_")}_buses")
        }
        if (isPolice) {
            categoryQueries.add("Category:Police_vehicles_in_Israel")
        }
        if (brand.isNotBlank() && modelClean.isNotBlank()) {
            categoryQueries.add("Category:${brand.replace(" ", "_")}_${modelClean.replace(" ", "_")}")
        }
        if (brand.isNotBlank()) {
            categoryQueries.add("Category:${brand.replace(" ", "_")}")
        }

        val parallelResults = coroutineScope {
            val d1 = commonsQueries.distinct().map { q -> async { fetchCommonsSearch(q, limit = 20).images } }
            val d2 = wikiQueries.map { q -> async { fetchWikipediaSearch(q, limit = 8, isHebrew = false) } }
            val d3 = wikiQueries.map { q -> async { fetchWikipediaSearch(q, limit = 6, isHebrew = true) } }
            val d4 = categoryQueries.distinct().map { cat -> async { fetchCommonsCategoryMembers(cat, limit = 15) } }
            (d1 + d2 + d3 + d4).awaitAll()
        }

        for (list in parallelResults) {
            for (img in list) {
                if (!candidatesMap.containsKey(img.imageUrl) && !candidatesMap.containsKey(img.thumbUrl)) {
                    candidatesMap[img.imageUrl] = img
                }
            }
        }

        // If there is no specific model name or brand, don't show random unrelated models
        if ((modelClean.isBlank() || modelClean.equals("car", ignoreCase = true)) && !isIsraeliPreferred) {
            return@withContext emptyList()
        }

        // Score and sort candidates
        val scored = candidatesMap.values.map { img ->
            val score = scoreImage(img, brand, modelClean, year, colorEn, isIsraeliPreferred)
            img to score
        }.filter {
            it.second >= (if (isIsraeliPreferred) 800 else 1200) // Lower threshold for verified special Israeli vehicles
        }.sortedByDescending { it.second }

        scored.map { it.first }.take(limit)
    }

    fun cleanImageTitle(raw: String): String {
        var t = raw
            .replace("File:", "", ignoreCase = true)
            .replace("Image:", "", ignoreCase = true)
            .replace(".jpg", "", ignoreCase = true)
            .replace(".jpeg", "", ignoreCase = true)
            .replace(".png", "", ignoreCase = true)
            .replace(".webp", "", ignoreCase = true)
            .replace("_", " ")
            .trim()

        // Strip camera prefixes (IMG, DSC, SAM, etc.)
        t = t.replace(Regex("^(IMG|DSC|SAM|P|DJI)_?\\d+[-_ ]*", RegexOption.IGNORE_CASE), "")
        // Strip camera date tags or bracketed crop notes
        t = t.replace(Regex("\\(\\s*\\d{4}-\\d{2}-\\d{2}\\s*\\)"), "")
        t = t.replace(Regex("\\((cropped|resized|edited|front|rear|side|interior|profile)[^)]*\\)", RegexOption.IGNORE_CASE), "")
        // Strip trailing location or exhibition notes
        t = t.replace(Regex("\\b(taken at|photographed at|spotted at|exhibited at|on display at|in Tokyo|in London)\\b.*$", RegexOption.IGNORE_CASE), "")
        t = t.replace(Regex("\\s+"), " ").trim()

        // Cap title length at word boundary
        if (t.length > 55) {
            val cut = t.take(52)
            val lastSpace = cut.lastIndexOf(' ')
            t = if (lastSpace > 25) cut.substring(0, lastSpace) + "..." else cut + "..."
        }
        return t
    }

    fun cleanImageDescription(raw: String): String {
        if (raw.isBlank()) return ""
        var d = raw.replace(Regex("<[^>]*>"), " ")
        // Remove wiki template syntax like {{en|...}}
        d = d.replace(Regex("\\{\\{[a-zA-Z0-9_|-]+"), " ").replace("}}", " ")
        d = d.replace("\n", " ").replace("\r", " ").replace(Regex("\\s+"), " ").trim()
        if (d.length > 180) {
            val cut = d.take(175)
            val lastSpace = cut.lastIndexOf(' ')
            d = if (lastSpace > 80) cut.substring(0, lastSpace) + "..." else cut + "..."
        }
        return d
    }

    private fun isJunkOrNonVehicle(title: String, description: String = "", artist: String = ""): Boolean {
        val t = title.lowercase()
        val d = description.lowercase()
        val a = artist.lowercase()
        val combined = "$t $d $a"

        val blockedKeywords = listOf(
            // Diagrams, graphics, non-photo assets
            "logo", "icon", "flag", "diagram", "map", "badge", "emblem", "symbol",
            "drawing", "sketch", "blueprint", "patent", "graph", "chart", "table", "stats", "infographic",
            // Real estate, buildings, headquarters, factories, dealerships, showrooms
            "building", "headquarters", "factory", "dealership", "dealer", "showroom", "warehouse",
            "station", "terminal", "facility", "head office", "tower", "museum", "cemetery", "graveyard",
            "monument", "memorial", "statue", "architecture", "bridge", "house",
            // Toys, scale models, miniatures, merchandise
            "diecast", "die-cast", "scale model", "scale-model", "miniature", "hot wheels", "hotwheels",
            "matchbox", "lego", "tomica", "tomy", "toy", "model car", "1:18", "1:24", "1:43", "1:64", "1/18", "1/24", "1/43", "1/64",
            "slot car", "rc car", "radio control",
            // Geography, cityscapes, aerials, nature
            "skyline", "panorama", "aerial view", "aerial photo", "satellite view", "satellite image", "landscape", "scenery",
            // Non-car transport
            "railway", "railroad", "train", "locomotive", "tram", "streetcar", "subway", "metro", "monorail",
            "boat", "ship", "ferry", "vessel", "yacht", "harbor", "port", "barge",
            // Awards, medals, stamps, currency
            "medal", "coin", "banknote", "currency", "postage stamp", "stamp", "trophy", "award",
            // Vehicle parts, engine bay, interior buttons, component closeups
            "engine", "motor", "fb20", "v6", "v8", "inline-4", "tsi", "tdi", "turbocharger", "supercharger",
            "switch", "button", "vdc off", "esp off", "traction control", "gauge", "gauges", "speedometer",
            "tachometer", "odometer", "dashboard", "interior", "steering wheel", "seats", "seat", "pedal", "pedals",
            "gearbox", "transmission", "gear shifter", "shifter", "knob", "center console", "console", "exhaust",
            "muffler", "tailpipe", "headlight", "taillight", "fog light", "indicator", "lamp", "mirror",
            "side mirror", "door handle", "fuel door", "fuel cap", "gas cap", "rim", "wheel rim", "tire", "tyre",
            "alloy wheel", "suspension", "brake", "caliper", "rotor", "radiator", "battery", "intake", "manifold",
            "airbox", "air filter", "oil filter", "fuse box", "fuse", "relay", "wiring", "harness", "chassis",
            "underbody", "undercarriage", "trunk", "boot", "glovebox", "sunroof", "wiper", "wipers", "vin plate",
            "identification plate", "plate number", "door panel", "detail", "close-up", "closeup", "crop",
            // Space, astronomy, aviation, naval, military hardware
            "satellite", "observatory", "spacecraft", "space probe", "telescope", "orbiting", "oao-", "oao", "astronomy", "celestial",
            "nasa", "rocket", "missile", "fighter jet", "aircraft", "airplane", "aerospace", "submarine", "warship", "space",
            // Text advertisements, scanned pages, newspaper clippings, brochures, documents
            "advertisement", "advert", "ad", "newspaper", "clipping", "poster", "brochure", "flyer", "leaflet",
            "pamphlet", "receipt", "invoice", "press release", "article", "page", "text", "document", "scanned",
            "letterhead", "magazine", "catalog", "catalogue", "price list", "tariff", "vintage ad", "amsterdam",
            // People, portraits, entertainment, politics
            "portrait", "singer", "actor", "actress", "politician", "minister", "prime minister", "president",
            "founder", "executive", "ceo", "director", "chairperson", "driver", "racer", "biography", "obituary",
            "knesset", "rabbi", "general", "officer", "soldier", "army", "military base",
            "benayoun", "dor daniel", "habibi", "hakol over habibi", "bennett", "netanyahu", "gaza", "genocide", "war",
            "attack", "october 7", "terror", "conflict", "protest", "memorial", "cemetery", "grave",
            "concert", "album", "cover", "band", "music", "song", "group", "person", "man", "woman", "people",
            "headshot", "selfie", "bundesarchiv bild", "israeli singer", "portrait of",
            "pikiwiki", "piki_wiki", "leonard cohen", "cohen", "performance", "recital", "historical photo", "troops", "soldiers"
        )

        return blockedKeywords.any { combined.contains(it) }
    }

    suspend fun fetchGalleryPage(
        rawMake: String,
        rawModel: String = "",
        offset: Int = 0,
        limit: Int = 40
    ): GalleryPageResult = withContext(Dispatchers.IO) {
        val cleanMake = if (rawMake == "הכל" || rawMake.equals("all", ignoreCase = true) || rawMake.contains("כל הרכבים")) "" else rawMake.trim()
        val cleanModel = if (rawModel == "כל הדגמים" || rawModel.equals("all", ignoreCase = true)) "" else rawModel.trim()

        if (cleanMake.isBlank() && cleanModel.isBlank()) {
            val showcaseQueries = listOf(
                "Toyota Corolla",
                "Hyundai Ioniq 5",
                "Tesla Model 3",
                "Mercedes-Benz",
                "BMW",
                "Porsche 911",
                "Audi",
                "Volkswagen Golf",
                "Kia Sportage",
                "BYD Atto 3"
            )
            val parallelImages = coroutineScope {
                showcaseQueries.map { q ->
                    async { fetchCommonsSearch("$q car", offset = 0, limit = 5).images }
                }.awaitAll().flatten().distinctBy { it.imageUrl }.shuffled()
            }
            if (parallelImages.isNotEmpty()) {
                return@withContext GalleryPageResult(parallelImages, null)
            }
            return@withContext fetchCommonsSearch("automobiles modern passenger cars", offset, limit)
        }

        val query = buildSearchQuery(cleanMake, cleanModel)
        val commonsResult = fetchCommonsSearch(query, offset, limit)

        // For initial load (offset == 0), augment with Wikipedia & Category images for maximum richness
        if (offset == 0) {
            val (makeEn, modelEn) = VehicleUtils.getEnglishMakeAndModel(cleanMake, cleanModel)
            val brand = if (makeEn != "car") makeEn else cleanMake
            val model = if (modelEn != "car") modelEn else cleanModel

            val extraImages = coroutineScope {
                val dWikiEn = async {
                    if (brand.isNotBlank() && model.isNotBlank()) fetchWikipediaSearch("$brand $model", limit = 10, isHebrew = false)
                    else if (brand.isNotBlank()) fetchWikipediaSearch(brand, limit = 10, isHebrew = false)
                    else emptyList()
                }
                val dWikiHe = async {
                    if (cleanMake.isNotBlank()) fetchWikipediaSearch("$cleanMake $cleanModel".trim(), limit = 6, isHebrew = true)
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
        val lightExclusions = " -logo -icon -diagram -flag -symbol -badge -map -drawing -blueprint -singer -portrait -politician -satellite -observatory -space -spacecraft -telescope -rocket -missile -aircraft -ship -train -helicopter"
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
                    val title = cleanImageTitle(rawTitle)

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
                        val cleanDesc = cleanImageDescription(rawDesc)
                        val altText = if (cleanDesc.isNotBlank() && cleanDesc.length > 5) cleanDesc else if (title.isNotBlank()) "תמונת רכב: $title" else "תמונת רכב ממאגר ויקימדיה"

                        val checkPath = fullUrl.substringBefore("?")
                        val isValidExtension = checkPath.endsWith(".jpg", ignoreCase = true) ||
                                              checkPath.endsWith(".jpeg", ignoreCase = true) ||
                                              checkPath.endsWith(".png", ignoreCase = true) ||
                                              checkPath.endsWith(".webp", ignoreCase = true)

                        val isJunk = isJunkOrNonVehicle(title, cleanDesc, cleanArtist)

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
        val qTrimmed = query.trim()
        if (qTrimmed.isBlank() || qTrimmed == "הכל" || qTrimmed.equals("all", ignoreCase = true)) {
            return@withContext emptyList()
        }

        val host = if (isHebrew) "he.wikipedia.org" else "en.wikipedia.org"
        val encodedQuery = URLEncoder.encode(qTrimmed, "UTF-8")
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
                    val rawTitle = page.optString("title", "")
                    val title = cleanImageTitle(rawTitle)
                    val thumbObj = page.optJSONObject("thumbnail")
                    if (thumbObj != null) {
                        val thumbUrl = thumbObj.optString("source")
                        val w = thumbObj.optInt("width", 800)
                        val h = thumbObj.optInt("height", 600)
                        val isJunk = isJunkOrNonVehicle(title, "")
                        if (thumbUrl.isNotBlank() && !thumbUrl.contains("svg", ignoreCase = true) && !isJunk) {
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
        val urlStr = "https://commons.wikimedia.org/w/api.php?action=query&generator=categorymembers&gcmtitle=$encodedCat&gcmtype=file&gcmlimit=$limit&prop=imageinfo&iiprop=url|size|extmetadata&iiurlwidth=800&format=json&origin=*"

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
                    val title = cleanImageTitle(rawTitle)

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

                        val isJunk = isJunkOrNonVehicle(title, "")

                        if (thumbUrl.isNotBlank() && isValidExtension && !isJunk) {
                            results.add(CarGalleryImage(
                                title = title,
                                imageUrl = fullUrl,
                                thumbUrl = thumbUrl,
                                descriptionUrl = descUrl,
                                license = "Creative Commons (Wikimedia Commons)",
                                artist = "",
                                description = title,
                                altText = "תמונת רכב: $title",
                                width = w,
                                height = h
                            ))
                        }
                    }
                }
            }
        } catch (_: Exception) {
            // Ignore category error
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
        colorEn: String?,
        isIsraeliPreferred: Boolean = false
    ): Int {
        if (isJunkOrNonVehicle(image.title, image.description, image.artist)) {
            return -100000
        }

        var score = 0
        val textToSearch = "${image.title} ${image.description}".lowercase()

        // Israeli livery and organization prioritization (MDA, Egged, Dan, Israel Police, etc.)
        val isIsraeliImage = listOf("israel", "israeli", "mda", "magen david adom", "magen david", "egged", "dan bus", "police of israel", "israel police", "מד\"א", "מדא", "אגד", "דן", "משטרת ישראל", "ישראל").any {
            textToSearch.contains(it)
        }

        if (isIsraeliImage) {
            score += if (isIsraeliPreferred) 1500 else 300
        }

        val makeLower = make.lowercase()
        if (makeLower.isNotBlank() && textToSearch.contains(makeLower)) {
            score += 1000
        }

        val modelLower = model.lowercase().trim()
        val hasSpecificModel = modelLower.isNotBlank() && !modelLower.equals("car", ignoreCase = true)
        if (hasSpecificModel) {
            if (textToSearch.contains(modelLower)) {
                score += 1500 // Strongly prioritize matching the exact car model
            } else {
                score -= 800 // Penalize images from the same brand that don't match the model
            }
        }

        if (year != null) {
            val yearRegex = Regex("\\b(19\\d\\d|20\\d\\d)\\b")
            val foundYears = yearRegex.findAll(textToSearch).mapNotNull { it.value.toIntOrNull() }.toList()
            if (foundYears.isNotEmpty() && foundYears.any { Math.abs(it - year) > 10 }) {
                return -100000
            }
            if (textToSearch.contains(year.toString())) {
                score += 400
            } else if (textToSearch.contains((year - 1).toString()) || textToSearch.contains((year + 1).toString())) {
                score += 250
            } else if (textToSearch.contains((year - 2).toString()) || textToSearch.contains((year + 2).toString())) {
                score += 150
            } else {
                // If the image explicitly specifies a distant year (e.g. 2005 vs 2012), apply small penalty
                if (foundYears.isNotEmpty() && foundYears.none { Math.abs(it - year) <= 3 }) {
                    score -= 200
                }
            }
        }

        if (!colorEn.isNullOrBlank() && textToSearch.contains(colorEn.lowercase())) {
            score += 300
        }

        // Bonus for full car exterior terms and landscape photos
        if (listOf("front", "rear", "side", "profile", "exterior", "automobile", "crossover", "suv", "sedan", "hatchback", "wagon").any { textToSearch.contains(it) }) {
            score += 300
        }
        if (image.width > 0 && image.height > 0 && image.width > image.height) {
            score += 150
        }

        return score
    }

    private fun buildSearchQuery(rawMake: String, rawModel: String): String {
        val trimmedMake = rawMake.trim()
        val trimmedModel = rawModel.trim()

        if (trimmedMake.isBlank() || trimmedMake == "הכל" || trimmedMake.equals("all", ignoreCase = true)) {
            return if (trimmedModel.isNotBlank()) {
                "$trimmedModel car vehicle -person -portrait"
            } else {
                "automobiles modern passenger cars incategory:Automobiles"
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