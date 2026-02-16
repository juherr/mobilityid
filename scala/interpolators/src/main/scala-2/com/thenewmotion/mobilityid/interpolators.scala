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

import ContractIdStandard.{DIN, EMI3, ISO}
import contextual.{Interpolator, Macros}
import scala.util.{Try, Failure}
import language.experimental.macros

object EvseIdInterpolator extends Interpolator {

  type Output = EvseId

  def contextualize(interpolation: StaticInterpolation) = {
    val lit@Literal(_, evseIdString) = (interpolation.parts.head: @unchecked)
    EvseId(evseIdString) match {
      case None => interpolation.abort(lit, 0, "not a valid EvseId")
      case _ =>
    }

    Nil
  }

  def evaluate(interpolation: RuntimeInterpolation): EvseId =
    EvseId(interpolation.literals.head).get
}

object EvseIdIsoInterpolator extends Interpolator {

  type Output = EvseIdIso

  def contextualize(interpolation: StaticInterpolation) = {
    val lit@Literal(_, evseIdString) = (interpolation.parts.head: @unchecked)
    EvseIdIso(evseIdString) match {
      case None => interpolation.abort(lit, 0, "not a valid EvseIdIso")
      case _ =>
    }

    Nil
  }

  def evaluate(interpolation: RuntimeInterpolation): EvseIdIso =
    EvseIdIso(interpolation.literals.head).get
}

object EvseIdDinInterpolator extends Interpolator {

  type Output = EvseIdDin

  def contextualize(interpolation: StaticInterpolation) = {
    val lit@Literal(_, evseIdString) = (interpolation.parts.head: @unchecked)
    EvseIdDin(evseIdString) match {
      case None => interpolation.abort(lit, 0, "not a valid EvseIdDin")
      case _ =>
    }

    Nil
  }

  def evaluate(interpolation: RuntimeInterpolation): EvseIdDin =
    EvseIdDin(interpolation.literals.head).get
}

class ContractIdInterpolator[T <: ContractIdStandard](implicit p: ContractIdParser[T]) extends Interpolator {

  def contextualize(interpolation: StaticInterpolation) = {
    val lit@Literal(_, emaIdString) = (interpolation.parts.head: @unchecked)
    Try(ContractId[T](emaIdString)) match {
      case Failure(ex) => interpolation.abort(lit, 0, ex.getMessage)
      case _ =>
    }

    Nil
  }

  def evaluate(interpolation: RuntimeInterpolation): ContractId[T] =
    ContractId[T](interpolation.literals.head)
}

object ContractIdIsoInterpolator extends ContractIdInterpolator[ISO] {
  type Output = ContractId[ISO]
}
object ContractIdDinInterpolator extends ContractIdInterpolator[DIN] {
  type Output = ContractId[DIN]
}
object ContractIdEmi3Interpolator extends ContractIdInterpolator[EMI3] {
  type Output = ContractId[EMI3]
}

object ProviderIdInterpolator extends Interpolator {

  type Output = ProviderId

  def contextualize(interpolation: StaticInterpolation) = {
    val lit@Literal(_, providerIdString) = (interpolation.parts.head: @unchecked)
    if (!ProviderId.isValid(providerIdString)) interpolation.abort(lit, 0, "not a valid ProviderId")
    Nil
  }

  def evaluate(interpolation: RuntimeInterpolation): ProviderId =
    ProviderId(interpolation.literals.head)
}

object CountryCodeInterpolator extends Interpolator {

  type Output = CountryCode

  def contextualize(interpolation: StaticInterpolation) = {
    val lit@Literal(_, countryCodeString) = (interpolation.parts.head: @unchecked)
    if (!CountryCode.isValid(countryCodeString)) interpolation.abort(lit, 0, "not a valid CountryCode")
    Nil
  }

  def evaluate(interpolation: RuntimeInterpolation): CountryCode =
    CountryCode(interpolation.literals.head)
}

object PhoneCountryCodeInterpolator extends Interpolator {

  type Output = PhoneCountryCode

  def contextualize(interpolation: StaticInterpolation) = {
    val lit@Literal(_, phoneCountryCodeString) = (interpolation.parts.head: @unchecked)
    if (!PhoneCountryCode.isValid(phoneCountryCodeString)) interpolation.abort(lit, 0, "not a valid PhoneCountryCode")
    Nil
  }

  def evaluate(interpolation: RuntimeInterpolation): PhoneCountryCode =
    PhoneCountryCode(interpolation.literals.head)
}

object OperatorIdIsoInterpolator extends Interpolator {

  type Output = OperatorIdIso

  def contextualize(interpolation: StaticInterpolation) = {
    val lit@Literal(_, operatorIdIso) = (interpolation.parts.head: @unchecked)
    if (!OperatorIdIso.isValid(operatorIdIso)) interpolation.abort(lit, 0, "not a valid OperatorIdIso")
    Nil
  }

  def evaluate(interpolation: RuntimeInterpolation): OperatorIdIso =
    OperatorIdIso(interpolation.literals.head)
}

object OperatorIdDinInterpolator extends Interpolator {

  type Output = OperatorIdDin

  def contextualize(interpolation: StaticInterpolation) = {
    val lit@Literal(_, operatorIdDin) = (interpolation.parts.head: @unchecked)
    if (!OperatorIdDin.isValid(operatorIdDin)) interpolation.abort(lit, 0, "not a valid OperatorIdDin")
    Nil
  }

  def evaluate(interpolation: RuntimeInterpolation): OperatorIdDin =
    OperatorIdDin(interpolation.literals.head)
}

object interpolators {
  implicit class MobilityIdStringContext(sc: StringContext) {
    def evseId(expressions: Interpolator.Embedded[EvseIdInterpolator.Input, EvseIdInterpolator.type]*):
        EvseIdInterpolator.Output = macro Macros.contextual[EvseIdInterpolator.type]
    def evseIdIso(expressions: Interpolator.Embedded[EvseIdIsoInterpolator.Input, EvseIdIsoInterpolator.type]*):
        EvseIdIsoInterpolator.Output = macro Macros.contextual[EvseIdIsoInterpolator.type]
    def evseIdDin(expressions: Interpolator.Embedded[EvseIdDinInterpolator.Input, EvseIdDinInterpolator.type]*):
        EvseIdDinInterpolator.Output = macro Macros.contextual[EvseIdDinInterpolator.type]
    def contractIdISO(expressions: Interpolator.Embedded[ContractIdIsoInterpolator.Input, ContractIdIsoInterpolator.type]*):
        ContractIdIsoInterpolator.Output = macro Macros.contextual[ContractIdIsoInterpolator.type]
    def contractIdDIN(expressions: Interpolator.Embedded[ContractIdDinInterpolator.Input, ContractIdDinInterpolator.type]*):
        ContractIdDinInterpolator.Output = macro Macros.contextual[ContractIdDinInterpolator.type]
    def contractIdEMI3(expressions: Interpolator.Embedded[ContractIdEmi3Interpolator.Input, ContractIdEmi3Interpolator.type]*):
        ContractIdEmi3Interpolator.Output = macro Macros.contextual[ContractIdEmi3Interpolator.type]
    def providerId(expressions: Interpolator.Embedded[ProviderIdInterpolator.Input, ProviderIdInterpolator.type]*):
        ProviderIdInterpolator.Output = macro Macros.contextual[ProviderIdInterpolator.type]
    def countryCode(expressions: Interpolator.Embedded[CountryCodeInterpolator.Input, CountryCodeInterpolator.type]*):
        CountryCodeInterpolator.Output = macro Macros.contextual[CountryCodeInterpolator.type]
    def phoneCountryCode(expressions: Interpolator.Embedded[PhoneCountryCodeInterpolator.Input, PhoneCountryCodeInterpolator.type]*):
        PhoneCountryCodeInterpolator.Output = macro Macros.contextual[PhoneCountryCodeInterpolator.type]
    def operatorIdIso(expressions: Interpolator.Embedded[OperatorIdIsoInterpolator.Input, OperatorIdIsoInterpolator.type]*):
        OperatorIdIsoInterpolator.Output = macro Macros.contextual[OperatorIdIsoInterpolator.type]
    def operatorIdDin(expressions: Interpolator.Embedded[OperatorIdDinInterpolator.Input, OperatorIdDinInterpolator.type]*):
        OperatorIdDinInterpolator.Output = macro Macros.contextual[OperatorIdDinInterpolator.type]
  }
}
