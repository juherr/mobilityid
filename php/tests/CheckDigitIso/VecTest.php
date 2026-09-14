<?php

declare(strict_types=1);

/*
 * This file is part of the Mobility ID library.
 *
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

namespace Juherr\MobilityId\Tests\CheckDigitIso;

use Juherr\MobilityId\CheckDigitIso\Matrix;
use Juherr\MobilityId\CheckDigitIso\Vec;
use PHPUnit\Framework\TestCase;

final class VecTest extends TestCase
{
    public function testAddIsComponentWise(): void
    {
        $sum = new Vec(1, 2)->add(new Vec(10, 20));

        self::assertSame([11, 22], [$sum->v1, $sum->v2]);
    }

    public function testMultiplyIsTheRowVectorByMatrixProduct(): void
    {
        $product = new Vec(1, 2)->multiply(new Matrix(3, 4, 5, 6));

        self::assertSame([13, 16], [$product->v1, $product->v2]);
    }
}
