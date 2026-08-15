package br.com.essampaio.nearnode.domain.model

data class Profile(
    val id: String,
    val username: String,
    val ip: String,
    val status: AvailableStatus
)
