Productions and Expansions
==========================

A **production** is a grammar rule. Each production describes how to match one
syntactic construct, and CongoCC turns each one into a method in the generated
parser. The body of a production is an **expansion**: a pattern built from
token references, calls to other productions, and the choice, grouping, and
repetition operators. This chapter describes the syntax of productions and
expansions; how the parser chooses between alternatives is the subject of
:doc:`disambiguation`.

A first production
------------------

In its most common form a production is just a name, a colon, an expansion, and
a semicolon:

.. code-block:: text

   NumberList : <NUMBER> ( <COMMA> <NUMBER> )* <EOF> ;

This reads: *a NumberList is a NUMBER, followed by zero or more occurrences of a
COMMA and a NUMBER, followed by the end of input.*

Expansion units
---------------

An expansion is a sequence of units. The units are:

Terminals
   A token to match, written either as a string literal (``"while"``) or as a
   token reference in angle brackets (``<IDENTIFIER>``, ``<EOF>``). A string
   literal used here implicitly defines a token unless
   ``REQUIRE_TOKEN_DECLARATION`` is set; see :doc:`lexical`.

Non-terminals
   The name of another production, optionally with arguments —
   ``Expression`` or ``ArgumentList(true)``. This calls the named production at
   that point in the parse.

Sequences
   Writing units one after another matches them in order:
   ``<IF> "(" Expression ")" Statement``.

Choices
   ``|`` separates alternatives, matching exactly one of them:
   ``Statement : IfStatement | WhileStatement | Block``.

The grouping and repetition operators apply to a parenthesized expansion:

==================  =====================================================
Form                Meaning
==================  =====================================================
``( … )``           grouping
``( … )?``          optional — zero or one occurrence
``[ … ]``           optional — an equivalent spelling of ``( … )?``
``( … )*``          zero or more occurrences
``( … )+``          one or more occurrences
==================  =====================================================

For example, an optional ``else`` clause and an optional list of comma-separated
arguments:

.. code-block:: text

   IfStatement : <IF> "(" Expression ")" Statement [ <ELSE> Statement ] ;

   ArgumentList : "(" [ Expression ( "," Expression )* ] ")" ;

Embedded code and actions
-------------------------

Braces enclose **embedded code** written in the target language. Two types of code
embeddings are defined based on their position: a *declaration prologue* 
immediately after the colon, holding local
declarations for the production, and *actions* interspersed in the expansion,
which run when the parser reaches that point.

The following grammar parses a sum of integers and actually evaluates it. The
production has a **return type** (``int``), a prologue declaring two locals, and
actions that accumulate the total:

.. code-block:: text

   PARSER_PACKAGE = "prod.test";

   SKIP : " " | "\t" | "\r" | "\n" ;

   TOKEN : <NUMBER : (["0"-"9"])+ > ;

   int Sum :
      {
         int total = 0;
         Token n;
      }
      n = <NUMBER> { total += Integer.parseInt(n.toString()); }
      ( "+" n = <NUMBER> { total += Integer.parseInt(n.toString()); } )*
      <EOF>
      { return total; }
   ;

The assignment ``n = <NUMBER>`` captures the matched token into the local
variable ``n``; ``n.toString()`` is its matched text. Because ``Sum`` declares a
return type, the generated method returns a value, so a caller can write:

.. code-block:: java

   int result = new SumParser("1 + 2 + 3").Sum();   // result == 6

.. note::

   Embedded code ties a grammar to one target language. A grammar that uses
   Java actions like the one above generates a working parser only for Java. To
   keep a grammar usable across all four target languages, avoid embedding
   target-language code in productions, or provide per-language variants; see the
   :doc:`Target Language Guide </docs/targets/targets>`.

Full declaration syntax
-----------------------

Beyond the common ``Name : expansion ;`` form, a production declaration may
include several optional parts, in this order:

.. code-block:: text

   [access]  [ReturnType]  Name  [(parameters)]  [throws …]
       [ #NodeDescriptor ]  [ RECOVER_TO expansion ]  :
       [ LexicalState : ]  expansion ;

access modifier
   ``public``, ``private``, or ``protected`` on the generated method.

return type and parameters
   A production may declare a return type and a formal parameter list, exactly
   as the above ``Sum`` example returns ``int``. Parameters let one production pass
   information to another: ``ArgumentList(boolean allowEmpty)`` is called as
   ``ArgumentList(true)``.

``throws`` list
   Declares checked exceptions the production's method may throw.

node descriptor
   A ``#`` annotation controls the tree node the production builds — its name,
   or whether it builds a node at all. Node descriptors are covered in
   :doc:`tree-building`.

``RECOVER_TO`` and ``!``
   Markers used by fault-tolerant parsing, described in
   :doc:`fault-tolerance`.

starting lexical state
   ``Name : STATE : expansion ;`` makes the production begin matching in the
   named :ref:`lexical state <docs/reference/lexical:Lexical states>`.

Lookahead and assertions in expansions
--------------------------------------

Two further types of descriptors may appear in an expansion, but they are discussed
elsewhere because they govern *how* the parser matches rather than *what* it
matches:

- ``SCAN``, the up-to-here markers (``=>||``), and ``=>`` control lookahead at
  choice points — see :doc:`disambiguation`.
- ``ASSERT`` and ``ENSURE`` assert conditions during parsing or lookahead, and
  ``FAIL`` forces an error — also in :doc:`disambiguation`.
