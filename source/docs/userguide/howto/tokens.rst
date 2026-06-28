How To: Design Tokens
=====================

Good tokens make the rest of a grammar easier to write. This guide collects
practical advice; the syntax reference is :doc:`/docs/reference/lexical`.

Name the tokens you refer to, inline the rest
---------------------------------------------

A literal that appears in a production — ``"("``, ``";"`` — does not need a
``TOKEN`` declaration; it defines an implicit token. Reserve explicit, *named*
token declarations for the tokens you refer to by name (``<IDENTIFIER>``,
``<NUMBER>``) and for anything with a non-trivial pattern. If you want every
token declared explicitly, set ``REQUIRE_TOKEN_DECLARATION`` and the tool will
flag undeclared literals.

Build complex patterns from private pieces
------------------------------------------

A pattern declared ``<#NAME : …>`` is private: a reusable building block that is
not a token itself. Factoring a hard pattern into named pieces makes it
readable, as the JSON number token shows:

.. code-block:: ccc

   TOKEN :
       <#DIGITS : (["0"-"9"])+ >
     | <#FRACTION : "." <DIGITS> >
     | <NUMBER : <DIGITS> (<FRACTION>)? >
   ;

Handle case where it belongs
----------------------------

For a case-insensitive keyword or two, put ``[IGNORE_CASE]`` on the one token
production: ``TOKEN [IGNORE_CASE] : <IF : "if"> | <ELSE : "else"> ;``. Reach for
the global ``IGNORE_CASE`` setting only when the *entire* language is
case-insensitive — it affects every token.

Keep comments out of the way
----------------------------

Declare comments and other ignorable runs that you might still want later as
``UNPARSED`` (or ``SKIP`` if you never need them). Unparsed tokens are kept and
attached to the following token, so tools can recover them, but they do not
clutter the grammar. Block comments usually want a :ref:`lazy token
<docs/reference/tokenization-advanced:Lazy tokens>` so they end at the first
``*/``.

Mind overlapping matches
------------------------

When two patterns can match the same text, the longest match wins, and ties go
to the token declared first. That is why a keyword like ``begin`` must be
declared before a general ``<IDENTIFIER>`` if it is to win the tie — or, when a
word should be a keyword only in some places, treated as a soft keyword (see
:doc:`context-sensitive`).
