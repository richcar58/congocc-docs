Where to Go Next
================

The tutorials so far have used small grammars and the bundled JSON grammar. The
best way to learn idiomatic CongoCC next is to read the larger grammars that
ship with the project, in increasing order of complexity.

A reading path
--------------

The CongoCC source distribution includes complete, maintained grammars under
its ``examples/`` directory. A good progression is:

1. **JSON** — the smallest complete grammar; you have already seen it in
   :doc:`json`. Tokens, a handful of recursive productions, and tree
   annotations.
2. **Lua** — a small but real programming language: statements, expressions
   with precedence, and control structures, without an overwhelming amount of
   detail.
3. **Python** — adds indentation-sensitive lexing, a genuine challenge that
   shows synthetic tokens and context-sensitive tokenization
   (:doc:`/docs/reference/tokenization-advanced`) in earnest.
4. **Java** and **C#** — full industrial languages. These are the most complex
   grammars and the best demonstration of lookahead, code injection, and large
   grammar organization with ``INCLUDE``.

Smaller, focused examples are also worth a look: ``arithmetic`` for a minimal
expression language, ``preprocessor`` for the C#-style preprocessor itself, and
the ``json``/``jsonc`` pair for how one grammar can build on another.

Reading a grammar productively
------------------------------

When you open an unfamiliar grammar, the same structure recurs:

- the **settings** at the top establish packages and options
  (:doc:`/docs/reference/settings`);
- the **token productions** define the lexical layer
  (:doc:`/docs/reference/lexical`);
- the **BNF productions** define the syntax, with ``#`` annotations shaping the
  tree (:doc:`/docs/reference/tree-building`) and ``SCAN`` / ``=>||`` markers
  resolving the hard choices (:doc:`/docs/reference/disambiguation`).

Reading the grammars alongside those reference chapters is the fastest way to
build fluency.

From here
---------

- The :doc:`how-to guides </docs/userguide/userguide>` address specific tasks —
  designing tokens, resolving conflicts, structuring a project, testing, and
  more.
- The :doc:`Reference Manual </docs/reference/reference>` is the complete
  description of the grammar language and generated API.
- The :doc:`Target Language Guide </docs/targets/targets>` covers generating
  parsers in Python, C#, and Rust as well as Java.
