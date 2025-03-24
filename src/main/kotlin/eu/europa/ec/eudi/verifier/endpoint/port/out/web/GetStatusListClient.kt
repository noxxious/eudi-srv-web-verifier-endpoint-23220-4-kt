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
import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import java.util.*

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

fun interface StatusListClient {
    suspend operator fun invoke(id: String): ClientResponse<StatusListWithBits>
}

class GetStatusListClient(
    private val statusListWebClient: WebClient,
    private val gson: Gson,
) : StatusListClient {

    private val logger: Logger = LoggerFactory.getLogger(GetStatusListClient::class.java)

    override suspend operator fun invoke(id: String): ClientResponse<StatusListWithBits> {
        val statusList = statusListWebClient
            .get()
            .uri("/$id")
            .retrieve()
            .awaitBody<ByteArray>()

        val statusListJwt = toStatusListJwt(statusList)
        logger.info("headerDecoded: ${statusListJwt.header}")
        logger.info("payloadDecoded: ${statusListJwt.payload}")

        val statusListJwtHeader = toStatusListJWTHeader(statusListJwt)
        val statusListJwtPayload = toStatusListJWTPayload(statusListJwt)
        logger.info("statusListJwtHeader: $statusListJwtHeader, statusListJwtPayload: $statusListJwtPayload")

        val statusListWithBits = toStatusListWithBits(statusListJwtPayload)
        logger.info("statusListWithBits: $statusListWithBits")

        return when (statusListWithBits.totalStatuses > 0) {
            true -> ClientResponse.Found(statusListWithBits)
            false -> ClientResponse.NotFound
        }
    }

    private fun toStatusListJwt(bytes: ByteArray): StatusListJwt {
        val response = bytes.toString(Charsets.UTF_8)
        val chunks: List<String> = response.split(".")
        val header = chunks.getOrElse(0) { "" }
        val payload = chunks.getOrElse(1) { "" }
        val signature = chunks.getOrElse(2) { "" }

        val decoder = Base64.getUrlDecoder()
        val headerDecoded = decoder.decode(header).toString(Charsets.UTF_8)
        val payloadDecoded = decoder.decode(payload).toString(Charsets.UTF_8)
        val signatureDecoded = decoder.decode(signature).toString(Charsets.UTF_8)

        return StatusListJwt(headerDecoded, payloadDecoded, signatureDecoded)
    }

    private fun toStatusListJWTHeader(statusListJwt: StatusListJwt): JWTHeaderParameters {
        return gson.fromJson(statusListJwt.header, JWTHeaderParameters::class.java)
    }

    private fun toStatusListJWTPayload(statusListJwt: StatusListJwt): StatusListJWTPayload {
        return gson.fromJson(statusListJwt.payload, StatusListJWTPayload::class.java)
    }

    private fun toStatusListWithBits(statusListJWTPayload: StatusListJWTPayload): StatusListWithBits {
        val statusListDecoded = StatusList.fromEncoded(statusListJWTPayload.status_list.bits, statusListJWTPayload.status_list.lst)
        logger.info("statusListDecoded: $statusListDecoded")

        return StatusListWithBits(
            statusListDecoded.getList(),
            statusListJWTPayload.status_list.bits,
            statusListDecoded.getList().size,
        )
    }
}
