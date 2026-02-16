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

func TestParserHelpers(t *testing.T) {
	if _, err := ParseCountryCode("NL"); err != nil {
		t.Fatalf("ParseCountryCode failed: %v", err)
	}
	if _, err := ParseProviderID("TNM"); err != nil {
		t.Fatalf("ParseProviderID failed: %v", err)
	}
	if _, err := ParseISOContractID("NL-TNM-000122045-U"); err != nil {
		t.Fatalf("ParseISOContractID failed: %v", err)
	}
	if _, err := ParseEvseID("DE*AB7*E840*6487"); err != nil {
		t.Fatalf("ParseEvseID failed: %v", err)
	}
	if _, err := ParseEvseIDFromParts("NL", "TNM", "E840*6487"); err != nil {
		t.Fatalf("ParseEvseIDFromParts failed: %v", err)
	}
	if _, err := ParseEvseIDISOFromParts("NL", "TNM", "E840*6487"); err != nil {
		t.Fatalf("ParseEvseIDISOFromParts failed: %v", err)
	}
	if _, err := ParseEvseIDDINFromParts("31", "745", "840*6487"); err != nil {
		t.Fatalf("ParseEvseIDDINFromParts failed: %v", err)
	}
}
