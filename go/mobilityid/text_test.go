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
	"encoding"
	"encoding/json"
	"errors"
	"maps"
	"testing"
)

// Compile-time proof that every identifier encodes as text and that every unambiguous one
// decodes from text. ContractID is left out of the decoders: its standard cannot be told from
// the string alone.
var (
	_ encoding.TextMarshaler = CountryCode{}
	_ encoding.TextMarshaler = PhoneCountryCode{}
	_ encoding.TextMarshaler = ProviderID{}
	_ encoding.TextMarshaler = OperatorIDISO{}
	_ encoding.TextMarshaler = OperatorIDDIN{}
	_ encoding.TextMarshaler = PartyID{}
	_ encoding.TextMarshaler = ContractID{}
	_ encoding.TextMarshaler = EvseID{}
	_ encoding.TextMarshaler = EvseIDISO{}
	_ encoding.TextMarshaler = EvseIDDIN{}

	_ encoding.TextUnmarshaler = (*CountryCode)(nil)
	_ encoding.TextUnmarshaler = (*PhoneCountryCode)(nil)
	_ encoding.TextUnmarshaler = (*ProviderID)(nil)
	_ encoding.TextUnmarshaler = (*OperatorIDISO)(nil)
	_ encoding.TextUnmarshaler = (*OperatorIDDIN)(nil)
	_ encoding.TextUnmarshaler = (*PartyID)(nil)
	_ encoding.TextUnmarshaler = (*EvseID)(nil)
	_ encoding.TextUnmarshaler = (*EvseIDISO)(nil)
	_ encoding.TextUnmarshaler = (*EvseIDDIN)(nil)
)

type identifiers struct {
	Country   *CountryCode      `json:"country"`
	Phone     *PhoneCountryCode `json:"phone"`
	Provider  *ProviderID       `json:"provider"`
	OpISO     *OperatorIDISO    `json:"opIso"`
	OpDIN     *OperatorIDDIN    `json:"opDin"`
	Party     *PartyID          `json:"party"`
	Contract  *ContractID       `json:"contract"`
	Evse      *EvseID           `json:"evse"`
	EvseISO   *EvseIDISO        `json:"evseIso"`
	EvseDIN   *EvseIDDIN        `json:"evseDin"`
	ByValue   CountryCode       `json:"byValue"`
	EvseValue EvseID            `json:"evseValue"`
}

// must unwraps the constructors of the fixtures below, which are known to be valid.
func must[T any](v *T, err error) *T {
	if err != nil {
		panic(err)
	}
	return v
}

func TestJSONRoundTrip(t *testing.T) {
	country := must(NewCountryCode("nl"))
	evse := must(NewEvseID("de*ab7*e840*6487"))
	in := identifiers{
		Country:   country,
		Phone:     must(NewPhoneCountryCode("+31")),
		Provider:  must(NewProviderID("tnm")),
		OpISO:     must(NewOperatorIDISO("ab7")),
		OpDIN:     must(NewOperatorIDDIN("745")),
		Party:     must(NewPartyID("nl-tnm")),
		Contract:  must(NewContractID("nltnm000122045", ContractIDStandardISO)),
		Evse:      evse,
		EvseISO:   must(NewEvseIDISO("nl*tnm*e840*6487")),
		EvseDIN:   must(NewEvseIDDIN("+31*745*840*6487")),
		ByValue:   *country,
		EvseValue: *evse,
	}

	encoded, err := json.Marshal(in)
	if err != nil {
		t.Fatalf("json.Marshal() error = %v", err)
	}
	want := `{"country":"NL","phone":"+31","provider":"TNM","opIso":"AB7","opDin":"745","party":"NL-TNM",` +
		`"contract":"NL-TNM-000122045-U","evse":"DE*AB7*E840*6487","evseIso":"NL*TNM*E840*6487",` +
		`"evseDin":"+31*745*840*6487","byValue":"NL","evseValue":"DE*AB7*E840*6487"}`
	if string(encoded) != want {
		t.Fatalf("json.Marshal() = %s, want %s", encoded, want)
	}

	// ContractID is decoded explicitly, through the standard the caller knows.
	var out struct {
		identifiers
		Contract string `json:"contract"`
	}
	if err = json.Unmarshal(encoded, &out); err != nil {
		t.Fatalf("json.Unmarshal() error = %v", err)
	}
	// The decoded identifiers re-encode to the same values (field order differs because the
	// shadowing Contract string is emitted last).
	reencoded, err := json.Marshal(out)
	if err != nil {
		t.Fatalf("json.Marshal() of the decoded struct error = %v", err)
	}
	var got, expected map[string]string
	if err = json.Unmarshal(reencoded, &got); err != nil {
		t.Fatal(err)
	}
	if err = json.Unmarshal([]byte(want), &expected); err != nil {
		t.Fatal(err)
	}
	if !maps.Equal(got, expected) {
		t.Fatalf("decoded then re-encoded = %s, want %s", reencoded, want)
	}
	if !out.Evse.IsISO() {
		t.Error("decoded EvseID should be ISO")
	}
	cid, err := NewContractID(out.Contract, ContractIDStandardISO)
	if err != nil {
		t.Fatalf("NewContractID(%q) error = %v", out.Contract, err)
	}
	if cid.String() != in.Contract.String() {
		t.Errorf("contract decoded as %q, want %q", cid, in.Contract)
	}
}

func TestUnmarshalTextRejectsInvalidInput(t *testing.T) {
	tests := []struct {
		name string
		json string
		into any
		want error
	}{
		{"country", `"ZZ"`, &CountryCode{}, ErrInvalidCountryCode},
		{"phone", `"1234"`, &PhoneCountryCode{}, ErrInvalidPhoneCountryCode},
		{"provider", `"TN"`, &ProviderID{}, ErrInvalidProviderID},
		{"operator iso", `"A|7"`, &OperatorIDISO{}, ErrInvalidOperatorID},
		{"operator din", `"AB7"`, &OperatorIDDIN{}, ErrInvalidOperatorID},
		{"party", `"ZZTNM"`, &PartyID{}, ErrInvalidPartyID},
		{"evse", `"nope"`, &EvseID{}, ErrInvalidEvseID},
		{"evse iso", `"+31*745*840"`, &EvseIDISO{}, ErrInvalidEvseID},
		{"evse din", `"NL*TNM*E840"`, &EvseIDDIN{}, ErrInvalidEvseID},
	}
	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			err := json.Unmarshal([]byte(tt.json), tt.into)
			if !errors.Is(err, tt.want) {
				t.Fatalf("json.Unmarshal(%s) error = %v, want errors.Is %v", tt.json, err, tt.want)
			}
		})
	}
}

func TestUnmarshalTextKeepsComponentCause(t *testing.T) {
	var eid EvseID
	err := json.Unmarshal([]byte(`"ZZ*TNM*E840*6487"`), &eid)
	if !errors.Is(err, ErrInvalidEvseID) || !errors.Is(err, ErrInvalidCountryCode) {
		t.Fatalf("json.Unmarshal() error = %v, want ErrInvalidEvseID and ErrInvalidCountryCode", err)
	}
}

func TestUnmarshalTextNormalizes(t *testing.T) {
	var cc CountryCode
	if err := cc.UnmarshalText([]byte("nl")); err != nil {
		t.Fatal(err)
	}
	if cc.String() != "NL" {
		t.Fatalf("UnmarshalText(nl) = %q, want NL", cc)
	}
	text, err := cc.MarshalText()
	if err != nil || string(text) != "NL" {
		t.Fatalf("MarshalText() = %q, %v", text, err)
	}
}

func TestMarshalTextRejectsZeroValues(t *testing.T) {
	tests := []struct {
		name string
		m    encoding.TextMarshaler
		want error
	}{
		{"country", CountryCode{}, ErrInvalidCountryCode},
		{"phone", PhoneCountryCode{}, ErrInvalidPhoneCountryCode},
		{"provider", ProviderID{}, ErrInvalidProviderID},
		{"operator iso", OperatorIDISO{}, ErrInvalidOperatorID},
		{"operator din", OperatorIDDIN{}, ErrInvalidOperatorID},
		{"party", PartyID{}, ErrInvalidPartyID},
		{"contract", ContractID{}, ErrInvalidContractID},
		{"evse", EvseID{}, ErrInvalidEvseID},
		{"evse iso", EvseIDISO{}, ErrInvalidEvseID},
		{"evse din", EvseIDDIN{}, ErrInvalidEvseID},
	}
	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			text, err := tt.m.MarshalText()
			if !errors.Is(err, tt.want) {
				t.Fatalf("MarshalText() = %q, %v; want errors.Is %v", text, err, tt.want)
			}
			if _, err := json.Marshal(tt.m); err == nil {
				t.Fatal("json.Marshal() of a zero value should fail")
			}
		})
	}
}
