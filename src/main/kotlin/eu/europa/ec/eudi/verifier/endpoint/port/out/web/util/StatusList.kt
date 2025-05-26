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
package eu.europa.ec.eudi.verifier.endpoint.port.out.web.util

import com.nimbusds.jose.util.Base64URL
import com.upokecenter.cbor.CBORObject
import java.lang.Integer.parseInt
import java.lang.Math.floor
import java.util.zip.Deflater
import java.util.zip.DeflaterInputStream
import java.util.zip.InflaterInputStream
import kotlin.experimental.and
import kotlin.experimental.inv
import kotlin.experimental.or

/**
 * Construct a new status list.
 *
 * @param size number of entities in the bit array
 * @param bits number of bits per entry
 * @param defaultValue default Value for new empty status list
 * @param list pre-initialized array for the status list
 *
 * Either use defaultValue or list.
 */
class StatusList(size: Int, bits: Int, defaultValue: Byte? = null, list: ByteArray? = null) {
    init {
        require(bits == 1 || bits == 2 || bits == 4 || bits == 8) {
            "The allowed values for bits are 1,2,4 and 8."
        }
        require(size > 0 && size % (8 / bits) == 0) {
            "The size must be greater than 0 and fit to byte size"
        }
        require(defaultValue == null || list == null) {
            "Either set default value for new list or import list from byte array"
        }
        require(list == null || list.size == size / (8 / bits)) {
            "The size of the pre-initialized byte array must fit the status list size"
        }
    }

    // bits per status for each Referenced Token
    private val bits = bits

    // size of the status list (this is not the size of the byte array)
    private val size = size

    // status list containing all status bits, initialized provided list
    private val list = list ?: ByteArray(size / (8 / bits))

    // bitmask with number of bits per status set to 1
    private val mask = ((1 shl bits) - 1).toByte()

    init {
        if (defaultValue != null) {
            this.list.fill(defaultValue)
        }
    }

    companion object {

        val CBOR_AGGREGATION_URI_CLAIM = CBORObject.FromObject("aggregation_uri")
        val CBOR_BITS_CLAIM = CBORObject.FromObject("bits")
        val CBOR_LIST_CLAIM = CBORObject.FromObject("lst")

        /**
         * Construct a status list from a status list token.
         *
         * @param bits number of bits per entry
         * @param encodedList the base64url encoded, compressed status list
         */
        @OptIn(ExperimentalUnsignedTypes::class)
        fun fromEncoded(bits: Int, encodedList: String): StatusList {
            val compressed = Base64URL.from(encodedList).decode()

            val byteArray = InflaterInputStream(compressed.inputStream()).use { it.readBytes() }

            val statusListOrdered = orderStatusListBytes(byteArray.toUByteArray(), bits)

            return StatusList(statusListOrdered.size * (8 / bits), bits, list = statusListOrdered)
        }

        fun fromCbor(sl: CBORObject): StatusList {
            val bits = sl[CBOR_BITS_CLAIM].AsInt32Value()
            val byteArray =
                InflaterInputStream(sl[CBOR_LIST_CLAIM].GetByteString().inputStream()).use {
                    it.readBytes()
                }
            return StatusList(byteArray.size * (8 / bits), bits, list = byteArray)
        }

        @OptIn(ExperimentalUnsignedTypes::class)
        fun orderStatusListBytes(byteArray: UByteArray, bitsPerStatus: Int): ByteArray {
            val numBits = bitsPerStatus
            val totalStatuses = (byteArray.size * 8) / numBits
            val statusList = ByteArray(totalStatuses)
            var bitIndex = 0
            for (i in 0..<totalStatuses) {
                val byte = byteArray[floor(((i * numBits) / 8).toDouble()).toInt()]
                var byteString = byte.toString(radix = 2)
                if (byteString.length < 8) {
                    byteString = "0".repeat(8 - byteString.length) + byteString
                }
                val status = byteString.slice(bitIndex until bitIndex + numBits)
                val group = floor((i / (8 / numBits)).toDouble())
                val indexInGroup = i % (8 / numBits)
                val position = (group * (8 / numBits)).toInt() + (8 / numBits + -1 - indexInGroup)
                statusList[position] = parseInt(status, 2).toByte()
                bitIndex = (bitIndex + numBits) % 8
            }
            return statusList
        }
    }

    fun set(index: Int, value: Byte) {
        require(index in 0 until size) { "index is not a valid value" }
        require(value or mask == mask) { "value is exceeding the bits of this status list" }

        val byteIndex = index * bits / 8
        val shift = index * bits % 8
        val oldValue = list[byteIndex] and (mask shl shift).inv()
        val newValue = value shl shift
        list[byteIndex] = oldValue or newValue
    }

    fun getList() = list

    private fun getCompressed() =
        DeflaterInputStream(list.inputStream(), Deflater(Deflater.BEST_COMPRESSION)).use {
            it.readBytes()
        }

    fun getEncoded() = Base64URL.encode(getCompressed()).toString()

    fun get(index: Int): Byte {
        require(index in 0 until size) { "index is not a valid value" }

        val byteIndex = index * bits / 8
        val shift = index * bits % 8

        return (list[byteIndex].toUInt().and(mask.toUInt().shl(shift))).shr(shift).toByte()
    }

    private infix fun Byte.shl(shift: Int): Byte = this.toInt().shl(shift).toByte()

    fun toJsonObject(aggregationUri: String? = null): Map<String, Any> {
        val result = mutableMapOf("bits" to bits, "lst" to getEncoded())
        aggregationUri?.let { result["aggregation_uri"] = aggregationUri }
        return result.toMap()
    }

    fun toCborObject(aggregationUri: String? = null): CBORObject {
        val cbor = CBORObject.NewMap()
        cbor[CBOR_BITS_CLAIM] = CBORObject.FromObject(bits)
        cbor[CBOR_LIST_CLAIM] = CBORObject.FromObject(getCompressed())
        aggregationUri?.let {
            cbor[CBOR_AGGREGATION_URI_CLAIM] = CBORObject.FromObject(aggregationUri)
        }
        return cbor
    }

    override fun toString(): String {
        return "size: $size; bits: $bits; list: ${list.contentToString()}"
    }
}
