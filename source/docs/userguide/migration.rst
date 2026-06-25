Migrating from JavaCC / JavaCC21
================================

CongoCC reads current CongoCC syntax only; it does not accept legacy JavaCC
grammars. Moving an existing grammar over is a two-part job: convert the syntax,
then adjust for the structural differences. The exhaustive old-to-new mapping is
in :doc:`/docs/reference/appendices/legacy`; this chapter is the workflow.

Step 1: convert the syntax
--------------------------

CongoCC itself has no converter. Use the syntax converter included with recent
JavaCC 21 builds to modernize the grammar's surface syntax:

.. code-block:: console

   $ java -jar javacc-full.jar convert OldGrammar.jj

This handles the mechanical changes — the ``options { … }`` block becoming
top-level settings, ``void Foo() : {} { … }`` becoming ``Foo : … ;``,
``LOOKAHEAD(…)`` becoming ``SCAN`` / ``=>||``, and so on.

Step 2: build with CongoCC and fix what remains
-----------------------------------------------

Generate the converted grammar with CongoCC and work through what conversion
does not cover. The recurring items are:

- **Imports and package references** in any embedded code and injections.
- **Two packages.** Tree building always generates a parser package and a
  separate node package; code cannot go in the default (unnamed) package. Set
  ``PARSER_PACKAGE`` (and ``NODE_PACKAGE`` if you want something other than the
  default ``…​.ast``).
- **No ``XXXConstants`` interface.** Token kinds are now the ``TokenType`` enum;
  replace integer-constant references accordingly
  (:doc:`/docs/reference/generated-api`).
- **The base node class** lives in the node package.
- **Hand edits to generated files** must move into ``INJECT`` statements
  (:doc:`/docs/reference/injection`), since regeneration overwrites the output.

Step 3: reconcile behavior
--------------------------

Once it builds, confirm it behaves as before:

- If the old grammar depended on JavaCC's original lookahead quirks, set
  ``LEGACY_GLITCHY_LOOKAHEAD = true;`` to reproduce them; otherwise prefer to fix
  the affected choice points properly with ``SCAN`` / ``=>||``.
- Re-run your corpus through the :doc:`test harness <howto/testing>` and compare
  results against the old parser.
- Expect the **tree shape to differ**: tree building is on by default, tokens
  are nodes, and smart node creation suppresses trivial wrappers. Adjust with
  ``#`` descriptors and the tree settings (:doc:`/docs/reference/tree-building`)
  until the tree matches what your application expects.

Coming from JavaCC 21
---------------------

JavaCC 21 grammars are much closer to CongoCC, since CongoCC descends from it.
The main adjustments are the package-layout and ``XXXConstants`` changes above,
plus possibly ``LEGACY_GLITCHY_LOOKAHEAD``; most grammars need little more.
