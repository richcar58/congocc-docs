Design Philosophy
=================

Understanding what CongoCC is trying to be explains many of its specific
choices — why the syntax looks as it does, why some legacy options are gone, and
why the defaults are what they are.

Convention over configuration
-----------------------------

CongoCC aims to make the common case work with no configuration. Names are
inferred — the parser and lexer classes, the node package, and the node names
all derive from the grammar by default — so a minimal grammar needs almost no
settings. Tree building is on by default, because most users want a tree.
Settings exist for when you need to override a convention, not as a checklist
you must fill in to get started. The guiding idea is that things should *just
work* out of the box.

A clean break with legacy syntax
--------------------------------

CongoCC removed the legacy JavaCC syntax rather than carry it forward
(:doc:`../../reference/appendices/legacy`). That is a real cost for people with
old grammars, taken deliberately: the old surface had accumulated boilerplate
(``PARSER_BEGIN``/``PARSER_END``, ``void Foo() : {} { … }``) and genuinely
broken corners (the original lookahead behavior) that could not be fixed while
remaining compatible. Removing them allowed a smaller, more regular language —
``Foo : … ;`` instead of the ceremony, ``SCAN`` and ``=>||`` instead of a
``LOOKAHEAD`` that "was always fundamentally broken," and a single consistent
rule that braces mean embedded code.

Modern by default
-----------------

The same spirit shows in the defaults that were modernized: type-safe
``TokenType`` enums instead of integer constants, location information kept
always rather than as an option, tree building and informative error reporting
built in. The aim throughout is that the obvious way to write a grammar is also
the recommended one.
