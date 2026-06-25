Walkthrough: A Calculator
=========================

The :doc:`first-grammar` tutorial parsed a flat list. This one builds a grammar
with real structure — an arithmetic expression evaluator that respects operator
precedence and parentheses — and has it compute the answer as it parses. It
assumes you have worked through the first tutorial.

The grammar
-----------

.. code-block:: text

   PARSER_PACKAGE = "calc";

   SKIP : " " | "\t" | "\r" | "\n" ;

   TOKEN :
       <NUMBER : (["0"-"9"])+ ( "." (["0"-"9"])+ )? >
     | <PLUS : "+"> | <MINUS : "-"> | <TIMES : "*"> | <DIVIDE : "/">
     | <LPAREN : "("> | <RPAREN : ")">
   ;

   double Calc :
       { double result; }
       result = Expression <EOF>
       { return result; }
   ;

   double Expression :
       { double result, r; }
       result = Term
       (
           <PLUS>  r = Term { result += r; }
         | <MINUS> r = Term { result -= r; }
       )*
       { return result; }
   ;

   double Term :
       { double result, r; }
       result = Factor
       (
           <TIMES>  r = Factor { result *= r; }
         | <DIVIDE> r = Factor { result /= r; }
       )*
       { return result; }
   ;

   double Factor :
       { double result; Token t; }
       (
           t = <NUMBER> { result = Double.parseDouble(t.toString()); }
         | <LPAREN> result = Expression <RPAREN>
       )
       { return result; }
   ;

Precedence through layering
---------------------------

Precedence is expressed by the *shape* of the grammar, not by any special
operator-priority feature. ``Expression`` handles ``+`` and ``-`` but defers to
``Term`` for its operands; ``Term`` handles ``*`` and ``/`` but defers to
``Factor``. Because multiplication is matched "below" addition, it binds
tighter. Writing each level as a loop — ``Term (("+"|"-") Term)*`` — also makes
the operators **left-associative**, so ``10 - 2 - 3`` groups as ``(10 - 2) - 3``.

Recursion through parentheses
-----------------------------

``Factor`` is either a number or a parenthesized expression, and the
parenthesized case calls ``Expression`` again. That single recursive reference
is what lets expressions nest to any depth.

Evaluating as you parse
-----------------------

Each production declares a ``double`` return type and evaluates its part of the
expression in embedded actions, combining the values its sub-productions return.
The result of ``Calc`` is the value of the whole expression — no separate
tree-walking pass is needed for a simple evaluator like this one.

Running it
----------

Generating the grammar and calling ``Calc()`` on a few inputs:

.. code-block:: text

   2 + 3 * 4        = 14.0
   (2 + 3) * 4      = 20.0
   10 - 2 - 3       = 5.0
   2 * (3 + 4) / 7  = 2.0
   1.5 + 2.5        = 4.0

The first two lines show precedence and parentheses at work; the third shows
left-associative subtraction.

.. note::

   Every choice in this grammar is decided by the next token — ``+`` versus
   ``-``, a number versus an opening parenthesis — so no lookahead is needed.
   When alternatives *cannot* be told apart by their first token, you add
   lookahead as described in :doc:`/docs/reference/disambiguation`.

Next
----

The calculator evaluates immediately and discards structure. Most real tools
keep the tree instead and process it afterward; :doc:`json` reads a complete
grammar that does exactly that, and :doc:`/docs/userguide/howto/trees` shows how
to consume the result.
