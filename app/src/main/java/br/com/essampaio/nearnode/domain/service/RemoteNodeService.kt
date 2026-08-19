package br.com.essampaio.nearnode.domain.service

import br.com.essampaio.nearnode.domain.model.Profile

interface RemoteNodeService {
    suspend fun getNodeInfo(ip: String, port: Int): Profile?
}
