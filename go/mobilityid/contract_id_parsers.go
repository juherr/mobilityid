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

// Common regex parts
var (
	countryCodeRegexStr = `([A-Z]{2})`
	partyCodeRegexStr   = `([A-Z0-9]{3})`
	checkDigitRegexStr  = `([A-Z0-9])` // Single alphanumeric character for check digit
	instanceRegex9Char  = `([A-Z0-9]{9})`
	instanceRegex6Char  = `([A-Z0-9]{6})`
	instanceRegexEMI3   = `([C][A-Z0-9]{8})` // Normalized to uppercase 'C'
)

// ISOParser implements ContractIDParser for ISO standard.
type isoParser struct {
	instanceRegex *regexp.Regexp
	fullRegex     *regexp.Regexp
}

func (p isoParser) InstanceRegex() *regexp.Regexp { return p.instanceRegex }
func (p isoParser) FullRegex() *regexp.Regexp     { return p.fullRegex }
func (p isoParser) Name() string                  { return "ISO 15118-1" }
func (p isoParser) ComputeCheckDigit(s string) (rune, error) {
	cd, err := CalculateISO7064Mod37_2(s)
	if err != nil {
		return 0, err
	}
	return rune(cd[0]), nil // Check digit is a single char
}
func (p isoParser) ValidateInstanceValue(s string) error {
	if !p.instanceRegex.MatchString(s) {
		return fmt.Errorf("'%s' is not a valid instance value for %s format (expected 9 alphanumeric)", s, p.Name())
	}
	return nil
}

// EMI3Parser implements ContractIDParser for EMI3 standard.
type emi3Parser struct {
	instanceRegex *regexp.Regexp
	fullRegex     *regexp.Regexp
}

func (p emi3Parser) InstanceRegex() *regexp.Regexp { return p.instanceRegex }
func (p emi3Parser) FullRegex() *regexp.Regexp     { return p.fullRegex }
func (p emi3Parser) Name() string                  { return "EMI3" }
func (p emi3Parser) ComputeCheckDigit(s string) (rune, error) {
	cd, err := CalculateISO7064Mod37_2(s) // EMI3 also uses ISO check digit
	if err != nil {
		return 0, err
	}
	return rune(cd[0]), nil
}
func (p emi3Parser) ValidateInstanceValue(s string) error {
	if !p.instanceRegex.MatchString(s) {
		return fmt.Errorf("'%s' is not a valid instance value for %s format (expected 'C' + 8 alphanumeric)", s, p.Name())
	}
	return nil
}

// DINParser implements ContractIDParser for DIN standard.
type dinParser struct {
	instanceRegex *regexp.Regexp
	fullRegex     *regexp.Regexp
}

func (p dinParser) InstanceRegex() *regexp.Regexp { return p.instanceRegex }
func (p dinParser) FullRegex() *regexp.Regexp     { return p.fullRegex }
func (p dinParser) Name() string                  { return "DIN SPEC 91286" }
func (p dinParser) ComputeCheckDigit(s string) (rune, error) {
	cd, err := CalculateDIN7064ModXY(s)
	if err != nil {
		return 0, err
	}
	return rune(cd[0]), nil
}
func (p dinParser) ValidateInstanceValue(s string) error {
	if !p.instanceRegex.MatchString(s) {
		return fmt.Errorf("'%s' is not a valid instance value for %s format (expected 6 alphanumeric)", s, p.Name())
	}
	return nil
}

// Global parser instances
var (
	ISOParser  ContractIDParser
	EMI3Parser ContractIDParser
	DINParser  ContractIDParser
)

func init() {
	// ISO Parser initialization
	isoInstanceRegex := regexp.MustCompile(instanceRegex9Char)
	isoFullRegexStr := "^" + countryCodeRegexStr + "(?:-?)" + partyCodeRegexStr + "(?:-?)" + instanceRegex9Char + "(?:(?:-?)" + checkDigitRegexStr + ")?$"
	isoFullRegex := regexp.MustCompile(isoFullRegexStr)
	ISOParser = isoParser{
		instanceRegex: isoInstanceRegex,
		fullRegex:     isoFullRegex,
	}

	// EMI3 Parser initialization
	emi3InstanceRegex := regexp.MustCompile(instanceRegexEMI3)
	emi3FullRegexStr := "^" + countryCodeRegexStr + "(?:-?)" + partyCodeRegexStr + "(?:-?)" + instanceRegexEMI3 + "(?:(?:-?)" + checkDigitRegexStr + ")?$"
	emi3FullRegex := regexp.MustCompile(emi3FullRegexStr)
	EMI3Parser = emi3Parser{
		instanceRegex: emi3InstanceRegex,
		fullRegex:     emi3FullRegex,
	}

	// DIN Parser initialization
	dinInstanceRegex := regexp.MustCompile(instanceRegex6Char)
	dinFullRegexStr := "^" + countryCodeRegexStr + "(?:[*-]?)" + partyCodeRegexStr + "(?:[*-]?)" + instanceRegex6Char + "(?:(?:[*-]?)" + checkDigitRegexStr + ")?$"
	dinFullRegex := regexp.MustCompile(dinFullRegexStr)
	DINParser = dinParser{
		instanceRegex: dinInstanceRegex,
		fullRegex:     dinFullRegex,
	}
}
