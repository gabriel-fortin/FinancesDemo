package com.example.g14.financesdemo.model

import org.joda.time.Instant

/**
 * Created by Gabriel Fortin
 */

data class Transaction(
        val date: Instant,
        val description: String,
        val amount: MoneyAmount,
        val currency: Currency)
