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
// Zero values are not identifiers and refuse to marshal.

func marshalText(zero bool, sentinel error, value string) ([]byte, error) {
	if zero {
		return nil, fmt.Errorf("%w: zero value cannot be marshaled", sentinel)
	}
	return []byte(value), nil
}

// MarshalText implements encoding.TextMarshaler.
func (cc CountryCode) MarshalText() ([]byte, error) {
	return marshalText(cc.value == "", ErrInvalidCountryCode, cc.value)
}

// UnmarshalText implements encoding.TextUnmarshaler.
func (cc *CountryCode) UnmarshalText(text []byte) error {
	parsed, err := NewCountryCode(string(text))
	if err != nil {
		return err
	}
	*cc = *parsed
	return nil
}

// MarshalText implements encoding.TextMarshaler.
func (pcc PhoneCountryCode) MarshalText() ([]byte, error) {
	return marshalText(pcc.value == "", ErrInvalidPhoneCountryCode, pcc.value)
}

// UnmarshalText implements encoding.TextUnmarshaler.
func (pcc *PhoneCountryCode) UnmarshalText(text []byte) error {
	parsed, err := NewPhoneCountryCode(string(text))
	if err != nil {
		return err
	}
	*pcc = *parsed
	return nil
}

// MarshalText implements encoding.TextMarshaler.
func (pid ProviderID) MarshalText() ([]byte, error) {
	return marshalText(pid.value == "", ErrInvalidProviderID, pid.value)
}

// UnmarshalText implements encoding.TextUnmarshaler.
func (pid *ProviderID) UnmarshalText(text []byte) error {
	parsed, err := NewProviderID(string(text))
	if err != nil {
		return err
	}
	*pid = *parsed
	return nil
}

// MarshalText implements encoding.TextMarshaler.
func (oid OperatorIDISO) MarshalText() ([]byte, error) {
	return marshalText(oid.value == "", ErrInvalidOperatorID, oid.value)
}

// UnmarshalText implements encoding.TextUnmarshaler.
func (oid *OperatorIDISO) UnmarshalText(text []byte) error {
	parsed, err := NewOperatorIDISO(string(text))
	if err != nil {
		return err
	}
	*oid = *parsed
	return nil
}

// MarshalText implements encoding.TextMarshaler.
func (oid OperatorIDDIN) MarshalText() ([]byte, error) {
	return marshalText(oid.value == "", ErrInvalidOperatorID, oid.value)
}

// UnmarshalText implements encoding.TextUnmarshaler.
func (oid *OperatorIDDIN) UnmarshalText(text []byte) error {
	parsed, err := NewOperatorIDDIN(string(text))
	if err != nil {
		return err
	}
	*oid = *parsed
	return nil
}

// MarshalText implements encoding.TextMarshaler.
func (p PartyID) MarshalText() ([]byte, error) {
	if p.countryCode == nil {
		return marshalText(true, ErrInvalidPartyID, "")
	}
	return marshalText(false, ErrInvalidPartyID, p.String())
}

// UnmarshalText implements encoding.TextUnmarshaler.
func (p *PartyID) UnmarshalText(text []byte) error {
	parsed, err := NewPartyID(string(text))
	if err != nil {
		return err
	}
	*p = *parsed
	return nil
}

// MarshalText implements encoding.TextMarshaler. There is no UnmarshalText: decode the string
// and call NewContractID with the expected standard.
func (cid ContractID) MarshalText() ([]byte, error) {
	if cid.countryCode == nil {
		return marshalText(true, ErrInvalidContractID, "")
	}
	return marshalText(false, ErrInvalidContractID, cid.String())
}

// MarshalText implements encoding.TextMarshaler.
func (eid EvseID) MarshalText() ([]byte, error) {
	if eid.iso == nil && eid.din == nil {
		return marshalText(true, ErrInvalidEvseID, "")
	}
	return marshalText(false, ErrInvalidEvseID, eid.String())
}

// UnmarshalText implements encoding.TextUnmarshaler, trying ISO before DIN like NewEvseID.
func (eid *EvseID) UnmarshalText(text []byte) error {
	parsed, err := NewEvseID(string(text))
	if err != nil {
		return err
	}
	*eid = *parsed
	return nil
}

// MarshalText implements encoding.TextMarshaler.
func (eido EvseIDISO) MarshalText() ([]byte, error) {
	if eido.countryCode == nil {
		return marshalText(true, ErrInvalidEvseID, "")
	}
	return marshalText(false, ErrInvalidEvseID, eido.String())
}

// UnmarshalText implements encoding.TextUnmarshaler.
func (eido *EvseIDISO) UnmarshalText(text []byte) error {
	parsed, err := NewEvseIDISO(string(text))
	if err != nil {
		return err
	}
	*eido = *parsed
	return nil
}

// MarshalText implements encoding.TextMarshaler.
func (eidd EvseIDDIN) MarshalText() ([]byte, error) {
	if eidd.countryCode == nil {
		return marshalText(true, ErrInvalidEvseID, "")
	}
	return marshalText(false, ErrInvalidEvseID, eidd.String())
}

// UnmarshalText implements encoding.TextUnmarshaler.
func (eidd *EvseIDDIN) UnmarshalText(text []byte) error {
	parsed, err := NewEvseIDDIN(string(text))
	if err != nil {
		return err
	}
	*eidd = *parsed
	return nil
}
