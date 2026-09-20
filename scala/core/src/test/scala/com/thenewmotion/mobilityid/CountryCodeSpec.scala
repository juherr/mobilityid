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

class CountryCodeSpec extends mutable.Specification {

  "CountryCode" should {

    "Accept assigned ISO 3166-1 alpha-2 codes, normalizing to uppercase" in {
      Seq("NL", "nl", "GB", "AX", "BQ", "CW", "SS").foreach { raw =>
        CountryCode(raw).toString mustEqual raw.toUpperCase
      }
      CountryCode("nl") mustEqual CountryCode("NL")
    }

    // Region codes that CLDR knows but ISO 3166-1 does not assign: every port rejects them.
    "Reject region codes outside ISO 3166-1" in {
      Seq("UK", "EU", "XK", "AN", "SU", "DD", "YU", "XX", "ZZ").foreach { raw =>
        CountryCode(raw) must throwA[IllegalArgumentException]
      }
      ok
    }

    "Reject malformed input" in {
      Seq("", "N", "NLD", "N1").foreach { raw =>
        CountryCode(raw) must throwA[IllegalArgumentException]
      }
      ok
    }
  }
}
