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
package eu.europa.ec.eudi.verifier.endpoint.adapter.input.web

import eu.europa.ec.eudi.verifier.endpoint.port.input.QueryResponse
import eu.europa.ec.eudi.verifier.endpoint.port.input.ValidationError
import eu.europa.ec.eudi.verifier.endpoint.port.input.statuslist.GetStatusListAggregationObject
import eu.europa.ec.eudi.verifier.endpoint.port.input.statuslist.GetStatusListObject
import eu.europa.ec.eudi.verifier.endpoint.port.out.web.StatusListAggregation
import eu.europa.ec.eudi.verifier.endpoint.port.out.web.StatusListWithBits
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.web.reactive.function.server.*
import org.springframework.web.reactive.function.server.ServerResponse.*

internal class StatusListApi(
    private val getStatusList: GetStatusListObject,
    private val getStatusListAggregation: GetStatusListAggregationObject,
) {

    val route = coRouter {
        GET(STATUS_LIST_RESPONSE_PATH, accept(APPLICATION_JSON), this@StatusListApi::handleGetStatusList)
        GET(STATUS_LIST_AGGREGATION_RESPONSE_PATH, accept(APPLICATION_JSON), this@StatusListApi::handleGetStatusListAggregation)
    }

    private suspend fun handleGetStatusList(req: ServerRequest): ServerResponse {
        suspend fun found(statusLists: StatusListWithBits) = ok().json().bodyValueAndAwait(statusLists)

        return when (val result = getStatusList(req)) {
            is QueryResponse.NotFound -> notFound().buildAndAwait()
            is QueryResponse.InvalidState -> badRequest().buildAndAwait()
            is QueryResponse.Found -> found(result.value)
        }
    }

    private suspend fun handleGetStatusListAggregation(req: ServerRequest): ServerResponse {
        suspend fun found(statusListAggregation: StatusListAggregation) = ok().json().bodyValueAndAwait(statusListAggregation)

        return when (val result = getStatusListAggregation(req)) {
            is QueryResponse.NotFound -> notFound().buildAndAwait()
            is QueryResponse.InvalidState -> badRequest().buildAndAwait()
            is QueryResponse.Found -> found(result.value)
        }
    }

    companion object {
        const val STATUS_LIST_RESPONSE_PATH = "/ui/status-list"
        const val STATUS_LIST_AGGREGATION_RESPONSE_PATH = "/ui/status-list/aggregation"

        private fun ServerRequest.id() = queryParam("id")
        private suspend fun ValidationError.asBadRequest() =
            badRequest().json().bodyValueAndAwait(mapOf("error" to this))
    }
}
