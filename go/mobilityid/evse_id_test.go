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

func TestNewEvseIDISO(t *testing.T) {
	tests := []struct {
		name  string
		input string
		want  string
	}{
		{name: "with separators", input: "DE*AB7*E840*6487", want: "DE*AB7*E840*6487"},
		{name: "without separators", input: "DEAB7E8406487", want: "DE*AB7*E8406487"},
		{name: "minimum length", input: "DEAB7E1", want: "DE*AB7*E1"},
		{name: "maximum length", input: "DE*AB7*E1234567890ABCDEFGHIJ1234567890", want: "DE*AB7*E1234567890ABCDEFGHIJ1234567890"},
		{name: "asterisk after E", input: "DE*DES*E*BMW*0113*2", want: "DE*DES*E*BMW*0113*2"},
		{name: "long real example", input: "DE*TNM*ETWL*HEDWIGLAUDIENRING*LS12001*0", want: "DE*TNM*ETWL*HEDWIGLAUDIENRING*LS12001*0"},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			eid, err := NewEvseIDISO(tt.input)
			if err != nil {
				t.Fatalf("NewEvseIDISO() unexpected error: %v", err)
			}
			if got := eid.String(); got != tt.want {
				t.Fatalf("EvseIDISO.String() = %v, want %v", got, tt.want)
			}
		})
	}
}

func TestNewEvseIDISOInvalid(t *testing.T) {
	tests := []string{
		"DE*AB7*E12345678901234567890123456789012",
		"NL*TNM*840*6487",
		"+49*810*000*438",
		"ZZ*TNM*840*64878",
	}

	for _, input := range tests {
		t.Run(input, func(t *testing.T) {
			if _, err := NewEvseIDISO(input); err == nil {
				t.Fatalf("NewEvseIDISO(%q) expected error", input)
			}
		})
	}
}

func TestNewEvseIDDIN(t *testing.T) {
	tests := []struct {
		name  string
		input string
		want  string
	}{
		{name: "without plus", input: "49*810*000*438", want: "+49*810*000*438"},
		{name: "with plus", input: "+49*810*000*438", want: "+49*810*000*438"},
		{name: "minimum length", input: "+49*810*1", want: "+49*810*1"},
		{name: "maximum length", input: "+49*810*12345678901234567890123456789012", want: "+49*810*12345678901234567890123456789012"},
		{name: "operator 6 chars", input: "+49*810548*1234567890", want: "+49*810548*1234567890"},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			eid, err := NewEvseIDDIN(tt.input)
			if err != nil {
				t.Fatalf("NewEvseIDDIN() unexpected error: %v", err)
			}
			if got := eid.String(); got != tt.want {
				t.Fatalf("EvseIDDIN.String() = %v, want %v", got, tt.want)
			}
		})
	}
}

func TestNewEvseIDDINInvalid(t *testing.T) {
	tests := []string{
		"+49*810*123456789012345678901234567890123",
		"+49*AB7*840*6487",
		"+49*645*E840*6487",
		"DE*AB7*E840*6487",
		"+4A*810*000*438",
	}

	for _, input := range tests {
		t.Run(input, func(t *testing.T) {
			if _, err := NewEvseIDDIN(input); err == nil {
				t.Fatalf("NewEvseIDDIN(%q) expected error", input)
			}
		})
	}
}

func TestNewEvseIDAutoDetect(t *testing.T) {
	iso, err := NewEvseID("DE*AB7*E840*6487")
	if err != nil {
		t.Fatalf("NewEvseID() ISO unexpected error: %v", err)
	}
	if got := iso.String(); got != "DE*AB7*E840*6487" {
		t.Fatalf("auto ISO = %v", got)
	}

	din, err := NewEvseID("+49*810*000*438")
	if err != nil {
		t.Fatalf("NewEvseID() DIN unexpected error: %v", err)
	}
	if got := din.String(); got != "+49*810*000*438" {
		t.Fatalf("auto DIN = %v", got)
	}

	if _, err := NewEvseID("ZZ*AB7*E840*6487"); err == nil {
		t.Fatal("expected invalid EVSE to fail")
	}
}

func TestNewEvseIDFromParts(t *testing.T) {
	iso, err := NewEvseIDFromParts("NL", "TNM", "E840*6487")
	if err != nil {
		t.Fatalf("NewEvseIDFromParts() ISO unexpected error: %v", err)
	}
	if got := iso.String(); got != "NL*TNM*E840*6487" {
		t.Fatalf("ISO from parts = %v", got)
	}
	if !iso.IsISO() || iso.IsDIN() {
		t.Fatal("expected ISO EvseID kind")
	}
	if iso.CountryCode() != "NL" || iso.OperatorID() != "TNM" || iso.PowerOutletID() != "840*6487" {
		t.Fatalf("unexpected ISO components: %s %s %s", iso.CountryCode(), iso.OperatorID(), iso.PowerOutletID())
	}

	din, err := NewEvseIDFromParts("+31", "745", "840*6487")
	if err != nil {
		t.Fatalf("NewEvseIDFromParts() DIN unexpected error: %v", err)
	}
	if got := din.String(); got != "+31*745*840*6487" {
		t.Fatalf("DIN from parts = %v", got)
	}
	if !din.IsDIN() || din.IsISO() {
		t.Fatal("expected DIN EvseID kind")
	}
	if din.CountryCode() != "+31" || din.OperatorID() != "745" || din.PowerOutletID() != "840*6487" {
		t.Fatalf("unexpected DIN components: %s %s %s", din.CountryCode(), din.OperatorID(), din.PowerOutletID())
	}
}

func TestNewEvseIDFormatSpecificFromParts(t *testing.T) {
	iso, err := NewEvseIDISOFromParts("NL", "TNM", "E840*6487")
	if err != nil {
		t.Fatalf("NewEvseIDISOFromParts() unexpected error: %v", err)
	}
	if got := iso.String(); got != "NL*TNM*E840*6487" {
		t.Fatalf("ISO from parts = %v", got)
	}

	din, err := NewEvseIDDINFromParts("31", "745", "840*6487")
	if err != nil {
		t.Fatalf("NewEvseIDDINFromParts() unexpected error: %v", err)
	}
	if got := din.String(); got != "+31*745*840*6487" {
		t.Fatalf("DIN from parts = %v", got)
	}
}

func TestNewEvseIDFromPartsInvalid(t *testing.T) {
	if _, err := NewEvseIDFromParts("+31", "ABC", "840*6487"); err == nil {
		t.Fatal("expected mixed ISO/DIN parameters to fail")
	}
	if _, err := NewEvseIDFromParts("+31", "745", "E840*6487"); err == nil {
		t.Fatal("expected DIN power outlet with E prefix to fail")
	}
	if _, err := NewEvseIDFromParts("A", "TNM", "000122045"); err == nil {
		t.Fatal("expected invalid country code to fail")
	}
}

func TestEvseISOHelpers(t *testing.T) {
	eid, err := NewEvseIDISO("NL*TNM*E840*6487")
	if err != nil {
		t.Fatalf("NewEvseIDISO() unexpected error: %v", err)
	}
	if got := eid.ToCompactString(); got != "NLTNME8406487" {
		t.Fatalf("ToCompactString() = %v", got)
	}
	partyID, err := eid.PartyID()
	if err != nil {
		t.Fatalf("PartyID() unexpected error: %v", err)
	}
	if got := partyID.Value(); got != "NLTNM" {
		t.Fatalf("party id = %v", got)
	}
}
