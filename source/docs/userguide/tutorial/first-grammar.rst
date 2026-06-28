Your First Grammar
==================

This tutorial walks through the whole CongoCC cycle on a deliberately tiny
language: writing a grammar, generating a parser from it, and then compiling
and running that parser to see the syntax tree it builds. The language we will 
parse is a comma-separated list of integers, such as ``1, 2, 3``. 

This tutorial
assumes you have the ``congocc.jar`` and a JDK 17+ on your path 
(see :doc:`../installation`).

.. note::

   More complex examples are available in the ``examples/`` directory of the 
   source code repository, including the examples for the different target languages.
   These examples are automatically built and tested when `ant test` is run 
   (see :doc:`../installation`).

Writing the grammar
-------------------

Create a file called ``NumberList.ccc`` with the following contents:

.. code-block:: ccc

   // NumberList.ccc -- a first CongoCC grammar.
   // It parses a comma-separated list of integers, such as "1, 2, 3".

   PARSER_PACKAGE = "org.example.numberlist";

   // Whitespace to discard between tokens.
   SKIP : " " | "\t" | "\r" | "\n" ;

   // The two kinds of token in this little language.
   TOKEN :
       <NUMBER : (["0"-"9"])+ >
     | <COMMA : "," >
   ;

   // The start production: at least one number, with commas in between,
   // anchored at the end of the input.
   NumberList : <NUMBER> ( <COMMA> <NUMBER> )* <EOF> ;

Even this small file shows the major parts of a CongoCC grammar:

- A **setting**, ``PARSER_PACKAGE``, controls code generation — here, the
  package the generated classes go into. Settings are written ``NAME = value;``
  at the top of the file; the full list is in :doc:`/docs/reference/settings`.
- A ``SKIP`` rule lists characters the lexer should silently discard. The
  ``TOKEN`` rule declares the *token types* the lexer can produce. Both are
  covered in :doc:`/docs/reference/lexical`.
- ``NumberList`` is a **production** — a grammar rule. Its right-hand side is an
  *expansion* built from token references (``<NUMBER>``), the choice and
  repetition operators (``(`` … ``)*``), and the built-in ``<EOF>`` token that
  matches the end of the input. Productions are covered in
  :doc:`/docs/reference/productions`.

.. note::

   Braces ``{ }`` are reserved for embedded target-language code. Because this
   grammar has none, there are no braces in it — token and production rules end
   with a semicolon.

Generating the parser
---------------------

Run CongoCC on the grammar file:

.. code-block:: console

   $ java -jar congocc.jar NumberList.ccc

CongoCC prints a line for each file it writes, ending with a success message:

.. code-block:: text

   Outputting: org/example/numberlist/Token.java
   ...
   Outputting: org/example/numberlist/ast/NumberList.java
   Parser generated successfully.

Because we set ``PARSER_PACKAGE`` to ``org.example.numberlist`` and did not pass
a ``-d`` flag, the files are written into a matching ``org/example/numberlist``
directory tree next to the grammar:

.. code-block:: text

   org/example/numberlist/
   ├── NumberListLexer.java     the lexer (tokenizer)
   ├── NumberListParser.java    the parser
   ├── Token.java               the token class, with a TokenType enum
   ├── InvalidToken.java
   ├── Node.java                the syntax-tree node interface
   ├── ParseException.java      thrown when the input does not match
   ├── NonTerminalCall.java
   ├── TokenSource.java
   └── ast/
       ├── BaseNode.java        base class for tree nodes
       ├── NumberList.java      node for the NumberList production
       ├── NUMBER.java          node for the NUMBER token
       └── COMMA.java           node for the COMMA token

Two things are worth noticing already. First, CongoCC generated **tree-building
code by default** — there is a node class for the ``NumberList`` production.
Second, it generated node classes for the ``NUMBER`` and ``COMMA`` **tokens**
too: by default every token is also a node in the tree. Both behaviors are
configurable, as described in :doc:`/docs/reference/tree-building`.

Using the parser
----------------

Add a small ``Main`` class in the same package to drive the parser. Put this in
``org/example/numberlist/Main.java``:

.. code-block:: java

   package org.example.numberlist;

   public class Main {
       public static void main(String[] args) {
           String input = args.length > 0 ? args[0] : "1, 2, 3";
           NumberListParser parser = new NumberListParser(input);
           try {
               parser.NumberList();
           } catch (ParseException e) {
               System.err.println("Parse error: " + e.getMessage());
               return;
           }
           System.out.println("Parsed: \"" + input + "\"");
           parser.rootNode().dump();
       }
   }

The generated API used here is small and predictable:

- The constructor ``new NumberListParser(input)`` accepts the text to parse —
  a ``CharSequence`` (as here) or a ``java.nio.file.Path``.
- Each production becomes a method, so the start production ``NumberList`` is
  invoked as ``parser.NumberList()``.
- After parsing, ``parser.rootNode()`` returns the root :file:`Node` of the
  tree, and ``Node.dump()`` prints the tree to standard output.

The generated API is described in full in :doc:`/docs/reference/generated-api`.

Compiling and running
---------------------

Compile the generated sources together with ``Main`` and run it:

.. code-block:: console

   $ javac org/example/numberlist/*.java
   $ java org.example.numberlist.Main "1, 2, 3"

The parser accepts the input and dumps the tree it built:

.. code-block:: text

   Parsed: "1, 2, 3"
   <NumberList (1, 1)-(1, 7)>
     NUMBER: (1, 1) - (1, 1): 1
     COMMA: (1, 2) - (1, 2): ,
     NUMBER: (1, 4) - (1, 4): 2
     COMMA: (1, 5) - (1, 5): ,
     NUMBER: (1, 7) - (1, 7): 3
     Token: (1, 1) - (1, 1): EOF

Reading the dump: the root line ``<NumberList (1, 1)-(1, 7)>`` is the production
node, with the begin and end ``(line, column)`` positions it spans. Indented
beneath it are its children — the ``NUMBER`` and ``COMMA`` token nodes (each
showing its position and matched text) and finally the end-of-input ``EOF``
token, which is included as a node like any other token.

Handling errors
---------------

CongoCC parsers report precise errors when the input does not match the
grammar. Feeding in two numbers with no comma between them stops the parse as
soon as the unexpected token appears:

.. code-block:: console

   $ java org.example.numberlist.Main "1 2"

.. code-block:: text

   Parse error: Encountered an error at input:1:3
   Found string "2" of type NUMBER
   Was expecting: EOF

A trailing comma with nothing after it fails the other way — the parser reaches
the end of the input while still expecting a number:

.. code-block:: console

   $ java org.example.numberlist.Main "1,"

.. code-block:: text

   Parse error: Encountered an error at input:1:1
   Unexpected end of input.
    Found token of type EOF
   Was expecting: NUMBER

In each case the message reports where the parser was, what it found, and what
it expected — the raw material for good diagnostics.

Recap and next steps
--------------------

In a few lines you defined a language, generated a working parser for it, and
ran that parser to produce and inspect a syntax tree. The same grammar can
generate parsers in Python, C#, or Rust by passing ``-lang`` — only embedded
code and the surrounding tooling differ. See the
:doc:`Target Language Guide </docs/targets/targets>`.

Next, :doc:`calculator` builds a grammar with real structure — operator
precedence and recursion — that evaluates an expression as it parses.
