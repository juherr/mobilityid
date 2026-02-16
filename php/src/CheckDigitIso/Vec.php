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

final class Vec
{
    public function __construct(
        public int $v1,
        public int $v2
    ) {
    }

    public function add(self $v): self
    {
        return new self(
            $this->v1 + $v->v1,
            $this->v2 + $v->v2
        );
    }

    public function multiply(Matrix $m): self
    {
        return new self(
            $this->v1 * $m->m11 + $this->v2 * $m->m21,
            $this->v1 * $m->m12 + $this->v2 * $m->m22
        );
    }
}
