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

	"golang.org/x/text/language"
)

// CountryCode represents a two-letter country code as defined by ISO 3166-1 alpha-2.
type CountryCode struct {
	value string
}

// NewCountryCode creates a new CountryCode if the provided value is valid.
// It returns an error if the value is not a valid ISO 3166-1 alpha-2 code.
func NewCountryCode(code string) (*CountryCode, error) {
	normalized := strings.ToUpper(code)
	if !isValidCountryCode(normalized) {
		return nil, fmt.Errorf("'%s' is not a valid ISO 3166-1 alpha-2 country code", code)
	}
	return &CountryCode{value: normalized}, nil
}

// String returns the canonical string representation of the CountryCode.
func (cc *CountryCode) String() string {
	return cc.value
}

// Value returns the underlying string value of the CountryCode.
func (cc *CountryCode) Value() string {
	return cc.value
}

func isValidCountryCode(code string) bool {
	if len(code) != 2 {
		return false
	}
	region, err := language.ParseRegion(code)
	if err != nil {
		return false
	}
	return region.IsCountry()
}
