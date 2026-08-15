package br.com.essampaio.nearnode.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface Route {
    @Serializable
    data object Registration : Route

    @Serializable
    data object ListContact : Route

    @Serializable
    data object NewChat : Route

    @Serializable
    data class Chat(val contactId: String) : Route
}
