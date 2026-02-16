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

import scala.quoted.*

private[mobilityid] object InterpolatorMacros {

  def evseIdImpl(sc: Expr[StringContext])(using Quotes): Expr[EvseId] = {
    val str = sc.valueOrAbort.parts.mkString
    EvseId(str) match {
      case None => quotes.reflect.report.errorAndAbort(s"not a valid EvseId: $str")
      case _    => '{ EvseId(${ Expr(str) }).get }
    }
  }

  def evseIdIsoImpl(sc: Expr[StringContext])(using Quotes): Expr[EvseIdIso] = {
    val str = sc.valueOrAbort.parts.mkString
    EvseIdIso(str) match {
      case None => quotes.reflect.report.errorAndAbort(s"not a valid EvseIdIso: $str")
      case _    => '{ EvseIdIso(${ Expr(str) }).get }
    }
  }

  def evseIdDinImpl(sc: Expr[StringContext])(using Quotes): Expr[EvseIdDin] = {
    val str = sc.valueOrAbort.parts.mkString
    EvseIdDin(str) match {
      case None => quotes.reflect.report.errorAndAbort(s"not a valid EvseIdDin: $str")
      case _    => '{ EvseIdDin(${ Expr(str) }).get }
    }
  }

  def contractIdIsoImpl(sc: Expr[StringContext])(using Quotes): Expr[ContractId[ContractIdStandard.ISO]] = {
    val str = sc.valueOrAbort.parts.mkString
    scala.util.Try(ContractId[ContractIdStandard.ISO](str)) match {
      case scala.util.Failure(ex) => quotes.reflect.report.errorAndAbort(ex.getMessage)
      case _                      => '{ ContractId[ContractIdStandard.ISO](${ Expr(str) }) }
    }
  }

  def contractIdDinImpl(sc: Expr[StringContext])(using Quotes): Expr[ContractId[ContractIdStandard.DIN]] = {
    val str = sc.valueOrAbort.parts.mkString
    scala.util.Try(ContractId[ContractIdStandard.DIN](str)) match {
      case scala.util.Failure(ex) => quotes.reflect.report.errorAndAbort(ex.getMessage)
      case _                      => '{ ContractId[ContractIdStandard.DIN](${ Expr(str) }) }
    }
  }

  def contractIdEmi3Impl(sc: Expr[StringContext])(using Quotes): Expr[ContractId[ContractIdStandard.EMI3]] = {
    val str = sc.valueOrAbort.parts.mkString
    scala.util.Try(ContractId[ContractIdStandard.EMI3](str)) match {
      case scala.util.Failure(ex) => quotes.reflect.report.errorAndAbort(ex.getMessage)
      case _                      => '{ ContractId[ContractIdStandard.EMI3](${ Expr(str) }) }
    }
  }

  def providerIdImpl(sc: Expr[StringContext])(using Quotes): Expr[ProviderId] = {
    val str = sc.valueOrAbort.parts.mkString
    if (!ProviderId.isValid(str)) quotes.reflect.report.errorAndAbort(s"not a valid ProviderId: $str")
    '{ ProviderId(${ Expr(str) }) }
  }

  def countryCodeImpl(sc: Expr[StringContext])(using Quotes): Expr[CountryCode] = {
    val str = sc.valueOrAbort.parts.mkString
    if (!CountryCode.isValid(str)) quotes.reflect.report.errorAndAbort(s"not a valid CountryCode: $str")
    '{ CountryCode(${ Expr(str) }) }
  }

  def phoneCountryCodeImpl(sc: Expr[StringContext])(using Quotes): Expr[PhoneCountryCode] = {
    val str = sc.valueOrAbort.parts.mkString
    if (!PhoneCountryCode.isValid(str)) quotes.reflect.report.errorAndAbort(s"not a valid PhoneCountryCode: $str")
    '{ PhoneCountryCode(${ Expr(str) }) }
  }

  def operatorIdIsoImpl(sc: Expr[StringContext])(using Quotes): Expr[OperatorIdIso] = {
    val str = sc.valueOrAbort.parts.mkString
    if (!OperatorIdIso.isValid(str)) quotes.reflect.report.errorAndAbort(s"not a valid OperatorIdIso: $str")
    '{ OperatorIdIso(${ Expr(str) }) }
  }

  def operatorIdDinImpl(sc: Expr[StringContext])(using Quotes): Expr[OperatorIdDin] = {
    val str = sc.valueOrAbort.parts.mkString
    if (!OperatorIdDin.isValid(str)) quotes.reflect.report.errorAndAbort(s"not a valid OperatorIdDin: $str")
    '{ OperatorIdDin(${ Expr(str) }) }
  }
}
