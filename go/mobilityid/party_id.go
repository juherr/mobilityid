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

// PartyID represents a unique identifier for a party, often used in e-roaming contexts.
// It typically consists of a CountryCode and an alphanumeric identifier.
type PartyID struct {
	countryCode *CountryCode
	partyCode   string
}

var partyIDRegex = regexp.MustCompile(`^([A-Z]{2})[-*]?([A-Z0-9]{3})$`)

// NewPartyID creates a new PartyID if the provided value is valid.
// It returns an error if the value does not conform to the expected format.
func NewPartyID(id string) (*PartyID, error) {
	matches := partyIDRegex.FindStringSubmatch(strings.ToUpper(id))
	if len(matches) != 3 {
		return nil, fmt.Errorf("'%s' is not a valid PartyID", id)
	}

	cc, err := NewCountryCode(matches[1])
	if err != nil {
		return nil, fmt.Errorf("'%s' is not a valid PartyID: %w", id, err)
	}

	return &PartyID{
		countryCode: cc,
		partyCode:   matches[2],
	}, nil
}

// String returns the canonical string representation of the PartyID.
func (pid *PartyID) String() string {
	return fmt.Sprintf("%s-%s", pid.countryCode.Value(), pid.partyCode)
}

// Value returns the compact representation of the PartyID.
func (pid *PartyID) Value() string {
	return pid.ToCompactString()
}

// ToCompactString returns the compact representation without separator.
func (pid *PartyID) ToCompactString() string {
	return pid.countryCode.Value() + pid.partyCode
}
