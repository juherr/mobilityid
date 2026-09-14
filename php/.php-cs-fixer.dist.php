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

use PhpCsFixer\Config;
use PhpCsFixer\Finder;
use PhpCsFixer\Runner\Parallel\ParallelConfigFactory;

$finder = new Finder()
    ->in(__DIR__ . '/src')
    ->in(__DIR__ . '/tests')
    ->append([__FILE__, __DIR__ . '/rector.php'])
;

return new Config()
    ->setParallelConfig(ParallelConfigFactory::detect())
    ->setRiskyAllowed(true)
    ->setRules([
        '@PER-CS2.0' => true,
        '@PER-CS2.0:risky' => true,
        '@PhpCsFixer' => true,
        '@PhpCsFixer:risky' => true,
        '@PHP84Migration' => true,
        '@PHPUnit100Migration:risky' => true,
        'concat_space' => ['spacing' => 'one'],
        'declare_strict_types' => true,
        'not_operator_with_successor_space' => true,
        'php_unit_test_class_requires_covers' => false,
        'php_unit_strict' => false,
        'yoda_style' => ['equal' => false, 'identical' => false, 'less_and_greater' => false],
        'php_unit_internal_class' => false,
        'header_comment' => [
            'header' => <<<'EOT'
                This file is part of the Mobility ID library.

                Copyright (c) 2026 Julien Herr, and respective contributors

                Licensed under the Apache License, Version 2.0 (the "License");
                you may not use this file except in compliance with the License.
                You may obtain a copy of the License at

                    http://www.apache.org/licenses/LICENSE-2.0

                Unless required by applicable law or agreed to in writing, software
                distributed under the License is distributed on an "AS IS" BASIS,
                WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
                See the License for the specific language governing permissions and
                limitations under the License.
                EOT,
            'comment_type' => 'comment',
            'location' => 'after_declare_strict',
            'separate' => 'both',
        ],
    ])
    ->setFinder($finder)
;
