Overview
========

`CongoCC <https://parsers.org>`_ is a parser generator. Users describe a language in a **grammar file**,
and CongoCC generates source code — a lexer, a parser, and a set of syntax-tree
classes — that reads text in that language and generates an abstract syntax tree that 
your application can then process. It is a recursive-descent generator that can produce 
parsers in **Java**, **Python**, **C#**, and **Rust** from the same grammar.

A Short History
---------------

CongoCC was originally developed as a fork of `JavaCC21 <https://github.com/javacc21/javacc21>`_, 
which was itself a fork of the original `JavaCC <https://javacc.github.io/javacc/>`_.  ConngoCC's goal is
to provide a more modern and flexible approach to parser generation. It has since evolved 
to support multiple target languages and has been used in various projects requiring 
custom language processing.  CongoCC source code is `here <https://github.com/congo-cc/congo-parser-generator>`_.

The processing model
--------------------

There are two distinct processing phases: *generation*, which you run once when the
grammar changes, and *parsing*, which the generated parser code does at run time.

**Generation.** You run CongoCC on a ``.ccc`` grammar file (see
:doc:`invocation`). It produces, in your chosen target language:

- a **lexer** that recognizes the grammar's tokens,
- a **parser** with one method per grammar production, and
- **node classes** for the syntax tree.

**Parsing.** At run time the generated code works in the classic two stages:

.. figure:: /_static/pipeline.svg
   :alt: input text, to the lexer, to tokens, to the parser, to a syntax tree.
   :align: center

   At run time the generated lexer and parser turn input text into a syntax tree.

The lexer turns characters into a stream of **tokens** (numbers, identifiers,
punctuation, …). The parser consumes that stream according to the grammar's
productions and, by default, builds a **syntax tree** of nodes as it goes. Your
application then walks the tree.

Terminology
-----------

A few terms recur throughout this manual; they are defined fully in the
:doc:`appendices/glossary`.

Grammar
   The complete description of a language, written in a ``.ccc`` file (:doc:`grammar-file`).

Token / terminal
   An indivisible lexical unit produced by the lexer. Token types are declared
   in token productions (:doc:`lexical`).

Production / non-terminal
   A named grammar rule (:doc:`productions`). CongoCC generates one parser
   method per production.

Expansion
   The right-hand side of a production — the pattern of tokens, non-terminals,
   and operators it matches.

Node / syntax tree
   The parser's output. Each production and token can contribute a node
   (:doc:`tree-building`).

Lexical state
   A mode the lexer is in that determines which tokens it can currently match
   (:doc:`lexical`).

Lookahead
   Information the parser uses to choose between alternatives at a choice point
   (:doc:`disambiguation`).

How this manual is organized
----------------------------

The chapters move from running the tool, through the grammar language, to the
code it generates:

- :doc:`invocation` and :doc:`grammar-file` — running CongoCC and the
  structure of a grammar file, including the preprocessor.
- :doc:`lexical`, :doc:`productions`, and :doc:`disambiguation` — the core
  grammar language: tokens, productions, and lookahead.
- :doc:`tree-building` and :doc:`injection` — shaping the syntax tree and
  adding your own code to the generated classes.
- :doc:`tokenization-advanced` and :doc:`fault-tolerance` — context-sensitive
  tokenization and recovery from malformed input.
- :doc:`generated-api` — the contract the generated parser, lexer, tokens, and
  nodes present to your application.
- :doc:`settings` — every configuration setting, by category.
- The appendices give the formal :doc:`grammar of the grammar
  <appendices/meta-grammar>`, a :doc:`legacy mapping <appendices/legacy>` for
  users coming from JavaCC, and the :doc:`appendices/glossary`.

For tutorials and task-oriented guidance, see the
:doc:`User Guide </docs/userguide/userguide>`; for what differs between target
languages, see the :doc:`Target Language Guide </docs/targets/targets>`.
