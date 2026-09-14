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

// OperatorIDISO represents an ISO 15118-compliant operator identifier.
// It consists of a CountryCode and a local OperatorID, optionally with a check digit.
type OperatorIDISO struct {
	value string
}

// NewOperatorIDISO creates a new OperatorIDISO if the provided value is valid.
// It returns an error if the value does not conform to the expected format and validation rules.
func NewOperatorIDISO(id string) (*OperatorIDISO, error) {
	normalized := strings.ToUpper(id)
	if !isValidOperatorIDISO(normalized) {
		return nil, fmt.Errorf("'%s' is not a valid ISO 15118 OperatorID", id)
	}
	return &OperatorIDISO{value: normalized}, nil
}

// String returns the canonical string representation of the OperatorIDISO.
func (oid *OperatorIDISO) String() string {
	return oid.value
}

// Value returns the underlying string value of the OperatorIDISO.
func (oid *OperatorIDISO) Value() string {
	return oid.value
}

func isValidOperatorIDISO(id string) bool {
	return partyCodeRegex.MatchString(id)
}
