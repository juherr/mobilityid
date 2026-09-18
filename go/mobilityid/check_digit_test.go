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
	"bufio"
	"os"
	"path/filepath"
	"strings"
	"testing"
)

// The fixtures in testdata/ are the cross-language vectors shared with the PHP port
// (php/tests/fixtures), computed by the TypeScript port and verified against Scala.
func checkDigitFixtures(t *testing.T, name string) [][2]string {
	t.Helper()
	file, err := os.Open(filepath.Join("testdata", name))
	if err != nil {
		t.Fatal(err)
	}
	defer func() {
		if err := file.Close(); err != nil {
			t.Error(err)
		}
	}()
	var rows [][2]string
	scanner := bufio.NewScanner(file)
	for scanner.Scan() {
		payload, cd, ok := strings.Cut(scanner.Text(), ",")
		if !ok {
			t.Fatalf("%s: malformed line %q", name, scanner.Text())
		}
		rows = append(rows, [2]string{payload, cd})
	}
	if err := scanner.Err(); err != nil {
		t.Fatal(err)
	}
	if len(rows) != 200 {
		t.Fatalf("%s: %d fixtures, want 200", name, len(rows))
	}
	return rows
}

func TestCheckDigitFixtures(t *testing.T) {
	algorithms := []struct {
		name    string
		fixture string
		compute func(string) (string, error)
	}{
		{"ISO 7064 Mod 37,2", "check-digit-iso.csv", CalculateISO7064Mod37_2},
		{"DIN 7064 Mod X,Y", "check-digit-din.csv", CalculateDIN7064ModXY},
	}
	for _, alg := range algorithms {
		t.Run(alg.name, func(t *testing.T) {
			for _, row := range checkDigitFixtures(t, alg.fixture) {
				got, err := alg.compute(row[0])
				if err != nil {
					t.Errorf("%s(%q) error: %v", alg.name, row[0], err)
					continue
				}
				if got != row[1] {
					t.Errorf("%s(%q) = %q, want %q", alg.name, row[0], got, row[1])
				}
			}
		})
	}
}

func TestCalculateDIN7064ModXYLongInput(t *testing.T) {
	// Long payloads made the power-of-two weights overflow and the result negative.
	for _, code := range []string{
		"00000000AAAAAAAAAAAAAAAAAAAAAAAAAA8",
		strings.Repeat("Z", 64),
		strings.Repeat("9", 100),
	} {
		got, err := CalculateDIN7064ModXY(code)
		if err != nil {
			t.Fatalf("CalculateDIN7064ModXY(%q) error: %v", code, err)
		}
		if len(got) != 1 || !strings.ContainsAny(got, "0123456789X") {
			t.Errorf("CalculateDIN7064ModXY(%q) = %q, want a single digit or X", code, got)
		}
	}
}
