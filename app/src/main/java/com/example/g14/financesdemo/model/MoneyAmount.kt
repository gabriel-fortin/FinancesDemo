package com.example.g14.financesdemo.model

import java.math.BigDecimal

/**
 * Created by Gabriel Fortin
 */

data class MoneyAmount private constructor(val value: BigDecimal) {
    companion object Factory {
        fun from(string: String) : MoneyAmount {
            return MoneyAmount(BigDecimal(string))
        }
    }
}
