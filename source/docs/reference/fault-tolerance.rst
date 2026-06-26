Fault-Tolerant Parsing
======================

.. warning::

   This feature is **experimental**. The syntax and behavior described here may
   change, and some capabilities are incomplete.

Ordinarily, a parser stops at the first input it cannot match. A
*fault-tolerant* parser instead does its best to recover and keep going, so
that even incomplete or incorrect input yields a usable tree. This is what
editors, IDEs, and language tooling need: the code being parsed is constantly
in a half-finished state, yet the tool must still produce an outline, offer
completions, and report more than one error at a time.

Enabling fault tolerance
------------------------

Fault tolerance is off unless you ask for it, because it changes the generated
code. Turn it on with the ``FAULT_TOLERANT`` setting:

.. code-block:: text

   FAULT_TOLERANT;

With it set, CongoCC generates the recovery machinery and honors the recovery
markers below. ``FAULT_TOLERANT_DEFAULT`` controls whether tolerant mode is
active at run time by default (it is), and a generated parser can be switched
between strict and tolerant parsing at run time with
``setParserTolerant(boolean)``. When ``FAULT_TOLERANT`` is *not* set, the
recovery markers in a grammar are simply ignored, so they can be left in place.

Tolerant points: the ``!`` marker
----------------------------------

A ``!`` after a token, a non-terminal, or a parenthesized group marks it as a
**tolerant point**: if the parser cannot match it, rather than failing it
inserts a placeholder and carries on. This grammar tolerates a missing closing
parenthesis:

.. code-block:: text

   FAULT_TOLERANT;
   TOKEN : <ID : (["a"-"z"])+ > | <LP : "("> | <RP : ")"> ;
   Root : <LP> <ID> <RP>! <EOF> ;

Parsing the incomplete input ``( a`` still produces a tree; the missing ``)`` is
represented by an incomplete node:

.. code-block:: text

   <Root (1, 1)-(1, 3)>
     LP: (1, 1) - (1, 1): (
     ID: (1, 3) - (1, 3): a
     RP: (1, 1) - (1, 1):  (incomplete)
     Token: (1, 1) - (1, 1): EOF

A variant, ``!->`` followed by a code block, runs that block to perform custom
recovery instead of simply inserting a placeholder.

``ATTEMPT`` / ``RECOVER``
-------------------------

For recovery that spans more than a single point, ``ATTEMPT`` wraps an
expansion and pairs it with a ``RECOVER`` clause that runs if the attempted
expansion fails:

.. code-block:: text

   ATTEMPT Expression RECOVER ( skipToSemicolon() )

The ``RECOVER`` clause is either a parenthesized expansion to parse instead or
an embedded code block to execute.

Production-level recovery with ``RECOVER_TO``
---------------------------------------------

A production may declare a ``RECOVER_TO`` expansion before its colon. If an
error occurs while the production is being parsed, the parser skips ahead to
that recovery expansion — a natural way to resynchronize at a statement or
declaration boundary:

.. code-block:: text

   Statement RECOVER_TO ";" : … ;

Incomplete nodes in the tree
----------------------------

Recovery leaves marks in the tree: nodes that were inserted or left unfinished
are flagged as incomplete (the ``(incomplete)`` annotation in the dump above).
Consumers of a fault-tolerantly parsed tree should expect such nodes and can
test for them through the node API (see :doc:`generated-api`). The list of
errors the parser recovered from is likewise available rather than thrown.

Status
------

Fault tolerance is usable but, as noted, experimental and not fully polished.
The bundled grammars carry ``!`` markers (ignored unless ``FAULT_TOLERANT`` is
set) and are a good source of worked examples. Task-oriented guidance is in
:doc:`/docs/userguide/howto/fault-tolerance`.
