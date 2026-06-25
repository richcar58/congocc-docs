Build and Runtime Matrix
========================

This chapter summarizes, per target, what CongoCC emits and how you turn it into
a running parser. In every case generation itself only needs ``congocc.jar`` and
Java 17+ (:doc:`/docs/userguide/installation`); the rows below are about
building and running the *generated* code.

.. list-table::
   :header-rows: 1
   :widths: 10 26 30 34

   * - Target
     - Output
     - Build
     - Runtime requirement
   * - Java
     - ``.java`` files in a package tree
     - ``javac`` (or your usual build tool)
     - a JRE/JDK 17+; standard library only
   * - Python
     - a Python package of ``.py`` modules
     - none — import and run
     - CPython; standard library only
   * - C#
     - a project with a ``.csproj``
     - ``dotnet build``
     - the .NET SDK/runtime; standard library only
   * - Rust
     - a crate with ``Cargo.toml``
     - ``cargo build`` / ``cargo test``
     - Rust 1.89+ (2024 edition); no dependencies (optional ``serde``)

No runtime library
------------------

There is no separate "CongoCC runtime" to ship. Each generated parser contains
the lexer, parser, token, and node types it needs, so deploying it means
deploying ordinary source in the target language. This keeps dependencies
minimal — for Rust, literally zero beyond the standard library unless you opt
into ``serde``.

Regeneration in the build
-------------------------

Whatever the target, wire generation into the build so the generated code stays
in sync with the grammar, and pass ``-n`` so CongoCC does not check for updates
during automated builds. The language-neutral advice is in
:doc:`/docs/userguide/howto/build-integration`; the per-language chapters note
where each toolchain expects generated sources to live.
