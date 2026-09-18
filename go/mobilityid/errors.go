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

import "errors"

// Sentinel errors returned (wrapped) by the constructors, parsers and conversions of this
// package. Match them with errors.Is; the wrapping message names the offending input.
// A composite identifier wraps both its own sentinel and the one of the failing component, so
// an unknown country inside a contract id satisfies both ErrInvalidContractID and
// ErrInvalidCountryCode.
var (
	// ErrInvalidCountryCode reports a value that is not an ISO 3166-1 alpha-2 country code.
	ErrInvalidCountryCode = errors.New("invalid ISO 3166-1 alpha-2 country code")
	// ErrInvalidPhoneCountryCode reports a value that is not an E.164 phone country code.
	ErrInvalidPhoneCountryCode = errors.New("invalid E.164 phone country code")
	// ErrInvalidProviderID reports a value that is not a three-character alphanumeric provider id.
	ErrInvalidProviderID = errors.New("invalid provider id")
	// ErrInvalidOperatorID reports a value that is neither a valid ISO nor a valid DIN operator id.
	ErrInvalidOperatorID = errors.New("invalid operator id")
	// ErrInvalidPartyID reports a value that is not a valid party id.
	ErrInvalidPartyID = errors.New("invalid party id")
	// ErrInvalidContractID reports a value that does not match the contract id format of the
	// requested standard.
	ErrInvalidContractID = errors.New("invalid contract id")
	// ErrInvalidCheckDigit reports a contract id whose given check digit differs from the
	// computed one.
	ErrInvalidCheckDigit = errors.New("invalid check digit")
	// ErrInvalidCheckDigitInput reports a payload the check-digit algorithms cannot encode.
	ErrInvalidCheckDigitInput = errors.New("invalid check digit input")
	// ErrInvalidEvseID reports a value that is neither a valid ISO nor a valid DIN EVSE id.
	ErrInvalidEvseID = errors.New("invalid EVSE id")
	// ErrUnsupportedStandard reports a ContractIDStandard this package does not know about.
	ErrUnsupportedStandard = errors.New("unsupported contract id standard")
	// ErrUnconvertibleContractID reports a contract id that cannot be expressed in the target
	// standard (for example an ISO instance value that does not start with "00" cannot become DIN).
	ErrUnconvertibleContractID = errors.New("contract id cannot be converted")
)
