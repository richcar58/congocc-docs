How To: Structure a Project
===========================

This guide covers organizing a non-trivial grammar and fitting the generated
code into a source tree.

Split large grammars with ``INCLUDE``
-------------------------------------

Keep a big grammar manageable by factoring shared or self-contained parts into
separate files and pulling them in with ``INCLUDE`` (see
:doc:`/docs/reference/grammar-file`). A common split is to keep the lexical
grammar in its own file, or to build on one of the bundled grammars by alias:

.. code-block:: text

   INCLUDE "Lexer.inc.ccc";
   INCLUDE JAVA              // the bundled Java grammar, by alias

Keep variants in one file with the preprocessor
-----------------------------------------------

Rather than maintain near-duplicate grammars, guard the differing parts with
the preprocessor and select them at generation time with ``-p``. The predefined
``__java__`` / ``__python__`` / ``__csharp__`` / ``__rust__`` symbols let one
grammar carry target-specific pieces:

.. code-block:: text

   #if __python__
   INJECT PARSER_CLASS : { /* Python-only members */ }
   #endif

Control where code is generated
-------------------------------

``PARSER_PACKAGE`` sets the package for the parser and lexer, and
``NODE_PACKAGE`` (default ``<PARSER_PACKAGE>.ast``) the package for nodes.
``BASE_SRC_DIR`` — or the ``-d`` flag, which overrides it — sets the output root;
the package becomes subdirectories beneath it. Point ``-d`` at your generated
sources directory so output lands where the compiler expects it.

Decide what goes in source control
----------------------------------

Generated code is a deterministic function of the grammar, so the cleanest
approach is to **commit the grammar and regenerate as part of the build**
(:doc:`build-integration`), keeping the generated directory out of version
control. Teams that prefer to review generated diffs sometimes check it in
instead; either works, but pick one and apply it consistently so the generated
files never drift from the grammar.
