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
