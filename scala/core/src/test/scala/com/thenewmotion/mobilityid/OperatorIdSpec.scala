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

class OperatorIdSpec extends mutable.Specification {

  "OperatorIdDin" should {
    "Reject less than 3 digits" in {
      OperatorIdDin("12") must throwA[IllegalArgumentException]
    }

    "Reject letters" in {
      OperatorIdDin("12A") must throwA[IllegalArgumentException]
    }

    "Reject more than 6 digits" in {
      OperatorIdDin("1234567") must throwA[IllegalArgumentException]
    }

    "Accept correct format" in {
      OperatorIdDin("12345") must not(throwA[IllegalArgumentException])
    }
  }

  "OperatorIdIso" should {
    "Reject less than 3 digits" in {
      OperatorIdIso("AB") must throwA[IllegalArgumentException]
    }

    "Reject more than 3 digits" in {
      OperatorIdIso("ABCD") must throwA[IllegalArgumentException]
    }

    "Accept correct format" in {
      OperatorIdIso("AB2") must not(throwA[IllegalArgumentException])
    }
  }
}
