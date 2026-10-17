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

import "strings"

// asciiUpper uppercases ASCII letters only. strings.ToUpper also maps some non-ASCII letters to
// ASCII ones (U+017F 'ſ' to 'S', U+0131 'ı' to 'I'), which would let the ASCII-only patterns
// accept them; every parser normalizes through this function instead, like the other ports.
func asciiUpper(s string) string {
	return strings.Map(func(r rune) rune {
		if 'a' <= r && r <= 'z' {
			return r - ('a' - 'A')
		}
		return r
	}, s)
}
