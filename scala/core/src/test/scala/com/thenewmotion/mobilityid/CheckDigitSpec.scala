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

class CheckDigitSpec extends mutable.Specification {

  "CheckDigitISO" should {
    "calculate check digits" in {
      val contractIds = List(
        "NN123ABCDEFGHI",
        "FRXYZ123456789",
        "ITA1B2C3E4F5G6",
        "ESZU8WOX834H1D",
        "PT73902837ABCZ",
        "DE83DUIEN83QGZ",
        "DE83DUIEN83ZGQ",
        "DE8AA001234567"
      )

      contractIds.map(CheckDigitIso(_)) must_== "T24RZDM0".toList
    }

    "fail with IllegalArgumentException on malformed input" in {
      CheckDigitIso("Европарулит123") must throwA[IllegalArgumentException]
      CheckDigitIso("DE٨٣DUIEN٨٣QGZ") must throwA[IllegalArgumentException]
      CheckDigitIso("Å∏@*(Td\uD83D\uDE3BgaR^&(%") must throwA[IllegalArgumentException]
      CheckDigitIso("Å∏@*(Td\uD83D\uDE3BgR^&(%") must throwA[IllegalArgumentException]
      CheckDigitIso("") must throwA[IllegalArgumentException]
      CheckDigitIso("DE8AA0012345678") must throwA[IllegalArgumentException]
    }
  }

  "Check digit DIN" should
    "be calculated according to the old Excel-sheet ways" >> {

      def calculate(instance: Int) = CheckDigitDin("INTNM" + "%06d".format(instance))

      calculate(71) === '9'
      calculate(110) === 'X'
      calculate(124) === '0'
      calculate(114) === '6'
      calculate(191) === '5'
    }
}
