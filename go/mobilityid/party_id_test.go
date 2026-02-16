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

func TestNewPartyID(t *testing.T) {
	tests := []struct {
		name         string
		input        string
		wantRendered string
		wantCompact  string
		wantErr      bool
	}{
		{name: "valid compact", input: "NLTNM", wantRendered: "NL-TNM", wantCompact: "NLTNM"},
		{name: "valid with dash", input: "NL-TNM", wantRendered: "NL-TNM", wantCompact: "NLTNM"},
		{name: "valid with star", input: "NL*TNM", wantRendered: "NL-TNM", wantCompact: "NLTNM"},
		{name: "invalid country", input: "ZZTNM", wantErr: true},
		{name: "invalid party length", input: "NLTM", wantErr: true},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			pid, err := NewPartyID(tt.input)
			if (err != nil) != tt.wantErr {
				t.Fatalf("NewPartyID() error = %v, wantErr %v", err, tt.wantErr)
			}
			if tt.wantErr {
				return
			}
			if pid.String() != tt.wantRendered {
				t.Fatalf("PartyID.String() = %v, want %v", pid.String(), tt.wantRendered)
			}
			if pid.ToCompactString() != tt.wantCompact {
				t.Fatalf("PartyID.ToCompactString() = %v, want %v", pid.ToCompactString(), tt.wantCompact)
			}
		})
	}
}

func TestNewPartyIDNonsenseInputs(t *testing.T) {
	nonsenseIDs := []string{
		"NLTNMA",
		"XYTNM",
		"NL%(@$",
		" NLTNM",
		"\nLTNM",
		"",
		"XY-TNMaargh",
		"\u041d\u041b-TNM",
		"NLT-NM",
	}

	for _, input := range nonsenseIDs {
		t.Run(input, func(t *testing.T) {
			if _, err := NewPartyID(input); err == nil {
				t.Fatalf("NewPartyID(%q) expected error", input)
			}
		})
	}
}
