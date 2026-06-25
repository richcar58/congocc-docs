Overview
========

CongoCC generates parsers in **Java, Python, C#, and Rust** from a single
grammar. The grammar language, the parsing behavior, and the shape of the syntax
tree are the same whichever target you choose; what changes is the language of
any code you embed, the layout and naming of the generated output, and how you
build and run it. This guide covers those differences.

Choosing a target
------------------

The ``-lang`` flag selects the target; Java is the default:

.. code-block:: console

   $ java -jar congocc.jar -lang python  MyGrammar.ccc
   $ java -jar congocc.jar -lang csharp  MyGrammar.ccc
   $ java -jar congocc.jar -lang rust -d my-parser MyGrammar.ccc

The same grammar can be generated for every target, *provided it contains no
embedded target-language code*. Embedded actions and ``INJECT`` blocks are
written in one language and must be ported or guarded to support others — see
:doc:`injected-code`.

What differs between targets
----------------------------

For a grammar with ``PARSER_PACKAGE = "demo.lang"``, the four targets produce
noticeably different output:

.. list-table::
   :header-rows: 1
   :widths: 12 30 28 30

   * - Target
     - Output layout
     - Start production call
     - Root of the tree
   * - Java
     - a package directory tree (``demo/lang/…`` plus an ``ast/`` subpackage)
     - ``parser.NumberList()``
     - ``parser.rootNode()``
   * - Python
     - a package, ``langparser/`` (``*.py`` modules)
     - ``parser.parse_NumberList()``
     - ``parser.root_node()``
   * - C#
     - a project, ``cs-langparser/`` (with a ``.csproj``)
     - ``parser.ParseNumberList()``
     - ``parser.RootNode``
   * - Rust
     - a self-contained crate (``Cargo.toml`` plus ``*.rs``, flat in ``-d``)
     - ``Parser::parse(input, Some("name"))``
     - ``ast.root()``

The conventions follow each language: production methods are bare in Java,
``parse_``-prefixed and snake_case in Python, ``Parse``-prefixed and PascalCase
in C#, and folded into a single ``Parser::parse`` entry point for Rust's
arena-based AST.

Self-contained output
---------------------

Whichever target you pick, the generated code is **self-contained**: it includes
its own lexer, parser, token, and node types and does not depend on a CongoCC
runtime library. Rust crates have zero external dependencies (with optional
``serde``); the others rely only on their standard libraries. The per-language
chapters — :doc:`java`, :doc:`python`, :doc:`csharp`, :doc:`rust` — give the
details, and :doc:`build-and-runtime` summarizes how to build and run each.
