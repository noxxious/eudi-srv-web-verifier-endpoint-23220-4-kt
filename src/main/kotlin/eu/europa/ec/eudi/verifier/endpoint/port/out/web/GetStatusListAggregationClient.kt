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

import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

@Serializable
data class StatusListAggregation(
    @SerialName("status_lists") @Required val statusLists: List<String>,
)

fun interface StatusListAggregationClient {
    suspend operator fun invoke(url: String): ClientResponse<StatusListAggregation>
}

class GetStatusListAggregationClient : StatusListAggregationClient {

    override suspend operator fun invoke(url: String): ClientResponse<StatusListAggregation> {
        val statusListAggregation = WebClient.builder()
            .baseUrl(url)
            .build()
            .get()
            .retrieve()
            .awaitBody<StatusListAggregation>()

        return when (statusListAggregation.statusLists.isNotEmpty()) {
            true -> ClientResponse.Found(statusListAggregation)
            false -> ClientResponse.NotFound
        }
    }
}
