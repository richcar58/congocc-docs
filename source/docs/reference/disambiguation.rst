Disambiguation
==============

CongoCC generates a deterministic recursive-descent parser. At each **choice
point** — a ``|`` alternation, an optional ``[ … ]``, or a loop ``( … )*`` /
``( … )+`` — the parser must decide what to do by looking at upcoming
input. By default, it decides using the next single token. When one
token is not enough to tell the alternatives apart, you can give the parser license to
**lookahead**. This chapter describes the choice-point model and the constructs
that control lookahead, along with the related ``ASSERT`` / ``ENSURE`` and
``FAIL`` features.

Choice points and the first-token rule
--------------------------------------

By default the parser chooses an alternative by checking whether the next token
can begin it — formally, whether the token is in that alternative's *first
set*. This is fast and, for most of a typical grammar, entirely sufficient.

It breaks down when two alternatives can begin with the same token. Consider a
loop that chooses between a declaration (``x : y``) and a bare reference
(``x``), both of which start with an identifier:

.. code-block:: ccc

   Entry : ( Declaration | Reference )+ <EOF> ;
   Declaration : <ID> <COLON> <ID> ;
   Reference   : <ID> ;

Generating this grammar produces a warning, because the parser will always
commit to ``Declaration`` on seeing an ``<ID>`` and can never reach
``Reference``:

.. code-block:: text

   Warning: Entry.ccc:5:25:Expansion is unreachable.

To choose correctly, the parser has to look past the first ``<ID>`` to see
whether a colon follows. There are two ways to tell it to.

The up-to-here marker
---------------------

The simplest and preferred tool is the **up-to-here marker**, ``=>||``. Placed
inside an expansion, it means *scan the input up to this point to decide whether
to take this path.* Mark ``Declaration`` just after the part that makes it
unambiguous — the colon:

.. code-block:: ccc

   Entry : ( Declaration | Reference )+ <EOF> ;
   Declaration : <ID> <COLON> =>|| <ID> ;
   Reference   : <ID> ;

Now the parser, deciding whether to enter ``Declaration``, scans ``<ID>
<COLON>``; if that matches it commits, otherwise it falls through to
``Reference``. The grammar generates without warnings and parses ``x : y z``
as expected:

.. code-block:: text

   <Entry (1, 1)-(1, 7)>
     <Declaration (1, 1)-(1, 5)>
       ID: (1, 1) - (1, 1): x
       COLON: (1, 3) - (1, 3): :
       ID: (1, 5) - (1, 5): y
     ID: (1, 7) - (1, 7): z
     Token: (1, 1) - (1, 1): EOF

(The bare ``ID`` node at the end is the ``Reference``; because ``Reference`` has
a single child, no separate node is created for it — see
:doc:`tree-building`.)

A variant, ``=>|+n|``, scans through the marked expansion **plus** *n* more
tokens — useful when one extra token settles the decision but you do not want to
scan an entire following construct.

The SCAN statement
------------------

``=>||`` is shorthand for the more general ``SCAN`` statement, which supersedes
the ``LOOKAHEAD`` construct of legacy JavaCC. A ``SCAN`` is written at the start
of an alternative, ending with ``=>`` before the expansion it guards. The same
example written with ``SCAN`` places the lookahead condition at the choice
point itself:

.. code-block:: ccc

   Entry : ( SCAN <ID> <COLON> => Declaration | Reference )+ <EOF> ;

``SCAN`` accepts, in combination, any of the following:

Numeric lookahead
   ``SCAN n => …`` permits the parser to look up to *n* tokens ahead when
   matching the guard.

Syntactic lookahead
   ``SCAN expansion => …`` succeeds if *expansion* matches the upcoming input.
   ``SCAN <ID> <COLON> =>`` above is syntactic lookahead. Prefix the expansion
   with ``~`` to negate it.

Semantic lookahead
   ``SCAN { booleanExpression } => …`` consults a boolean expression in the
   target language. It is true when the expression is true. For example,
   ``SCAN { allowTrailingComma } => ","``.

Contextual predicates
   A look-behind condition tests the parser's call stack rather than the
   upcoming tokens — see below.

Unlike legacy ``LOOKAHEAD``, ``SCAN`` defaults to **unlimited** lookahead when
it scans an expansion, and nested syntactic lookahead works correctly: a
``SCAN`` may itself invoke productions that contain their own ``SCAN``\ s.

.. tip::

   Reach for ``=>||`` first; it expresses the common case — "look as far as
   this point" — with the least ceremony. Use a full ``SCAN`` when you need a
   numeric limit, a semantic condition, a contextual predicate, or a negated
   look-ahead. Task-oriented advice on resolving conflicts is in
   :doc:`/docs/userguide/howto/choices`.

Contextual predicates
---------------------

A **contextual predicate** (a look-behind) decides whether to enter an
expansion based on which productions are currently on the parse stack, rather
than on the tokens ahead. The path syntax uses:

==========  ============================================================
Element     Meaning
==========  ============================================================
``\``       step backward, up the call stack toward the current rule
``/``       step forward, down from the root
``.``       match exactly one production of any name
``...``     match zero or more productions of any name
``~``       negate the following step
==========  ============================================================

The most common use is to forbid re-entering a production that is already
active. This enters ``Foo`` only if ``Foo`` is not already somewhere above on
the stack:

.. code-block:: ccc

   [ SCAN ~\...\Foo => Foo ]

Contextual predicates combine with the other forms; for example
``( SCAN 2 ~\...\Foo => Foo )*`` applies both a non-reentrancy check and a
two-token limit. They are a distinctive CongoCC feature and are explored
further in :doc:`/docs/userguide/howto/context-sensitive`.

Assertions
----------

``ASSERT`` and ``ENSURE`` state a condition that must hold; the difference is
*when* the condition is checked. ``ASSERT`` is checked during normal parsing and
raises an error if it fails. ``ENSURE`` is evaluated during lookahead, to steer
a choice. Both accept either a semantic condition or a syntactic expansion:

.. code-block:: ccc

   ASSERT { depth < MAX_DEPTH } : "Nesting too deep"     // semantic, with a message
   ASSERT ~( <ELSE> )                                    // syntactic: assert no else follows

A semantic assertion takes a boolean expression and an optional message
(after ``:``); a syntactic assertion takes a parenthesized expansion, optionally
negated with ``~``. Assertions are also the basis of *cardinality constraints*
on repetitions, covered in :doc:`tree-building`.

.. note::

   Whether a plain ``ASSERT`` is also consulted during lookahead is governed by
   the ``ASSERT_APPLIES_IN_LOOKAHEAD`` setting (default off); ``ENSURE`` always
   applies in lookahead. See :doc:`settings`.

Forcing a failure
-----------------

``FAIL`` aborts the current parse with an error. It is useful in a choice
branch that should never be reached, or to give a better message than the
default for known-bad input:

.. code-block:: ccc

   ( "(" Expression ")" | FAIL "Expected a parenthesized expression" )

``FAIL`` may be followed by a message expression, or by a code block to run.
Fault-tolerant alternatives to outright failure — ``ATTEMPT`` / ``RECOVER`` and
the ``!`` recovery markers — are described in :doc:`fault-tolerance`.
