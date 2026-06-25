How the Lexer Works
===================

This chapter explains how CongoCC's lexer recognizes tokens. As with the parsing
model, it is background to help you understand behavior, not a how-to.

From patterns to an automaton
-----------------------------

The token patterns in a grammar are regular expressions, and CongoCC compiles
them into a **nondeterministic finite automaton** (NFA) per lexical state. At
run time the lexer feeds characters through the automaton, tracking the set of
states it could be in, and at each point remembers the longest match found so
far. When no further character can extend a match, it emits the longest matching
token — the *maximal-munch* rule — with ties broken by declaration order.

No DFA table to tune
--------------------

Many lexer generators convert the automaton to a deterministic finite automaton
(DFA) and emit a big transition table. CongoCC works directly from the NFA
representation instead. The practical consequence for you is that there is **no
generated lexer table to size or tune** and no table-related limits to run into;
the lexer is ordinary, readable code. It also makes features like full 32-bit
Unicode character classes, :ref:`lazy tokens
<docs/reference/tokenization-advanced:Lazy tokens>`, and per-state token sets
fall out naturally, because they are expressed in the automaton rather than
fought against a fixed table format.

Lexical states and context
--------------------------

Each :term:`lexical state` is its own automaton over just the tokens active in
that state, which is what makes states an efficient way to handle regions with
different lexical rules. The more dynamic forms of context sensitivity — token
activation and contextual tokens (:doc:`../../reference/tokenization-advanced`) —
work by adjusting which tokens the lexer will currently accept, on top of this
same automaton-based core.
