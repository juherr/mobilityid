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
		err   error
		want  []error
		input string
	}{
		{
			name:  "country code",
			err:   errOf(NewCountryCode("ZZ")),
			want:  []error{ErrInvalidCountryCode},
			input: "ZZ",
		},
		{
			name:  "phone country code",
			err:   errOf(NewPhoneCountryCode("1234")),
			want:  []error{ErrInvalidPhoneCountryCode},
			input: "1234",
		},
		{
			name:  "provider id",
			err:   errOf(NewProviderID("TN")),
			want:  []error{ErrInvalidProviderID},
			input: "TN",
		},
		{
			name:  "operator id iso",
			err:   errOf(NewOperatorIDISO("A|7")),
			want:  []error{ErrInvalidOperatorID},
			input: "A|7",
		},
		{
			name:  "operator id din",
			err:   errOf(NewOperatorIDDIN("AB7")),
			want:  []error{ErrInvalidOperatorID},
			input: "AB7",
		},
		{
			name:  "party id format",
			err:   errOf(NewPartyID("NL-TNM-X")),
			want:  []error{ErrInvalidPartyID},
			input: "NL-TNM-X",
		},
		{
			name:  "party id with unknown country",
			err:   errOf(NewPartyID("ZZTNM")),
			want:  []error{ErrInvalidPartyID, ErrInvalidCountryCode},
			input: "ZZTNM",
		},
		{
			name:  "contract id too short",
			err:   errOf(NewContractID("NL", ContractIDStandardISO)),
			want:  []error{ErrInvalidContractID},
			input: "NL",
		},
		{
			name:  "contract id format",
			err:   errOf(NewContractID("NL-TNM-00012204-U", ContractIDStandardISO)),
			want:  []error{ErrInvalidContractID},
			input: "NL-TNM-00012204-U",
		},
		{
			name:  "contract id with unknown country",
			err:   errOf(NewContractID("ZZ-TNM-000122045-U", ContractIDStandardISO)),
			want:  []error{ErrInvalidContractID, ErrInvalidCountryCode},
			input: "ZZ-TNM-000122045-U",
		},
		{
			name:  "contract id instance value",
			err:   errOf(NewContractID("NL-TNM-000122045-U", ContractIDStandardEMI3)),
			want:  []error{ErrInvalidContractID},
			input: "000122045",
		},
		{
			name:  "contract id check digit mismatch",
			err:   errOf(NewContractID("NL-TNM-000122045-X", ContractIDStandardISO)),
			want:  []error{ErrInvalidContractID, ErrInvalidCheckDigit},
			input: "NL-TNM-000122045-X",
		},
		{
			name:  "iso check digit input character",
			err:   errOf(CalculateISO7064Mod37_2("NLTNM00012204*")),
			want:  []error{ErrInvalidCheckDigitInput},
			input: "NLTNM00012204*",
		},
		{
			name:  "iso check digit input length",
			err:   errOf(CalculateISO7064Mod37_2("NLTNM")),
			want:  []error{ErrInvalidCheckDigitInput},
			input: "NLTNM",
		},
		{
			name:  "din check digit input character",
			err:   errOf(CalculateDIN7064ModXY("NLTNM12204*")),
			want:  []error{ErrInvalidCheckDigitInput},
			input: "NLTNM12204*",
		},
		{
			name:  "evse id",
			err:   errOf(NewEvseID("not an evse id")),
			want:  []error{ErrInvalidEvseID},
			input: "not an evse id",
		},
		{
			name:  "evse id iso with unknown country",
			err:   errOf(NewEvseIDISO("ZZ*TNM*E840*6487")),
			want:  []error{ErrInvalidEvseID, ErrInvalidCountryCode},
			input: "ZZ*TNM*E840*6487",
		},
		{
			name:  "evse id din from parts with invalid phone country code",
			err:   errOf(NewEvseIDDINFromParts("1234", "745", "840*6487")),
			want:  []error{ErrInvalidEvseID, ErrInvalidPhoneCountryCode},
			input: "+1234",
		},
		{
			name:  "evse id iso from parts power outlet",
			err:   errOf(NewEvseIDISOFromParts("NL", "TNM", "840|6487")),
			want:  []error{ErrInvalidEvseID},
			input: "840|6487",
		},
		{
			name:  "evse id din from parts operator",
			err:   errOf(NewEvseIDDINFromParts("31", "AB7", "840*6487")),
			want:  []error{ErrInvalidEvseID, ErrInvalidOperatorID},
			input: "AB7",
		},
		{
			name:  "evse id from parts",
			err:   errOf(NewEvseIDFromParts("ZZ", "TNM", "840*6487")),
			want:  []error{ErrInvalidEvseID},
			input: "ZZ",
		},
		{
			name:  "unconvertible iso contract id",
			err:   errOf(must(NewContractID("NL-TNM-123122045-R", ContractIDStandardISO)).ToDIN()),
			want:  []error{ErrUnconvertibleContractID},
			input: "NL-TNM-123122045-R",
		},
		{
			name:  "unsupported standard",
			err:   errOf(NewContractID("NL-TNM-000122045-U", unsupportedStandard{})),
			want:  []error{ErrInvalidContractID, ErrUnsupportedStandard},
			input: "unsupported",
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			if tt.err == nil {
				t.Fatal("expected an error")
			}
			for _, want := range tt.want {
				if !errors.Is(tt.err, want) {
					t.Errorf("errors.Is(%v, %v) = false", tt.err, want)
				}
			}
			if !strings.Contains(tt.err.Error(), tt.input) {
				t.Errorf("error %q does not name the input %q", tt.err, tt.input)
			}
		})
	}
}

// errOf keeps only the error of a constructor call, for the table above.
func errOf[T any](_ T, err error) error { return err }

// unsupportedStandard cannot be built outside the package; it stands for a standard the
// conversions do not know about.
type unsupportedStandard struct{}

func (unsupportedStandard) isContractIDStandard() {}
func (unsupportedStandard) String() string        { return "unsupported" }
