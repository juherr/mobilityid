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

import "fmt"

// Text encoding of the identifiers: every type implements encoding.TextMarshaler with its
// canonical String() form, so encoding/json (and any text-based codec) writes them as strings.
// Every type whose format is unambiguous also implements encoding.TextUnmarshaler through its
// strict constructor, so decoding validates and normalizes the input and fails with the same
// sentinel errors. ContractID has no UnmarshalText: the standard (ISO, EMI3, DIN) cannot be
// told from the string alone (every EMI3 id is also a valid ISO id), decode the string and call
// NewContractID with the standard the caller knows.
// Zero values render as "" and are not identifiers, so they refuse to marshal.

func marshalText(sentinel error, canonical string) ([]byte, error) {
	if canonical == "" {
		return nil, fmt.Errorf("%w: zero value cannot be marshaled", sentinel)
	}
	return []byte(canonical), nil
}

func unmarshalText[T any](dst *T, text []byte, parse func(string) (*T, error)) error {
	parsed, err := parse(string(text))
	if err != nil {
		return err
	}
	*dst = *parsed
	return nil
}

// MarshalText implements encoding.TextMarshaler.
func (cc CountryCode) MarshalText() ([]byte, error) {
	return marshalText(ErrInvalidCountryCode, cc.value)
}

// UnmarshalText implements encoding.TextUnmarshaler.
func (cc *CountryCode) UnmarshalText(text []byte) error {
	return unmarshalText(cc, text, NewCountryCode)
}

// MarshalText implements encoding.TextMarshaler.
func (pcc PhoneCountryCode) MarshalText() ([]byte, error) {
	return marshalText(ErrInvalidPhoneCountryCode, pcc.value)
}

// UnmarshalText implements encoding.TextUnmarshaler.
func (pcc *PhoneCountryCode) UnmarshalText(text []byte) error {
	return unmarshalText(pcc, text, NewPhoneCountryCode)
}

// MarshalText implements encoding.TextMarshaler.
func (pid ProviderID) MarshalText() ([]byte, error) {
	return marshalText(ErrInvalidProviderID, pid.value)
}

// UnmarshalText implements encoding.TextUnmarshaler.
func (pid *ProviderID) UnmarshalText(text []byte) error {
	return unmarshalText(pid, text, NewProviderID)
}

// MarshalText implements encoding.TextMarshaler.
func (oid OperatorIDISO) MarshalText() ([]byte, error) {
	return marshalText(ErrInvalidOperatorID, oid.value)
}

// UnmarshalText implements encoding.TextUnmarshaler.
func (oid *OperatorIDISO) UnmarshalText(text []byte) error {
	return unmarshalText(oid, text, NewOperatorIDISO)
}

// MarshalText implements encoding.TextMarshaler.
func (oid OperatorIDDIN) MarshalText() ([]byte, error) {
	return marshalText(ErrInvalidOperatorID, oid.value)
}

// UnmarshalText implements encoding.TextUnmarshaler.
func (oid *OperatorIDDIN) UnmarshalText(text []byte) error {
	return unmarshalText(oid, text, NewOperatorIDDIN)
}

// MarshalText implements encoding.TextMarshaler.
func (pid PartyID) MarshalText() ([]byte, error) { return marshalText(ErrInvalidPartyID, pid.String()) }

// UnmarshalText implements encoding.TextUnmarshaler.
func (pid *PartyID) UnmarshalText(text []byte) error { return unmarshalText(pid, text, NewPartyID) }

// MarshalText implements encoding.TextMarshaler. There is no UnmarshalText: decode the string
// and call NewContractID with the expected standard.
func (cid ContractID) MarshalText() ([]byte, error) {
	return marshalText(ErrInvalidContractID, cid.value)
}

// MarshalText implements encoding.TextMarshaler.
func (eid EvseID) MarshalText() ([]byte, error) { return marshalText(ErrInvalidEvseID, eid.String()) }

// UnmarshalText implements encoding.TextUnmarshaler, trying ISO before DIN like NewEvseID.
func (eid *EvseID) UnmarshalText(text []byte) error { return unmarshalText(eid, text, NewEvseID) }

// MarshalText implements encoding.TextMarshaler.
func (eido EvseIDISO) MarshalText() ([]byte, error) {
	return marshalText(ErrInvalidEvseID, eido.String())
}

// UnmarshalText implements encoding.TextUnmarshaler.
func (eido *EvseIDISO) UnmarshalText(text []byte) error {
	return unmarshalText(eido, text, NewEvseIDISO)
}

// MarshalText implements encoding.TextMarshaler.
func (eidd EvseIDDIN) MarshalText() ([]byte, error) {
	return marshalText(ErrInvalidEvseID, eidd.String())
}

// UnmarshalText implements encoding.TextUnmarshaler.
func (eidd *EvseIDDIN) UnmarshalText(text []byte) error {
	return unmarshalText(eidd, text, NewEvseIDDIN)
}
