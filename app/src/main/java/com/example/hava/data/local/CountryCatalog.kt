package com.example.hava.data.local

import com.example.hava.data.model.Country
import com.example.hava.data.remote.dto.City

/**
 * Ülke seçiciye çıkan ülkeler ve her birinin başlıca şehirleri.
 * Geocoding API bir ülkenin şehirlerini listeleyemediği için listeler uygulamayla gelir.
 */
object CountryCatalog {

    const val TURKEY_CODE = "TR"

    val defaultCity: City = TurkishProvinces.ankara

    private val foreignCities: Map<Country, List<City>> = buildMap {
        country(1, "AZ", "Azerbaycan") {
            city("Bakü", 40.4093, 49.8671)
            city("Gence", 40.6828, 46.3606)
            city("Lenkeran", 38.7529, 48.8475)
            city("Mingeçevir", 40.7703, 47.0496)
            city("Nahçıvan", 39.2089, 45.4122)
            city("Sumgayıt", 40.5897, 49.6686)
            city("Şeki", 41.1919, 47.1706)
        }
        country(2, "DE", "Almanya") {
            city("Berlin", 52.5200, 13.4050)
            city("Bremen", 53.0793, 8.8017)
            city("Dortmund", 51.5136, 7.4653)
            city("Düsseldorf", 51.2277, 6.7735)
            city("Essen", 51.4556, 7.0116)
            city("Frankfurt", 50.1109, 8.6821)
            city("Hamburg", 53.5511, 9.9937)
            city("Hannover", 52.3759, 9.7320)
            city("Köln", 50.9375, 6.9603)
            city("Münih", 48.1351, 11.5820)
            city("Nürnberg", 49.4521, 11.0767)
            city("Stuttgart", 48.7758, 9.1829)
        }
        country(3, "GB", "Birleşik Krallık") {
            city("Birmingham", 52.4862, -1.8904)
            city("Bristol", 51.4545, -2.5879)
            city("Edinburgh", 55.9533, -3.1883)
            city("Glasgow", 55.8642, -4.2518)
            city("Leeds", 53.8008, -1.5491)
            city("Liverpool", 53.4084, -2.9916)
            city("Londra", 51.5074, -0.1278)
            city("Manchester", 53.4808, -2.2426)
        }
        country(4, "FR", "Fransa") {
            city("Bordeaux", 44.8378, -0.5792)
            city("Lyon", 45.7640, 4.8357)
            city("Marsilya", 43.2965, 5.3698)
            city("Nantes", 47.2184, -1.5536)
            city("Nice", 43.7102, 7.2620)
            city("Paris", 48.8566, 2.3522)
            city("Strazburg", 48.5734, 7.7521)
            city("Toulouse", 43.6047, 1.4442)
        }
        country(5, "NL", "Hollanda") {
            city("Amsterdam", 52.3676, 4.9041)
            city("Eindhoven", 51.4416, 5.4697)
            city("Groningen", 53.2194, 6.5665)
            city("Lahey", 52.0705, 4.3007)
            city("Rotterdam", 51.9244, 4.4777)
            city("Utrecht", 52.0907, 5.1214)
        }
        country(6, "IT", "İtalya") {
            city("Bologna", 44.4949, 11.3426)
            city("Floransa", 43.7696, 11.2558)
            city("Milano", 45.4642, 9.1900)
            city("Napoli", 40.8518, 14.2681)
            city("Palermo", 38.1157, 13.3615)
            city("Roma", 41.9028, 12.4964)
            city("Torino", 45.0703, 7.6869)
            city("Venedik", 45.4408, 12.3155)
        }
        country(7, "ES", "İspanya") {
            city("Barselona", 41.3874, 2.1686)
            city("Bilbao", 43.2630, -2.9350)
            city("Madrid", 40.4168, -3.7038)
            city("Malaga", 36.7213, -4.4214)
            city("Sevilla", 37.3891, -5.9845)
            city("Valensiya", 39.4699, -0.3763)
        }
        country(8, "GR", "Yunanistan") {
            city("Atina", 37.9838, 23.7275)
            city("Girit (Kandiye)", 35.3387, 25.1442)
            city("Larissa", 39.6390, 22.4191)
            city("Patras", 38.2466, 21.7346)
            city("Rodos", 36.4341, 28.2176)
            city("Selanik", 40.6401, 22.9444)
        }
        country(9, "BG", "Bulgaristan") {
            city("Burgaz", 42.5048, 27.4626)
            city("Filibe", 42.1354, 24.7453)
            city("Kırcaali", 41.6338, 25.3777)
            city("Rusçuk", 43.8356, 25.9657)
            city("Sofya", 42.6977, 23.3219)
            city("Varna", 43.2141, 27.9147)
        }
        country(10, "GE", "Gürcistan") {
            city("Batum", 41.6168, 41.6367)
            city("Kutaisi", 42.2679, 42.6946)
            city("Rustavi", 41.5495, 44.9932)
            city("Tiflis", 41.7151, 44.8271)
            city("Zugdidi", 42.5088, 41.8709)
        }
        country(11, "CY", "Kıbrıs") {
            city("Baf", 34.7720, 32.4297)
            city("Gazimağusa", 35.1250, 33.9417)
            city("Girne", 35.3364, 33.3182)
            city("Güzelyurt", 35.1983, 32.9936)
            city("Larnaka", 34.9003, 33.6232)
            city("Lefkoşa", 35.1856, 33.3823)
            city("Limasol", 34.6786, 33.0413)
        }
        country(12, "RU", "Rusya") {
            city("Kazan", 55.7887, 49.1221)
            city("Moskova", 55.7558, 37.6173)
            city("Novosibirsk", 55.0084, 82.9357)
            city("Soçi", 43.5855, 39.7231)
            city("St. Petersburg", 59.9311, 30.3609)
            city("Yekaterinburg", 56.8389, 60.6057)
        }
        country(13, "AE", "Birleşik Arap Emirlikleri") {
            city("Abu Dabi", 24.4539, 54.3773)
            city("Al Ain", 24.2075, 55.7447)
            city("Dubai", 25.2048, 55.2708)
            city("Şarika", 25.3463, 55.4209)
        }
        country(14, "SA", "Suudi Arabistan") {
            city("Cidde", 21.4858, 39.1925)
            city("Dammam", 26.4207, 50.0888)
            city("Medine", 24.5247, 39.5692)
            city("Mekke", 21.3891, 39.8579)
            city("Riyad", 24.7136, 46.6753)
        }
        country(15, "US", "Amerika Birleşik Devletleri") {
            city("Chicago", 41.8781, -87.6298)
            city("Houston", 29.7604, -95.3698)
            city("Los Angeles", 34.0522, -118.2437)
            city("Miami", 25.7617, -80.1918)
            city("New York", 40.7128, -74.0060)
            city("San Francisco", 37.7749, -122.4194)
            city("Seattle", 47.6062, -122.3321)
            city("Washington", 38.9072, -77.0369)
        }
        country(16, "JP", "Japonya") {
            city("Fukuoka", 33.5904, 130.4017)
            city("Kyoto", 35.0116, 135.7681)
            city("Nagoya", 35.1815, 136.9066)
            city("Osaka", 34.6937, 135.5023)
            city("Sapporo", 43.0618, 141.3545)
            city("Tokyo", 35.6762, 139.6503)
            city("Yokohama", 35.4437, 139.6380)
        }
    }

    private val turkey = Country(TURKEY_CODE, "Türkiye")

    /** Türkiye başta, diğerleri alfabetik. */
    val countries: List<Country> =
        listOf(turkey) + foreignCities.keys.sortedWith(compareBy(TurkishCollator) { it.name })

    fun countryOf(code: String): Country = countries.firstOrNull { it.code == code } ?: turkey

    /** Ülkenin şehirleri, Türkçe alfabeye göre sıralı. */
    fun citiesOf(countryCode: String): List<City> {
        val cities = if (countryCode == TURKEY_CODE) {
            TurkishProvinces.list
        } else {
            foreignCities.entries.firstOrNull { it.key.code == countryCode }?.value.orEmpty()
        }
        return cities.sortedWith(compareBy(TurkishCollator) { it.name })
    }

    private class CityListBuilder(private val country: Country, private val idBase: Long) {
        val cities = mutableListOf<City>()

        fun city(name: String, latitude: Double, longitude: Double) {
            cities += City(
                id = idBase + cities.size,
                name = name,
                latitude = latitude,
                longitude = longitude,
                country = country.name,
                countryCode = country.code,
            )
        }
    }

    // Kimlikler plaka numaralarıyla (1..81) ve geocoding kimlikleriyle çakışmasın diye ülke başına ayrı blok
    private fun MutableMap<Country, List<City>>.country(
        index: Int,
        code: String,
        name: String,
        block: CityListBuilder.() -> Unit,
    ) {
        val country = Country(code, name)
        put(country, CityListBuilder(country, idBase = index * 1_000L).apply(block).cities)
    }
}
