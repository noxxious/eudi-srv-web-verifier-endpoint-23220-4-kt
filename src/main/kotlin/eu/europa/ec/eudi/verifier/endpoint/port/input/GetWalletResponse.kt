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
package eu.europa.ec.eudi.verifier.endpoint.port.input

import eu.europa.ec.eudi.prex.PresentationSubmission
import eu.europa.ec.eudi.verifier.endpoint.domain.*
import eu.europa.ec.eudi.verifier.endpoint.port.input.QueryResponse.*
import eu.europa.ec.eudi.verifier.endpoint.port.out.persistence.LoadPresentationById
import eu.europa.ec.eudi.verifier.endpoint.port.out.persistence.PresentationEvent
import eu.europa.ec.eudi.verifier.endpoint.port.out.persistence.PublishPresentationEvent
import eu.europa.ec.eudi.verifier.endpoint.port.out.web.GetStatusListClient
import eu.europa.ec.eudi.verifier.endpoint.port.out.web.StatusListWithBits
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import org.springframework.core.env.Environment
import java.time.Clock

/**
 * Represent the [WalletResponse] as returned by the wallet
 */
@Serializable
@SerialName("wallet_response")
data class WalletResponseTO(
    @SerialName("id_token") val idToken: String? = null,
    @SerialName("vp_token") val vpToken: JsonArray? = null,
    @SerialName("presentation_submission") val presentationSubmission: PresentationSubmission? = null,
    @SerialName("error") val error: String? = null,
    @SerialName("error_description") val errorDescription: String? = null,
    @SerialName("statuses") val statuses: List<DocumentStatusTO>? = null,
)

@Serializable
@SerialName("status_response")
data class DocumentStatusTO(
    @SerialName("document") val document: String,
    @SerialName("status") val status: Byte? = null,
    @SerialName("status_list") val statusList: StatusListWithBits,
)

@Serializable
@SerialName("attestation_document")
data class AttestationDocumentTO(
    @SerialName("docType") val docType: String? = null,
    @SerialName("attributes") val attributes: AttestationDocumentAttributesTO? = null,
)

@Serializable
@SerialName("attributes")
data class AttestationDocumentAttributesTO(
    @SerialName("eu.europa.ec.eudi.pid.1") val pidAttributes: AttestationDocumentPidAttributesTO? = null,
    @SerialName("org.iso.18013.5.1.mDL") val mdlAttributes: AttestationDocumentMdlAttributesTO? = null,
)

@Serializable
@SerialName("eu.europa.ec.eudi.pid.1")
data class AttestationDocumentPidAttributesTO(
    @SerialName("document_number") val documentNumber: String,
    @SerialName("status") val status: DocumentStatusResponse? = null,
)

@Serializable
@SerialName("org.iso.18013.5.1.mDL")
data class AttestationDocumentMdlAttributesTO(
    @SerialName("document_number") val documentNumber: String,
    @SerialName("status") val status: DocumentStatusResponse? = null,
)

@Serializable
@SerialName("status")
data class DocumentStatusResponse(
    @SerialName("status_list") val statusList: DocumentStatusListResponse? = null,
)

@Serializable
@SerialName("status_list")
data class DocumentStatusListResponse(
    @SerialName("idx") val idx: Int? = null,
    @SerialName("uri") val uri: String? = null,
)

internal suspend fun WalletResponse.toTO(getStatusListClient: GetStatusListClient? = null): WalletResponseTO {
    fun VerifiablePresentation.toJsonElement(): JsonElement =
        when (this) {
            is VerifiablePresentation.Generic -> JsonPrimitive(value)
            is VerifiablePresentation.Json -> value
        }

    return when (this) {
        is WalletResponse.IdToken -> WalletResponseTO(idToken = idToken)
        is WalletResponse.VpToken -> WalletResponseTO(
            vpToken = JsonArray(vpContent.verifiablePresentations().map { it.toJsonElement() }),
            statuses = when (getStatusListClient) {
                is GetStatusListClient -> formatStatuses(
                    vpContent.verifiablePresentations().map { it.toJsonElement() },
                    getStatusListClient,
                )
                else -> null
            },
            presentationSubmission = vpContent.presentationSubmissionOrNull(),
        )

        is WalletResponse.IdAndVpToken -> WalletResponseTO(
            idToken = idToken,
            vpToken = JsonArray(vpContent.verifiablePresentations().map { it.toJsonElement() }),
            statuses = (
                when (getStatusListClient) {
                    is GetStatusListClient -> formatStatuses(
                        vpContent.verifiablePresentations().map { it.toJsonElement() },
                        getStatusListClient,
                    )
                    else -> null
                }
                ),
            presentationSubmission = vpContent.presentationSubmissionOrNull(),
        )

        is WalletResponse.Error -> WalletResponseTO(
            error = value,
            errorDescription = description,
        )
    }
}

internal suspend fun formatStatuses(elements: List<JsonElement>, getStatusListClient: GetStatusListClient): List<DocumentStatusTO>? {
    return elements
        .map { element ->
            try {
                Json.decodeFromJsonElement<AttestationDocumentTO>(element)
            } catch (_: Exception) {
            }
        }
        .filterIsInstance<AttestationDocumentTO>()
        .filter { it.docType != null && it.attributes == null }
        .map {
            val pidAttributes = it.attributes?.pidAttributes
            val mdlAttributes = it.attributes?.mdlAttributes
            var documentNumber: String? = null
            var idx: Int? = null
            var uri: String? = null
            if (pidAttributes != null) {
                documentNumber = pidAttributes.documentNumber
                idx = pidAttributes.status?.statusList?.idx
                uri = pidAttributes.status?.statusList?.uri
            } else if (mdlAttributes != null) {
                documentNumber = mdlAttributes.documentNumber
                idx = mdlAttributes.status?.statusList?.idx
                uri = mdlAttributes.status?.statusList?.uri
            }

            if (documentNumber != null && idx != null && uri != null) {
                when (val statusListWithBits = getStatusListClient.doInvoke(uri)) {
                    is StatusListWithBits -> DocumentStatusTO(
                        documentNumber,
                        statusListWithBits.statusList.getOrNull(idx),
                        statusListWithBits,
                    )

                    else -> null
                }
            } else {
                null
            }
        }
        .filterNotNull()
        .toList()
}

/**
 * Given a [TransactionId] and a [Nonce] returns the [WalletResponse]
 */
fun interface GetWalletResponse {
    suspend operator fun invoke(
        transactionId: TransactionId,
        responseCode: ResponseCode?,
    ): QueryResponse<WalletResponseTO>
}

class GetWalletResponseLive(
    private val clock: Clock,
    private val loadPresentationById: LoadPresentationById,
    private val publishPresentationEvent: PublishPresentationEvent,
    private val getStatusListClient: GetStatusListClient,
    private val environment: Environment,
) : GetWalletResponse {
    override suspend fun invoke(
        transactionId: TransactionId,
        responseCode: ResponseCode?,
    ): QueryResponse<WalletResponseTO> {
        return when (val presentation = loadPresentationById(transactionId)) {
            null -> NotFound
            is Presentation.Submitted ->
                when (responseCode) {
                    presentation.responseCode -> found(presentation)
                    else -> responseCodeMismatch(presentation, responseCode)
                }

            else -> invalidState(presentation)
        }
    }

    private suspend fun found(presentation: Presentation.Submitted): Found<WalletResponseTO> {
        val walletResponse = when (environment.getProperty("statusList.enabled", "true").toBooleanStrictOrNull()) {
            true -> presentation.walletResponse.toTO(getStatusListClient)
            else -> presentation.walletResponse.toTO()
        }

        logVerifierGotWalletResponse(presentation, walletResponse)
        return Found(walletResponse)
    }

    private suspend fun responseCodeMismatch(
        presentation: Presentation.Submitted,
        responseCode: ResponseCode?,
    ): InvalidState {
        fun ResponseCode?.txt() = this?.let { value } ?: "N/A"
        val cause =
            "Invalid response_code. " +
                "Expected: ${presentation.responseCode.txt()}, " +
                "Provided ${responseCode.txt()}"
        logVerifierFailedToGetWalletResponse(presentation, cause)
        return InvalidState
    }

    private suspend fun invalidState(presentation: Presentation): InvalidState {
        val cause = "Presentation should be in Submitted state but is in ${presentation.javaClass.name}"
        logVerifierFailedToGetWalletResponse(presentation, cause)
        return InvalidState
    }

    private suspend fun logVerifierGotWalletResponse(
        presentation: Presentation.Submitted,
        walletResponse: WalletResponseTO,
    ) {
        val event = PresentationEvent.VerifierGotWalletResponse(presentation.id, clock.instant(), walletResponse)
        publishPresentationEvent(event)
    }

    private suspend fun logVerifierFailedToGetWalletResponse(
        presentation: Presentation,
        cause: String,
    ) {
        val event = PresentationEvent.VerifierFailedToGetWalletResponse(presentation.id, clock.instant(), cause)
        publishPresentationEvent(event)
    }
}
