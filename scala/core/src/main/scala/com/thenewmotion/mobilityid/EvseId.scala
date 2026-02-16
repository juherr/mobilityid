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

import scala.util.Try
import scala.util.matching.Regex

sealed trait EvseId {
  def countryCode: CountryId
  def operatorId: OperatorId
  def powerOutletId: String

  protected val separator = "*"

  protected def normalizedId =
    List(countryCode.toString, operatorId.toString, powerOutletId).mkString(separator)

  override def toString = normalizedId
}

private case class Error(priority: Int, desc: String)

trait EvseIdFormat[T <: EvseId] {
  def Description: String
  def CountryCodeRegex: Regex
  def OperatorCode: Regex
  def PowerOutletId: Regex
  def EvseIdRegex: Regex

  def apply(countryCode: String, operatorId: String, powerOutletId: String): T =
    validateAndCreate(countryCode.toUpperCase, operatorId.toUpperCase, powerOutletId.toUpperCase)

  private[mobilityid] def create(countryCode: String, operatorId: String, powerOutletId: String): T

  def apply(evseId: String): Option[T] = {
    val IdRegex = EvseIdRegex
    evseId match {
      case IdRegex(c, o, po) => Some(apply(c, o, po))
      case _ => None
    }
  }

  private[mobilityid] def validate
    (countryCode: String, operatorId: String, powerOutletId: String): Either[Error, T] = {

    def parse(part: String, regex: Regex, err: => Error): Either[Error, Unit] =
      regex.unapplySeq(part).toRight(err).map(_ => ())

    for {
      _ <- parse(countryCode, CountryCodeRegex,
        Error(1, "Invalid countryCode for ISO or DIN format"))
      _ <- parse(operatorId, OperatorCode,
        Error(2, s"Invalid operatorId for $Description format"))
      _ <- parse(powerOutletId, PowerOutletId,
        Error(3, s"Invalid powerOutletId for $Description format"))
    } yield
      create(countryCode.toUpperCase, operatorId.toUpperCase, powerOutletId.toUpperCase)
  }

  private[mobilityid] def validateAndCreate(countryCode: String, operatorId: String, powerOutletId: String): T =
    validate(countryCode, operatorId, powerOutletId) match {
      case Right(evseId) => evseId
      case Left(error) => throw new IllegalArgumentException(error.desc)
    }
}

object EvseId {
  def apply(countryCode: String, operatorId: String, powerOutletId: String): EvseId = {
    (EvseIdIso.validate(countryCode, operatorId, powerOutletId),
      EvseIdDin.validate(countryCode, operatorId, powerOutletId)) match {
      case (Right(evseId), _) => evseId
      case (_, Right(evseId)) => evseId
      case (Left(error1), Left(error2)) =>
        if (error1.priority >= error2.priority)
          throw new IllegalArgumentException(error1.desc)
        else
          throw new IllegalArgumentException(error2.desc)
    }
  }

  def apply(evseId: String): Option[EvseId] = {
    evseId match {
      case EvseIdIso.EvseIdRegex(c, o, po) => Try(EvseIdIso.create(c.toUpperCase, o.toUpperCase, po.toUpperCase)).toOption
      case EvseIdDin.EvseIdRegex(c, o, po) => Try(EvseIdDin.create(c.toUpperCase, o.toUpperCase, po.toUpperCase)).toOption
      case _ => None
    }
  }

  object AsEvseId {
    def unapply(evseId: String) = Some(EvseId(evseId))
  }

  def unapply(x: EvseId): Option[String] = Some(x.toString)
}

object EvseIdDin extends EvseIdFormat[EvseIdDin] {
  val Description = "DIN"
  val CountryCodeRegex = PhoneCountryCode.Regex
  val OperatorCode = OperatorIdDin.Regex
  val PowerOutletId = """([0-9\*]{1,32})""".r
  val EvseIdRegex = s"""$CountryCodeRegex\\*$OperatorCode\\*$PowerOutletId""".r

  def apply(cc: PhoneCountryCode, o: OperatorIdDin, powerOutletId: String): EvseIdDin =
    EvseIdDinImpl(cc, o, powerOutletId)

  private[mobilityid] override def create(cc: String, operatorId: String, powerOutletId: String): EvseIdDin = {
    val ccWithPlus = if (cc.startsWith("+")) cc else s"+$cc"
    apply(PhoneCountryCode(ccWithPlus), OperatorIdDin(operatorId), powerOutletId)
  }
}

sealed trait EvseIdDin extends EvseId

private case class EvseIdDinImpl(
  countryCode: PhoneCountryCode,
  operatorId: OperatorIdDin,
  powerOutletId: String
) extends EvseIdDin

object EvseIdIso extends EvseIdFormat[EvseIdIso] {
  val Description = "ISO"
  val CountryCodeRegex = CountryCode.Regex
  val OperatorCode = PartyCode.Regex
  val IdType = "E"
  val PowerOutletId = """([A-Za-z0-9\*]{1,31})""".r
  val EvseIdRegex = s"""$CountryCodeRegex\\*?$OperatorCode\\*?$IdType$PowerOutletId""".r

  def apply(cc: CountryCode, o: OperatorIdIso, powerOutletId: String): EvseIdIso =
    EvseIdIsoImpl(cc, o, powerOutletId)

  private[mobilityid] override def create(cc: String, operatorId: String, powerOutletId: String): EvseIdIso = {
    apply(CountryCode(cc), OperatorIdIso(operatorId), powerOutletId)
  }
}

sealed trait EvseIdIso extends EvseId {
  def toCompactString: String = toString.replace(separator, "")

  def partyId: PartyId
}

private case class EvseIdIsoImpl (
  countryCode: CountryCode,
  operatorId: OperatorIdIso,
  powerOutletId: String
) extends EvseIdIso {
  override def normalizedId =
    Seq(countryCode.toString, operatorId.toString, EvseIdIso.IdType + powerOutletId).mkString(separator)

  def partyId = PartyId(countryCode, operatorId)
}
