C#
==

CongoCC generates a ready-to-build C# project from the same grammar used for any
other target, provided the grammar's embedded code (if any) is C# or is guarded
for C# (:doc:`injected-code`).

Output layout
-------------

The output is a project directory named ``cs-`` plus the last component of
``PARSER_PACKAGE`` plus ``parser`` — ``demo.lang`` becomes ``cs-langparser/`` —
containing a ``.csproj`` (named after the full package) and the generated
sources:

.. code-block:: text

   cs-langparser/
   ├── demo.lang.csproj
   ├── Lexer.cs
   ├── Parser.cs
   ├── Tokens.cs
   └── Utils.cs

The generated code uses the full ``PARSER_PACKAGE`` as its namespace.

Using the parser
----------------

The API follows C# conventions: a constructor taking the input, production
methods that are ``Parse``-prefixed and PascalCase, and ``RootNode`` as a
property rather than a method:

.. code-block:: csharp

   var parser = new Parser(input);
   parser.ParseNumberList();
   Node root = parser.RootNode;

``ParseException`` derives from ``System.Exception``.

The preprocessor on the C# target
----------------------------------

CongoCC's own grammar preprocessor (``#if`` and friends,
:doc:`/docs/reference/grammar-file`) works for every target, but when generating
C# its ``#define`` and ``#undef`` directives must appear **before the first real
token of the grammar file**, matching C#'s own rule for those directives. Place
any such definitions at the very top.

Runtime
-------

Build with ``dotnet build`` against the generated ``.csproj``. Generated parsers
need only the .NET standard library; there is no package to add.
