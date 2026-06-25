How To: Parse Large Inputs
==========================

CongoCC parsers read the **entire input into memory** rather than streaming it.
This is a deliberate simplification — it makes lookahead, backtracking, and the
position information on every node straightforward — and for the overwhelming
majority of inputs it is exactly the right trade-off. This guide is about the
cases where input size actually matters.

The memory model
----------------

When you construct a parser from a file or a ``CharSequence``, the whole text is
held in memory, and the tokens and tree are built on top of it. As a rule of
thumb, budget for the input plus the token stream plus the tree. Modern machines
have a lot of memory; inputs of many megabytes are unremarkable.

Releasing tokens you are past
-----------------------------

For genuinely large inputs where you do not need to revisit earlier tokens, the
``UNCACHE_TOKENS`` construct (:doc:`/docs/reference/tokenization-advanced`) lets
the parser release tokens it has moved beyond, bounding the token memory rather
than retaining the whole stream.

Practical advice
----------------

- **Measure before optimizing.** Most "large" inputs are not large enough to
  matter; confirm there is a real memory problem before complicating a grammar.
- **Split at natural boundaries.** If your input is really a sequence of
  independent records, parse them one at a time with a fresh parser per record
  instead of one giant parse.
- **Give the JVM room.** When you do parse very large files, raise the heap size
  rather than fighting the whole-file model.
- **Skip aggressively.** Sending bulk ignorable content to ``SKIP`` keeps it out
  of the token stream and the tree entirely.
