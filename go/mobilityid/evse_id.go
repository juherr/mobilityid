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

import "fmt"

// EvseID represents an EVSE identifier in either ISO or DIN format.
type EvseID struct {
	iso *EvseIDISO
	din *EvseIDDIN
}

// NewEvseID parses an EVSE ID and returns either its ISO or DIN representation.
func NewEvseID(id string) (*EvseID, error) {
	if iso, err := NewEvseIDISO(id); err == nil {
		return &EvseID{iso: iso}, nil
	}
	if din, err := NewEvseIDDIN(id); err == nil {
		return &EvseID{din: din}, nil
	}
	return nil, fmt.Errorf("'%s' is not a valid EvseID", id)
}

// NewEvseIDFromParts builds an EVSE ID from components, preferring ISO when both are valid.
func NewEvseIDFromParts(countryCode string, operatorID string, powerOutletID string) (*EvseID, error) {
	iso, isoErr := NewEvseIDISOFromParts(countryCode, operatorID, powerOutletID)
	din, dinErr := NewEvseIDDINFromParts(countryCode, operatorID, powerOutletID)

	if isoErr == nil {
		return &EvseID{iso: iso}, nil
	}
	if dinErr == nil {
		return &EvseID{din: din}, nil
	}

	isoPriority := evseIDPartsErrorPriority(countryCode, operatorID, powerOutletID, true)
	dinPriority := evseIDPartsErrorPriority(countryCode, operatorID, powerOutletID, false)
	if isoPriority >= dinPriority {
		return nil, isoErr
	}
	return nil, dinErr
}

func (eid *EvseID) String() string {
	if eid.iso != nil {
		return eid.iso.String()
	}
	if eid.din != nil {
		return eid.din.String()
	}
	return ""
}

// Value returns the canonical string representation of the EVSE ID.
func (eid *EvseID) Value() string {
	return eid.String()
}

// CountryCode returns the country code part of the EVSE ID.
func (eid *EvseID) CountryCode() string {
	if eid.iso != nil {
		return eid.iso.countryCode.Value()
	}
	if eid.din != nil {
		return eid.din.countryCode.Value()
	}
	return ""
}

// OperatorID returns the operator identifier part of the EVSE ID.
func (eid *EvseID) OperatorID() string {
	if eid.iso != nil {
		return eid.iso.operatorID.Value()
	}
	if eid.din != nil {
		return eid.din.operatorID.Value()
	}
	return ""
}

// PowerOutletID returns the power outlet identifier part of the EVSE ID.
func (eid *EvseID) PowerOutletID() string {
	if eid.iso != nil {
		return eid.iso.powerOutletID
	}
	if eid.din != nil {
		return eid.din.powerOutletID
	}
	return ""
}

// IsISO reports whether this EVSE ID is represented in ISO format.
func (eid *EvseID) IsISO() bool {
	return eid != nil && eid.iso != nil
}

// IsDIN reports whether this EVSE ID is represented in DIN format.
func (eid *EvseID) IsDIN() bool {
	return eid != nil && eid.din != nil
}

func evseIDPartsErrorPriority(countryCode string, operatorID string, powerOutletID string, iso bool) int {
	if iso {
		if _, err := NewCountryCode(countryCode); err != nil {
			return 1
		}
		if _, err := NewOperatorIDISO(operatorID); err != nil {
			return 2
		}
		if !evseIDISOFromPartsPowerOutletRegex.MatchString(powerOutletID) {
			return 3
		}
		return 0
	}

	ccRaw := countryCode
	if len(ccRaw) == 0 || ccRaw[0] != '+' {
		ccRaw = "+" + ccRaw
	}
	if _, err := NewPhoneCountryCode(ccRaw); err != nil {
		return 1
	}
	if _, err := NewOperatorIDDIN(operatorID); err != nil {
		return 2
	}
	if !evseIDDINFromPartsPowerOutletRegex.MatchString(powerOutletID) {
		return 3
	}
	return 0
}
