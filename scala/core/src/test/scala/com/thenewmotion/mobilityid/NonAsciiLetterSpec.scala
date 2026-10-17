/*
 * Copyright (c) 2014 The New Motion team, and respective contributors
 * Copyright (c) 2026 Julien Herr, and respective contributors
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

package com.thenewmotion.mobilityid

import org.specs2._

import ContractIdStandard.ISO

/**
 * `toUpperCase` maps some non-ASCII letters to ASCII ones ('ſ' U+017F to 'S', 'ı' U+0131 to 'I'). Only ASCII letters
 * are folded, so these inputs are rejected; the vectors are shared with every port.
 */
class NonAsciiLetterSpec extends mutable.Specification {

  "Non-ASCII letters that uppercase to ASCII" should {

    "be rejected by the EVSE id factories" in {
      EvseIdIso("FR", "55C", "84400*ſVG*1") must throwA[IllegalArgumentException]
      EvseIdIso("ſe", "55C", "1") must throwA[IllegalArgumentException]
      EvseIdIso("NL", "ıBM", "1") must throwA[IllegalArgumentException]
      EvseId("FR", "55C", "84400*ſVG*1") must throwA[IllegalArgumentException]
      EvseId("ſe", "55C", "1") must throwA[IllegalArgumentException]
    }

    "be rejected by the contract id factories" in {
      ContractId[ISO]("NL", "TNM", "00012204ı") must throwA[IllegalArgumentException]
      ContractId[ISO]("NL", "TNM", "000000899", 'ı') must throwA[IllegalArgumentException]
    }

    "be rejected by the check digit algorithms" in {
      CheckDigitIso("NLſVG000122045") must throwA[IllegalArgumentException]
      CheckDigitDin("NLſVG012204") must throwA[Exception]
    }
  }
}
