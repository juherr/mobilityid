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

// OperatorIDDIN represents a DIN-compliant operator identifier.
// It typically consists of a country code and a local operator identifier,
// and may include a check digit according to DIN specifications.
type OperatorIDDIN struct {
	value string
}

var operatorIDDINRegex = regexp.MustCompile(`^[0-9]{3,6}$`)

// NewOperatorIDDIN creates a new OperatorIDDIN if the provided value is valid.
// It returns an error if the value does not conform to the expected format and validation rules.
func NewOperatorIDDIN(id string) (*OperatorIDDIN, error) {
	normalized := strings.ToUpper(id)
	if !isValidOperatorIDDIN(normalized) {
		return nil, fmt.Errorf("'%s' is not a valid DIN OperatorID", id)
	}
	return &OperatorIDDIN{value: normalized}, nil
}

// String returns the canonical string representation of the OperatorIDDIN.
func (did *OperatorIDDIN) String() string {
	return did.value
}

// Value returns the underlying string value of the OperatorIDDIN.
func (did *OperatorIDDIN) Value() string {
	return did.value
}

func isValidOperatorIDDIN(id string) bool {
	return operatorIDDINRegex.MatchString(id)
}
