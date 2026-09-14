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
)

// PhoneCountryCode represents a country calling code as defined by E.164.
type PhoneCountryCode struct {
	value string
}

var phoneCountryCodeRegex = regexp.MustCompile(`^\+?[0-9]{1,3}$`)

// NewPhoneCountryCode creates a new PhoneCountryCode if the provided value is valid.
// It returns an error if the value is not a valid E.164 country calling code.
func NewPhoneCountryCode(code string) (*PhoneCountryCode, error) {
	if !isValidPhoneCountryCode(code) {
		return nil, fmt.Errorf("'%s' is not a valid E.164 phone country code", code)
	}
	return &PhoneCountryCode{value: code}, nil
}

// String returns the canonical string representation of the PhoneCountryCode.
func (pcc *PhoneCountryCode) String() string {
	return pcc.value
}

// Value returns the underlying string value of the PhoneCountryCode.
func (pcc *PhoneCountryCode) Value() string {
	return pcc.value
}

func isValidPhoneCountryCode(code string) bool {
	return phoneCountryCodeRegex.MatchString(code)
}
