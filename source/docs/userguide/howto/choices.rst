How To: Resolve Choice Conflicts
================================

When CongoCC cannot tell two alternatives apart, it warns — typically
``Expansion is unreachable.`` — and the parser silently always takes the first.
This guide is the practical workflow for fixing that; the full reference is
:doc:`/docs/reference/disambiguation`.

Step 1: see whether you have a conflict
---------------------------------------

Generate the grammar and read the warnings. An ``Expansion is unreachable``
warning at a choice means two branches begin with the same token, so the later
branch can never be reached:

.. code-block:: ccc

   Entry : ( Declaration | Reference )+ <EOF> ;
   Declaration : <ID> <COLON> <ID> ;
   Reference   : <ID> ;            // unreachable: both start with <ID>

Step 2: reach for ``=>||`` first
--------------------------------

The up-to-here marker handles the common case with the least ceremony. Put it
in the alternative just past the point that makes the choice unambiguous:

.. code-block:: ccc

   Declaration : <ID> <COLON> =>|| <ID> ;

Now the parser scans ``<ID> <COLON>`` before committing to ``Declaration`` and
falls through to ``Reference`` otherwise. This is the right tool for the large
majority of conflicts.

Step 3: use ``SCAN`` for the special cases
------------------------------------------

Switch to a full ``SCAN`` when ``=>||`` is not enough:

- a **numeric** limit when unbounded scanning is wasteful — ``SCAN 3 => …``;
- a **semantic** condition that depends on run-time state —
  ``SCAN { someFlag } => …``;
- a **contextual** predicate that depends on the parse stack, such as the
  non-reentrancy guard ``SCAN ~\...\Foo => Foo``.

Step 4: prefer restructuring to heavy lookahead
-----------------------------------------------

If two alternatives share a long common prefix, factor the prefix out so the
choice happens later, when the alternatives genuinely differ. A grammar that
needs little lookahead is usually clearer and faster than one that scans far
ahead at every turn — left-factoring is often the better fix.
