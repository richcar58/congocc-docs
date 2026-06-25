How Parsing Works
=================

This chapter explains the parsing model behind CongoCC, to help you reason about
why a grammar behaves as it does. It is background, not a how-to.

Recursive descent
-----------------

CongoCC generates a **recursive-descent** parser. Each production becomes a
method, and a production that references another production compiles to a method
that calls the other's method. Parsing the grammar is thus, quite literally,
running a set of mutually recursive functions whose call structure mirrors the
grammar's structure. This is why the generated code is readable, why a stack
trace points at recognizable productions, and why embedding target-language
actions in a production is natural — they are just statements in a method.

The cost of this model is that the grammar must not be left-recursive: a
production cannot call itself as its very first action, or the parser would
recurse forever. Left recursion is expressed instead with the repetition
operators, which is exactly what the :doc:`calculator <../tutorial/calculator>`
grammar does to make its operators left-associative.

Deciding at choice points
-------------------------

At each :term:`choice point` the parser must pick an alternative. By default it
decides on the next single token: if that token can begin an alternative, the
parser commits to it. This is fast and, for most of a grammar, sufficient.

When one token is not enough, the parser does **scan-ahead**: it tentatively
scans the upcoming tokens far enough to decide, then rolls back and parses for
real. The ``=>||`` marker and the ``SCAN`` statement
(:doc:`../../reference/disambiguation`) are how you tell it where and how far to
scan. A long-standing limitation of the original JavaCC — that nested
scan-ahead did not work — has been fixed, so a scan may itself enter productions
that scan.

What "deterministic" means here
-------------------------------

The generated parser is deterministic: at every point it makes one choice and
follows it, using lookahead to choose well rather than trying alternatives in
parallel. Scan-ahead is bounded backtracking *for the decision only* — the
parser looks, decides, and then parses once. Understanding that the decision and
the parse are separate steps is the key to reasoning about lookahead.
