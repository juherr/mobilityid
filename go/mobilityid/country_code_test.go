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

func TestNewCountryCode(t *testing.T) {
	tests := []struct {
		name    string
		input   string
		want    string
		wantErr bool
	}{
		{name: "valid uppercase", input: "US", want: "US"},
		{name: "valid lowercase", input: "nl", want: "NL"},
		{name: "invalid empty", input: "", wantErr: true},
		{name: "invalid too short", input: "U", wantErr: true},
		{name: "invalid too long", input: "USA", wantErr: true},
		{name: "invalid unknown", input: "ZZ", wantErr: true},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			cc, err := NewCountryCode(tt.input)
			if (err != nil) != tt.wantErr {
				t.Fatalf("NewCountryCode() error = %v, wantErr %v", err, tt.wantErr)
			}
			if !tt.wantErr && cc.String() != tt.want {
				t.Fatalf("NewCountryCode() got = %v, want %v", cc.String(), tt.want)
			}
		})
	}
}
