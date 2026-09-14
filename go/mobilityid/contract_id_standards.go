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
	"regexp"
)

// ContractIDStandard represents the standard under which a ContractID is issued.
// It is an interface to allow for distinct types for each standard, similar to Scala's traits.
type ContractIDStandard interface {
	isContractIDStandard() // Unexported method to prevent external implementations
	String() string
}

// Internal concrete types for each standard
type isoStandard struct{}
type emi3Standard struct{}
type dinStandard struct{}

func (isoStandard) isContractIDStandard()  {}
func (emi3Standard) isContractIDStandard() {}
func (dinStandard) isContractIDStandard()  {}

func (isoStandard) String() string  { return "ISO" }
func (emi3Standard) String() string { return "EMI3" }
func (dinStandard) String() string  { return "DIN" }

// Exported instances for each standard
var (
	ContractIDStandardISO  ContractIDStandard = isoStandard{}
	ContractIDStandardEMI3 ContractIDStandard = emi3Standard{}
	ContractIDStandardDIN  ContractIDStandard = dinStandard{}
)

// ContractIDParser defines the interface for parsing and validating ContractIDs based on a specific standard.
type ContractIDParser interface {
	InstanceRegex() *regexp.Regexp            // Regex for the instance value (local ID)
	FullRegex() *regexp.Regexp                // Regex for the full ContractID string
	Name() string                             // Name of the standard
	ComputeCheckDigit(s string) (rune, error) // Computes the check digit
	ValidateInstanceValue(s string) error     // Validates the instance value part
}
