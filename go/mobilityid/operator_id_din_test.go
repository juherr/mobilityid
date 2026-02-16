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
	"fmt"
	"testing"
)

func TestNewOperatorIDDIN(t *testing.T) {
	tests := []struct {
		input   string
		wantErr bool
	}{
		{input: "810"},
		{input: "810548"},
		{input: "", wantErr: true},
		{input: "81", wantErr: true},
		{input: "8105487", wantErr: true},
		{input: "AB7", wantErr: true},
	}

	for _, tt := range tests {
		_, err := NewOperatorIDDIN(tt.input)
		if (err != nil) != tt.wantErr {
			t.Fatalf("NewOperatorIDDIN(%q) error = %v, wantErr %v", tt.input, err, tt.wantErr)
		}
	}
}

func TestCalculateDIN7064ModXY(t *testing.T) {
	tests := []struct {
		instance int
		wantCD   string
	}{
		{instance: 71, wantCD: "9"},
		{instance: 110, wantCD: "X"},
		{instance: 124, wantCD: "0"},
		{instance: 114, wantCD: "6"},
		{instance: 191, wantCD: "5"},
	}

	for _, tt := range tests {
		codeWithoutCD := fmt.Sprintf("INTNM%06d", tt.instance)
		got, err := CalculateDIN7064ModXY(codeWithoutCD)
		if err != nil {
			t.Fatalf("CalculateDIN7064ModXY(%q) unexpected error: %v", codeWithoutCD, err)
		}
		if got != tt.wantCD {
			t.Fatalf("CalculateDIN7064ModXY(%q) got = %v, want %v", codeWithoutCD, got, tt.wantCD)
		}
	}
}

func TestVerifyDIN7064ModXY(t *testing.T) {
	if !VerifyDIN7064ModXY("INTNM0000719") {
		t.Fatal("expected valid DIN check digit")
	}
	if !VerifyDIN7064ModXY("intnm000110x") {
		t.Fatal("expected lowercase DIN input to be valid")
	}
	if VerifyDIN7064ModXY("INTNM0000718") {
		t.Fatal("expected invalid DIN check digit")
	}
}
