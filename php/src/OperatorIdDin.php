<?php

declare(strict_types=1);

/*
 * This file is part of the Mobility ID library.
 *
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

namespace Juherr\MobilityId;

use InvalidArgumentException;

final class OperatorIdDin
{
    private const REGEX = '([0-9]{3,6})'; // Removed anchors

    private function __construct(
        public string $id
    ) {
    }

    public static function isValid(string $id): bool
    {
        return preg_match('/^' . self::REGEX . '$/', $id) === 1; // Anchors added for validation
    }

    public static function of(string $id): self
    {
        if (self::isValid($id)) {
            return new self($id); // DIN IDs are numeric, no case conversion needed
        }

        throw new InvalidArgumentException(
            "Operator ID (DIN) must be a numeric string between 3 and 6 digits. (Was: $id)"
        );
    }

    public function __toString(): string
    {
        return $this->id;
    }

    public static function getRegex(): string
    {
        return self::REGEX;
    }
}
