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

import "testing"

// The ISO parsers are case-insensitive on ASCII letters, the E id type included, and render in
// uppercase (issue #92). The vectors are shared with the Scala, Java, TypeScript and PHP ports.
func TestEvseIDISOIsCaseInsensitive(t *testing.T) {
	tests := []struct {
		input string
		want  string
	}{
		{input: "fr*55c*e84400*svg*1", want: "FR*55C*E84400*SVG*1"},
		{input: "Fr*55C*e84400*SvG*1", want: "FR*55C*E84400*SVG*1"},
		{input: "fr55ce844001", want: "FR*55C*E844001"},
	}

	for _, tt := range tests {
		t.Run(tt.input, func(t *testing.T) {
			iso, err := NewEvseIDISO(tt.input)
			if err != nil {
				t.Fatalf("NewEvseIDISO(%q) unexpected error: %v", tt.input, err)
			}
			if got := iso.String(); got != tt.want {
				t.Fatalf("NewEvseIDISO(%q) = %v, want %v", tt.input, got, tt.want)
			}
			evse, err := NewEvseID(tt.input)
			if err != nil {
				t.Fatalf("NewEvseID(%q) unexpected error: %v", tt.input, err)
			}
			if got := evse.String(); got != tt.want {
				t.Fatalf("NewEvseID(%q) = %v, want %v", tt.input, got, tt.want)
			}
		})
	}
}

// strings.ToUpper maps some non-ASCII letters to ASCII ones (U+017F 'ſ' to 'S', U+0131 'ı' to
// 'I'). Uppercasing before matching would accept them; like the other ports, only ASCII letters
// are case-folded.
func TestParsersRejectNonASCIILettersThatUppercaseToASCII(t *testing.T) {
	tests := []struct {
		name  string
		parse func() error
	}{
		{name: "country code", parse: func() error { _, err := NewCountryCode("ſe"); return err }},
		{name: "provider id", parse: func() error { _, err := NewProviderID("ſVG"); return err }},
		{name: "ISO operator id", parse: func() error { _, err := NewOperatorIDISO("ıBM"); return err }},
		{name: "party id", parse: func() error { _, err := NewPartyID("NL*ſVG"); return err }},
		{name: "ISO contract id", parse: func() error {
			_, err := NewContractID("NL-ſVG-000122045", ContractIDStandardISO)
			return err
		}},
		{name: "ISO EVSE id", parse: func() error { _, err := NewEvseIDISO("FR*55C*E84400*ſVG*1"); return err }},
		{name: "ISO EVSE id, dotless i", parse: func() error { _, err := NewEvseIDISO("FR*55C*E84400*ıBM*1"); return err }},
		{name: "EVSE id", parse: func() error { _, err := NewEvseID("FR*55C*E84400*ſVG*1"); return err }},
		{name: "ISO EVSE id from parts", parse: func() error {
			_, err := NewEvseIDISOFromParts("FR", "55C", "84400*ſVG*1")
			return err
		}},
		{name: "check digit", parse: func() error { _, err := CalculateISO7064Mod37_2("NLſVG000122045"); return err }},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			if err := tt.parse(); err == nil {
				t.Fatal("expected a non-ASCII letter to be rejected")
			}
		})
	}
}
