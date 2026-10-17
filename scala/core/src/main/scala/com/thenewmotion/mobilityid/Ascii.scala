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

package com.thenewmotion.mobilityid

/**
 * ASCII-only case folding. `String.toUpperCase` also maps some non-ASCII letters to ASCII ones ('ſ' U+017F to 'S', 'ı'
 * U+0131 to 'I'), and follows the default locale, which would let the ASCII-only patterns accept them; every factory
 * and parser normalizes through this object.
 */
private[mobilityid] object Ascii {
  def toUpperCase(c: Char): Char = if (c >= 'a' && c <= 'z') (c - ('a' - 'A')).toChar else c

  def toUpperCase(s: String): String = s.map(toUpperCase(_: Char))
}
