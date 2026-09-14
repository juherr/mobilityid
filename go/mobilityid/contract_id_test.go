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

func TestNewContractIDISOParsing(t *testing.T) {
	tests := []struct {
		name  string
		input string
		want  string
	}{
		{name: "normalized", input: "NL-TNM-000122045-U", want: "NL-TNM-000122045-U"},
		{name: "compact with cd", input: "NLTNM000122045U", want: "NL-TNM-000122045-U"},
		{name: "without cd", input: "NLTNM000122045", want: "NL-TNM-000122045-U"},
		{name: "case insensitive", input: "nl-tnm-000122045-u", want: "NL-TNM-000122045-U"},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			cid, err := NewContractID(tt.input, ContractIDStandardISO)
			if err != nil {
				t.Fatalf("NewContractID() unexpected error: %v", err)
			}
			if cid.String() != tt.want {
				t.Fatalf("ContractID.String() = %v, want %v", cid.String(), tt.want)
			}
		})
	}
}

func TestNewContractIDEMI3Parsing(t *testing.T) {
	tests := []struct {
		name  string
		input string
		want  string
	}{
		{name: "normalized", input: "NL-TNM-C00122045-K", want: "NL-TNM-C00122045-K"},
		{name: "compact with cd", input: "NLTNMC00122045K", want: "NL-TNM-C00122045-K"},
		{name: "without cd", input: "NLTNMC00122045", want: "NL-TNM-C00122045-K"},
		{name: "case insensitive", input: "nl-tnm-c00122045-k", want: "NL-TNM-C00122045-K"},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			cid, err := NewContractID(tt.input, ContractIDStandardEMI3)
			if err != nil {
				t.Fatalf("NewContractID() unexpected error: %v", err)
			}
			if cid.String() != tt.want {
				t.Fatalf("ContractID.String() = %v, want %v", cid.String(), tt.want)
			}
		})
	}
}

func TestNewContractIDDINParsing(t *testing.T) {
	tests := []struct {
		name  string
		input string
		want  string
	}{
		{name: "normalized", input: "NL-TNM-122045-0", want: "NL-TNM-122045-0"},
		{name: "with stars", input: "NL*TNM*122045*0", want: "NL-TNM-122045-0"},
		{name: "compact with cd", input: "NLTNM1220450", want: "NL-TNM-122045-0"},
		{name: "without cd", input: "NLTNM122045", want: "NL-TNM-122045-0"},
		{name: "case insensitive", input: "nl-tnm-122045-0", want: "NL-TNM-122045-0"},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			cid, err := NewContractID(tt.input, ContractIDStandardDIN)
			if err != nil {
				t.Fatalf("NewContractID() unexpected error: %v", err)
			}
			if cid.String() != tt.want {
				t.Fatalf("ContractID.String() = %v, want %v", cid.String(), tt.want)
			}
		})
	}
}

func TestNewContractIDInvalid(t *testing.T) {
	tests := []struct {
		name     string
		input    string
		standard ContractIDStandard
	}{
		{name: "wrong check digit", input: "NL-TNM-000122045-X", standard: ContractIDStandardISO},
		{name: "wrong length", input: "NLTNM076", standard: ContractIDStandardISO},
		{name: "wrong field lengths", input: "X-aargh-131331234", standard: ContractIDStandardISO},
		{name: "nonsense input", input: " \x00t24\u2396a\t", standard: ContractIDStandardISO},
		{name: "bad country", input: "XX-TNM-000122045-U", standard: ContractIDStandardISO},
		{name: "invalid provider", input: "NL-T|M-000122045-U", standard: ContractIDStandardISO},
		{name: "wrong emi3 instance", input: "NL-TNM-000122045-K", standard: ContractIDStandardEMI3},
		{name: "wrong din length", input: "NL-TNM-12204-0", standard: ContractIDStandardDIN},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			if _, err := NewContractID(tt.input, tt.standard); err == nil {
				t.Fatalf("NewContractID(%q) expected error", tt.input)
			}
		})
	}

	if cid, err := NewContractID("NL-TNM-012345678-W", ContractIDStandardISO); err != nil {
		t.Fatalf("NewContractID() unexpected error: %v", err)
	} else if _, err := cid.ToDIN(); err == nil {
		t.Fatal("ISO -> DIN should fail for non-DIN-compatible instance value")
	}
}

func TestContractIDRenderingAndPartyID(t *testing.T) {
	cid, err := NewContractID("NL-TNM-000722345-X", ContractIDStandardISO)
	if err != nil {
		t.Fatalf("NewContractID() unexpected error: %v", err)
	}

	if got := cid.String(); got != "NL-TNM-000722345-X" {
		t.Fatalf("String() = %v", got)
	}
	if got := cid.ToCompactString(); got != "NLTNM000722345X" {
		t.Fatalf("ToCompactString() = %v", got)
	}
	if got := cid.ToCompactStringWithoutCheckDigit(); got != "NLTNM000722345" {
		t.Fatalf("ToCompactStringWithoutCheckDigit() = %v", got)
	}

	partyID, err := cid.PartyID()
	if err != nil {
		t.Fatalf("PartyID() unexpected error: %v", err)
	}
	if got := partyID.Value(); got != "NLTNM" {
		t.Fatalf("party compact value = %v", got)
	}
}

func TestContractIDConversions(t *testing.T) {
	iso, err := NewContractID("NL-TNM-000122045-U", ContractIDStandardISO)
	if err != nil {
		t.Fatalf("NewContractID ISO unexpected error: %v", err)
	}
	din, err := iso.ToDIN()
	if err != nil {
		t.Fatalf("ISO -> DIN unexpected error: %v", err)
	}
	if got := din.String(); got != "NL-TNM-012204-5" {
		t.Fatalf("ISO -> DIN = %v", got)
	}

	iso2, err := din.ToISO()
	if err != nil {
		t.Fatalf("DIN -> ISO unexpected error: %v", err)
	}
	if got := iso2.String(); got != "NL-TNM-000122045-U" {
		t.Fatalf("DIN -> ISO = %v", got)
	}

	emi3, err := din.ToEMI3()
	if err != nil {
		t.Fatalf("DIN -> EMI3 unexpected error: %v", err)
	}
	if got := emi3.String(); got != "NL-TNM-C00122045-K" {
		t.Fatalf("DIN -> EMI3 = %v", got)
	}

	din2, err := emi3.ToDIN()
	if err != nil {
		t.Fatalf("EMI3 -> DIN unexpected error: %v", err)
	}
	if got := din2.String(); got != "NL-TNM-012204-5" {
		t.Fatalf("EMI3 -> DIN = %v", got)
	}

	isoFromEmi3, err := emi3.ToISO()
	if err != nil {
		t.Fatalf("EMI3 -> ISO unexpected error: %v", err)
	}
	if got := isoFromEmi3.String(); got != "NL-TNM-C00122045-K" {
		t.Fatalf("EMI3 -> ISO = %v", got)
	}
}
