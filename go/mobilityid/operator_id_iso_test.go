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

func TestNewOperatorIDISO(t *testing.T) {
	tests := []struct {
		input   string
		want    string
		wantErr bool
	}{
		{input: "TNM", want: "TNM"},
		{input: "ab7", want: "AB7"},
		{input: "", wantErr: true},
		{input: "AB", wantErr: true},
		{input: "ABCD", wantErr: true},
		{input: "A|7", wantErr: true},
	}

	for _, tt := range tests {
		oid, err := NewOperatorIDISO(tt.input)
		if (err != nil) != tt.wantErr {
			t.Fatalf("NewOperatorIDISO(%q) error = %v, wantErr %v", tt.input, err, tt.wantErr)
		}
		if !tt.wantErr && oid.String() != tt.want {
			t.Fatalf("NewOperatorIDISO(%q) got = %v, want %v", tt.input, oid.String(), tt.want)
		}
	}
}

func TestCalculateISO7064Mod37_2(t *testing.T) {
	tests := []struct {
		input   string
		wantCD  string
		wantErr bool
	}{
		{input: "NN123ABCDEFGHI", wantCD: "T"},
		{input: "FRXYZ123456789", wantCD: "2"},
		{input: "ITA1B2C3E4F5G6", wantCD: "4"},
		{input: "ESZU8WOX834H1D", wantCD: "R"},
		{input: "PT73902837ABCZ", wantCD: "Z"},
		{input: "DE83DUIEN83QGZ", wantCD: "D"},
		{input: "DE83DUIEN83ZGQ", wantCD: "M"},
		{input: "DE8AA001234567", wantCD: "0"},
		{input: "", wantErr: true},
		{input: "SHORT", wantErr: true},
		{input: "\u0415\u0432\u0440\u043e\u043f\u0430\u0440\u0443\u043b\u0438\u0442123", wantErr: true},
		{input: "DE\u0668\u0663DUIEN\u0668\u0663QGZ", wantErr: true},
		{input: "\u00c5\u220f@*(Td\U0001F63BgaR^&(%", wantErr: true},
		{input: "\u00c5\u220f@*(Td\U0001F63BgR^&(%", wantErr: true},
		{input: "DE8AA0012345678", wantErr: true},
	}

	for _, tt := range tests {
		got, err := CalculateISO7064Mod37_2(tt.input)
		if (err != nil) != tt.wantErr {
			t.Fatalf("CalculateISO7064Mod37_2(%q) error = %v, wantErr %v", tt.input, err, tt.wantErr)
		}
		if !tt.wantErr && got != tt.wantCD {
			t.Fatalf("CalculateISO7064Mod37_2(%q) got = %v, want %v", tt.input, got, tt.wantCD)
		}
	}
}

func TestVerifyISO7064Mod37_2(t *testing.T) {
	if !VerifyISO7064Mod37_2("NN123ABCDEFGHIT") {
		t.Fatal("expected valid ISO check digit")
	}
	if !VerifyISO7064Mod37_2("nn123abcdefghit") {
		t.Fatal("expected lowercase ISO check digit input to be valid")
	}
	if VerifyISO7064Mod37_2("NN123ABCDEFGHIX") {
		t.Fatal("expected invalid ISO check digit")
	}
}
