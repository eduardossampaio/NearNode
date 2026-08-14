package br.com.essampaio.nearnode.domain.model

import br.com.essampaio.nearnode.AvailableStatus

data class Profile(
    val id: String,
    val username: String,
    val ip: String,
    val status: AvailableStatus
)
