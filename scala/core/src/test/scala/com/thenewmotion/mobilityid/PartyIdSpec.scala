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

class PartyIdSpec extends mutable.Specification {

  "PartyId" should {

    "parse party-IDs with dash" in {
      PartyId("NL-TNM") should beSome[PartyId].which(_.toCompactString == "NLTNM")
    }

    "parse party-IDs with asterisk" in {
      PartyId("NL*TNM") should beSome[PartyId].which(_.toCompactString == "NLTNM")
    }

    "parse party-IDs without dash or asterisk" in {
      PartyId("NLTNM") should beSome[PartyId].which(_.toCompactString == "NLTNM")
    }

    "render to String with a dash" in {
      PartyId("NL*TNM") should beSome[PartyId].which(_.toString == "NL-TNM")
    }

    "not parse various nonsense input strings" in {
      val nonsenseIds = List("NLTNMA", "XYTNM", "NL%(@$", " NLTNM", "\u000aLTNM", "", "XY-TNMaargh", "НЛ-TNM", "NLT-NM")

      nonsenseIds.map(PartyId.apply) must contain(beNone).foreach
    }
  }
}
