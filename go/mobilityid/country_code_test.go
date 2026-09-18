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

func TestNewCountryCode(t *testing.T) {
	tests := []struct {
		name    string
		input   string
		want    string
		wantErr bool
	}{
		{name: "valid uppercase", input: "US", want: "US"},
		{name: "valid lowercase", input: "nl", want: "NL"},
		{name: "invalid empty", input: "", wantErr: true},
		{name: "invalid too short", input: "U", wantErr: true},
		{name: "invalid too long", input: "USA", wantErr: true},
		{name: "invalid unknown", input: "ZZ", wantErr: true},
		// Assigned in ISO 3166-1 but easy to get wrong.
		{name: "valid Aland Islands", input: "AX", want: "AX"},
		{name: "valid Caribbean Netherlands", input: "BQ", want: "BQ"},
		{name: "valid Curacao", input: "CW", want: "CW"},
		{name: "valid South Sudan", input: "SS", want: "SS"},
		{name: "valid United Kingdom", input: "GB", want: "GB"},
		// Region codes that CLDR knows but ISO 3166-1 does not assign; the Scala, Java, PHP and
		// TypeScript ports reject them.
		{name: "invalid exceptional reservation UK", input: "UK", wantErr: true},
		{name: "invalid exceptional reservation EU", input: "EU", wantErr: true},
		{name: "invalid user-assigned XK", input: "XK", wantErr: true},
		{name: "invalid transitional AN", input: "AN", wantErr: true},
		{name: "invalid formerly used SU", input: "SU", wantErr: true},
		{name: "invalid formerly used DD", input: "DD", wantErr: true},
		{name: "invalid formerly used YU", input: "YU", wantErr: true},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			cc, err := NewCountryCode(tt.input)
			if (err != nil) != tt.wantErr {
				t.Fatalf("NewCountryCode() error = %v, wantErr %v", err, tt.wantErr)
			}
			if !tt.wantErr && cc.String() != tt.want {
				t.Fatalf("NewCountryCode() got = %v, want %v", cc.String(), tt.want)
			}
		})
	}
}

// The table is generated (scripts/generate-iso3166.sh) and the generator does not run in CI.
// This guards against a truncated or malformed file (count and entry shape); it cannot tell a
// same-size substitution of one code by another, which the explicit cases above cover for the
// codes that matter. Locale.getISOCountries() lists 249 codes; bump the constant with the JDK
// when a code is assigned or withdrawn.
func TestISO3166Alpha2TableIntegrity(t *testing.T) {
	const wantCount = 249
	if got := len(iso3166Alpha2); got != wantCount {
		t.Fatalf("len(iso3166Alpha2) = %d, want %d", got, wantCount)
	}
	for code := range iso3166Alpha2 {
		if len(code) != 2 || code[0] < 'A' || code[0] > 'Z' || code[1] < 'A' || code[1] > 'Z' {
			t.Errorf("iso3166Alpha2 contains %q, want two uppercase letters", code)
		}
	}
}
