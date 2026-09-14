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

// ProviderID represents a unique identifier for a Charge Point Operator (CPO) or e-Mobility Service Provider (EMP).
// It follows a specific format: <CountryCode><PartyCode>
// where CountryCode is a 2-letter ISO 3166-1 alpha-2 code and PartyCode is a string of 3 alphanumeric characters.
type ProviderID struct {
	value string
}

// partyCodeRegex from Scala's PartyCode.Regex
var partyCodeRegex = regexp.MustCompile(`^[A-Z0-9]{3}$`)

// NewProviderID creates a new ProviderID from a 3-character party code.
func NewProviderID(id string) (*ProviderID, error) {
	upperID := strings.ToUpper(id)
	if !partyCodeRegex.MatchString(upperID) {
		return nil, fmt.Errorf("invalid provider ID '%s': must be 3 alphanumeric characters", id)
	}
	return &ProviderID{value: upperID}, nil
}

// String returns the 3-character provider code.
func (pid *ProviderID) String() string {
	return pid.value
}

// Value returns the full CountryCode + PartyCode string of the ProviderID.
func (pid *ProviderID) Value() string {
	return pid.value
}
