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
package eu.europa.ec.eudi.verifier.endpoint.port.input.statuslist

import eu.europa.ec.eudi.verifier.endpoint.port.input.QueryResponse
import eu.europa.ec.eudi.verifier.endpoint.port.input.QueryResponse.Found
import eu.europa.ec.eudi.verifier.endpoint.port.input.QueryResponse.InvalidState
import eu.europa.ec.eudi.verifier.endpoint.port.out.web.ClientResponse
import eu.europa.ec.eudi.verifier.endpoint.port.out.web.GetStatusListAggregationClient
import eu.europa.ec.eudi.verifier.endpoint.port.out.web.StatusListAggregation
import org.slf4j.Logger
import org.slf4j.LoggerFactory

fun interface GetStatusListAggregationObject {
    suspend operator fun invoke(): QueryResponse<StatusListAggregation>
}

class GetStatusListAggregationObjectLive(
    private val getStatusListAggregationLive: GetStatusListAggregationClient,
) : GetStatusListAggregationObject {

    private val logger: Logger = LoggerFactory.getLogger(GetStatusListAggregationObjectLive::class.java)

    override suspend operator fun invoke(): QueryResponse<StatusListAggregation> {
        return when (val statusListAggregation = getStatusListAggregationLive.invoke()) {
            is ClientResponse.Found -> Found(statusListAggregation.value)
            is ClientResponse.NotFound -> QueryResponse.NotFound
            else -> invalidState()
        }
    }


    private fun invalidState(): InvalidState {
        fun log() {
            logger.info("Getting status list aggregation, invalid state")
        }
        log()
        return InvalidState
    }
}
