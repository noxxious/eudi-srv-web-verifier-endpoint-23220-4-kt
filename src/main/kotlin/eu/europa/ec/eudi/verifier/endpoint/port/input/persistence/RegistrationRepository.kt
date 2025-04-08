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
package eu.europa.ec.eudi.verifier.endpoint.port.input.persistence

import com.google.cloud.firestore.FirestoreOptions
import eu.europa.ec.eudi.verifier.endpoint.domain.TransactionId
import eu.europa.ec.eudi.verifier.endpoint.port.input.RegistrationDataTO

interface RegistrationRepositoryObject {
    fun findByTransactionId(transactionId: TransactionId): Map<String, Any>
    fun saveRegistrationData(registrationData: RegistrationDataTO, transactionId: TransactionId): Map<String, String?>
    fun updateStatus(status: String, transactionId: TransactionId): Map<String, Any>
}

class RegistrationRepository(
    private val firestoreOptions: FirestoreOptions,
    private val collectionName: String,
) : RegistrationRepositoryObject {

    override fun findByTransactionId(transactionId: TransactionId): Map<String, Any> {
        val db = firestoreOptions.service

        val docRef = db.collection(collectionName).document(transactionId.value)
        val data = docRef
            .get()
            .get()
            .data ?: error("Transaction $collectionName:$transactionId not found")

        return data
    }

    override fun saveRegistrationData(registrationData: RegistrationDataTO, transactionId: TransactionId): Map<String, String?> {
        val db = firestoreOptions.service
        val docRef = db.collection(collectionName).document(transactionId.value)

        val data = mapOf(
            "readerCountry" to registrationData.readerCountry,
            "readerCompanyName" to registrationData.readerCompanyName,
            "holderTesterInitials" to registrationData.holderTesterInitials,
            "holderDevice" to registrationData.holderDevice,
            "dataset" to registrationData.dataset,
            "testScenario" to registrationData.testScenario,
        )

        docRef.set(data)
        return data
    }

    override fun updateStatus(status: String, transactionId: TransactionId): Map<String, Any> {
        val db = firestoreOptions.service
        val docRef = db.collection(collectionName).document(transactionId.value)

        val data = mapOf("status" to status)

        docRef.update(data)
        return docRef.get().get().data ?: error("Transaction $collectionName:$transactionId not found")
    }
}
