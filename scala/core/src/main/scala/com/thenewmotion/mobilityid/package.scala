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

package com.thenewmotion

package object mobilityid {
  implicit private[mobilityid] class CharacterChecks(val c: Char) extends AnyVal {
    def isAsciiUpper: Boolean = c >= 'A' && c <= 'Z'
    def isAsciiLower: Boolean = c >= 'a' && c <= 'z'
    def isAsciiLetter:Boolean = isAsciiUpper || isAsciiLower
    def isAsciiDigit: Boolean = c >= '0' && c <= '9'
    def isAsciiUpperOrDigit: Boolean = isAsciiUpper || isAsciiDigit
    def isAsciiLetterOrDigit: Boolean = isAsciiLetter || isAsciiDigit
  }
}
