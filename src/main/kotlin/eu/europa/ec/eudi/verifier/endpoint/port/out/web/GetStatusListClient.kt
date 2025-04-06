/*
 * Copyright (c) 2023 European Commission
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.europa.ec.eudi.verifier.endpoint.port.out.web

import com.google.gson.Gson
import com.nimbusds.jwt.SignedJWT
import eu.europa.ec.eudi.verifier.endpoint.port.out.web.util.StatusList
import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

@Serializable
data class StatusListJwt(
    @SerialName("header") @Required val header: String,
    @SerialName("payload") @Required val payload: String,
    @SerialName("signature") val signature: String,
)

@Serializable
data class JWTHeaderParameters(
    @SerialName("alg") @Required val alg: String,
)

@Serializable
data class StatusListJWTPayload(
    @SerialName("iss") @Required val iss: String,
    @SerialName("sub") @Required val sub: String,
    @SerialName("iat") @Required val iat: Long,
    @SerialName("exp") val exp: Long,
    @SerialName("ttl") val ttl: Long,
    @SerialName("status_list") val status_list: StatusListPayload,
)

@Serializable
data class StatusListPayload(
    @SerialName("bits") @Required val bits: Int,
    @SerialName("lst") @Required val lst: String,
    @SerialName("aggregation_uri") val aggregation_uri: String,
)

@Serializable
data class StatusListWithBits(
    @SerialName("statusList") @Required val statusList: ByteArray,
    @SerialName("bitsPerStatus") @Required val bitsPerStatus: Int,
    @SerialName("totalStatuses") @Required val totalStatuses: Int,
)

interface StatusListClient {
    suspend operator fun invoke(url: String): ClientResponse<StatusListWithBits>
    suspend fun doInvoke(url: String): StatusListWithBits?
}

class GetStatusListClient(
    private val gson: Gson,
) : StatusListClient {

    private val logger: Logger = LoggerFactory.getLogger(GetStatusListClient::class.java)

    override suspend operator fun invoke(url: String): ClientResponse<StatusListWithBits> {
        val statusListWithBits = doInvoke(url)

        return if (statusListWithBits != null) {
            when (statusListWithBits.totalStatuses > 0) {
                true -> ClientResponse.Found(statusListWithBits)
                false -> ClientResponse.NotFound
            }
        } else {
            return ClientResponse.NotFound
        }
    }

    override suspend fun doInvoke(url: String): StatusListWithBits? {
        val statusListResponse = WebClient.builder()
            .baseUrl(url)
            .build()
            .get()
            .retrieve()
            .awaitBody<String>()

        val jwt = toSignedJWT(statusListResponse)
        val statusListJwtPayload = toStatusListJwtPayload(jwt.payload.toString())
        logger.info("statusListJwtPayload: $statusListJwtPayload")

        val statusListWithBits = toStatusListWithBits(statusListJwtPayload)
        logger.debug("statusListWithBits: {}", statusListWithBits)

        return when (statusListWithBits.totalStatuses > 0) {
            true -> statusListWithBits
            false -> null
        }
    }

    private fun toSignedJWT(jwt: String): SignedJWT {
        return SignedJWT.parse(jwt)
    }

    private fun toStatusListJwtPayload(jwtPayload: String): StatusListJWTPayload {
        return gson.fromJson(jwtPayload, StatusListJWTPayload::class.java)
    }

    private fun toStatusListWithBits(statusListJwtPayload: StatusListJWTPayload): StatusListWithBits {
        val list = StatusList.fromEncoded(statusListJwtPayload.status_list.bits, statusListJwtPayload.status_list.lst).getList()
        return StatusListWithBits(
            list,
            statusListJwtPayload.status_list.bits,
            list.size,
        )
    }
}
