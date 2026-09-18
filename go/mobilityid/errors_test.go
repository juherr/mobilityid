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
	"errors"
	"strings"
	"testing"
)

func TestSentinelErrors(t *testing.T) {
	tests := []struct {
		name  string
		call  func() error
		want  []error
		input string
	}{
		{
			name:  "country code",
			call:  func() error { _, err := NewCountryCode("ZZ"); return err },
			want:  []error{ErrInvalidCountryCode},
			input: "ZZ",
		},
		{
			name:  "phone country code",
			call:  func() error { _, err := NewPhoneCountryCode("1234"); return err },
			want:  []error{ErrInvalidPhoneCountryCode},
			input: "1234",
		},
		{
			name:  "provider id",
			call:  func() error { _, err := NewProviderID("TN"); return err },
			want:  []error{ErrInvalidProviderID},
			input: "TN",
		},
		{
			name:  "operator id iso",
			call:  func() error { _, err := NewOperatorIDISO("A|7"); return err },
			want:  []error{ErrInvalidOperatorID},
			input: "A|7",
		},
		{
			name:  "operator id din",
			call:  func() error { _, err := NewOperatorIDDIN("AB7"); return err },
			want:  []error{ErrInvalidOperatorID},
			input: "AB7",
		},
		{
			name:  "party id format",
			call:  func() error { _, err := NewPartyID("NL-TNM-X"); return err },
			want:  []error{ErrInvalidPartyID},
			input: "NL-TNM-X",
		},
		{
			name:  "party id with unknown country",
			call:  func() error { _, err := NewPartyID("ZZTNM"); return err },
			want:  []error{ErrInvalidPartyID, ErrInvalidCountryCode},
			input: "ZZTNM",
		},
		{
			name:  "contract id too short",
			call:  func() error { _, err := NewContractID("NL", ContractIDStandardISO); return err },
			want:  []error{ErrInvalidContractID},
			input: "NL",
		},
		{
			name:  "contract id format",
			call:  func() error { _, err := NewContractID("NL-TNM-00012204-U", ContractIDStandardISO); return err },
			want:  []error{ErrInvalidContractID},
			input: "NL-TNM-00012204-U",
		},
		{
			name:  "contract id with unknown country",
			call:  func() error { _, err := NewContractID("ZZ-TNM-000122045-U", ContractIDStandardISO); return err },
			want:  []error{ErrInvalidContractID, ErrInvalidCountryCode},
			input: "ZZ-TNM-000122045-U",
		},
		{
			name:  "contract id instance value",
			call:  func() error { _, err := NewContractID("NL-TNM-000122045-U", ContractIDStandardEMI3); return err },
			want:  []error{ErrInvalidContractID},
			input: "000122045",
		},
		{
			name:  "contract id check digit mismatch",
			call:  func() error { _, err := NewContractID("NL-TNM-000122045-X", ContractIDStandardISO); return err },
			want:  []error{ErrInvalidCheckDigit},
			input: "NL-TNM-000122045-X",
		},
		{
			name:  "iso check digit input character",
			call:  func() error { _, err := CalculateISO7064Mod37_2("NLTNM00012204*"); return err },
			want:  []error{ErrInvalidCheckDigitInput},
			input: "NLTNM00012204*",
		},
		{
			name:  "iso check digit input length",
			call:  func() error { _, err := CalculateISO7064Mod37_2("NLTNM"); return err },
			want:  []error{ErrInvalidCheckDigitInput},
			input: "5",
		},
		{
			name:  "din check digit input character",
			call:  func() error { _, err := CalculateDIN7064ModXY("NLTNM12204*"); return err },
			want:  []error{ErrInvalidCheckDigitInput},
			input: "NLTNM12204*",
		},
		{
			name:  "evse id",
			call:  func() error { _, err := NewEvseID("not an evse id"); return err },
			want:  []error{ErrInvalidEvseID},
			input: "not an evse id",
		},
		{
			name:  "evse id iso with unknown country",
			call:  func() error { _, err := NewEvseIDISO("ZZ*TNM*E840*6487"); return err },
			want:  []error{ErrInvalidEvseID, ErrInvalidCountryCode},
			input: "ZZ*TNM*E840*6487",
		},
		{
			name:  "evse id din from parts with invalid phone country code",
			call:  func() error { _, err := NewEvseIDDINFromParts("1234", "745", "840*6487"); return err },
			want:  []error{ErrInvalidEvseID, ErrInvalidPhoneCountryCode},
			input: "+1234",
		},
		{
			name:  "evse id iso from parts power outlet",
			call:  func() error { _, err := NewEvseIDISOFromParts("NL", "TNM", "840|6487"); return err },
			want:  []error{ErrInvalidEvseID},
			input: "840|6487",
		},
		{
			name:  "evse id din from parts operator",
			call:  func() error { _, err := NewEvseIDDINFromParts("31", "AB7", "840*6487"); return err },
			want:  []error{ErrInvalidEvseID, ErrInvalidOperatorID},
			input: "AB7",
		},
		{
			name:  "evse id from parts",
			call:  func() error { _, err := NewEvseIDFromParts("ZZ", "TNM", "840*6487"); return err },
			want:  []error{ErrInvalidEvseID},
			input: "ZZ",
		},
		{
			name: "unconvertible iso contract id",
			call: func() error {
				cid, err := NewContractID("NL-TNM-123122045-R", ContractIDStandardISO)
				if err != nil {
					return err
				}
				_, err = cid.ToDIN()
				return err
			},
			want:  []error{ErrUnconvertibleContractID},
			input: "NL-TNM-123122045-R",
		},
		{
			name: "unsupported standard",
			call: func() error {
				_, err := NewContractID("NL-TNM-000122045-U", unsupportedStandard{})
				return err
			},
			want:  []error{ErrUnsupportedStandard},
			input: "unsupported",
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			err := tt.call()
			if err == nil {
				t.Fatal("expected an error")
			}
			for _, want := range tt.want {
				if !errors.Is(err, want) {
					t.Errorf("errors.Is(%v, %v) = false", err, want)
				}
			}
			if !strings.Contains(err.Error(), tt.input) {
				t.Errorf("error %q does not name the input %q", err, tt.input)
			}
		})
	}
}

// unsupportedStandard cannot be built outside the package; it stands for a standard the
// conversions do not know about.
type unsupportedStandard struct{}

func (unsupportedStandard) isContractIDStandard() {}
func (unsupportedStandard) String() string        { return "unsupported" }
