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
	"regexp"
	"strings"
)

var (
	evseIDDINRegex                     = regexp.MustCompile(`^(\+?[0-9]{1,3})\*([0-9]{3,6})\*([0-9*]{1,32})$`)
	evseIDDINFromPartsPowerOutletRegex = regexp.MustCompile(`^[0-9*]{1,32}$`)
)

// EvseIDDIN represents an EVSE identifier in DIN format.
type EvseIDDIN struct {
	countryCode   *PhoneCountryCode
	operatorID    *OperatorIDDIN
	powerOutletID string
}

// NewEvseIDDIN parses a DIN EVSE ID.
func NewEvseIDDIN(id string) (*EvseIDDIN, error) {
	matches := evseIDDINRegex.FindStringSubmatch(strings.ToUpper(id))
	if len(matches) != 4 {
		return nil, fmt.Errorf("%w: '%s' does not match the DIN format", ErrInvalidEvseID, id)
	}

	ccRaw := matches[1]
	if !strings.HasPrefix(ccRaw, "+") {
		ccRaw = "+" + ccRaw
	}
	cc, err := NewPhoneCountryCode(ccRaw)
	if err != nil {
		return nil, fmt.Errorf("%w: '%s': %w", ErrInvalidEvseID, id, err)
	}
	op, err := NewOperatorIDDIN(matches[2])
	if err != nil {
		return nil, fmt.Errorf("%w: '%s': %w", ErrInvalidEvseID, id, err)
	}

	return &EvseIDDIN{
		countryCode:   cc,
		operatorID:    op,
		powerOutletID: matches[3],
	}, nil
}

// NewEvseIDDINFromParts builds a DIN EVSE ID from its components.
func NewEvseIDDINFromParts(countryCode string, operatorID string, powerOutletID string) (*EvseIDDIN, error) {
	ccRaw := strings.ToUpper(countryCode)
	if !strings.HasPrefix(ccRaw, "+") {
		ccRaw = "+" + ccRaw
	}

	cc, err := NewPhoneCountryCode(ccRaw)
	if err != nil {
		return nil, fmt.Errorf("%w: invalid country code for DIN format: %w", ErrInvalidEvseID, err)
	}

	op, err := NewOperatorIDDIN(operatorID)
	if err != nil {
		return nil, fmt.Errorf("%w: invalid operator id for DIN format: %w", ErrInvalidEvseID, err)
	}

	normalizedPowerOutletID := strings.ToUpper(powerOutletID)
	if !evseIDDINFromPartsPowerOutletRegex.MatchString(normalizedPowerOutletID) {
		return nil, fmt.Errorf("%w: invalid power outlet id '%s' for DIN format", ErrInvalidEvseID, powerOutletID)
	}

	return &EvseIDDIN{
		countryCode:   cc,
		operatorID:    op,
		powerOutletID: normalizedPowerOutletID,
	}, nil
}

func (eidd *EvseIDDIN) String() string {
	return fmt.Sprintf("%s*%s*%s", eidd.countryCode.Value(), eidd.operatorID.Value(), eidd.powerOutletID)
}

// Value returns the canonical string representation of the DIN EVSE ID.
func (eidd *EvseIDDIN) Value() string {
	return eidd.String()
}
