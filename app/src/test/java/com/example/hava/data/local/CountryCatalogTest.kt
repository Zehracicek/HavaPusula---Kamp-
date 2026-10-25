package com.example.hava.data.local

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CountryCatalogTest {

    private val turkey = CountryCatalog.citiesOf(CountryCatalog.TURKEY_CODE)

    @Test
    fun `81 ilin her biri kendi plakasıyla eşleşir`() {
        assertEquals(81, turkey.size)
        turkey.forEach { city ->
            assertEquals(city.name, city.id.toString().padStart(2, '0'), TurkishPlates.plateFor(city))
        }
    }

    @Test
    fun `iller Türk alfabesine göre sıralanır`() {
        val names = turkey.map { it.name }
        // C < Ç < D, I < İ ve Ş, Z'den önce gelir
        assertTrue(names.indexOf("Çorum") < names.indexOf("Denizli"))
        assertTrue(names.indexOf("Bursa") < names.indexOf("Çanakkale"))
        assertTrue(names.indexOf("Isparta") < names.indexOf("İstanbul"))
        assertTrue(names.indexOf("Şırnak") < names.indexOf("Tekirdağ"))
        assertEquals("Adana", names.first())
        assertEquals("Zonguldak", names.last())
    }

    @Test
    fun `Türkiye ilk sırada, diğer ülkeler alfabetik`() {
        val names = CountryCatalog.countries.map { it.name }
        assertEquals("Türkiye", names.first())
        val others = names.drop(1)
        assertEquals(others.sortedWith(TurkishCollator), others)
    }

    @Test
    fun `şehir kimlikleri benzersizdir`() {
        val all = CountryCatalog.countries.flatMap { CountryCatalog.citiesOf(it.code) }
        assertEquals(all.size, all.map { it.id }.toSet().size)
    }

    @Test
    fun `arama anahtarı Türkçe karakterlerden bağımsızdır`() {
        assertEquals("igdir", "Iğdır".toSearchKey())
        assertEquals("istanbul", "İSTANBUL".toSearchKey())
        assertEquals("sanliurfa", "Şanlıurfa".toSearchKey())
    }
}
