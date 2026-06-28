Java
====

Java is CongoCC's default target — ``-lang java`` is assumed when no language is
given — and the most thoroughly exercised. Because the generated-API reference
is written against the Java output, this chapter briefly notes Java-specific particulars.

Output layout
-------------

Generation produces a package directory tree: the parser and lexer in
``PARSER_PACKAGE``, and the node classes in ``NODE_PACKAGE`` (by default
``<PARSER_PACKAGE>.ast``). For ``PARSER_PACKAGE = "demo.lang"`` that is
``demo/lang/`` with a ``demo/lang/ast/`` subpackage. Point ``-d`` at your
generated-sources root so the package path lands where ``javac`` expects it.

Using the parser
----------------

The Java API is the one documented in full in
:doc:`/docs/reference/generated-api`: construct ``FooParser`` from a
``CharSequence`` or ``Path``, call the start production by its bare name, and get
the tree from ``rootNode()``:

.. code-block:: java

   var parser = new FooParser(input);
   parser.NumberList();
   Node root = parser.rootNode();

Embedded code
-------------

Actions and ``INJECT`` blocks in the grammar are Java, inserted into the
generated classes. This is the most direct of the four targets, since the tool
itself is written in Java and node types are ordinary Java classes
(:doc:`injected-code`).

Runtime
-------

Generated parsers require **Java 17 or later** and use only the standard
library. There is nothing else to add to the classpath.

.. note::

   The old ``JDK_TARGET`` setting (and the ``-jdk`` flag) are gone — ``JDK_TARGET``
   is accepted but has no effect, and ``-jdk`` is ignored. There is no
   per-version code-generation switch; generate modern Java and compile it with
   any JDK 17 or newer.
