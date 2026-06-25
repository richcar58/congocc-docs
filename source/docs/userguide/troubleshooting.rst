Troubleshooting
===============

This chapter collects messages you are likely to meet, what they mean, and how
to fix them. They fall into two groups: those CongoCC reports while *generating*
a parser, and those the *generated parser* reports while parsing.

Generation-time messages
-------------------------

``Expansion is unreachable.``
   Two alternatives at a choice begin with the same token, so the parser would
   always take the first and never reach the second. Add lookahead so the
   choice can be made — usually a ``=>||`` marker, occasionally a full
   ``SCAN`` — as described in :doc:`howto/choices`.

``Found string "~" … Was expecting one of: LT, CHARACTER_LITERAL, STRING_LITERAL, SINGLE_QUOTE_STRING``
   A character class (or other non-literal pattern) appears as a token
   specification without enclosing angle brackets — for example
   ``SKIP : ~["\n"] ;``. Wrap it: ``SKIP : <~["\n"]> ;``. Bare *string* literals
   are fine; anything more must be inside ``< … >`` (:doc:`/docs/reference/lexical`).

``The option X is not recognized and will be ignored.``
   A setting name is misspelled, or it is a legacy JavaCC option that no longer
   exists. Check the spelling against :doc:`/docs/reference/settings`, and
   :doc:`/docs/reference/appendices/legacy` for removed options.

``Option JDK_TARGET is deprecated and currently has no effect.``
   Remove the ``JDK_TARGET`` setting; it does nothing.

``File X does not exist!``
   The grammar path on the command line is wrong, or an ``INCLUDE`` names a file
   that cannot be found relative to the including grammar.

Parse-time messages
-------------------

When the generated parser meets input that does not match the grammar, it
reports where it was and what it expected:

.. code-block:: text

   Encountered an error at input:1:3
   Found string "2" of type NUMBER
   Was expecting: EOF

Read it as: at line 1, column 3 the parser found a ``NUMBER`` where the grammar
allowed only end-of-input. Two readings are possible — the *input* is genuinely
invalid, or the *grammar* does not accept input you intended it to. If valid
input is being rejected, the grammar is usually missing a case or has a choice
resolved the wrong way.

An unexpected end of input reads similarly:

.. code-block:: text

   Encountered an error at input:1:1
   Unexpected end of input.
    Found token of type EOF
   Was expecting: NUMBER

Here the input ended while the parser still needed a ``NUMBER``.

When valid input is rejected
----------------------------

To track down a grammar that rejects input it should accept:

- Reduce the input to the smallest fragment that still fails — the error's
  line and column point you at it.
- Dump the token stream or tree of a *similar* passing input to confirm the
  lexer produces the tokens you expect; a frequent cause is a token-precedence
  surprise where one pattern shadows another (:doc:`howto/tokens`).
- Check the choice points on the path to the failure for missing lookahead
  (:doc:`howto/choices`).
