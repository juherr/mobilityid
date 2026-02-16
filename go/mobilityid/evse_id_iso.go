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

var evseIDISORegex = regexp.MustCompile(`^([A-Z]{2})\*?([A-Z0-9]{3})\*?E([A-Z0-9*]{1,31})$`)
var evseIDISOFromPartsPowerOutletRegex = regexp.MustCompile(`^[A-Z0-9*]{1,31}$`)

type EvseIDISO struct {
	countryCode   *CountryCode
	operatorID    *OperatorIDISO
	powerOutletID string
}

func NewEvseIDISO(id string) (*EvseIDISO, error) {
	matches := evseIDISORegex.FindStringSubmatch(strings.ToUpper(id))
	if len(matches) != 4 {
		return nil, fmt.Errorf("'%s' is not a valid ISO 15118 EvseID", id)
	}

	cc, err := NewCountryCode(matches[1])
	if err != nil {
		return nil, err
	}
	op, err := NewOperatorIDISO(matches[2])
	if err != nil {
		return nil, err
	}

	return &EvseIDISO{
		countryCode:   cc,
		operatorID:    op,
		powerOutletID: matches[3],
	}, nil
}

func NewEvseIDISOFromParts(countryCode string, operatorID string, powerOutletID string) (*EvseIDISO, error) {
	cc, err := NewCountryCode(countryCode)
	if err != nil {
		return nil, fmt.Errorf("invalid countryCode for ISO format: %w", err)
	}

	op, err := NewOperatorIDISO(operatorID)
	if err != nil {
		return nil, fmt.Errorf("invalid operatorID for ISO format: %w", err)
	}

	normalizedPowerOutletID := strings.ToUpper(powerOutletID)
	if strings.HasPrefix(normalizedPowerOutletID, "E") {
		normalizedPowerOutletID = normalizedPowerOutletID[1:]
	}

	if !evseIDISOFromPartsPowerOutletRegex.MatchString(normalizedPowerOutletID) {
		return nil, fmt.Errorf("invalid powerOutletID for ISO format")
	}

	return &EvseIDISO{
		countryCode:   cc,
		operatorID:    op,
		powerOutletID: normalizedPowerOutletID,
	}, nil
}

func (eido *EvseIDISO) String() string {
	return fmt.Sprintf("%s*%s*E%s", eido.countryCode.Value(), eido.operatorID.Value(), eido.powerOutletID)
}

func (eido *EvseIDISO) Value() string {
	return eido.String()
}

func (eido *EvseIDISO) ToCompactString() string {
	return strings.ReplaceAll(eido.String(), "*", "")
}

func (eido *EvseIDISO) PartyID() (*PartyID, error) {
	return NewPartyID(eido.countryCode.Value() + eido.operatorID.Value())
}
