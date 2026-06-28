Settings Reference
==================

Settings configure code generation. They are written at the top of the grammar
file, before any productions, one per line.

.. note::

   The settings and defaults in this chapter were verified against the
   CongoCC source (``AppSettings.java``). Because CongoCC is released as a
   rolling build, treat a setting marked here against a date as current as of
   that date.

Writing settings
----------------

A setting is a name, an optional value, and a semicolon. The name is
case-insensitive. The value may be omitted for a boolean (a bare name means
*true*):

.. code-block:: ccc

   FAULT_TOLERANT;                       // boolean shorthand for = true
   SMART_NODE_CREATION = false;          // boolean
   TAB_SIZE = 4;                         // integer
   PARSER_PACKAGE = "org.example.lang";  // quoted string
   DEFAULT_LEXICAL_STATE = JSON;         // bare identifier
   EXTRA_TOKENS = INDENT, DEDENT;        // comma-separated list

An unrecognized setting is not an error; CongoCC emits a warning — *"The option
X is not recognized and will be ignored."* — and continues. A few settings can
also be supplied or overridden on the command line; see :doc:`invocation`.

Naming and output
-----------------

``BASE_NAME``
   *String. Default: derived from the grammar file name.* Base for the
   generated class names — ``BASE_NAME = Foo`` yields ``FooParser`` and
   ``FooLexer``.

``PARSER_CLASS``
   *String. Default:* ``<BASE_NAME>Parser``. Name of the generated parser
   class.

``LEXER_CLASS``
   *String. Default:* ``<BASE_NAME>Lexer``. Name of the generated lexer class.

``PARSER_PACKAGE``
   *String. Default: the lower-cased parser class name.* Package for the parser
   and lexer.

``NODE_PACKAGE``
   *String. Default:* ``<PARSER_PACKAGE>.ast``. Package for the generated
   syntax-tree node classes.

``ROOT_API_PACKAGE``
   *String. Default: none.* When set, the base node/token API is generated into
   this separate package.

``BASE_NODE_CLASS``
   *String. Default:* ``BaseNode``. Base class for generated node classes.

``BASE_TOKEN_CLASS``
   *String. Default:* ``Token``. Base class for the generated token class.

``BASE_SRC_DIR``
   *String. Default:* ``"."``. Output directory, absolute or relative to the
   grammar file. ``OUTPUT_DIRECTORY`` is a synonym, and the ``-d`` command-line
   flag sets the same thing.

``NODE_PREFIX``
   *String. Default: empty.* A prefix applied to generated node class names.

``COPYRIGHT_BLURB``
   *String. Default: empty.* Text inserted as a comment at the top of each
   generated file.

Tree building
-------------

See :doc:`tree-building` for what these control.

``TREE_BUILDING_ENABLED``
   *Boolean. Default: true.* Master switch; when false, no tree-building code is
   generated and the other tree settings are ignored (with a warning).

``TREE_BUILDING_DEFAULT``
   *Boolean. Default: true.* Whether the parser builds a tree unless turned off
   at run time.

``TOKENS_ARE_NODES``
   *Boolean. Default: true.* Whether tokens are included in the tree as
   terminal nodes.

``UNPARSED_TOKENS_ARE_NODES``
   *Boolean. Default: false.* Whether unparsed (special) tokens, such as
   comments, are included as nodes. Requires ``TOKENS_ARE_NODES``.
   ``SPECIAL_TOKENS_ARE_NODES`` is a legacy synonym read only as a fallback.

``SMART_NODE_CREATION``
   *Boolean. Default: true.* When true, a production creates a node only if more
   than one node is on the stack, so trivial single-child productions do not
   add a level to the tree.

``NODE_DEFAULT_VOID``
   *Boolean. Default: false.* When true, productions build no node unless they
   explicitly request one with a ``#`` descriptor.

``NODE_USES_PARSER``
   *Boolean. Default: false.* Generates node classes that hold a reference to
   the parser.

Lexical
-------

See :doc:`lexical` and :doc:`tokenization-advanced`.

``DEFAULT_LEXICAL_STATE``
   *Identifier. Default:* ``DEFAULT``. The lexical state the lexer starts in.

``IGNORE_CASE``
   *Boolean. Default: false.* Makes the entire lexer case-insensitive. (Case can
   also be ignored per token production with ``[IGNORE_CASE]``.)

``REQUIRE_TOKEN_DECLARATION``
   *Boolean. Default: false.* When true, every token must be declared in a token
   production; an undeclared string literal used in a production is an error
   rather than an implicit token.

   .. versionadded:: 2026-06-10

``DEACTIVATE_TOKENS``
   *List. Default: none.* Token names that start out inactive and must be
   switched on with ``ACTIVATE_TOKENS`` during parsing.

``EXTRA_TOKENS``
   *List. Default: none.* Synthetic token types not produced by the lexical
   grammar (for example ``INDENT``/``DEDENT`` inserted by a token hook). An
   entry may name its class as ``NAME#ClassName``.

``TOKEN_CHAINING``
   *Boolean. Default: false (auto-enabled if the grammar uses* ``preInsert`` *).*
   Allows synthetic tokens to be chained into the token stream.

``LEXER_USES_PARSER``
   *Boolean. Default: false.* Generates a lexer that holds a reference to the
   parser, for tightly coupled context-sensitive lexing.

Output text formatting
----------------------

``TAB_SIZE``
   *Integer. Default: 1.* Width to which tab characters are expanded in
   generated output. ``TABS_TO_SPACES`` is a synonym.

``PRESERVE_TABS``
   *Boolean. Default: true unless a tab size is set.* Keeps tab characters
   instead of expanding them.

``PRESERVE_LINE_ENDINGS``
   *Boolean. Default: false.* Preserves the input's line endings instead of
   normalizing them.

``ENSURE_FINAL_EOL``
   *Boolean. Default: false.* Appends a newline to generated files that lack
   one.

``TERMINATING_STRING``
   *String. Default: empty (or* ``"\n"`` *when* ``ENSURE_FINAL_EOL`` *is set).*
   A string appended to generated output if not already present.

Parsing behavior
----------------

``USE_CHECKED_EXCEPTION``
   *Boolean. Default: false.* Whether the generated ``ParseException`` is a
   checked exception.

``LEGACY_GLITCHY_LOOKAHEAD``
   *Boolean. Default: false.* Restores the quirky lookahead behavior of legacy
   JavaCC, for grammars that depended on it. See :doc:`appendices/legacy`.

``ASSERT_APPLIES_IN_LOOKAHEAD``
   *Boolean. Default: false.* Whether a plain ``ASSERT`` is also evaluated during
   lookahead (``ENSURE`` always is). See :doc:`disambiguation`.

``USES_PREPROCESSOR``
   *Boolean. Default: false (true when* ``C_CONTINUATION_LINE`` *is set).*
   Enables the preprocessor. See :doc:`grammar-file`.

``C_CONTINUATION_LINE``
   *Boolean. Default: false.* Honors C-style backslash line continuations in the
   preprocessor.

Fault tolerance
---------------

See :doc:`fault-tolerance`.

``FAULT_TOLERANT``
   *Boolean. Default: false.* Enables the experimental fault-tolerant parsing
   mode.

``FAULT_TOLERANT_DEFAULT``
   *Boolean. Default: true.* When fault tolerance is enabled, whether it is on
   by default at run time.

Testing
-------

``TEST_PRODUCTION``
   *Identifier. Default: none.* The production used as the entry point by the
   generated self-test harness.

``TEST_EXTENSION``
   *String. Default: none.* The file extension the self-test harness uses to
   find test inputs.

Target-language options
-----------------------

``JAVA_UNICODE_ESCAPE``
   *Boolean. Default: false.* (Java) Processes ``\u`` escapes in the input
   stream the way legacy JavaCC's ``JavaCharStream`` did.

Experimental and advanced
-------------------------

Settings whose names begin with ``X_`` are experimental and may change without
notice.

``X_JTB_PARSE_TREE``
   *Boolean. Default: false.* Experimental alternate parse-tree mode.

``X_SYNTHETIC_NODES_ENABLED``
   *Boolean. Default: false.* Experimental synthetic-node generation.

Deprecated and ignored
----------------------

``JDK_TARGET``
   Deprecated and has no effect; setting it produces the warning *"Option
   JDK_TARGET is deprecated and currently has no effect. (It never did!)"*.

``NODE_CLASS``
   Accepted for backward compatibility but unused; use ``BASE_NODE_CLASS``
   instead.

``TOKEN_MANAGER_USES_PARSER``
   Accepted for backward compatibility but unused — the legacy name for the
   lexer was the "token manager". Use ``LEXER_USES_PARSER`` instead.

The many configuration options of legacy JavaCC that CongoCC removed entirely —
``STATIC``, a global ``LOOKAHEAD``, the ambiguity-check options, and others —
are listed with their replacements in :doc:`appendices/legacy`.
