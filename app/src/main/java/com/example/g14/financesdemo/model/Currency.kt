package com.example.g14.financesdemo.model

/**
 * Created by Gabriel Fortin
 */

data class Currency private constructor(val symbol: String) {
    companion object Factory {
        fun from(symbol: String): Currency {
            if (symbol.isEmpty()) throw IllegalArgumentException("currency string cannot be empty")
            return Currency(symbol)
            TODO("implement flyweight pattern for currency")
            TODO("restrict allowed values to a predefined set of accepted currencies")
        }
    }
}
