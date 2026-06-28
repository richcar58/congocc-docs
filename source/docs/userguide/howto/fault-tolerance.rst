How To: Parse Resiliently
=========================

.. warning::

   This feature is **experimental**. The syntax and behavior described here may
   change, and some capabilities are incomplete.

Tools that parse code as it is typed — editors, IDEs, linters — must cope with
input that is constantly incomplete or wrong, and still produce something
useful. This guide shows how to use CongoCC's fault-tolerant parsing for that;
the reference is :doc:`/docs/reference/fault-tolerance`.

Turn it on
----------

Set ``FAULT_TOLERANT`` in the grammar. This generates the recovery machinery and
makes the recovery markers active (without it, they are ignored). A generated
parser can also be switched between strict and tolerant at run time with
``setParserTolerant(boolean)`` — strict for a final, authoritative parse;
tolerant for the live, in-progress one.

Mark the points where recovery makes sense
------------------------------------------

Put a ``!`` on the tokens whose absence should not derail the parse — typically
closing delimiters and statement terminators. A grammar that tolerates a missing
closing parenthesis:

.. code-block:: ccc

   FAULT_TOLERANT;
   Root : <LP> <ID> <RP>! <EOF> ;

Parsing the incomplete input ``( a`` still yields a tree; the missing ``)``
becomes an incomplete node rather than a thrown exception:

.. code-block:: text

   <Root (1, 1)-(1, 3)>
     LP: (1, 1) - (1, 1): (
     ID: (1, 3) - (1, 3): a
     RP: (1, 1) - (1, 1):  (incomplete)
     Token: (1, 1) - (1, 1): EOF

For larger recovery scopes, wrap a construct in ``ATTEMPT … RECOVER …`` or give
a production a ``RECOVER_TO`` target so the parser resynchronizes at the next
statement or declaration boundary.

Consume the result defensively
------------------------------

Code that walks a fault-tolerantly parsed tree must expect nodes flagged as
incomplete and handle them gracefully — skipping them, or offering completions
where they sit. The errors the parser recovered from are collected and available
rather than thrown, so a tool can surface several problems at once instead of
stopping at the first.

A good way to learn the feature is to generate one of the bundled grammars (they
already carry ``!`` markers) with ``-p FT`` or a ``FAULT_TOLERANT`` setting and
feed it deliberately broken input.
