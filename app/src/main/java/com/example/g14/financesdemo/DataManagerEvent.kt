package com.example.g14.financesdemo

/**
 * Created by Gabriel Fortin
 */


/* Values for the 'Requesting' class */
const val ENDPOINT_LOGIN = "LOGIN"
const val ENDPOINT_BALANCE = "BALANCE"
const val ENDPOINT_TRANSACTIONS = "TRANSACTIONS"
const val ENDPOINT_SPEND = "SPEND"

sealed class DataManagerEvent {

    object
    LoggedOut : DataManagerEvent()

    data class
    Requesting(val endpoint: String, val description: String? = null) : DataManagerEvent()

    data class
    RequestSuccess(val endpoint: String, val detailedMessage: String? = null) : DataManagerEvent()

    abstract class
    RequestFailure : DataManagerEvent()

        object
        TokenRejected : RequestFailure()

        data class
        ConnectionProblem(val detailedMessage: String? = null) : RequestFailure()

        data class
        ServerProblem(val detailedMessage: String? = null) : RequestFailure()

        data class
        UnknownProblem(val detailedMessage: String? = null) : RequestFailure()
}
