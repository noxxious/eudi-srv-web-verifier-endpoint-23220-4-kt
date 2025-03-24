package eu.europa.ec.eudi.verifier.endpoint.port.out.web

sealed interface ClientResponse<out T : Any> {
    data object NotFound : ClientResponse<Nothing>
    data object InvalidState : ClientResponse<Nothing>
    data class Found<T : Any>(val value: T) : ClientResponse<T>
}
