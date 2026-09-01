package com.archi.tmpnces.presentation.common

object CurrencyFlag {
	
	fun emojiFor(currencyCode: String): String? {
		if (currencyCode.length < 2) return null
		
		val countryCode = currencyCode.take(2)
			.uppercase()
		if (countryCode[0] == 'X') return null
		if (countryCode.any { it !in 'A'..'Z' }) return null
		
		return countryCode.map { letter -> REGIONAL_INDICATOR_A + (letter - 'A') }
			.joinToString("") { codePoint -> String(Character.toChars(codePoint)) }
	}
	
	private const val REGIONAL_INDICATOR_A = 0x1F1E6
}