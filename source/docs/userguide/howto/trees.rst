How To: Shape and Use the Tree
==============================

CongoCC builds a syntax tree for you automatically. This guide is about the
next step: turning that default tree into something convenient to consume, and
getting your data out of it. For the precise meaning of each construct used
here, see the :doc:`/docs/reference/tree-building` reference.

The running example is a tiny configuration language:

.. code-block:: ccc

   PARSER_PACKAGE = "tree.test";
   SKIP : " " | "\t" | "\r" | "\n" ;
   TOKEN : <ID : (["a"-"z"])+ > | <NUM : (["0"-"9"])+ > ;

   Config : ( Pair )+ <EOF> ;
   Pair : <ID> "=" Value ;
   Value : <NUM> ;

Start by looking at the tree
----------------------------

Before shaping anything, generate the parser and dump a tree so you can see
what you actually get. ``Node.dump()`` prints the subtree:

.. code-block:: java

   TreeParser parser = new TreeParser("a=1 b=22");
   parser.Config();
   parser.rootNode().dump();

Working from the real output — rather than from what you imagine the tree looks
like — is the single most useful habit when shaping a grammar's tree.

Decide what is noise, and remove it
-----------------------------------

Two defaults already keep the tree tidy: tokens that wrap a single child do not
get their own node (smart node creation), and you can drop any pass-through
production from the tree with ``#void``. If ``Pair`` were a thin wrapper you did
not care about, ``Pair #void : …`` would lift its children up into ``Config``.

Conversely, if you find a useful production has been optimized away by smart
node creation and you want it back, give it an explicit name (next section) or
turn ``SMART_NODE_CREATION`` off for the whole grammar.

.. tip::

   You rarely need to remove token nodes such as ``"="`` from the tree — it is
   usually easier to simply ignore them when you traverse, using the typed
   accessors below, than to reshape the grammar around them.

Name nodes for the consumer
---------------------------

Give nodes the names the *consumer* of the tree wants to see, not necessarily
the names that were convenient in the grammar. Renaming ``Pair`` to
``KeyValue`` makes the downstream code read well:

.. code-block:: ccc

   Pair #KeyValue : <ID> "=" Value ;

Naming a family of tokens with a shared class — ``TOKEN #Keyword : …`` — is the
token-level equivalent; see :doc:`/docs/reference/lexical`.

Pull data out with the Node API
-------------------------------

Most consumers do not need a visitor at all — the typed accessors on ``Node``
are enough. To collect every key/value pair, find the ``KeyValue`` nodes and,
within each, the ``ID`` and ``NUM`` children:

.. code-block:: java

   import tree.test.ast.*;

   TreeParser p = new TreeParser("a=1 b=22");
   p.Config();
   for (KeyValue kv : p.rootNode().descendantsOfType(KeyValue.class)) {
       Node key = kv.firstChildOfType(ID.class);
       Node val = kv.firstChildOfType(NUM.class);
       System.out.println(key.getSource() + " => " + val.getSource());
   }

This prints:

.. code-block:: text

   a => 1
   b => 22

``descendantsOfType`` and ``firstChildOfType`` accept either a node class (as
here) or a ``NodeType`` value, and ``getSource()`` returns the matched text.
The full set of accessors is in :doc:`/docs/reference/generated-api`.

Use a visitor for type-dispatched work
--------------------------------------

When processing varies by node type — an interpreter or a code generator, say —
a visitor is cleaner than a cascade of type checks. Extend ``Node.Visitor``,
write one ``visit`` method per node type, and call ``recurse`` to descend:

.. code-block:: java

   class Printer extends Node.Visitor {
       public void visit(KeyValue kv) {
           System.out.println("pair -> " + kv.getSource());
           recurse(kv);
       }
   }

   new Printer().visit(parser.rootNode());

Because dispatch follows the class hierarchy, a ``visit`` method for a base
class or an ``#interface`` node type handles all of its subtypes at once — a
good reason to give related productions a common node supertype.

Put behavior on the nodes themselves
------------------------------------

For anything beyond reading, it is often cleanest to add methods or fields
directly to the generated node classes rather than computing over them from
outside. The ``INJECT`` statement does this without your having to edit
generated code — for example, giving ``KeyValue`` a ``getKey()`` method. See
:doc:`/docs/reference/injection`.

Where to go next
----------------

- :doc:`/docs/reference/tree-building` — the full reference for ``#``
  descriptors, smart node creation, and tree settings.
- :doc:`/docs/reference/injection` — adding behavior to node classes.
- :doc:`/docs/reference/generated-api` — the complete ``Node`` API.
