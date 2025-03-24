package eu.europa.ec.eudi.verifier.endpoint.port.out.web

import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.springframework.core.env.Environment
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

@Serializable
data class StatusListAggregation(
    @SerialName("status_lists") @Required val statusLists: List<String>,
)

fun interface StatusListAggregationClient {
    suspend operator fun invoke(): ClientResponse<StatusListAggregation>
}

class GetStatusListAggregationClient(
    private val statusListWebClient: WebClient,
    private val environment: Environment,
) : StatusListAggregationClient {

    override suspend operator fun invoke(): ClientResponse<StatusListAggregation> {
        val statusListAggregation = statusListWebClient
            .get()
            .uri("/aggregation/${environment.getProperty("statusList.poolId")}")
            .retrieve()
            .awaitBody<StatusListAggregation>()

        return when (statusListAggregation.statusLists.isNotEmpty()) {
            true -> ClientResponse.Found(statusListAggregation)
            false -> ClientResponse.NotFound
        }
    }
}
