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

final class OperatorIdIso
{
    private const REGEX = '/^([A-Za-z0-9]{3})$/';

    private function __construct(
        public string $id
    ) {
    }

    public static function isValid(string $id): bool
    {
        return preg_match(self::REGEX, $id) === 1;
    }

    public static function of(string $id): self
    {
        if (self::isValid($id)) {
            return new self(strtoupper($id)); // Ensure uppercase as per Scala's PartyCodeImpl
        }

        throw new InvalidArgumentException(
            "Operator ID (ISO) must have a length of 3 and be ASCII letters or digits. (Was: $id)"
        );
    }

    public function __toString(): string
    {
        return $this->id;
    }
}
