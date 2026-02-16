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

var evseIDDINRegex = regexp.MustCompile(`^(\+?[0-9]{1,3})\*([0-9]{3,6})\*([0-9*]{1,32})$`)
var evseIDDINFromPartsPowerOutletRegex = regexp.MustCompile(`^[0-9*]{1,32}$`)

type EvseIDDIN struct {
	countryCode   *PhoneCountryCode
	operatorID    *OperatorIDDIN
	powerOutletID string
}

func NewEvseIDDIN(id string) (*EvseIDDIN, error) {
	matches := evseIDDINRegex.FindStringSubmatch(strings.ToUpper(id))
	if len(matches) != 4 {
		return nil, fmt.Errorf("'%s' is not a valid DIN EvseID", id)
	}

	ccRaw := matches[1]
	if !strings.HasPrefix(ccRaw, "+") {
		ccRaw = "+" + ccRaw
	}
	cc, err := NewPhoneCountryCode(ccRaw)
	if err != nil {
		return nil, err
	}
	op, err := NewOperatorIDDIN(matches[2])
	if err != nil {
		return nil, err
	}

	return &EvseIDDIN{
		countryCode:   cc,
		operatorID:    op,
		powerOutletID: matches[3],
	}, nil
}

func NewEvseIDDINFromParts(countryCode string, operatorID string, powerOutletID string) (*EvseIDDIN, error) {
	ccRaw := strings.ToUpper(countryCode)
	if !strings.HasPrefix(ccRaw, "+") {
		ccRaw = "+" + ccRaw
	}

	cc, err := NewPhoneCountryCode(ccRaw)
	if err != nil {
		return nil, fmt.Errorf("invalid countryCode for DIN format: %w", err)
	}

	op, err := NewOperatorIDDIN(operatorID)
	if err != nil {
		return nil, fmt.Errorf("invalid operatorID for DIN format: %w", err)
	}

	normalizedPowerOutletID := strings.ToUpper(powerOutletID)
	if !evseIDDINFromPartsPowerOutletRegex.MatchString(normalizedPowerOutletID) {
		return nil, fmt.Errorf("invalid powerOutletID for DIN format")
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

func (eidd *EvseIDDIN) Value() string {
	return eidd.String()
}
