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
	"testing"
)

// isCheckDigit reports whether cd is exactly one uppercase ASCII letter or digit.
func isCheckDigit(cd string) bool { return len(cd) == 1 && isASCIIUpperOrDigit(rune(cd[0])) }

// contractIDStandards enumerates the standards for the fuzz targets; the fuzzer picks one by
// index so every generated input is tried against a known standard.
var contractIDStandards = []ContractIDStandard{ContractIDStandardISO, ContractIDStandardEMI3, ContractIDStandardDIN}

func FuzzNewContractID(f *testing.F) {
	seeds := []struct {
		id       string
		standard uint8
	}{
		{"NL-TNM-000122045-U", 0},
		{"NLTNM000122045", 0},
		{"nl-tnm-000122045-u", 0},
		{"NL-TNM-C00122045-K", 1},
		{"NLTNMC00122045", 1},
		{"NL-TNM-122045-0", 2},
		{"NL*TNM*122045*0", 2},
		{"NLTNM122045", 2},
		{"ZZ-TNM-000122045-U", 0},
		{"NL-TNM-000122045-X", 0},
		{"", 0},
		{"NL", 2},
		{"NL-TNM-000122045-U", 7},
	}
	for _, s := range seeds {
		f.Add(s.id, s.standard)
	}
	f.Fuzz(func(t *testing.T, id string, standardIndex uint8) {
		standard := contractIDStandards[int(standardIndex)%len(contractIDStandards)]
		cid, err := NewContractID(id, standard)
		if err != nil {
			if !errors.Is(err, ErrInvalidContractID) {
				t.Fatalf("NewContractID(%q, %v) error %v does not wrap ErrInvalidContractID", id, standard, err)
			}
			return
		}
		for _, again := range []string{cid.String(), cid.ToCompactString(), cid.ToCompactStringWithoutCheckDigit()} {
			reparsed, err := NewContractID(again, standard)
			if err != nil {
				t.Fatalf("NewContractID(%q, %v) does not round-trip through %q: %v", id, standard, again, err)
			}
			if reparsed.String() != cid.String() {
				t.Fatalf("NewContractID(%q, %v) round-trips through %q as %q", id, standard, again, reparsed)
			}
		}
		if !isASCIIUpperOrDigit(cid.CheckDigit()) {
			t.Fatalf("NewContractID(%q, %v) has check digit %q", id, standard, cid.CheckDigit())
		}
		if _, err := cid.PartyID(); err != nil {
			t.Fatalf("NewContractID(%q, %v).PartyID() error: %v", id, standard, err)
		}
	})
}

func FuzzNewEvseID(f *testing.F) {
	for _, seed := range []string{
		"DE*AB7*E840*6487", "DEAB7E8406487", "nl*tnm*e840*6487", "+49*810*000*438", "49*810*000*438",
		"ZZ*AB7*E840*6487", "", "DE*AB7*840*6487", "+1234*810*000",
	} {
		f.Add(seed)
	}
	f.Fuzz(func(t *testing.T, id string) {
		eid, err := NewEvseID(id)
		if err != nil {
			if !errors.Is(err, ErrInvalidEvseID) {
				t.Fatalf("NewEvseID(%q) error %v does not wrap ErrInvalidEvseID", id, err)
			}
			return
		}
		if eid.IsISO() == eid.IsDIN() {
			t.Fatalf("NewEvseID(%q) is neither or both ISO and DIN", id)
		}
		// ISO wins whenever it accepts the input.
		if _, isoErr := NewEvseIDISO(id); (isoErr == nil) != eid.IsISO() {
			t.Fatalf("NewEvseID(%q) ISO precedence broken: iso err %v, IsISO %v", id, isoErr, eid.IsISO())
		}
		reparsed, err := NewEvseID(eid.String())
		if err != nil {
			t.Fatalf("NewEvseID(%q) does not round-trip through %q: %v", id, eid, err)
		}
		if reparsed.String() != eid.String() || reparsed.IsISO() != eid.IsISO() {
			t.Fatalf("NewEvseID(%q) round-trips as %q (ISO %v)", id, reparsed, reparsed.IsISO())
		}
	})
}

func FuzzNewEvseIDFromParts(f *testing.F) {
	f.Add("NL", "TNM", "E840*6487")
	f.Add("nl", "tnm", "840*6487")
	f.Add("31", "745", "840*6487")
	f.Add("+49", "810", "000*438")
	f.Add("ZZ", "TNM", "840")
	f.Add("", "", "")
	f.Fuzz(func(t *testing.T, countryCode, operatorID, powerOutletID string) {
		eid, err := NewEvseIDFromParts(countryCode, operatorID, powerOutletID)
		if err != nil {
			if !errors.Is(err, ErrInvalidEvseID) {
				t.Fatalf("NewEvseIDFromParts(%q, %q, %q) error %v does not wrap ErrInvalidEvseID", countryCode, operatorID, powerOutletID, err)
			}
			return
		}
		if _, err := NewEvseID(eid.String()); err != nil {
			t.Fatalf("NewEvseIDFromParts(%q, %q, %q) = %q does not parse back: %v", countryCode, operatorID, powerOutletID, eid, err)
		}
	})
}

func FuzzCalculateISO7064Mod37_2(f *testing.F) {
	for _, seed := range []string{"NLTNM000122045", "nltnm000122045", "NLTNMC00122045", "NLTNM", "NLTNM00012204*", ""} {
		f.Add(seed)
	}
	f.Fuzz(func(t *testing.T, code string) {
		cd, err := CalculateISO7064Mod37_2(code)
		if err != nil {
			if !errors.Is(err, ErrInvalidCheckDigitInput) {
				t.Fatalf("CalculateISO7064Mod37_2(%q) error %v does not wrap ErrInvalidCheckDigitInput", code, err)
			}
			return
		}
		if !isCheckDigit(cd) {
			t.Fatalf("CalculateISO7064Mod37_2(%q) = %q, want one of [A-Z0-9]", code, cd)
		}
	})
}

func FuzzCalculateDIN7064ModXY(f *testing.F) {
	for _, seed := range []string{"NLTNM122045", "nltnm122045", "NLTNM12204*", "", "A"} {
		f.Add(seed)
	}
	f.Fuzz(func(t *testing.T, code string) {
		cd, err := CalculateDIN7064ModXY(code)
		if err != nil {
			if !errors.Is(err, ErrInvalidCheckDigitInput) {
				t.Fatalf("CalculateDIN7064ModXY(%q) error %v does not wrap ErrInvalidCheckDigitInput", code, err)
			}
			return
		}
		if !isCheckDigit(cd) {
			t.Fatalf("CalculateDIN7064ModXY(%q) = %q, want one of [A-Z0-9]", code, cd)
		}
	})
}
