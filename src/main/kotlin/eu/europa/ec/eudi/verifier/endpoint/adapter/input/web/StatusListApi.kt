package eu.europa.ec.eudi.verifier.endpoint.adapter.input.web

import eu.europa.ec.eudi.verifier.endpoint.port.input.QueryResponse
import eu.europa.ec.eudi.verifier.endpoint.port.input.ValidationError
import eu.europa.ec.eudi.verifier.endpoint.port.input.statuslist.GetStatusListAggregationObject
import eu.europa.ec.eudi.verifier.endpoint.port.input.statuslist.GetStatusListObject
import eu.europa.ec.eudi.verifier.endpoint.port.out.web.StatusListAggregation
import eu.europa.ec.eudi.verifier.endpoint.port.out.web.StatusListWithBits
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.env.Environment
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.web.reactive.function.server.*
import org.springframework.web.reactive.function.server.ServerResponse.*

internal class StatusListApi(
    private val getStatusList: GetStatusListObject,
    private val getStatusListAggregation: GetStatusListAggregationObject,
    private val environment: Environment,
) {

    private val logger: Logger = LoggerFactory.getLogger(StatusListApi::class.java)
    val route = coRouter {
        GET(STATUS_LIST_RESPONSE_PATH, accept(APPLICATION_JSON), this@StatusListApi::handleGetStatusList)
        GET(STATUS_LIST_AGGREGATION_RESPONSE_PATH, accept(APPLICATION_JSON), this@StatusListApi::handleGetStatusListAggregation)
    }

    private suspend fun handleGetStatusList(req: ServerRequest): ServerResponse {
        suspend fun found(statusLists: StatusListWithBits) = ok().json().bodyValueAndAwait(statusLists)

        val id = req.id()

        logger.info("Handling Get StatusList id: ${id.orElse("(status list ID missing)")}")
        return when (val result = getStatusList(req)) {
            is QueryResponse.NotFound -> notFound().buildAndAwait()
            is QueryResponse.InvalidState -> badRequest().buildAndAwait()
            is QueryResponse.Found -> found(result.value)
        }
    }

    private suspend fun handleGetStatusListAggregation(req: ServerRequest): ServerResponse {
        suspend fun found(statusListAggregation: StatusListAggregation) = ok().json().bodyValueAndAwait(statusListAggregation)

        logger.info("Handling Get StatusListAggregation for pool ${environment.getProperty("statusList.poolId")}")
        return when (val result = getStatusListAggregation()) {
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
