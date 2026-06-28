Rust
====

CongoCC generates **complete, self-contained Rust parser crates**. Rust is a
first-class target: the generated crate includes a lexer, a recursive-descent
parser, an arena-based AST, a visitor trait, an AST mapper, and a pretty-printer,
with no external dependencies.

Enabling Rust generation
------------------------

Rust support is gated behind a build flag, off by default, so the ``congocc.jar``
you use must have been built with it enabled:

.. code-block:: console

   $ ant build -Drust.enabled=true

Then generate as usual, giving ``-d`` the crate directory:

.. code-block:: console

   $ java -jar congocc.jar -n -lang rust -d my-parser MyGrammar.ccc
   $ cd my-parser && cargo test

Output layout
-------------

Unlike the other targets, the crate files are written flat into the ``-d``
directory:

.. code-block:: text

   Cargo.toml            crate metadata (no dependencies required)
   lib.rs                crate root: module declarations and re-exports
   tokens.rs             token type enum and token struct
   lexer.rs              NFA-based lexer with multi-state support
   parser.rs             recursive-descent parser with scan/lookahead
   ast.rs                arena AST: NodeId, Ast, AstBuilder
   error.rs              ParseError with location and expected-set info
   visitor.rs            Visitor trait and AstMapper
   pretty.rs             Wadler-Lindig pretty-printer
   tests/parse_files.rs  integration harness for a test-data directory
   inject.rs, FIXME.md   injected-code translation and its status

Using the parser
----------------

A single entry point parses the input and returns an owned AST; nodes are
navigated through ``NodeId`` handles rather than references:

.. code-block:: rust

   use my_parser::parser::Parser;

   let ast = Parser::parse(input, Some("filename.ext"))?;
   let root = ast.root().expect("AST has no root");
   println!("{:?} = {}", ast.kind(root), ast.text(root));

The AST is **arena-based** — a flat ``Vec`` of nodes addressed by
``NodeId(u32)`` — which avoids recursive allocations and lets you navigate with
``ast.parent(id)``, ``ast.children(id)``, and ``ast.next_sibling(id)`` without
lifetime juggling. ``ast.dump(id)`` prints a subtree for debugging.

Traversing and transforming
----------------------------

The generated crate offers three levels of AST processing:

- **Read-only**: implement the ``Visitor`` trait (override ``visit_*`` methods,
  call ``visit_children`` to recurse), use ``ast.dump()``, or render with the
  ``PrettyPrinter`` (default width 120 columns).
- **Value rewriting**: implement ``AstMapper``, a bottom-up functional transform
  that returns a *new* AST with nodes replaced, spliced, or removed.
- **Structural editing**: mutate ``&mut Ast`` in place with the synthetic-token
  API — ``new_synthetic_token``, ``new_node``, ``append_child``,
  ``insert_after``, ``detach`` — then ``unparse()`` to regenerate text or
  ``reparse()`` to validate the edited tree by running it back through the
  parser.

See `README_RUST <https://github.com/congo-cc/congo-parser-generator/blob/master/README_RUST.md>`_
for more information on these techniques and pointers to example code.


Injected code
-------------

Because the arena AST has no per-type node structs, ``INJECT`` blocks cannot be
inserted into node classes the way they are for the other targets. CongoCC
translates them as far as it can into ``inject.rs`` and records what needs
hand-finishing in ``FIXME.md``; see :doc:`injected-code`. 

CongoCC ships with a number of Rust example grammars.  Pure-syntax grammars
(JSON, Lua, CICS, SqlExpr, the arithmetic examples) generate fully working crates with no
manual step; the large injection-heavy grammars (Java, C#, Python) generate but
need their injected logic ported to Rust by hand.  See 
`README_RUST <https://github.com/congo-cc/congo-parser-generator/blob/master/README_RUST.md>`_.

Optional serialization (``serde``)
----------------------------------

Generated crates have no dependencies, but the ``Cargo.toml`` is set up so you
can opt into `serde <https://serde.rs/>`_ serialization of the AST. Add the
``serde`` dependency and feature as described in the project's Rust README, then
build with it on:

.. code-block:: console

   $ cargo build --features serde
   $ cargo build --release --features serde

Runtime
-------

Generated crates require **Rust 1.89 or later** (2024 edition) and, by default,
nothing else. Build and test with ``cargo``; the bundled examples also provide
``ant rust-gen`` and ``ant test-rust`` targets that regenerate the crates and run
their ``cargo test`` suites.
