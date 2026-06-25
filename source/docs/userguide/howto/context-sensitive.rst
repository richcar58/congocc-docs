How To: Handle Context-Sensitive Input
======================================

Sometimes the same characters should tokenize differently depending on where
they appear. CongoCC offers three tools for this; the trick is choosing the
right one. The reference for all three is
:doc:`/docs/reference/tokenization-advanced`.

The three tools
---------------

Lexical states
   A *mode* the lexer is in. Use a lexical state when a stretch of input follows
   entirely different rules — the inside of a string or comment, a here-document,
   an embedded sublanguage. You switch states on entering and leaving the region.

Token activation
   Turning individual token types on and off as the parser proceeds, with
   ``DEACTIVATE_TOKENS`` / ``ACTIVATE_TOKENS``. Use it when a token should exist
   only during part of the parse — for example a word that is a keyword in one
   construct and an ordinary identifier elsewhere, where *you* know from the
   grammar exactly where it is active.

Contextual tokens
   A token, declared ``CONTEXTUAL``, that the lexer produces only where the
   grammar already expects it. Use it for soft keywords when you want the
   "keyword only where it fits" behavior automatically, without managing
   activation by hand.

Choosing among them
-------------------

- Is it a whole **region** with different lexical rules? → **lexical state**.
- Is it a **soft keyword** you want recognized automatically wherever the
  grammar allows it? → **contextual token**.
- Do you need **explicit control** over exactly where a token is live, or to
  toggle several tokens together? → **token activation**.

A soft keyword, two ways
------------------------

Suppose ``begin`` should be a keyword only in one place. With activation, you
deactivate it globally and switch it on for the relevant expansion:

.. code-block:: text

   DEACTIVATE_TOKENS = KW;
   Root : <ID> ACTIVATE_TOKENS KW ( <KW> ) <EOF> ;

With a contextual token, you declare it and let the parser decide:

.. code-block:: text

   CONTEXTUAL : <KW : "begin"> ;

Both make ``begin`` a keyword only where intended; reach for activation when you
want explicit control, and a contextual token when you want it handled for you.
