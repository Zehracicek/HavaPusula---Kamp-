package com.example.hava.data.local

import com.example.hava.data.remote.dto.City
import java.util.Locale

object TurkishPlates {

    private val plates: Map<String, Int> = mapOf(
        "adana" to 1, "adiyaman" to 2, "afyonkarahisar" to 3, "afyon" to 3, "agri" to 4,
        "amasya" to 5, "ankara" to 6, "antalya" to 7, "artvin" to 8, "aydin" to 9,
        "balikesir" to 10, "bilecik" to 11, "bingol" to 12, "bitlis" to 13, "bolu" to 14,
        "burdur" to 15, "bursa" to 16, "canakkale" to 17, "cankiri" to 18, "corum" to 19,
        "denizli" to 20, "diyarbakir" to 21, "edirne" to 22, "elazig" to 23, "erzincan" to 24,
        "erzurum" to 25, "eskisehir" to 26, "gaziantep" to 27, "giresun" to 28, "gumushane" to 29,
        "hakkari" to 30, "hatay" to 31, "isparta" to 32, "mersin" to 33,
        "icel" to 33, "istanbul" to 34, "izmir" to 35, "kars" to 36, "kastamonu" to 37,
        "kayseri" to 38, "kirklareli" to 39, "kirsehir" to 40, "kocaeli" to 41, "izmit" to 41,
        "konya" to 42, "kutahya" to 43, "malatya" to 44, "manisa" to 45, "kahramanmaras" to 46,
        "maras" to 46, "mardin" to 47, "mugla" to 48, "mus" to 49, "nevsehir" to 50,
        "nigde" to 51, "ordu" to 52, "rize" to 53, "sakarya" to 54, "adapazari" to 54,
        "samsun" to 55, "siirt" to 56, "sinop" to 57, "sivas" to 58, "tekirdag" to 59,
        "tokat" to 60, "trabzon" to 61, "tunceli" to 62, "sanliurfa" to 63, "urfa" to 63,
        "usak" to 64, "van" to 65, "yozgat" to 66, "zonguldak" to 67, "aksaray" to 68,
        "bayburt" to 69, "karaman" to 70, "kirikkale" to 71, "batman" to 72, "sirnak" to 73,
        "bartin" to 74, "ardahan" to 75, "igdir" to 76, "yalova" to 77, "karabuk" to 78,
        "kilis" to 79, "osmaniye" to 80, "duzce" to 81,
    )

    private val turkishLocale = Locale("tr", "TR")
    private val suffixes = listOf(" ili", " province", " il")

    /** Türkiye'deki bir şehir için iki haneli plaka kodunu ("06") döndürür, bulunamazsa null. */
    fun plateFor(city: City): String? {
        if (!city.countryCode.equals("TR", ignoreCase = true)) return null
        val code = listOfNotNull(city.region, city.name)
            .firstNotNullOfOrNull { plates[normalize(it)] }
            ?: return null
        return code.toString().padStart(2, '0')
    }

    private fun normalize(raw: String): String {
        var s = raw.trim().lowercase(turkishLocale)
        suffixes.firstOrNull { s.endsWith(it) }?.let { s = s.removeSuffix(it) }
        return s.toSearchKey()
    }
}
