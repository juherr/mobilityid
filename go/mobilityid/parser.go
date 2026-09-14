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

package mobilityid

// ParseCountryCode attempts to parse the given string into a CountryCode.
// It returns a pointer to a CountryCode and a nil error if successful,
// or nil and an error if the string is not a valid CountryCode.
func ParseCountryCode(s string) (*CountryCode, error) {
	return NewCountryCode(s)
}

// ParsePhoneCountryCode attempts to parse the given string into a PhoneCountryCode.
// It returns a pointer to a PhoneCountryCode and a nil error if successful,
// or nil and an error if the string is not a valid PhoneCountryCode.
func ParsePhoneCountryCode(s string) (*PhoneCountryCode, error) {
	return NewPhoneCountryCode(s)
}

// ParseProviderID attempts to parse the given string into a ProviderID.
// It returns a pointer to a ProviderID and a nil error if successful,
// or nil and an error if the string is not a valid ProviderID.
func ParseProviderID(s string) (*ProviderID, error) {
	return NewProviderID(s)
}

// ParseOperatorIDISO attempts to parse the given string into an OperatorIDISO.
// It returns a pointer to an OperatorIDISO and a nil error if successful,
// or nil and an error if the string is not a valid OperatorIDISO.
func ParseOperatorIDISO(s string) (*OperatorIDISO, error) {
	return NewOperatorIDISO(s)
}

// ParseOperatorIDDIN attempts to parse the given string into an OperatorIDDIN.
// It returns a pointer to an OperatorIDDIN and a nil error if successful,
// or nil and an error if the string is not a valid OperatorIDDIN.
func ParseOperatorIDDIN(s string) (*OperatorIDDIN, error) {
	return NewOperatorIDDIN(s)
}

// ParsePartyID attempts to parse the given string into a PartyID.
// It returns a pointer to a PartyID and a nil error if successful,
// or nil and an error if the string is not a valid PartyID.
func ParsePartyID(s string) (*PartyID, error) {
	return NewPartyID(s)
}

// ParseISOContractID attempts to parse the given string into an ISO ContractID.
func ParseISOContractID(s string) (*ContractID, error) {
	return NewContractID(s, ContractIDStandardISO)
}

// ParseEMI3ContractID attempts to parse the given string into an EMI3 ContractID.
func ParseEMI3ContractID(s string) (*ContractID, error) {
	return NewContractID(s, ContractIDStandardEMI3)
}

// ParseDINContractID attempts to parse the given string into a DIN ContractID.
func ParseDINContractID(s string) (*ContractID, error) {
	return NewContractID(s, ContractIDStandardDIN)
}

// ParseEvseID attempts to parse the given string into an EvseID.
// It returns a pointer to an EvseID and a nil error if successful,
// or nil and an error if the string is not a valid EvseID.
func ParseEvseID(s string) (*EvseID, error) {
	return NewEvseID(s)
}

// ParseEvseIDFromParts attempts to construct an EvseID from discrete fields.
// It matches Scala behavior by trying ISO first and then DIN.
func ParseEvseIDFromParts(countryCode string, operatorID string, powerOutletID string) (*EvseID, error) {
	return NewEvseIDFromParts(countryCode, operatorID, powerOutletID)
}

// ParseEvseIDISO attempts to parse the given string into an EvseIDISO.
// It returns a pointer to an EvseIDISO and a nil error if successful,
// or nil and an error if the string is not a valid EvseIDISO.
func ParseEvseIDISO(s string) (*EvseIDISO, error) {
	return NewEvseIDISO(s)
}

// ParseEvseIDISOFromParts attempts to construct an ISO EvseID from discrete fields.
func ParseEvseIDISOFromParts(countryCode string, operatorID string, powerOutletID string) (*EvseIDISO, error) {
	return NewEvseIDISOFromParts(countryCode, operatorID, powerOutletID)
}

// ParseEvseIDDIN attempts to parse the given string into an EvseIDDIN.
// It returns a pointer to an EvseIDDIN and a nil error if successful,
// or nil and an error if the string is not a valid EvseIDDIN.
func ParseEvseIDDIN(s string) (*EvseIDDIN, error) {
	return NewEvseIDDIN(s)
}

// ParseEvseIDDINFromParts attempts to construct a DIN EvseID from discrete fields.
func ParseEvseIDDINFromParts(countryCode string, operatorID string, powerOutletID string) (*EvseIDDIN, error) {
	return NewEvseIDDINFromParts(countryCode, operatorID, powerOutletID)
}
