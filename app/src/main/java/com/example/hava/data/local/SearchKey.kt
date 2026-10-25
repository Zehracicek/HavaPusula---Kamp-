package com.example.hava.data.local

import java.text.Collator
import java.text.Normalizer
import java.util.Locale

private val TurkishLocale = Locale.forLanguageTag("tr-TR")

/** Ç, Ğ, İ, Ö, Ş, Ü harflerini Türk alfabesindeki yerlerine göre sıralar. */
internal val TurkishCollator: Collator = Collator.getInstance(TurkishLocale)

/** "Iğdır" ve "igdir" gibi yazımların eşleşmesi için aksansız, küçük harfli arama anahtarı. */
internal fun String.toSearchKey(): String {
    val lower = trim().lowercase(TurkishLocale).replace('ı', 'i')
    return Normalizer.normalize(lower, Normalizer.Form.NFD)
        .replace(Regex("[^a-z0-9]"), "")
}
