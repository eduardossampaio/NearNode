package br.com.essampaio.nearnode.data.service.communication

import br.com.essampaio.nearnode.domain.model.AvailableStatus
import br.com.essampaio.nearnode.domain.model.Profile
import br.com.essampaio.nearnode.domain.service.RemoteNodeService
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

// 1. Criamos um DTO privado para espelhar o mapOf do Ktor Server
private data class ProfileDto(
    @SerializedName("id") val id: String,
    @SerializedName("userName") val userName: String,
    @SerializedName("profilePicture") val profilePicture: String,
    @SerializedName("status") val status: String
)

class RestfulServerNodeService(
    private val httpClient: OkHttpClient,
    private val gson: Gson
) : RemoteNodeService {

    override suspend fun getNodeInfo(ip: String, port: Int): Profile? {
        // 2. Garante que a requisição de rede rode fora da Main Thread
        val deviceIp = ip
        return withContext(Dispatchers.IO) {
            try {
                // Monta a URL dinâmica usando o IP resolvido pelo NsdManager
                val url = "http://$ip:$port/info"

                val request = Request.Builder()
                    .url(url)
                    .get()
                    .build()

                // 3. Executa a chamada HTTP
                val response = httpClient.newCall(request).execute()

                // 4. Valida se retornou HTTP 200 (OK)
                if (response.isSuccessful) {
                    val jsonBody = response.body?.string()

                    if (jsonBody != null) {
                        // 5. Converte o JSON string para o nosso DTO
                        val dto = gson.fromJson(jsonBody, ProfileDto::class.java)

                        // 6. Mapeia o DTO para o seu modelo de Domínio (Profile)
                        // NOTA: Ajuste a criação do Profile conforme os construtores
                        // e Enums (AvailableStatus) do seu projeto.
                        return@withContext Profile(
                            id = dto.id,
                            username = dto.userName,
                            ip = deviceIp,
                            status = AvailableStatus.ONLINE
                        )
                    }
                }
                // Se der erro HTTP (404, 500) ou corpo vazio, retorna null
                null
            } catch (e: Exception) {
                // Captura falhas de rede (ex: Timeout, aparelho desconectou)
                android.util.Log.e("RemoteNodeService", "Falha ao buscar perfil no IP $ip", e)
                null
            }
        }
    }
}
