Example Grammars
================

CongoCC ships a set of example grammars in the ``examples/`` directory of the
source distribution. They range from tiny demonstrations to complete
programming-language grammars, and most are generated and tested across several
target languages by ``ant test``.

Two senses of "language" meet in the table below, and it helps to keep them
apart:

- the **language a grammar defines** — what its generated parser recognizes
  (Java, JSON, Lua, …); this is what the example is named for; and
- the **target languages** CongoCC generates that parser *in*
  (``-lang java|python|csharp|rust``).

So the ``python`` example is a grammar *for* the Python language that is itself
generated as a Java, Python, C#, and Rust parser. Rust generation is gated
behind the ``rust.enabled`` build flag (see :doc:`rust`).

The examples
------------

.. list-table::
   :header-rows: 1
   :widths: 14 26 34 26

   * - Example
     - Grammar file(s)
     - Language defined
     - Targets generated
   * - ``arithmetic``
     - ``Arithmetic1.ccc``, ``Arithmetic2.ccc``
     - A simple arithmetic-expression language. ``Arithmetic1`` parses;
       ``Arithmetic2`` ``INCLUDE``\ s it and ``INJECT``\ s evaluation.
     - Java, Rust
   * - ``cics``
     - ``Cics.ccc``
     - A subset of IBM's CICS embedded command language; demonstrates
       repetition cardinality constraints.
     - Java, Python, C#, Rust
   * - ``congo-templates``
     - ``CTL.ccc`` (+ ``Directives.inc.ccc``, ``Expressions.inc.ccc``,
       ``Lexer.ccc``)
     - CTL, CongoCC's own template language used for code generation.
     - Java (see notes)
   * - ``csharp``
     - ``CSharp.ccc`` (+ ``CSharpLexer.ccc``, ``CSharpIdentifierDef.ccc``);
       ``PPDirectiveLine.ccc``
     - The C# language; ``PPDirectiveLine`` parses a single C# preprocessor
       directive line.
     - ``CSharp.ccc``: Java, Python, C#, Rust; ``PPDirectiveLine.ccc``: Java,
       Python, C#
   * - ``java``
     - ``Java.ccc`` (+ ``JavaLexer.ccc``, ``Java*IdentifierDef.ccc``)
     - The Java language; identifier-definition includes cover JDK 8 through 26.
     - Java, Python, C#, Rust
   * - ``json``
     - ``JSON.ccc``, ``JSONC.ccc``
     - JSON, and JSON with comments.
     - Java, Python, C#, Rust (both grammars)
   * - ``lua``
     - ``Lua.ccc`` (+ ``LuaLexer.ccc``)
     - The Lua language.
     - Java, Python, C#, Rust
   * - ``php``
     - ``PHP.ccc`` (+ ``PHPLexer.ccc``)
     - The PHP language.
     - Java (see notes)
   * - ``preprocessor``
     - ``Preprocessor.ccc``
     - A barebones C#-style preprocessor for ``#if`` / ``#define`` / … directives.
     - Java, Python, C#
   * - ``python``
     - ``Python.ccc`` (+ ``PythonLexer.ccc``, ``PythonIdentifierDef.ccc``)
     - The Python language.
     - Java, Python, C#, Rust
   * - ``rust``
     - ``Rust.ccc`` (+ ``RustLexer.ccc``, ``RustIdentifierDef.ccc``)
     - The Rust language.
     - Java
   * - ``sqlexpr``
     - ``SqlExprParser.ccc``
     - A SQL expression language.
     - Rust

Notes
-----

- **Split grammars.** Many examples divide their grammar across files: a main
  grammar that ``INCLUDE``\ s a separate lexer (for instance ``LuaLexer.ccc``)
  and, for the programming languages, an identifier-definition file. The Java
  example carries several — ``Java8IdentifierDef.ccc`` through
  ``Java26IdentifierDef.ccc`` — because the set of legal identifier characters
  changed across JDK versions. These ``INCLUDE``\ d files are summarized
  compactly above and are not generated on their own.
- **Examples without a build file.** ``congo-templates`` (the CTL template
  language) and ``php`` have no ``build.xml`` and so are not part of the
  automated cross-target build; CTL is generated as Java as part of building
  CongoCC itself.
- **Targets reflect the build files.** The target set shown for each example is
  what its ``build.xml`` actually generates in the current source. Some examples
  — notably ``sqlexpr`` and the ``rust`` language grammar — are generated for a
  single target rather than all four.
