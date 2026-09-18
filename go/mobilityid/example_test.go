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

package mobilityid_test

import (
	"encoding/json"
	"errors"
	"fmt"

	"mobilityid.juherr.dev/go/mobilityid"
)

func ExampleNewContractID() {
	cid, err := mobilityid.NewContractID("nltnm000122045", mobilityid.ContractIDStandardISO)
	if err != nil {
		fmt.Println(err)
		return
	}
	fmt.Println(cid)
	fmt.Println(cid.CountryCode(), cid.ProviderID(), cid.InstanceValue(), string(cid.CheckDigit()))
	fmt.Println(cid.ToCompactString())
	// Output:
	// NL-TNM-000122045-U
	// NL TNM 000122045 U
	// NLTNM000122045U
}

func ExampleContractID_ToDIN() {
	iso, _ := mobilityid.NewContractID("NL-TNM-001220450-U", mobilityid.ContractIDStandardISO)
	din, err := iso.ToDIN()
	if err != nil {
		fmt.Println(err)
		return
	}
	fmt.Println(din, din.Standard())
	// Output:
	// NL-TNM-122045-0 DIN
}

func ExampleContractID_ToEMI3() {
	din, _ := mobilityid.NewContractID("NL-TNM-122045-0", mobilityid.ContractIDStandardDIN)
	emi3, err := din.ToEMI3()
	if err != nil {
		fmt.Println(err)
		return
	}
	fmt.Println(emi3, emi3.Standard())
	// Output:
	// NL-TNM-C01220450-K EMI3
}

func ExampleNewEvseID() {
	// ISO is tried before DIN.
	for _, raw := range []string{"DE*AB7*E840*6487", "+49*810*000*438"} {
		id, err := mobilityid.NewEvseID(raw)
		if err != nil {
			fmt.Println(err)
			continue
		}
		fmt.Println(id, id.IsISO(), id.IsDIN(), id.CountryCode(), id.OperatorID(), id.PowerOutletID())
	}
	// Output:
	// DE*AB7*E840*6487 true false DE AB7 840*6487
	// +49*810*000*438 false true +49 810 000*438
}

func ExampleNewEvseIDFromParts() {
	id, err := mobilityid.NewEvseIDFromParts("nl", "tnm", "840*6487")
	if err != nil {
		fmt.Println(err)
		return
	}
	fmt.Println(id)
	// Output:
	// NL*TNM*E840*6487
}

func ExampleParseCountryCode() {
	cc, err := mobilityid.ParseCountryCode("nl")
	if err != nil {
		fmt.Println(err)
		return
	}
	fmt.Println(cc)
	// Output:
	// NL
}

func ExampleNewPartyID() {
	party, err := mobilityid.NewPartyID("nl-tnm")
	if err != nil {
		fmt.Println(err)
		return
	}
	fmt.Println(party, party.CountryCode(), party.PartyCode())
	// Output:
	// NL-TNM NL TNM
}

func Example_errorsIs() {
	_, err := mobilityid.NewContractID("ZZ-TNM-000122045-U", mobilityid.ContractIDStandardISO)
	fmt.Println(errors.Is(err, mobilityid.ErrInvalidContractID))
	fmt.Println(errors.Is(err, mobilityid.ErrInvalidCountryCode))
	fmt.Println(err)

	_, err = mobilityid.NewContractID("NL-TNM-000122045-X", mobilityid.ContractIDStandardISO)
	fmt.Println(errors.Is(err, mobilityid.ErrInvalidCheckDigit))
	// Output:
	// true
	// true
	// invalid contract id: 'ZZ-TNM-000122045-U': invalid ISO 3166-1 alpha-2 country code: 'ZZ'
	// true
}

func ExampleContractID_MarshalText() {
	type token struct {
		Contract *mobilityid.ContractID `json:"contract"`
		Evse     *mobilityid.EvseID     `json:"evse"`
	}
	contract, _ := mobilityid.NewContractID("NL-TNM-000122045-U", mobilityid.ContractIDStandardISO)
	evse, _ := mobilityid.NewEvseID("NL*TNM*E840*6487")
	encoded, _ := json.Marshal(token{Contract: contract, Evse: evse})
	fmt.Println(string(encoded))

	// EvseID decodes on its own; a ContractID needs the standard the caller expects.
	var decoded struct {
		Contract string             `json:"contract"`
		Evse     *mobilityid.EvseID `json:"evse"`
	}
	_ = json.Unmarshal(encoded, &decoded)
	cid, _ := mobilityid.NewContractID(decoded.Contract, mobilityid.ContractIDStandardISO)
	fmt.Println(cid, decoded.Evse)
	// Output:
	// {"contract":"NL-TNM-000122045-U","evse":"NL*TNM*E840*6487"}
	// NL-TNM-000122045-U NL*TNM*E840*6487
}
