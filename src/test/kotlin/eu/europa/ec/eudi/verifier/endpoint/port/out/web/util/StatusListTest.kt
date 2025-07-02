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
@file:Suppress("invisible_reference", "invisible_member")

package eu.europa.ec.eudi.verifier.endpoint.port.out.web.util

import kotlin.test.Test
import kotlin.test.assertEquals

class StatusListTest {

    @Test
    fun `Decode string into correct status bytes using a 1 bit Status List, short`() {
        val bits = 1
        val token = "eNrbuRgAAhcBXQ"

        val indicesOfStatus1 = listOf(0, 3, 4, 5, 7, 8, 9, 13, 15)

        val list = StatusList.fromEncoded(bits, token).getList()

        assertEquals(16, list.size)
        assertEquals(9, list.filter { it == 1.toByte() }.size)
        assertEquals(7, list.filter { it == 0.toByte() }.size)

        for (i in list.indices) {
            if (indicesOfStatus1.contains(i)) {
                assertEquals(1, list[i])
            } else {
                assertEquals(0, list[i])
            }
        }
    }

    @Test
    fun `Decode string into correct status bytes using a 1 bit Status List`() {
        val bits = 1
        val token = "eNrt3AENwCAMAEGogklACtKQPg9LugC9k_ACvreiogE\n" +
            "AAKkeCQAAAAAAAAAAAAAAAAAAAIBylgQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\n" +
            "AAAAAAAAAAAAAAAAAAAXG9IAAAAAAAAAPwsJAAAAAAAAAAAAAAAvhsSAAAAAAAAAAA\n" +
            "A7KpLAAAAAAAAAAAAAAAAAAAAAJsLCQAAAAAAAAAAADjelAAAAAAAAAAAKjDMAQAAA\n" +
            "ACAZC8L2AEb"

        val indicesOfStatus1 = listOf(0, 1993, 25460, 159495, 495669, 554353, 645645, 723232, 854545, 934534, 1000345)

        val list = StatusList.fromEncoded(bits, token).getList()

        assertEquals(1048576, list.size)
        assertEquals(11, list.filter { it == 1.toByte() }.size)
        assertEquals(1048565, list.filter { it == 0.toByte() }.size)

        for (i in list.indices) {
            if (indicesOfStatus1.contains(i)) {
                assertEquals(1, list[i])
            } else {
                assertEquals(0, list[i])
            }
        }
    }

    @Test
    fun `Decode string into correct status bytes using a 2 bit Status List, short`() {
        val bits = 2
        val token = "eNo76fITAAPfAgc"

        val indicesOfStatus1 = listOf(0, 5, 7, 8)
        val indicesOfStatus2 = listOf(1, 9)
        val indicesOfStatus3 = listOf(3, 10, 11)

        val list = StatusList.fromEncoded(bits, token).getList()

        assertEquals(12, list.size)
        assertEquals(4, list.filter { it == 1.toByte() }.size)
        assertEquals(2, list.filter { it == 2.toByte() }.size)
        assertEquals(3, list.filter { it == 3.toByte() }.size)
        assertEquals(3, list.filter { it == 0.toByte() }.size)

        for (i in list.indices) {
            if (indicesOfStatus1.contains(i)) {
                assertEquals(1, list[i])
            } else if (indicesOfStatus2.contains(i)) {
                assertEquals(2, list[i])
            } else if (indicesOfStatus3.contains(i)) {
                assertEquals(3, list[i])
            } else {
                assertEquals(0, list[i])
            }
        }
    }

    @Test
    fun `Decode string into correct status bytes using a 2 bit Status List`() {
        val bits = 2
        val token = "eNrt2zENACEQAEEuoaBABP5VIO01fCjIHTMStt9ovGV\n" +
            "IAAAAAABAbiEBAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAEB5WwIAAAAAA\n" +
            "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\n" +
            "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAID0ugQAAAAAAAAAAAAAAAAAQG12SgAAA\n" +
            "AAAAAAAAAAAAAAAAAAAAAAAAOCSIQEAAAAAAAAAAAAAAAAAAAAAAAD8ExIAAAAAAAA\n" +
            "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAwJEuAQAAAAAAAAAAAAAAAAAAAAAAAMB9S\n" +
            "wIAAAAAAAAAAAAAAAAAAACoYUoAAAAAAAAAAAAAAEBqH81gAQw"

        val indicesOfStatus1 = listOf(0, 25460, 495669, 554353, 723232, 854545)
        val indicesOfStatus2 = listOf(1993, 645645, 934534)
        val indicesOfStatus3 = listOf(159495, 1000345)

        val list = StatusList.fromEncoded(bits, token).getList()

        assertEquals(1048576, list.size)
        assertEquals(6, list.filter { it == 1.toByte() }.size)
        assertEquals(3, list.filter { it == 2.toByte() }.size)
        assertEquals(2, list.filter { it == 3.toByte() }.size)
        assertEquals(1048565, list.filter { it == 0.toByte() }.size)

        for (i in list.indices) {
            if (indicesOfStatus1.contains(i)) {
                assertEquals(1, list[i])
            } else if (indicesOfStatus2.contains(i)) {
                assertEquals(2, list[i])
            } else if (indicesOfStatus3.contains(i)) {
                assertEquals(3, list[i])
            } else {
                assertEquals(0, list[i])
            }
        }
    }

    @Test
    fun `Decode string into correct status bytes using a 4 bit Status List`() {
        val bits = 4
        val token = "eNrt0EENgDAQADAIHwImkIIEJEwCUpCEBBQRHOy35Li\n" +
            "1EjoOQGabAgAAAAAAAAAAAAAAAAAAACC1SQEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\n" +
            "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\n" +
            "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\n" +
            "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\n" +
            "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABADrsCAAAAAAAAAAAAAAAAA\n" +
            "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\n" +
            "AAADoxaEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\n" +
            "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAIIoCgAAAAAAAAAAAAAAAAA\n" +
            "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAACArpwKAAAAAAAAAAAAAAAAAAAAA\n" +
            "AAAAAAAAAAAAAAAAAAAAAAAAAAAAGhqVkAzlwIAAAAAiGVRAAAAAAAAAAAAAAAAAAA\n" +
            "AAAAAAAAAAAAAAAAAAAAAAABx3AoAgLpVAQAAAAAAAAAAAAAAwM89rwMAAAAAAAAAA\n" +
            "AjsA9xMBMA"

        val indicesOfStatus1 = listOf(0)
        val indicesOfStatus2 = listOf(1993)
        val indicesOfStatus3 = listOf(35460)
        val indicesOfStatus4 = listOf(459495)
        val indicesOfStatus5 = listOf(595669)
        val indicesOfStatus6 = listOf(754353)
        val indicesOfStatus7 = listOf(845645)
        val indicesOfStatus8 = listOf(923232)
        val indicesOfStatus9 = listOf(924445)
        val indicesOfStatus10 = listOf(934534)
        val indicesOfStatus11 = listOf(1004534)
        val indicesOfStatus12 = listOf(1000345)
        val indicesOfStatus13 = listOf(1030203)
        val indicesOfStatus14 = listOf(1030204)
        val indicesOfStatus15 = listOf(1030205)

        val list = StatusList.fromEncoded(bits, token).getList()

        assertEquals(1048576, list.size)
        assertEquals(1, list.filter { it == 1.toByte() }.size)
        assertEquals(1, list.filter { it == 2.toByte() }.size)
        assertEquals(1, list.filter { it == 3.toByte() }.size)
        assertEquals(1, list.filter { it == 4.toByte() }.size)
        assertEquals(1, list.filter { it == 5.toByte() }.size)
        assertEquals(1, list.filter { it == 6.toByte() }.size)
        assertEquals(1, list.filter { it == 7.toByte() }.size)
        assertEquals(1, list.filter { it == 8.toByte() }.size)
        assertEquals(1, list.filter { it == 9.toByte() }.size)
        assertEquals(1, list.filter { it == 10.toByte() }.size)
        assertEquals(1, list.filter { it == 11.toByte() }.size)
        assertEquals(1, list.filter { it == 12.toByte() }.size)
        assertEquals(1, list.filter { it == 13.toByte() }.size)
        assertEquals(1, list.filter { it == 14.toByte() }.size)
        assertEquals(1, list.filter { it == 15.toByte() }.size)
        assertEquals(1048561, list.filter { it == 0.toByte() }.size)

        for (i in list.indices) {
            if (indicesOfStatus1.contains(i)) {
                assertEquals(1, list[i])
            } else if (indicesOfStatus2.contains(i)) {
                assertEquals(2, list[i])
            } else if (indicesOfStatus3.contains(i)) {
                assertEquals(3, list[i])
            } else if (indicesOfStatus4.contains(i)) {
                assertEquals(4, list[i])
            } else if (indicesOfStatus5.contains(i)) {
                assertEquals(5, list[i])
            } else if (indicesOfStatus6.contains(i)) {
                assertEquals(6, list[i])
            } else if (indicesOfStatus7.contains(i)) {
                assertEquals(7, list[i])
            } else if (indicesOfStatus8.contains(i)) {
                assertEquals(8, list[i])
            } else if (indicesOfStatus9.contains(i)) {
                assertEquals(9, list[i])
            } else if (indicesOfStatus10.contains(i)) {
                assertEquals(10, list[i])
            } else if (indicesOfStatus11.contains(i)) {
                assertEquals(11, list[i])
            } else if (indicesOfStatus12.contains(i)) {
                assertEquals(12, list[i])
            } else if (indicesOfStatus13.contains(i)) {
                assertEquals(13, list[i])
            } else if (indicesOfStatus14.contains(i)) {
                assertEquals(14, list[i])
            } else if (indicesOfStatus15.contains(i)) {
                assertEquals(15, list[i])
            } else {
                assertEquals(0, list[i])
            }
        }
    }

    @Test
    fun `Decode string into correct status bytes using a 8 bit Status List`() {
        val bits = 8
        val token =
            "\"eNrt0WOQM2kYhtGsbdu2bdu2bdu2bdu2bdu2jVnU1my\n" +
                "-SWYm6U5enFPVf7ue97orFYAo7CQBAACQuuckAABStqUEAAAAAAAAtN6wEgAE71QJA\n" +
                "AAAAIrwhwQAAAAAAdtAAgAAAAAAACLwkAQAAAAAAAAAAACUaFcJAACAeJwkAQAAAAA\n" +
                "AAABQvL4kAAAAWmJwCQAAAAAAAAjAwBIAAAB06ywJoDKQBARpfgkAAAAAAAAAAAAAA\n" +
                "AAAAACo50sJAAAAAAAAAOiRcSQAAAAAgAJNKgEAAG23mgQAAAAAAECw3pUAQvegBAA\n" +
                "AAAAAAADduE4CAAAAyjSvBAAQiw8koHjvSABAb-wlARCONyVoxtMSZOd0CQAAAOjWD\n" +
                "RKQmLckAAAAAACysLYEQGcnSAAAAAAQooUlAABI15kSAIH5RAIgLB9LABC4_SUgGZN\n" +
                "IAABAmM6RoLbTJIASzCIBAEAhfpcAAAAAAABquk8CAAAAAAAAaJl9SvvzBOICAFWmk\n" +
                "IBgfSgBAAAANOgrCQAAAAAAAADStK8EAAC03gASAAAAAAAAAADFWFUCAAAAMjOaBEA\n" +
                "DHpYAQjCIBADduFwCAAAAAGitMSSI3BUSAECOHpAA6IHrJQAAAAAAsjeVBAAAKRpVA\n" +
                "orWvwQAAAAAAAAAkKRtJAAAAAAAgCbcLAF0bXUJAAAAoF02kYDg7CYBAAAAAEB6NpQ\n" +
                "AAAAAAAAAAAAAAEr1uQQAAF06VgIAAAAAAAAAqDaeBAAQqgMkAAAAAABogQMlAAAAA\n" +
                "AAa87MEAAAQiwslAAAAAAAAAAAAAAAAMrOyBAAAiekv-hcsY0Sgne6QAAAAAAAgaUt\n" +
                "JAAAAAAAAAAAAAAAAAAAAAAAAAADwt-07vjVkAAAAgDy8KgFAUEaSAAAAAJL3vgQAW\n" +
                "dhcAgAAoBHDSUDo1pQAAACI2o4SAABZm14CALoyuwQAAPznGQkgZwdLAAAQukclAAA\n" +
                "AAAAAAAAAgKbMKgEAAAAAAAAAAAAAAAAAAECftpYAAAAAAAAAAAAACnaXBAAAAADk7\n" +
                "iMJAAAAAAAAAABqe00CAnGbBBG4TAIAgFDdKgFAXCaWAAAAAAAAAAAAAAAAAKAJQwR\n" +
                "72XbGAQAAAKAhh0sAAAAAAABQgO8kAAAAAAAAAAAAACAaM0kAAAC5W0QCAIJ3mAQAx\n" +
                "GwxCQAA6nhSAsjZBRIAANEbWQIAAAAAaJE3JACAwA0qAUBIVpKAlphbAiAPp0iQnKE\n" +
                "kAAAAAAAgBP1KAAAAdOl4CQAAAAAAAPjLZBIAAG10RtrPm8_CAEBMTpYAAAAAAIjQY\n" +
                "BL8z5QSAAAAAEDYPpUAACAsj0gAAADQkHMlAAjHDxIA0Lg9JQAAgHDsLQEAAABAQS6\n" +
                "WAAAAgLjNFs2l_RgLAIAEfCEBlGZZCQAAaIHjJACgtlskAAAozb0SAAAAVFtfAgAAA\n" +
                "AAAAAAAAAAAAAAAAAAAAKDDtxIAAAAAVZaTAKB5W0kAANCAsSUgJ0tL0GqHSNBbL0g\n" +
                "AZflRAgCARG0kQXNmlgCABiwkAQAAAEB25pIAAAAAAAAAAAAAoFh9SwAAAAAAADWNm\n" +
                "OSrpjFsEoaRgDKcF9Q1dxsEAAAAAAAAAAAAAAAAgPZ6SQIAAAAAAAAAgChMLgEAAAA\n" +
                "AAAAAqZlQAsK2qQQAAAAAAAD06XUJAAAAqG9bCQAAgLD9IgEAAAAAAAAAAAAAAAAAA\n" +
                "EBNe0gAAAAAAAAAAEBPHSEBAAAAlOZtCYA4fS8B0GFRCQAo0gISAOTgNwmC840EAAA\n" +
                "AAAAAAAAAAAAAAAAAUJydJfjXPBIAAAAAAAAAAAAAAABk6WwJAAAAAAAAAAAAAAAAq\n" +
                "G8UCQAAgPpOlAAAIA83SQAANWwc9HUjGAgAAAAAAACAusaSAAAAAAAAAAAAAAAAAAA\n" +
                "AAAAAAAAAqHKVBACQjxklAAAAAAAAAKBHxpQAAAAAACBME0lAdlaUAACyt7sEAAAA0\n" +
                "Nl0EgAAAAAAAAAAAABA-8wgAQAAAAAAAKU4SgKgUtlBAgAAAAAAAAAAgMCMLwEE51k\n" +
                "JICdzSgCJGl2CsE0tAQAA0L11JQAAAAAAAAjUOhIAAAAAAAAAAAAAAGTqeQkAAAAAA\n" +
                "AAAAAAAKM8SEjTrJwkAAAAAAACocqQEULgVJAAAACjDUxJUKgtKAAAAqbpRAgCA0n0\n" +
                "mAQAAAABAGzwmAUCTLpUAAAAAAAAAAEjZNRIAAAAAAAAAAAAAAAAAAAAA8I-vJaAlh\n" +
                "pQAAAAAAHrvzjJ-OqCuuVlLAojP8BJAr70sQZVDJYAgXS0BAAAAAAAAAAAAtMnyEgA\n" +
                "AAAAAFONKCQAAAAAAAADorc0kAAAAAAAAgDqOlgAAAAAAAAAAAADIwv0SAAAAAAAAA\n" +
                "AAAAADBuV0CIFVDSwAAAABAAI6RAAAAAGIwrQSEZAsJAABouRclAAAAAKDDrxIAAAA\n" +
                "0bkkJgFiMKwEAAAAAAHQyhwRk7h4JAAAAAAAAAAAgatdKAACUYj0JAAAAAAAAAAAAQ\n" +
                "nORBLTFJRIAAAAAkIaDJAAAAJryngQAAAAAAAAAAAA98oQEAAAAAAAAAEC2zpcgWY9\n" +
                "LQKL2kwAgGK9IAAAAAPHaRQIAAAAAAAAAAADIxyoSAAAAAAAAAAAAAADQFotLAECz_\n" +
                "gQ1PX-B\""

        class IndexAndValue(index: Int, value: Int) {
            val index: Int = index
                get() {
                    return field
                }
            val value = value
                get() {
                    return field
                }
        }

        val indicesAndValues = listOf<IndexAndValue>(
            IndexAndValue(233478, 0),
            IndexAndValue(52451, 1),
            IndexAndValue(576778, 2),
            IndexAndValue(513575, 3),
            IndexAndValue(468106, 4),
            IndexAndValue(292632, 5),
            IndexAndValue(214947, 6),
            IndexAndValue(182323, 7),
            IndexAndValue(884834, 8),
            IndexAndValue(66653, 9),
            IndexAndValue(62489, 10),
            IndexAndValue(196493, 11),
            IndexAndValue(458517, 12),
            IndexAndValue(487925, 13),
            IndexAndValue(55649, 14),
            IndexAndValue(416992, 15),
            IndexAndValue(879796, 16),
            IndexAndValue(462297, 17),
            IndexAndValue(942059, 18),
            IndexAndValue(583408, 19),
            IndexAndValue(13628, 20),
            IndexAndValue(334829, 21),
            IndexAndValue(886286, 22),
            IndexAndValue(713557, 23),
            IndexAndValue(582738, 24),
            IndexAndValue(326064, 25),
            IndexAndValue(451545, 26),
            IndexAndValue(705889, 27),
            IndexAndValue(214350, 28),
            IndexAndValue(194502, 29),
            IndexAndValue(796765, 30),
            IndexAndValue(202828, 31),
            IndexAndValue(752834, 32),
            IndexAndValue(721327, 33),
            IndexAndValue(554740, 34),
            IndexAndValue(91122, 35),
            IndexAndValue(963483, 36),
            IndexAndValue(261779, 37),
            IndexAndValue(793844, 38),
            IndexAndValue(165255, 39),
            IndexAndValue(614839, 40),
            IndexAndValue(758403, 41),
            IndexAndValue(403258, 42),
            IndexAndValue(145867, 43),
            IndexAndValue(96100, 44),
            IndexAndValue(477937, 45),
            IndexAndValue(606890, 46),
            IndexAndValue(167335, 47),
            IndexAndValue(488197, 48),
            IndexAndValue(211815, 49),
            IndexAndValue(797182, 50),
            IndexAndValue(582952, 51),
            IndexAndValue(950870, 52),
            IndexAndValue(765108, 53),
            IndexAndValue(341110, 54),
            IndexAndValue(776325, 55),
            IndexAndValue(745056, 56),
            IndexAndValue(439368, 57),
            IndexAndValue(559893, 58),
            IndexAndValue(149741, 59),
            IndexAndValue(358903, 60),
            IndexAndValue(513405, 61),
            IndexAndValue(342679, 62),
            IndexAndValue(969429, 63),
            IndexAndValue(795775, 64),
            IndexAndValue(566121, 65),
            IndexAndValue(460566, 66),
            IndexAndValue(680070, 67),
            IndexAndValue(117310, 68),
            IndexAndValue(480348, 69),
            IndexAndValue(67319, 70),
            IndexAndValue(661552, 71),
            IndexAndValue(841303, 72),
            IndexAndValue(561493, 73),
            IndexAndValue(138807, 74),
            IndexAndValue(442463, 75),
            IndexAndValue(659927, 76),
            IndexAndValue(445910, 77),
            IndexAndValue(1046963, 78),
            IndexAndValue(829700, 79),
            IndexAndValue(962282, 80),
            IndexAndValue(299623, 81),
            IndexAndValue(555493, 82),
            IndexAndValue(292826, 83),
            IndexAndValue(517215, 84),
            IndexAndValue(551009, 85),
            IndexAndValue(898490, 86),
            IndexAndValue(837603, 87),
            IndexAndValue(759161, 88),
            IndexAndValue(459948, 89),
            IndexAndValue(290102, 90),
            IndexAndValue(1034977, 91),
            IndexAndValue(190650, 92),
            IndexAndValue(98810, 93),
            IndexAndValue(229950, 94),
            IndexAndValue(320531, 95),
            IndexAndValue(335506, 96),
            IndexAndValue(885333, 97),
            IndexAndValue(133227, 98),
            IndexAndValue(806915, 99),
            IndexAndValue(800313, 100),
            IndexAndValue(981571, 101),
            IndexAndValue(527253, 102),
            IndexAndValue(24077, 103),
            IndexAndValue(240232, 104),
            IndexAndValue(559572, 105),
            IndexAndValue(713399, 106),
            IndexAndValue(233941, 107),
            IndexAndValue(615514, 108),
            IndexAndValue(911768, 109),
            IndexAndValue(331680, 110),
            IndexAndValue(951527, 111),
            IndexAndValue(6805, 112),
            IndexAndValue(552366, 113),
            IndexAndValue(374660, 114),
            IndexAndValue(223159, 115),
            IndexAndValue(625884, 116),
            IndexAndValue(417146, 117),
            IndexAndValue(320527, 118),
            IndexAndValue(784154, 119),
            IndexAndValue(338792, 120),
            IndexAndValue(1199, 121),
            IndexAndValue(679804, 122),
            IndexAndValue(1024680, 123),
            IndexAndValue(40845, 124),
            IndexAndValue(234603, 125),
            IndexAndValue(761225, 126),
            IndexAndValue(644903, 127),
            IndexAndValue(502167, 128),
            IndexAndValue(121477, 129),
            IndexAndValue(505144, 130),
            IndexAndValue(165165, 131),
            IndexAndValue(179628, 132),
            IndexAndValue(1019195, 133),
            IndexAndValue(145149, 134),
            IndexAndValue(263738, 135),
            IndexAndValue(269256, 136),
            IndexAndValue(996739, 137),
            IndexAndValue(346296, 138),
            IndexAndValue(555864, 139),
            IndexAndValue(887384, 140),
            IndexAndValue(444173, 141),
            IndexAndValue(421844, 142),
            IndexAndValue(653716, 143),
            IndexAndValue(836747, 144),
            IndexAndValue(783119, 145),
            IndexAndValue(918762, 146),
            IndexAndValue(946835, 147),
            IndexAndValue(253764, 148),
            IndexAndValue(519895, 149),
            IndexAndValue(471224, 150),
            IndexAndValue(134272, 151),
            IndexAndValue(709016, 152),
            IndexAndValue(44112, 153),
            IndexAndValue(482585, 154),
            IndexAndValue(461829, 155),
            IndexAndValue(15080, 156),
            IndexAndValue(148883, 157),
            IndexAndValue(123467, 158),
            IndexAndValue(480125, 159),
            IndexAndValue(141348, 160),
            IndexAndValue(65877, 161),
            IndexAndValue(692958, 162),
            IndexAndValue(148598, 163),
            IndexAndValue(499131, 164),
            IndexAndValue(584009, 165),
            IndexAndValue(1017987, 166),
            IndexAndValue(449287, 167),
            IndexAndValue(277478, 168),
            IndexAndValue(991262, 169),
            IndexAndValue(509602, 170),
            IndexAndValue(991896, 171),
            IndexAndValue(853666, 172),
            IndexAndValue(399318, 173),
            IndexAndValue(197815, 174),
            IndexAndValue(203278, 175),
            IndexAndValue(903979, 176),
            IndexAndValue(743015, 177),
            IndexAndValue(888308, 178),
            IndexAndValue(862143, 179),
            IndexAndValue(979421, 180),
            IndexAndValue(113605, 181),
            IndexAndValue(206397, 182),
            IndexAndValue(127113, 183),
            IndexAndValue(844358, 184),
            IndexAndValue(711569, 185),
            IndexAndValue(229153, 186),
            IndexAndValue(521470, 187),
            IndexAndValue(401793, 188),
            IndexAndValue(398896, 189),
            IndexAndValue(940810, 190),
            IndexAndValue(293983, 191),
            IndexAndValue(884749, 192),
            IndexAndValue(384802, 193),
            IndexAndValue(584151, 194),
            IndexAndValue(970201, 195),
            IndexAndValue(523882, 196),
            IndexAndValue(158093, 197),
            IndexAndValue(929312, 198),
            IndexAndValue(205329, 199),
            IndexAndValue(106091, 200),
            IndexAndValue(30949, 201),
            IndexAndValue(195586, 202),
            IndexAndValue(495723, 203),
            IndexAndValue(348779, 204),
            IndexAndValue(852312, 205),
            IndexAndValue(1018463, 206),
            IndexAndValue(1009481, 207),
            IndexAndValue(448260, 208),
            IndexAndValue(841042, 209),
            IndexAndValue(122967, 210),
            IndexAndValue(345269, 211),
            IndexAndValue(794764, 212),
            IndexAndValue(4520, 213),
            IndexAndValue(818773, 214),
            IndexAndValue(556171, 215),
            IndexAndValue(954221, 216),
            IndexAndValue(598210, 217),
            IndexAndValue(887110, 218),
            IndexAndValue(1020623, 219),
            IndexAndValue(324632, 220),
            IndexAndValue(398244, 221),
            IndexAndValue(622241, 222),
            IndexAndValue(456551, 223),
            IndexAndValue(122648, 224),
            IndexAndValue(127837, 225),
            IndexAndValue(657676, 226),
            IndexAndValue(119884, 227),
            IndexAndValue(105156, 228),
            IndexAndValue(999897, 229),
            IndexAndValue(330160, 230),
            IndexAndValue(119285, 231),
            IndexAndValue(168005, 232),
            IndexAndValue(389703, 233),
            IndexAndValue(143699, 234),
            IndexAndValue(142524, 235),
            IndexAndValue(493258, 236),
            IndexAndValue(846778, 237),
            IndexAndValue(251420, 238),
            IndexAndValue(516351, 239),
            IndexAndValue(83344, 240),
            IndexAndValue(171931, 241),
            IndexAndValue(879178, 242),
            IndexAndValue(663475, 243),
            IndexAndValue(546865, 244),
            IndexAndValue(428362, 245),
            IndexAndValue(658891, 246),
            IndexAndValue(500560, 247),
            IndexAndValue(557034, 248),
            IndexAndValue(830023, 249),
            IndexAndValue(274471, 250),
            IndexAndValue(629139, 251),
            IndexAndValue(958869, 252),
            IndexAndValue(663071, 253),
            IndexAndValue(152133, 254),
            IndexAndValue(19535, 255),
        )

        val list = StatusList.fromEncoded(bits, token).getList()

        assertEquals(1048576, list.size)
        for (i in 1..255) {
            assertEquals(1, list.filter { it == i.toByte() }.size, "Unexpected result for for i = $i")
        }
        assertEquals(1048321, list.filter { it == 0.toByte() }.size)

        for (i in list.indices) {
            if (indicesAndValues.map { it.index }.contains(i)) {
                val index = indicesAndValues.find { it.index == i }?.index
                val value = indicesAndValues.find { it.index == i }?.value
                assertEquals(
                    indicesAndValues.find { it.index == i }?.value?.toByte(),
                    list[i],
                    "Unexpected result for i = $i, (index, value) = ($index, $value)",
                )
            } else {
                assertEquals(0.toByte(), list[i])
            }
        }
    }
}
