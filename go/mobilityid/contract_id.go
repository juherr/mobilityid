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

import (
	"fmt"
	"strings"
)

// ContractID represents a unique identifier for a contract in the e-mobility domain.
// It typically consists of a CountryCode, ProviderID, and a local contract identifier.
type ContractID struct {
	value         string
	standard      ContractIDStandard
	countryCode   *CountryCode
	providerID    *ProviderID
	instanceValue string // Renamed from localID to match Scala's instanceValue
	checkDigit    rune   // New field for check digit
}

// Minimal length for ContractID: 2 (CC) + 1 (PID suffix) + 1 (LocalID) = 4 (or more depending on standard)
// This minimal length check will be handled by specific parser regexes.
const minContractIDLength = 4 // Keep for basic sanity check

// NewContractID creates a new ContractID if the provided value is valid for the given standard.
// It returns an error if the value does not conform to the expected format and validation rules.
func NewContractID(id string, standard ContractIDStandard) (*ContractID, error) {
	upperID := strings.ToUpper(id)

	if len(upperID) < minContractIDLength { // Basic length check
		return nil, fmt.Errorf("'%s' is too short to be a valid ContractID", id)
	}

	parser, err := parserForStandard(standard)
	if err != nil {
		return nil, err
	}

	// Use the parser's FullRegex to match and extract components
	matcher := parser.FullRegex()
	matches := matcher.FindStringSubmatch(upperID) // Use upperID for matching
	if len(matches) < 5 {                          // Expecting full match + 4 capturing groups: CC, PID_Suffix, InstanceValue, CheckDigit (optional)
		return nil, fmt.Errorf("'%s' is not a valid Contract ID for %s format", id, parser.Name())
	}

	// Extract components (remembering groups start from 1)
	countryCodeStr := matches[1]
	providerIDPartStr := matches[2]
	instanceValueStr := matches[3]
	checkDigitStr := matches[4] // This might be empty if check digit is optional

	// Validate CountryCode
	cc, err := NewCountryCode(countryCodeStr)
	if err != nil {
		return nil, fmt.Errorf("invalid country code in ContractID '%s': %w", id, err)
	}

	pid, err := NewProviderID(providerIDPartStr)
	if err != nil {
		return nil, fmt.Errorf("invalid provider ID '%s' in ContractID '%s': %w", providerIDPartStr, id, err)
	}

	if err := parser.ValidateInstanceValue(instanceValueStr); err != nil {
		return nil, err
	}

	// Compute and verify check digit
	computedCD, err := parser.ComputeCheckDigit(countryCodeStr + pid.Value() + instanceValueStr)
	if err != nil {
		return nil, fmt.Errorf("failed to compute check digit for '%s': %w", id, err)
	}

	var finalCheckDigit rune
	if checkDigitStr != "" { // If check digit is present in input
		inputCD := rune(checkDigitStr[0])
		if inputCD != computedCD {
			return nil, fmt.Errorf("given check digit '%c' is not equal to computed '%c' for '%s'", inputCD, computedCD, id)
		}
		finalCheckDigit = inputCD
	} else { // If check digit is optional and not provided in input
		// In Scala, if check digit is None, it uses the computed one.
		// So we use the computed one for the final ContractID object.
		finalCheckDigit = computedCD
	}

	result := &ContractID{
		value:         upperID,
		standard:      standard,
		countryCode:   cc,
		providerID:    pid,
		instanceValue: instanceValueStr,
		checkDigit:    finalCheckDigit,
	}
	result.value = result.String()
	return result, nil
}

// String returns the canonical string representation of the ContractID, formatted as "CC-PS-IV-CD".
func (cid *ContractID) String() string {
	return fmt.Sprintf("%s-%s-%s-%c", cid.countryCode.Value(), cid.providerID.String(), cid.instanceValue, cid.checkDigit)
}

// Value returns the underlying string value of the ContractID.
func (cid *ContractID) Value() string {
	return cid.value
}

// ToCompactString returns a compact string representation of the ContractID, formatted as "CCPSIVCD".
// This aligns with Scala's toCompactString.
func (cid *ContractID) ToCompactString() string {
	return cid.countryCode.Value() + cid.providerID.String() + cid.instanceValue + string(cid.checkDigit)
}

// ToCompactStringWithoutCheckDigit returns a compact string representation of the ContractID without the check digit.
// This aligns with Scala's toCompactStringWithoutCheckDigit.
func (cid *ContractID) ToCompactStringWithoutCheckDigit() string {
	return cid.countryCode.Value() + cid.providerID.String() + cid.instanceValue
}

// Standard returns the ContractIDStandard associated with this ContractID.
func (cid *ContractID) Standard() ContractIDStandard {
	return cid.standard
}

// CountryCode returns the CountryCode part of the ContractID.
func (cid *ContractID) CountryCode() *CountryCode {
	return cid.countryCode
}

// ProviderID returns the ProviderID part of the ContractID.
func (cid *ContractID) ProviderID() *ProviderID {
	return cid.providerID
}

// InstanceValue returns the instance value (local ID) part of the ContractID.
func (cid *ContractID) InstanceValue() string {
	return cid.instanceValue
}

// CheckDigit returns the check digit of the ContractID.
func (cid *ContractID) CheckDigit() rune {
	return cid.checkDigit
}

// PartyID returns the PartyID derived from the ContractID's CountryCode and ProviderID.
func (cid *ContractID) PartyID() (*PartyID, error) {
	if cid.countryCode == nil || cid.providerID == nil {
		return nil, fmt.Errorf("cannot derive PartyID from incomplete ContractID")
	}
	return NewPartyID(cid.countryCode.Value() + cid.providerID.Value())
}

// ToDIN converts the current ContractID to DIN format when possible.
func (cid *ContractID) ToDIN() (*ContractID, error) {
	switch cid.standard {
	case ContractIDStandardDIN:
		return cid, nil
	case ContractIDStandardISO:
		if !strings.HasPrefix(cid.instanceValue, "00") {
			return nil, fmt.Errorf("%s cannot be converted to %s format", cid.String(), DINParser.Name())
		}
		dinInstance := cid.instanceValue[2:8]
		dinCheck := cid.instanceValue[8]
		return NewContractID(fmt.Sprintf("%s-%s-%s-%c", cid.countryCode.Value(), cid.providerID.Value(), dinInstance, dinCheck), ContractIDStandardDIN)
	case ContractIDStandardEMI3:
		if !strings.HasPrefix(cid.instanceValue, "C0") {
			return nil, fmt.Errorf("%s cannot be converted to %s format", cid.String(), DINParser.Name())
		}
		dinInstance := cid.instanceValue[2:8]
		dinCheck := cid.instanceValue[8]
		return NewContractID(fmt.Sprintf("%s-%s-%s-%c", cid.countryCode.Value(), cid.providerID.Value(), dinInstance, dinCheck), ContractIDStandardDIN)
	default:
		return nil, fmt.Errorf("unsupported ContractIDStandard: %v", cid.standard)
	}
}

// ToEMI3 converts the current ContractID to EMI3 format when possible.
func (cid *ContractID) ToEMI3() (*ContractID, error) {
	switch cid.standard {
	case ContractIDStandardEMI3:
		return cid, nil
	case ContractIDStandardDIN:
		return NewContractID(
			fmt.Sprintf("%s-%s-C0%s%c", cid.countryCode.Value(), cid.providerID.Value(), cid.instanceValue, cid.checkDigit),
			ContractIDStandardEMI3,
		)
	case ContractIDStandardISO:
		return NewContractID(cid.String(), ContractIDStandardEMI3)
	default:
		return nil, fmt.Errorf("unsupported ContractIDStandard: %v", cid.standard)
	}
}

// ToISO converts the current ContractID to ISO format when possible.
func (cid *ContractID) ToISO() (*ContractID, error) {
	switch cid.standard {
	case ContractIDStandardISO:
		return cid, nil
	case ContractIDStandardDIN:
		return NewContractID(
			fmt.Sprintf("%s-%s-00%s%c", cid.countryCode.Value(), cid.providerID.Value(), cid.instanceValue, cid.checkDigit),
			ContractIDStandardISO,
		)
	case ContractIDStandardEMI3:
		return NewContractID(cid.String(), ContractIDStandardISO)
	default:
		return nil, fmt.Errorf("unsupported ContractIDStandard: %v", cid.standard)
	}
}

func parserForStandard(standard ContractIDStandard) (ContractIDParser, error) {
	switch standard {
	case ContractIDStandardISO:
		return ISOParser, nil
	case ContractIDStandardEMI3:
		return EMI3Parser, nil
	case ContractIDStandardDIN:
		return DINParser, nil
	default:
		return nil, fmt.Errorf("unsupported ContractIDStandard: %v", standard)
	}
}
