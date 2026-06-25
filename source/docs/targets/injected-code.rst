Injected Code Across Languages
==============================

Embedded actions (``{ … }``) and :doc:`INJECT </docs/reference/injection>`
blocks contain code in the **target language**. That makes them the one part of
a grammar that is not target-neutral: a grammar that embeds Java works only for
Java until the embedded code is ported or guarded. This chapter explains how
injection maps onto each target and how to keep a grammar multi-target.

The per-type model (Java, Python, C#)
-------------------------------------

In Java, Python, and C#, each node type is a class, so injected members go
straight into the corresponding class body. ``INJECT KeyValue : { … }`` adds the
members to the generated ``KeyValue`` class, in whichever of the three languages
you are generating. The mental model is the same across all three; only the
syntax of the injected code differs.

Rust's arena model
------------------

Rust is different. Its AST is **arena-based** — a flat array of nodes addressed
by ``NodeId`` — so there are no per-type node structs to inject into. Code that
other targets would put in a node class must instead be written against the
``(&Ast, NodeId)`` pair that identifies a node in the arena.

To bridge this, CongoCC's Rust generator attempts a mechanical Java-to-Rust
translation of each ``INJECT`` block and writes the results to two files in the
generated crate:

``inject.rs``
   The translated injected code. Constructs that translate cleanly are emitted
   as Rust; those that cannot are left as ``FIXME`` comments containing the
   original Java, to be completed by hand against the ``Ast``/``NodeId`` API.

``FIXME.md``
   A status report listing what needs manual attention. For a grammar with no
   injections it simply records that none were found and the parser is fully
   functional:

   .. code-block:: text

      # Code Injection Status

      No INJECT blocks found in this grammar.  The generated Rust parser is
      fully functional without manual intervention.

So porting an injection-heavy grammar to Rust is a real task: generate, read
``FIXME.md``, and fill in the Rust equivalents in ``inject.rs``. The bundled
grammars that are pure syntax (JSON, Lua) need none of this; the large
injection-heavy ones (Java, C#, Python) need substantial manual work for Rust.

Keeping a grammar multi-target
------------------------------

Two approaches keep one grammar usable for several targets:

- **Guard with the preprocessor.** Wrap each language's injected code in
  ``#if __java__`` / ``#if __python__`` / ``#if __csharp__`` / ``#if __rust__``
  so only the relevant block is active for a given ``-lang``
  (:doc:`/docs/reference/grammar-file`).
- **Keep actions out of the grammar.** A grammar with no embedded code generates
  for every target unchanged; do as much processing as you can by walking the
  tree afterward (:doc:`/docs/userguide/howto/trees`) rather than in actions.
