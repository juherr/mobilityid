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

import com.thenewmotion.mobilityid._
import com.thenewmotion.mobilityid.ContractIdStandard._
import com.thenewmotion.mobilityid.interpolators._

object Smoke {
  private def check(condition: Boolean, message: => String): Unit =
    if (!condition) throw new AssertionError(message)

  def main(args: Array[String]): Unit = {
    val din = ContractId[DIN]("NL-TNM-012204-5")
    val emi3 = din.convertTo[EMI3]
    check(emi3.toString == "NL-TNM-C00122045-K", s"DIN -> EMI3 conversion gave $emi3")
    check(emi3.convertTo[DIN] == din, "EMI3 -> DIN round trip")

    val iso = ContractId[ISO]("NL", "TNM", "000122045")
    check(iso.checkDigit == 'U', s"ISO check digit ${iso.checkDigit}")
    check(iso.partyId == PartyId("NL*TNM").get, "party id")

    check(EvseId("NLTNME01225045").map(_.toString).contains("NL*TNM*E01225045"), "EVSE id normalization")
    check(EvseId("NL*T|M*E01225045").isEmpty, "invalid EVSE id must be rejected")

    val interpolated: EvseId = evseId"NL*TNM*E840*6487"
    check(interpolated.toString == "NL*TNM*E840*6487", "evseId interpolator")
    val contract: ContractId[ISO] = contractIdISO"NL-TNM-000722345-X"
    check(contract.toCompactString == "NLTNM000722345X", "contractIdISO interpolator")

    println(s"consumer smoke OK: $din -> $emi3, $iso, $interpolated, $contract")
  }
}
