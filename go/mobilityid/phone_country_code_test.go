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

func TestNewPhoneCountryCode(t *testing.T) {
	tests := []struct {
		name    string
		input   string
		wantErr bool
	}{
		{name: "valid short", input: "1"},
		{name: "valid with plus", input: "+49"},
		{name: "valid three digits", input: "358"},
		{name: "invalid empty", input: "", wantErr: true},
		{name: "invalid alpha", input: "US", wantErr: true},
		{name: "invalid too long", input: "1234", wantErr: true},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			_, err := NewPhoneCountryCode(tt.input)
			if (err != nil) != tt.wantErr {
				t.Fatalf("NewPhoneCountryCode() error = %v, wantErr %v", err, tt.wantErr)
			}
		})
	}
}
