package com.avih6.vehiclecheck.data

data class DtcCodeInfo(
    val code: String,
    val titleHe: String,
    val titleEn: String,
    val categoryHe: String,
    val severity: DtcSeverity,
    val descriptionHe: String,
    val symptomsHe: List<String>,
    val possibleCausesHe: List<String>,
    val solutionsHe: List<String>
)

enum class DtcSeverity(val titleHe: String, val colorHex: Long) {
    LOW("נמוכה - ניתן להמשיך בנסיעה בזהירות", 0xFF4CAF50),
    MEDIUM("בינונית - מומלץ לגשת למוסך בהקדם", 0xFFFFA000),
    HIGH("גבוהה - סכנה לנזק, מומלץ לא לאמץ את הרכב", 0xFFE64A19),
    CRITICAL("קריטית - עצור בצד וכבה מנוע מיד", 0xFFD32F2F)
}

object DtcRepository {
    private val dtcMap = mutableMapOf<String, DtcCodeInfo>()

    init {
        registerKnownCodes()
    }

    fun searchCodes(query: String): List<DtcCodeInfo> {
        val q = query.trim().lowercase()
        if (q.isBlank()) return dtcMap.values.take(8).toList()

        return dtcMap.values.filter { item ->
            item.code.lowercase().contains(q) ||
            item.titleHe.lowercase().contains(q) ||
            item.titleEn.lowercase().contains(q) ||
            item.categoryHe.lowercase().contains(q) ||
            item.descriptionHe.lowercase().contains(q)
        }.sortedByDescending { 
            if (it.code.lowercase().startsWith(q)) 3
            else if (it.code.lowercase().contains(q)) 2
            else 1
        }.take(12)
    }

    fun lookupCode(rawCode: String): DtcCodeInfo {
        val clean = rawCode.trim().uppercase().replace(" ", "")
        val exact = dtcMap[clean]
        if (exact != null) return exact

        // Dynamic fallback parser for any valid OBD2 code format: [PCBU][0-9]{4}
        val prefix = clean.take(1)
        val system = when (prefix) {
            "P" -> "מערכת הינע ומנוע (Powertrain)"
            "C" -> "שלדה, בלמים והיגוי (Chassis / ABS)"
            "B" -> "מרכב, נוחות ובטיחות (Body / Airbags)"
            "U" -> "רשת מחשבי הרכב ותקשורת (CAN-Bus / Network)"
            else -> "מערכת כללית"
        }

        val subType = if (clean.length >= 2) {
            when (clean[1]) {
                '0' -> "קוד תקלה אוניברסלי בתקן SAE / ISO"
                '1', '2', '3' -> "קוד תקלה ייעודי של יצרן הרכב (Manufacturer Specific)"
                else -> "קוד תקלה OBD-II"
            }
        } else "קוד OBD-II"

        return DtcCodeInfo(
            code = clean,
            titleHe = "קוד תקלה $clean - $subType",
            titleEn = "Diagnostic Trouble Code $clean",
            categoryHe = system,
            severity = if (prefix == "P" || prefix == "C") DtcSeverity.MEDIUM else DtcSeverity.LOW,
            descriptionHe = "זוהתה תקלה במערכת $system ($subType). מומלץ לבצע דיאגנוסטיקה מעמיקה באמצעות סורק ייעודי או לגשת למוסך מורשה.",
            symptomsHe = listOf(
                "נורת 'בדוק מנוע' (Check Engine) או נורת אזהרה דולקת בלוח המחוונים",
                "ייתכן שינוי בהתנהגות המערכת הרלוונטית",
                "קריאת קוד תקלה בזיכרון מחשב הרכב (ECU/OBD)"
            ),
            possibleCausesHe = listOf(
                "קריאת חיישן חריגה או חיישן פגום",
                "נתק, קצר או מגע רופף בחיווט החשמלי",
                "בלאי באחד מרכיבי המערכת",
                "תקלת תוכנה או צורך באיפוס מחשב"
            ),
            solutionsHe = listOf(
                "בדיקת חיבורים חשמליים וחיווט סביב הרכיב",
                "ניקוי או החלפת הרכיב הפגום במידת הצורך",
                "איפוס קוד התקלה ובדיקה חוזרת האם הקוד חוזר"
            )
        )
    }

    private fun register(code: String, titleHe: String, titleEn: String, categoryHe: String, severity: DtcSeverity, desc: String, symptoms: List<String>, causes: List<String>, solutions: List<String>) {
        dtcMap[code.uppercase()] = DtcCodeInfo(
            code = code.uppercase(),
            titleHe = titleHe,
            titleEn = titleEn,
            categoryHe = categoryHe,
            severity = severity,
            descriptionHe = desc,
            symptomsHe = symptoms,
            possibleCausesHe = causes,
            solutionsHe = solutions
        )
    }

    private fun registerKnownCodes() {
        // P0300 - Random/Multiple Cylinder Misfire
        register(
            code = "P0300",
            titleHe = "פספוסי בעירה (Misfire) במספר צילינדרים אקראיים",
            titleEn = "Random/Multiple Cylinder Misfire Detected",
            categoryHe = "מערכת הצתה והזרקת דלק",
            severity = DtcSeverity.HIGH,
            desc = "מחשב המנוע (ECU) זיהה כי צילינדר אחד או יותר אינם מייצרים בעירה תקינה. מצב זה גורם לעומס על המנוע ועלול לפגוע בממיר הקטליטי.",
            symptoms = listOf("רעידות חזקות של המנוע (בסרק או בהאצה)", "נורת Check Engine מהבהבת או דולקת קבוע", "ירידה משמעותית בסחיבה ובכוח המנוע", "צריכת דלק מוגברת וריח דלק מהאגזוז"),
            causes = listOf("מצתים (פלאגים) בלויים או פגומים", "כוהל הצתה (Ignition Coil) תקול", "מזרקי דלק סתומים או דולפים", "דליפת ואקום בסעפת היניקה", "לחץ דלק נמוך (משאבת דלק/פילטר)"),
            solutions = listOf("בדיקה והחלפת מצתים (פלאגים)", "בדיקת תקינות סלילי ההצתה (כוהלים)", "בדיקת דליפות ואקום בצנרת היניקה", "ניקוי או החלפת מזרקי דלק")
        )

        // P0301 - Cylinder 1 Misfire
        register(
            code = "P0301",
            titleHe = "פספוס בעירה (Misfire) בצילינדר 1",
            titleEn = "Cylinder 1 Misfire Detected",
            categoryHe = "מערכת הצתה והזרקת דלק",
            severity = DtcSeverity.HIGH,
            desc = "מחשב המנוע זיהה חוסר בעירה או בעירה לקויה בצילינדר מספר 1.",
            symptoms = listOf("רעידות מנוע מורגשות", "חוסר כוח בעלייה או בהאצה", "נורת בדוק מנוע דולקת"),
            causes = listOf("פלאג פגום בצילינדר 1", "סליל הצתה (כוהל) תקול בצילינדר 1", "מזרק דלק 1 סתום", "דחיסה (קומפרסיה) נמוכה בצילינדר 1"),
            solutions = listOf("החלפת המצת בצילינדר 1", "החלפת כוהל הצתה של צילינדר 1", "בדיקת לחץ דחיסה במנוע")
        )

        // P0302
        register(
            code = "P0302",
            titleHe = "פספוס בעירה (Misfire) בצילינדר 2",
            titleEn = "Cylinder 2 Misfire Detected",
            categoryHe = "מערכת הצתה והזרקת דלק",
            severity = DtcSeverity.HIGH,
            desc = "מחשב המנוע זיהה חוסר בעירה בצילינדר מספר 2.",
            symptoms = listOf("רעידות במנוע", "חוסר סחיבה", "נורת מנוע דולקת"),
            causes = listOf("פלאג או כוהל הצתה בצילינדר 2", "מזרק דלק 2", "בעיית דחיסה"),
            solutions = listOf("החלפת פלאג וכוהל בצילינדר 2", "בדיקת מזרק דלק")
        )

        // P0303
        register(
            code = "P0303",
            titleHe = "פספוס בעירה (Misfire) בצילינדר 3",
            titleEn = "Cylinder 3 Misfire Detected",
            categoryHe = "מערכת הצתה והזרקת דלק",
            severity = DtcSeverity.HIGH,
            desc = "מחשב המנוע זיהה חוסר בעירה בצילינדר מספר 3.",
            symptoms = listOf("רעידות במנוע", "נורת מנוע דולקת"),
            causes = listOf("פלאג/כוהל בצילינדר 3", "מזרק דלק", "קומפרסיה"),
            solutions = listOf("החלפת פלאג וכוהל בצילינדר 3")
        )

        // P0304
        register(
            code = "P0304",
            titleHe = "פספוס בעירה (Misfire) בצילינדר 4",
            titleEn = "Cylinder 4 Misfire Detected",
            categoryHe = "מערכת הצתה והזרקת דלק",
            severity = DtcSeverity.HIGH,
            desc = "מחשב המנוע זיהה חוסר בעירה בצילינדר מספר 4.",
            symptoms = listOf("רעידות במנוע", "חוסר סחיבה"),
            causes = listOf("פלאג/כוהל בצילינדר 4", "מזרק דלק"),
            solutions = listOf("החלפת פלאג וכוהל בצילינדר 4")
        )

        // P0420 - Catalyst System Efficiency Below Threshold
        register(
            code = "P0420",
            titleHe = "יעילות ממיר קטליטי נמוכה מהסף (בנק 1)",
            titleEn = "Catalyst System Efficiency Below Threshold (Bank 1)",
            categoryHe = "מערכת פליטה ובקרת זיהום",
            severity = DtcSeverity.MEDIUM,
            desc = "הממיר הקטליטי אינו מפרק את גזי הפליטה המזהמים ביעילות הנדרשת, כפי שנמדד על ידי חיישני החמצן לפני ואחרי הממיר.",
            symptoms = listOf("נורת בדוק מנוע דולקת קבוע", "עלייה מתונה בצריכת הדלק", "ריח גופרית או גזים חריף מהאגזוז", "כישלון בבדיקת זיהום אוויר בטסט"),
            causes = listOf("ממיר קטליטי בלוי, שרוף או סתום", "חיישן חמצן אחורי (Downstream O2 Sensor) פגום", "דליפת גזים בצנרת הפליטה לפני החיישן", "בעיות בעירה קודמות שהציפו את הממיר בדלק"),
            solutions = listOf("בדיקת תקינות חיישן חמצן אחורי", "בדיקת דליפות בצנרת הפליטה", "החלפת ממיר קטליטי במידת הצורך")
        )

        // P0430
        register(
            code = "P0430",
            titleHe = "יעילות ממיר קטליטי נמוכה מהסף (בנק 2)",
            titleEn = "Catalyst System Efficiency Below Threshold (Bank 2)",
            categoryHe = "מערכת פליטה ובקרת זיהום",
            severity = DtcSeverity.MEDIUM,
            desc = "הממיר הקטליטי של בנק 2 (במנועי V6/V8) אינו פועל ביעילות מספקת.",
            symptoms = listOf("נורת מנוע דולקת", "כישלון בטסט זיהום אוויר"),
            causes = listOf("ממיר קטליטי בנק 2 פגום", "חיישן חמצן בנק 2"),
            solutions = listOf("החלפת ממיר קטליטי או חיישן חמצן")
        )

        // P0171 - System Too Lean (Bank 1)
        register(
            code = "P0171",
            titleHe = "תערובת דלק ענייה מדי (יותר מדי אוויר / מעט דלק - בנק 1)",
            titleEn = "System Too Lean (Bank 1)",
            categoryHe = "מערכת הזרקת דלק ואוויר",
            severity = DtcSeverity.MEDIUM,
            desc = "מחשב הרכב מזהה שיש כמות גדולה מדי של חמצן ביחס לדלק בתאי הבעירה.",
            symptoms = listOf("גמגום או חוסר תגובה בלחיצה על הגז", "סל\"ד סרק לא יציב (קופץ או נמוך)", "נורת מנוע דולקת", "קשיי התנעה כשהמנוע קר"),
            causes = listOf("דליפת ואקום (צינור גומי סדוק, אטם סעפת יניקה פגום)", "חיישן כמות אוויר (MAF) מלוכלך או תקול", "משאבת דלק חלשה או פילטר דלק סתום", "מזרקי דלק סתומים"),
            solutions = listOf("בדיקת דליפות עשן/ואקום בסעפת היניקה", "ניקוי חיישן MAF עם ספריי ייעודי", "בדיקת לחץ משאבת דלק")
        )

        // P0172 - System Too Rich
        register(
            code = "P0172",
            titleHe = "תערובת דלק עשירה מדי (יותר מדי דלק / חוסר אוויר - בנק 1)",
            titleEn = "System Too Rich (Bank 1)",
            categoryHe = "מערכת הזרקת דלק ואוויר",
            severity = DtcSeverity.MEDIUM,
            desc = "מחשב המנוע מזהה שיותר מדי דלק מוזרק ביחס לאוויר הנכנס.",
            symptoms = listOf("עשן שחור מהאגזוז", "צריכת דלק גבוהה מאוד", "ריח חזק של דלק לא שרוף"),
            causes = listOf("מזרק דלק דולף/תקוע במצב פתוח", "ווסת לחץ דלק תקול", "חיישן MAF תקול", "חיישן טמפרטורת נוזל קירור (ECT) מזייף"),
            solutions = listOf("בדיקת מזרקי דלק", "בדיקת חיישן חמצן ו-MAF", "בדיקת לחץ דלק")
        )

        // P0101 - MAF Sensor Circuit Range/Performance
        register(
            code = "P0101",
            titleHe = "חיישן כמות אוויר (MAF) - קריאה מחוץ לטווח התקין",
            titleEn = "Mass or Volume Air Flow Circuit Range/Performance",
            categoryHe = "חיישני כניסת אוויר",
            severity = DtcSeverity.MEDIUM,
            desc = "חיישן ה-MAF מודד את נפח ומסת האוויר הנכנס למנוע. התקבל ערך שאינו הגיוני ביחס למצב המצערת והסל\"ד.",
            symptoms = listOf("מנוע כבה בעצירה", "תגובת מצערת איטית", "נורת מנוע דולקת"),
            causes = listOf("חיישן MAF מלוכלך באבק או שמן", "פילטר אוויר סתום מאוד", "דליפת אוויר בין ה-MAF למצערת"),
            solutions = listOf("ניקוי חיישן MAF בחומר ניקוי ייעודי", "החלפת מסנן אוויר", "חיזוק בנדים וצינור יניקה")
        )

        // P0113 - IAT Sensor Circuit High
        register(
            code = "P0113",
            titleHe = "חיישן טמפרטורת אוויר יניקה (IAT) - מתח גבוה",
            titleEn = "Intake Air Temperature Sensor 1 Circuit High",
            categoryHe = "חיישני כניסת אוויר",
            severity = DtcSeverity.LOW,
            desc = "החיישן מדווח על טמפרטורת אוויר יניקה קרה במיוחד (קצר/נתק במעגל).",
            symptoms = listOf("נורת מנוע דולקת", "התנעה קשה"),
            causes = listOf("קונקטור מנותק בחיישן IAT", "חיישן IAT תקול"),
            solutions = listOf("חיבור קונקטור או החלפת חיישן IAT")
        )

        // P0128 - Coolant Thermostat Malfunction
        register(
            code = "P0128",
            titleHe = "תרמוסטט נוזל קירור - המנוע אינו מגיע לטמפרטורת עבודה בזמן",
            titleEn = "Coolant Thermostat (Coolant Temp Below Thermostat Regulating Temp)",
            categoryHe = "מערכת קירור מנוע",
            severity = DtcSeverity.LOW,
            desc = "המנוע נשאר קר מדי זמן רב לאחר ההתנעה עקב תרמוסטט שנשאר תקוע במצב פתוח.",
            symptoms = listOf("מד חום מנוע עולה לאט מאוד או נשאר נמוך", "חימום חלש בתא הנוסעים בחורף", "צריכת דלק מוגברת מעט"),
            causes = listOf("תרמוסטט תקוע פתוח", "חיישן טמפרטורת נוזל קירור (ECT) תקול", "מפלס נוזל קירור נמוך"),
            solutions = listOf("החלפת תרמוסטט מנוע", "בדיקת נוזל קירור וניקוז אוויר")
        )

        // P0135 - O2 Sensor Heater Circuit (Bank 1 Sensor 1)
        register(
            code = "P0135",
            titleHe = "גוף חימום חיישן חמצן קדמי (בנק 1, חיישן 1)",
            titleEn = "O2 Sensor Heater Circuit Malfunction (Bank 1 Sensor 1)",
            categoryHe = "חיישני פליטה וחמצן",
            severity = DtcSeverity.LOW,
            desc = "גוף החימום הפנימי של חיישן החמצן אינו פועל כראוי, מה שמאריך את הזמן שלוקח לחיישן להתחמם ולהגיע לקריאה מדויקת.",
            symptoms = listOf("נורת בדוק מנוע דולקת", "צריכת דלק מעט גבוהה בדקות הראשונות לנסיעה"),
            causes = listOf("חיישן חמצן בלוי/שרוף", "פיוז חימום חיישן שרוף", "נתק בחיווט"),
            solutions = listOf("בדיקת פיוזים והחלפת חיישן חמצן קדמי (Upstream O2)")
        )

        // P0340 - Camshaft Position Sensor Circuit Malfunction
        register(
            code = "P0340",
            titleHe = "תקלה בחיישן מיקום גל זיזים (קמשפט)",
            titleEn = "Camshaft Position Sensor 'A' Circuit Malfunction",
            categoryHe = "מערכת תזמון והצתה",
            severity = DtcSeverity.HIGH,
            desc = "מחשב הרכב אינו מקבל אות אמין לגבי מיקום שסתומי המנוע ביחס לבוכנות.",
            symptoms = listOf("התנעה ארוכה מאוד או חוסר התנעה", "מנוע כבה לפתע בנסיעה", "חוסר כוח מורגש"),
            causes = listOf("חיישן קמשפט פגום", "רצועת/שרשרת טיימינג רופפת או קפצה שן", "חיווט קרוע"),
            solutions = listOf("בדיקת חיישן קמשפט וטיימינג במוסך")
        )

        // P0335 - Crankshaft Position Sensor Circuit Malfunction
        register(
            code = "P0335",
            titleHe = "תקלה בחיישן מיקום גל ארכובה (קרנק)",
            titleEn = "Crankshaft Position Sensor 'A' Circuit Malfunction",
            categoryHe = "מערכת תזמון והצתה",
            severity = DtcSeverity.CRITICAL,
            desc = "חיישן הקרנק מודד את סיבובי המנוע. ללא אות תקין ממנו המנוע לא יניע או יכבה מיידית.",
            symptoms = listOf("הרכב אינו מניע כלל (סטרטר מסתובב אך אין הנעה)", "המנוע נכבה בפתאומיות תוך כדי נסיעה!"),
            causes = listOf("חיישן קרנק תקול", "שבבי מתכת או לכלוך על החיישן", "קונקטור רופף"),
            solutions = listOf("החלפת חיישן גל ארכובה (Crankshaft Sensor)")
        )

        // P0442 - EVAP System Small Leak Detected
        register(
            code = "P0442",
            titleHe = "דליפה קטנה במערכת מיחזור אדי דלק (EVAP)",
            titleEn = "Evaporative Emission Control System Leak Detected (Small Leak)",
            categoryHe = "מערכת בקרת אדי דלק",
            severity = DtcSeverity.LOW,
            desc = "מערכת ה-EVAP לוכדת אדי דלק ממיכל הדלק. זוהתה דליפת לחץ קלה במערכת.",
            symptoms = listOf("נורת בדוק מנוע דולקת", "לעיתים ריח דלק קל ליד הרכב"),
            causes = listOf("מכסה מיכל דלק לא סגור היטב או אטם גומי סדוק", "צינורית ואקום של ה-EVAP סדוקה", "שסתום טיהור (Purge Valve) דולף"),
            solutions = listOf("הידוק או החלפת מכסה מיכל דלק", "בדיקת עשן לאיתור דליפות בצנרת ה-EVAP")
        )

        // P0500 - Vehicle Speed Sensor Malfunction
        register(
            code = "P0500",
            titleHe = "תקלה בחיישן מהירות הרכב (VSS)",
            titleEn = "Vehicle Speed Sensor Malfunction",
            categoryHe = "מערכת מהירות והעברת הילוכים",
            severity = DtcSeverity.MEDIUM,
            desc = "מחשב הרכב אינו מקבל אות מהירות תקין מגלגלי הרכב או מתיבת ההילוכים.",
            symptoms = listOf("מד המהירות בלוח השעונים אינו עובד או קופץ", "הילוכים עוברים בגסות או באיחור", "בקרת שיוט לא עובדת"),
            causes = listOf("חיישן מהירות (VSS / ABS) פגום", "חיווט קרוע אל החיישן", "שיני גלגל השיניים בתיבת ההילוכים שחוקות"),
            solutions = listOf("בדיקת חיווט והחלפת חיישן מהירות VSS")
        )

        // P0700 - Transmission Control System Malfunction
        register(
            code = "P0700",
            titleHe = "תקלה כללית במחשב תיבת ההילוכים (TCM)",
            titleEn = "Transmission Control System (MIL Request)",
            categoryHe = "תיבת הילוכים אוטומטית (גיר)",
            severity = DtcSeverity.HIGH,
            desc = "מחשב הגיר (TCM) זיהה תקלה בתיבת ההילוכים וביקש ממחשב המנוע להדליק את נורת האזהרה.",
            symptoms = listOf("הגיר נכנס למצב חירום (Limp Mode - נשאר בהילוך 3)", "החלפות הילוכים קשות או החלקות", "נורת מנוע/גיר דולקת"),
            causes = listOf("תקלת שסתומי גיר (סולנואידים)", "מפלס שמן גיר נמוך או שמן שרוף", "חיישן מהירות גיר תקול"),
            solutions = listOf("סריקת קודי תקלה ייעודיים במחשב הגיר TCM", "בדיקה והחלפת שמן גיר ופילטר")
        )

        // C0035 - Left Front Wheel Speed Circuit
        register(
            code = "C0035",
            titleHe = "תקלה בחיישן מהירות גלגל קדמי שמאלי (ABS)",
            titleEn = "Left Front Wheel Speed Circuit Malfunction",
            categoryHe = "מערכת בלמים ו-ABS",
            severity = DtcSeverity.MEDIUM,
            desc = "חיישן ה-ABS בגלגל הקדמי שמאלי אינו מעביר אות מהירות תקין.",
            symptoms = listOf("נורת אזהרה ABS ו-ESP דולקות", "מערכת ABS אינה פועלת בבלימת חירום"),
            causes = listOf("חיישן ABS מלוכלך בשבבי מתכת או פגום", "חיווט קרוע בבית הגלגל", "טבעת מגנטית/שיניים פגומה במיסב הגלגל"),
            solutions = listOf("ניקוי או החלפת חיישן ABS קדמי שמאלי", "בדיקת מיסב גלגל")
        )

        // C1201 - Engine Control System Malfunction (ABS disabled)
        register(
            code = "C1201",
            titleHe = "מערכת ABS/VSC הושבתה עקב תקלה במחשב המנוע",
            titleEn = "Engine Control System Malfunction (ABS / VSC disabled)",
            categoryHe = "מערכת בקרת יציבות ו-ABS",
            severity = DtcSeverity.MEDIUM,
            desc = "מחשב בקרת היציבות והבלמים השבית זמנית חלק מתכונות ה-VSC/ABS כיוון שיש קוד תקלה פעיל במנוע.",
            symptoms = listOf("נורות Check Engine, ABS ו-TRAC / VSC דולקות ביחד"),
            causes = listOf("תקלת מנוע ראשית (כגון P0420 או P0300) שגורמת לכיבוי ה-TRAC"),
            solutions = listOf("תיקון ואיפוס קוד התקלה הראשי במנוע")
        )

        // B0001 - Driver Frontal Stage 1 Deployment Control
        register(
            code = "B0001",
            titleHe = "תקלה במעגל כרית אוויר נהג (שלב 1)",
            titleEn = "Driver Frontal Stage 1 Deployment Control",
            categoryHe = "מערכת כריות אוויר (SRS / Airbag)",
            severity = DtcSeverity.CRITICAL,
            desc = "זוהתה תקלה, נתק או התנגדות חריגה במעגל ההפעלה של כרית האוויר בהגה.",
            symptoms = listOf("נורת כרית אוויר (Airbag / SRS) דולקת קבוע בלוח השעונים", "כרית האוויר לא תפעל בעת תאונה!"),
            causes = listOf("סליל כרית אוויר בהגה (Clock Spring) קרוע", "מגע רופף בקונקטור הצהוב מתחת להגה", "כרית אוויר פגומה"),
            solutions = listOf("בדיקת מגעים והחלפת סליל הגה (Clock Spring) במוסך מורשה")
        )

        // B1000 - ECU Malfunction
        register(
            code = "B1000",
            titleHe = "תקלת זיכרון / חומרה במחשב כריות אוויר",
            titleEn = "Electronic Control Unit (ECU) Malfunction",
            categoryHe = "מערכת בטיחות ונוחות",
            severity = DtcSeverity.CRITICAL,
            desc = "מחשב ה-SRS זיהה שגיאה פנימית בזיכרון לאחר תאונה או כתוצאה ממתח מצבר לא תקין.",
            symptoms = listOf("נורת כריות אוויר דולקת קבוע"),
            causes = listOf("נעילת מחשב לאחר פתיחת כריות", "מתח מצבר חריג"),
            solutions = listOf("איפוס או החלפת מודול כריות אוויר במוסך")
        )

        // U0100 - Lost Communication With ECM/PCM
        register(
            code = "U0100",
            titleHe = "אובדן תקשורת עם מחשב המנוע (ECM / PCM)",
            titleEn = "Lost Communication With ECM/PCM 'A'",
            categoryHe = "רשת תקשורת מחשבים (CAN-Bus)",
            severity = DtcSeverity.HIGH,
            desc = "אחד ממחשבי הרכב (כגון ABS, לוח שעונים או גיר) אינו מקבל נתונים ממחשב המנוע הראשי.",
            symptoms = listOf("הרכב אינו מניע או כבה", "לוח מחוונים מציג קווים במקום ספרות", "נורות אזהרה רבות דולקות בו-זמנית"),
            causes = listOf("מתח מצבר נמוך או הארקה ראשית רופפת", "פיוז מחשב מנוע שרוף", "נתק בקווי תקשורת CAN-Bus"),
            solutions = listOf("בדיקת מצבר והארקות", "בדיקת פיוזים וממסרי מחשב מנוע")
        )

        // U0121 - Lost Communication With Anti-Lock Brake System (ABS)
        register(
            code = "U0121",
            titleHe = "אובדן תקשורת עם מחשב ה-ABS",
            titleEn = "Lost Communication With Anti-Lock Brake System (ABS) Module",
            categoryHe = "רשת תקשורת מחשבים (CAN-Bus)",
            severity = DtcSeverity.HIGH,
            desc = "מחשב המנוע אינו מצליח לתקשר עם מודול הבלמים ו-ABS ברשת ה-CAN.",
            symptoms = listOf("נורת ABS דולקת", "מד מהירות לא פעיל"),
            causes = listOf("פיוז ABS שרוף", "קונקטור ABS מלוכלך/רופף", "תקלת מחשב ABS"),
            solutions = listOf("בדיקת מתח והארקה למחשב ה-ABS")
        )
    }
}