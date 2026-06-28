Walkthrough: JSON
=================

The earlier tutorials wrote small grammars from scratch. This one reads a
**complete, real grammar** — the JSON grammar bundled with CongoCC — to see how
the pieces covered so far fit together in practice. The grammar lives at
``examples/json/JSON.ccc`` in the source distribution and is an exact
implementation of the JSON specification.

The grammar, part by part
--------------------------

It opens with settings:

.. code-block:: ccc

   PARSER_PACKAGE = "org.parsers.json";
   NODE_PACKAGE   = "org.parsers.json.ast";
   DEFAULT_LEXICAL_STATE = JSON;
   TEST_PRODUCTION = Root;
   TEST_EXTENSION  = json;

``TEST_PRODUCTION`` and ``TEST_EXTENSION`` ask CongoCC to generate a small test
harness that parses ``.json`` files with the ``Root`` production — see
:doc:`/docs/userguide/howto/testing`.

Whitespace is skipped, and the punctuation tokens are grouped under a single
``Delimiter`` node class with a ``#`` annotation:

.. code-block:: ccc

   SKIP : <WHITESPACE : (" "| "\t"| "\n"| "\r")+>;

   TOKEN #Delimiter :
       <COLON : ':'> | <COMMA : ','>
     | <OPEN_BRACKET : '['> | <CLOSE_BRACKET : ']'>
     | <OPEN_BRACE : "{" > | <CLOSE_BRACE : "}">
   ;

The literals show two more techniques: per-alternative node classes, and
private regular expressions used as building blocks. Each kind of literal gets
its own node type (``#BooleanLiteral``, ``#NumberLiteral``, …), and the
``<#…>`` patterns are private — reusable inside other patterns but not tokens
themselves:

.. code-block:: ccc

   TOKEN #Literal :
       <TRUE: 'true'> #BooleanLiteral
       | <FALSE: "false"> #BooleanLiteral
       | <NULL: "null"> #NullLiteral
       | <#ESCAPE1 : '\\' (['\\', '"', '/',"b","f","n","r","t"])>
       | <#ESCAPE2 : "\\u" (["0"-"9", "a"-"f", "A"-"F"]) {4}>
       | <#REGULAR_CHAR : ~["\u0000"-"\u001F",'"',"\\"]>
       | <STRING_LITERAL : '"' (<REGULAR_CHAR>|<ESCAPE2>|<ESCAPE1>)* '"'> #StringLiteral
       | <#ZERO : "0"> | <#NON_ZERO : (['1'-'9'])(["0"-"9"])*>
       | <#FRACTION : "." (["0"-"9"])+>
       | <#EXPONENT : ["E","e"]["+","-"](["1"-"9"])+>
       | <NUMBER : ("-")?(<ZERO>|<NON_ZERO>)(<FRACTION>)?(<EXPONENT>)?> #NumberLiteral
   ;

The productions are short and mutually recursive — a ``Value`` may be an
``Array`` or a ``JSONObject``, each of which contains ``Value``\ s:

.. code-block:: ccc

   Root : Value! <EOF>! ;
   Value : <TRUE> | <FALSE> | <NULL> | <STRING_LITERAL> | <NUMBER> | Array | JSONObject ;
   Array : <OPEN_BRACKET> [ Value (<COMMA> Value)*! ] <CLOSE_BRACKET> ;
   KeyValuePair : <STRING_LITERAL> <COLON> Value;
   JSONObject : <OPEN_BRACE>! [ KeyValuePair ("," KeyValuePair)*! ] <CLOSE_BRACE>! ;

.. note::

   The ``!`` markers are for :doc:`/docs/reference/fault-tolerance` and are
   ignored unless the grammar is generated with ``FAULT_TOLERANT`` set, so they
   can be left in place for ordinary use.

The resulting tree
------------------

Parsing ``{"a": 1, "b": [true, null]}`` produces a tree in which every value
has a precisely typed node — the per-alternative ``#`` annotations paying off:

.. code-block:: text

   <Root (1, 1)-(1, 28)>
     <JSONObject (1, 1)-(1, 27)>
       Delimiter: (1, 1) - (1, 1): {
       <KeyValuePair (1, 2)-(1, 7)>
         StringLiteral: (1, 2) - (1, 4): "a"
         Delimiter: (1, 5) - (1, 5): :
         NumberLiteral: (1, 7) - (1, 7): 1
       Delimiter: (1, 8) - (1, 8): ,
       <KeyValuePair (1, 10)-(1, 26)>
         StringLiteral: (1, 10) - (1, 12): "b"
         Delimiter: (1, 13) - (1, 13): :
         <Array (1, 15)-(1, 26)>
           Delimiter: (1, 15) - (1, 15): [
           BooleanLiteral: (1, 16) - (1, 19): true
           Delimiter: (1, 20) - (1, 20): ,
           NullLiteral: (1, 22) - (1, 25): null
           Delimiter: (1, 26) - (1, 26): ]
       Delimiter: (1, 27) - (1, 27): }
     Token: (2, 1) - (2, 1): EOF

Notice ``true`` is a ``BooleanLiteral`` and ``null`` a ``NullLiteral`` — the
node types come straight from the per-alternative annotations in the ``Literal``
token production.

Next
----

:doc:`beyond` points to the larger bundled grammars to study next, and the
:doc:`/docs/userguide/howto/trees` guide shows how to walk a tree like this one.
