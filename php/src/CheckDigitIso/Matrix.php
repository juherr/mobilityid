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

namespace Juherr\MobilityId\CheckDigitIso;

final class Matrix
{
    public function __construct(
        public int $m11,
        public int $m12,
        public int $m21,
        public int $m22
    ) {
    }

    public function multiply(self $m): self
    {
        return new self(
            $this->m11 * $m->m11 + $this->m12 * $m->m21,
            $this->m11 * $m->m12 + $this->m12 * $m->m22,
            $this->m21 * $m->m11 + $this->m22 * $m->m21,
            $this->m21 * $m->m12 + $this->m22 * $m->m22
        );
    }

    public function equals(self $other): bool
    {
        return $this->m11 === $other->m11 &&
               $this->m12 === $other->m12 &&
               $this->m21 === $other->m21 &&
               $this->m22 === $other->m22;
    }
}
