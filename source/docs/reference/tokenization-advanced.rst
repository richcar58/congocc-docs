Advanced Tokenization
=====================

The :doc:`lexical` chapter covers declaring tokens and lexical states. This
chapter covers the features for **context-sensitive tokenization** — having the
same input tokenize differently depending on where the parser is — together
with lazy tokens, synthetic tokens, and token memory management.

Three tools address context sensitivity, in rough order of how localized they
are: lexical states (a mode the lexer is in), token activation (turning token
types on and off as parsing proceeds), and contextual tokens (a token that is
only recognized where the grammar expects it). Guidance on choosing among them
is in :doc:`/docs/userguide/howto/context-sensitive`.

Activating and deactivating tokens
----------------------------------

A token type can be switched on and off during parsing, so a word is treated as
a keyword in some places and an ordinary identifier in others — the classic
problem of "soft" keywords.

Start a token off with the ``DEACTIVATE_TOKENS`` setting, then turn it on for a
specific expansion with an ``ACTIVATE_TOKENS`` prefix on a parenthesized group
(``DEACTIVATE_TOKENS`` works the same way to turn one off):

.. code-block:: ccc

   DEACTIVATE_TOKENS = KW;
   TOKEN : <KW : "begin"> | <ID : (["a"-"z"])+ > ;

   Root : <ID> ACTIVATE_TOKENS KW ( <KW> ) <EOF> ;

Because ``KW`` starts deactivated, the first ``begin`` lexes as an ``ID``; the
``ACTIVATE_TOKENS KW`` prefix makes ``KW`` live for the following group, so the
second ``begin`` lexes as the keyword. Parsing ``begin begin`` gives:

.. code-block:: text

   <Root (1, 1)-(1, 11)>
     ID: (1, 1) - (1, 5): begin
     KW: (1, 7) - (1, 11): begin
     Token: (1, 1) - (1, 1): EOF

Contextual tokens
-----------------

A token declared with the ``CONTEXTUAL`` kind is only produced where the parser
actually allows it; elsewhere its text tokenizes by the other rules. This gives
soft-keyword behavior without managing activation by hand. The pattern of a
contextual token must be a plain string literal:

.. code-block:: ccc

   CONTEXTUAL : <FROM : "from"> ;
   TOKEN : <ID : (["a"-"z"])+ > ;

Where the grammar expects ``<FROM>``, the word ``from`` is the ``FROM`` token;
everywhere else it is an ordinary ``ID``.

Lazy tokens
-----------

A token whose name is prefixed with ``?`` is **lazy**: it matches the *shortest*
text that satisfies its pattern rather than the longest. This is exactly what
block comments need, so that ``/* … */`` ends at the first ``*/`` instead of the
last:

.. code-block:: ccc

   UNPARSED : <?BLOCK_COMMENT : "/*" (~[])* "*/" > ;
   TOKEN : <ID : (["a"-"z"])+ > ;
   Root : ( <ID> )* <EOF> ;

Parsing ``a /* one */ b /* two */ c`` keeps the two comments separate, so all
three identifiers come through:

.. code-block:: text

   <Root (1, 1)-(1, 25)>
     ID: (1, 1) - (1, 1): a
     ID: (1, 13) - (1, 13): b
     ID: (1, 25) - (1, 25): c
     Token: (1, 1) - (1, 1): EOF

Without the ``?``, the greedy ``(~[])*`` would run from the first ``/*`` to the
last ``*/``, swallowing ``b`` along with both comment bodies.

Synthetic tokens
----------------

Some languages need token types that the lexer cannot produce by pattern
matching alone — the ``INDENT`` and ``DEDENT`` tokens of an
indentation-sensitive language, for instance. Declare such types with the
``EXTRA_TOKENS`` setting, and emit them from a ``TOKEN_HOOK`` (see
:doc:`injection`), which can inspect each token and insert others around it:

.. code-block:: ccc

   EXTRA_TOKENS = INDENT, DEDENT;

Inserting a token into the stream this way is *token chaining*; it is enabled by
the ``TOKEN_CHAINING`` setting, which also turns on automatically when the
grammar uses the chaining API. See :doc:`settings`.

Releasing tokens
----------------

For very large inputs you may not want the whole token stream retained in
memory. The ``UNCACHE_TOKENS`` construct, used in an expansion, releases tokens
the parser no longer needs as it moves forward.

Token hooks
-----------

The ``TOKEN_HOOK`` method underlies several of the features above: it runs for
every token and may inspect, replace, or chain tokens. Because a hook is just an
injected method, it is documented with the injection mechanism in
:doc:`injection`.
