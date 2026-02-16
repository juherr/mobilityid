package com.thenewmotion.mobilityid

import ContractIdStandard.{DIN, EMI3, ISO}

object interpolators {
  extension (inline sc: StringContext)
    inline def evseId(inline args: Any*): EvseId =
      ${ InterpolatorMacros.evseIdImpl('sc) }
    inline def evseIdIso(inline args: Any*): EvseIdIso =
      ${ InterpolatorMacros.evseIdIsoImpl('sc) }
    inline def evseIdDin(inline args: Any*): EvseIdDin =
      ${ InterpolatorMacros.evseIdDinImpl('sc) }
    inline def contractIdISO(inline args: Any*): ContractId[ISO] =
      ${ InterpolatorMacros.contractIdIsoImpl('sc) }
    inline def contractIdDIN(inline args: Any*): ContractId[DIN] =
      ${ InterpolatorMacros.contractIdDinImpl('sc) }
    inline def contractIdEMI3(inline args: Any*): ContractId[EMI3] =
      ${ InterpolatorMacros.contractIdEmi3Impl('sc) }
    inline def providerId(inline args: Any*): ProviderId =
      ${ InterpolatorMacros.providerIdImpl('sc) }
    inline def countryCode(inline args: Any*): CountryCode =
      ${ InterpolatorMacros.countryCodeImpl('sc) }
    inline def phoneCountryCode(inline args: Any*): PhoneCountryCode =
      ${ InterpolatorMacros.phoneCountryCodeImpl('sc) }
    inline def operatorIdIso(inline args: Any*): OperatorIdIso =
      ${ InterpolatorMacros.operatorIdIsoImpl('sc) }
    inline def operatorIdDin(inline args: Any*): OperatorIdDin =
      ${ InterpolatorMacros.operatorIdDinImpl('sc) }
}
