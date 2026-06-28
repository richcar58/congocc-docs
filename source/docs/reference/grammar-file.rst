The Grammar File
================

A CongoCC grammar is a single UTF-8 text file, conventionally given a ``.ccc``
extension. This chapter describes the overall structure of that file and the
two mechanisms for composing and conditionally including grammar text:
``INCLUDE`` and the preprocessor.

Structure
---------

A grammar file consists of, in order:

1. **Settings** — zero or more ``NAME = value;`` lines that configure code
   generation. They come first, before any productions. The full catalog is in
   :doc:`settings`.
2. **The body** — the productions that make up the grammar, freely
   interleaved: token productions (:doc:`lexical`), BNF productions
   (:doc:`productions`), ``INJECT`` statements (:doc:`injection`), and
   ``INCLUDE`` statements.

Comments follow Java/C conventions — ``//`` to end of line, and ``/* … */`` for
block comments — and may appear anywhere whitespace is allowed.

A small but complete grammar therefore looks like this:

.. code-block:: ccc

   // settings
   PARSER_PACKAGE = "org.example.lang";

   // token productions
   SKIP : " " | "\t" | "\r" | "\n" ;
   TOKEN : <NUMBER : (["0"-"9"])+ > ;

   // BNF productions
   NumberList : <NUMBER> ( "," <NUMBER> )* <EOF> ;

Including other files
---------------------

``INCLUDE`` splices another grammar file into this one, so common constructs can
be shared across grammars instead of copied. The included file's productions
and settings become part of the including grammar.

Give the file as a string literal path:

.. code-block:: ccc

   INCLUDE "common/Identifiers.ccc";

CongoCC ships several complete grammars that can be included by a built-in
**alias** — a bare identifier rather than a path. For example, to build on the
bundled Java grammar:

.. code-block:: ccc

   INCLUDE JAVA

The available aliases are ``JAVA``, ``PYTHON``, ``CSHARP``, ``RUST``, ``JSON``,
``JSONC``, ``LUA``, and ``PREPROCESSOR``, along with the ``_LEXER`` and
``_IDENTIFIER_DEF`` variants of the programming-language grammars (for example
``JAVA_LEXER``, ``JAVA_IDENTIFIER_DEF``) for including just the lexical or
identifier portion.

A single ``INCLUDE`` may list fallback locations separated by ``!``; the first
one that resolves is used:

.. code-block:: ccc

   INCLUDE "local/Java.ccc" ! JAVA

Including the same file twice is harmless — the second inclusion is skipped with
a warning rather than duplication.

The preprocessor
----------------

CongoCC has a preprocessor, modeled on the C# one, that runs over the grammar
text before it is parsed and selects which lines are active. It can be used to
keep target-language-specific or optional parts of a grammar in a single
file.

Directives occupy their own lines and begin with ``#``:

==================  ===================================================
Directive           Meaning
==================  ===================================================
``#if`` *cond*      begin a conditional block, active when *cond* is true
``#elif`` *cond*    alternative block
``#else``           fallback block
``#endif``          end the conditional
``#define`` *NAME*  define a symbol
``#undef`` *NAME*   remove a symbol
==================  ===================================================

A condition is a symbol name, optionally negated with ``!``. A symbol is true
when it is defined.

Two sources supply symbols. The **target language** is always available as a
predefined symbol — exactly one of ``__java__``, ``__python__``, ``__csharp__``,
or ``__rust__`` is defined, according to the ``-lang`` in effect. Additional
symbols come from ``#define`` directives or from the command line with ``-p``
(see :doc:`invocation`).

For example, this grammar defines the ``B`` token only when the ``EXTRA`` symbol
is passed, and the ``J`` token only when generating Java:

.. code-block:: ccc

   TOKEN : <A : "a"> ;

   #if EXTRA
   TOKEN : <B : "b"> ;
   #endif

   #if __java__
   TOKEN : <J : "j"> ;
   #endif

Generated as ``-lang java`` with no ``-p`` flag, it has the ``A`` and ``J``
tokens; generated with ``-p EXTRA`` it additionally has ``B``.

.. note::

   For the C# target, ``#define`` and ``#undef`` are only allowed before the
   first real token of the file, matching C#'s own rule.
