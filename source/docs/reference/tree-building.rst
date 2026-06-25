Tree Building
=============

CongoCC builds a syntax tree as it parses, with no extra tooling — tree
building is part of the core and is on by default. This chapter explains the
default tree, the ``#`` annotations that shape it, and the API for walking the
result. The examples below are language-neutral in spirit; the dumps shown are
from the Java target.

The default tree
----------------

Out of the box, every production builds a node, and every token is a node too.
Given this grammar:

.. code-block:: text

   Config : ( Pair )+ <EOF> ;
   Pair : <ID> "=" Value ;
   Value : <NUM> ;

parsing ``a=1 b=22`` produces:

.. code-block:: text

   <Config (1, 1)-(1, 8)>
     <Pair (1, 1)-(1, 3)>
       ID: (1, 1) - (1, 1): a
       Token: (1, 2) - (1, 2): =
       NUM: (1, 3) - (1, 3): 1
     <Pair (1, 5)-(1, 8)>
       ID: (1, 5) - (1, 5): b
       Token: (1, 6) - (1, 6): =
       NUM: (1, 7) - (1, 8): 22
     Token: (1, 1) - (1, 1): EOF

Each ``Pair`` production became a ``Pair`` node, each token (including the
implicit ``"="`` and the final ``EOF``) is a node, and the whole thing is
rooted at a ``Config`` node. Notice there is **no** ``Value`` node even though
``Value`` is a production — that is the effect of smart node creation, described next.

Smart node creation
-------------------

By default (``SMART_NODE_CREATION = true``) a production builds a node only when
it would have **more than one** child. A production like ``Value : <NUM>`` that
wraps a single child therefore adds no node of its own; the child takes its
place. This keeps trees free of trivial one-child wrappers.

Turn it off to get a node for every production. With
``SMART_NODE_CREATION = false;`` the same grammar yields a ``Value`` node around
each number:

.. code-block:: text

       <Pair (1, 1)-(1, 3)>
         ID: (1, 1) - (1, 1): a
         Token: (1, 2) - (1, 2): =
         <Value (1, 3)-(1, 3)>
           NUM: (1, 3) - (1, 3): 1

Shaping the tree with ``#`` descriptors
---------------------------------------

A ``#`` annotation on a production (or on a parenthesized expansion) overrides
the default and states exactly what node, if any, to build.

Renaming a node
   ``Pair #KeyValue : <ID> "=" Value ;`` builds a node named ``KeyValue``
   instead of ``Pair``. The generated node class is named accordingly.

Suppressing a node with ``#void``
   ``Pair #void : <ID> "=" Value ;`` builds **no** node for ``Pair``; its
   children attach to the parent instead. With ``#void`` on ``Pair`` the tree
   above flattens to:

   .. code-block:: text

      <Config (1, 1)-(1, 8)>
        ID: (1, 1) - (1, 1): a
        Token: (1, 2) - (1, 2): =
        NUM: (1, 3) - (1, 3): 1
        ID: (1, 5) - (1, 5): b
        Token: (1, 6) - (1, 6): =
        NUM: (1, 7) - (1, 8): 22
        Token: (1, 1) - (1, 1): EOF

Conditional nodes
   A condition in parentheses builds the node only when the condition holds. The
   comparison operators ``>``, ``>=``, ``<``, ``<=`` test the number of child
   nodes the production produced. For example, ``#List(>1)`` builds a ``List``
   node only when it would hold more than one child:

   .. code-block:: text

      Root : Items <EOF> ;
      Items #List(>1) : ( <NUM> )+ ;

   Parsing ``5`` builds no ``List`` (the lone ``NUM`` attaches to ``Root``),
   while ``5 6 7`` does:

   .. code-block:: text

      <Root (1, 1)-(1, 5)>
        <List (1, 1)-(1, 5)>
          NUM: (1, 1) - (1, 1): 5
          NUM: (1, 3) - (1, 3): 6
          NUM: (1, 5) - (1, 5): 7
        Token: (1, 1) - (1, 1): EOF

   .. note::

      The count is of **child nodes**, which includes token nodes. If the
      production also matches punctuation or ``<EOF>``, those count too — keep
      that in mind when choosing the threshold.

Abstract and interface node types
   ``#abstract`` and ``#interface`` mark the generated node type as abstract or
   as an interface, for grammars that build a node class hierarchy and inject
   shared behavior into it (see :doc:`injection`).

Inline descriptors
   A ``#`` descriptor may also follow a parenthesized expansion *inside* a
   production, naming a node for just that part: ``( A B C )#Group``.

Whole-grammar defaults
   Two settings change the baseline. ``NODE_DEFAULT_VOID = true;`` inverts the
   default so that productions build a node only when they explicitly ask for
   one with a ``#`` descriptor. ``NODE_PREFIX`` prepends a fixed string to every
   generated node class name. See :doc:`settings`.

Tokens as nodes
---------------

Because ``TOKENS_ARE_NODES`` is true by default, tokens appear in the tree as
terminal nodes, as the dumps above show. Set ``TOKENS_ARE_NODES = false;`` to
keep tokens out of the tree entirely (the JJTree-style behavior).

Unparsed tokens — comments and the like — are excluded by default; set
``UNPARSED_TOKENS_ARE_NODES = true;`` to include them. And as covered in
:doc:`lexical`, a ``#ClassName`` on a token production groups its tokens under a
shared node class, which is handy when you want to treat a family of tokens
uniformly in the tree.

Walking the tree
----------------

Every node implements the ``Node`` interface, which provides a broad traversal
API. The most commonly used members are:

- ``children()`` — the direct child nodes; ``descendants()`` — all nodes below.
- ``firstChildOfType(...)`` / ``childrenOfType(...)`` and the ``descendant``
  equivalents, selecting by node class or by ``NodeType``.
- ``getType()`` — the node's ``NodeType``; ``getParent()`` — its parent.
- ``getSource()`` — the source text the node spans.
- ``dump()`` — print the subtree, as used throughout this chapter.

The full contract is in :doc:`generated-api`.

Visitors
--------

For traversals that dispatch on node type, extend the built-in
``Node.Visitor`` base class and define a ``visit`` method for each node type you
care about. Start the walk with ``visit(rootNode)``; call ``recurse(node)`` from
within a ``visit`` method to descend into that node's children. A visitor that
prints every ``KeyValue`` node looks like this:

.. code-block:: java

   class Printer extends Node.Visitor {
       public void visit(KeyValue kv) {
           System.out.println("pair -> " + kv.getSource());
           recurse(kv);
       }
   }

   // new Printer().visit(parser.rootNode());  prints:
   //   pair -> a=1
   //   pair -> b=22

Dispatch is by the node's runtime class, falling back to its superclasses and
marked interfaces, so a ``visit`` method written for a base class or an
``#interface`` node type handles all of its subtypes. Set the visitor's
``visitUnparsedTokens`` field to also visit comment tokens. No separate visitor
class is generated and there is no ``VISITOR`` setting — the one reflective base
class above is the whole mechanism.

Cardinality constraints
-----------------------

A repetition can carry a *cardinality constraint* — an assertion of how many
times it must match — written with the ``&`` form of an assertion. These build
on the assertion machinery in :doc:`disambiguation`.

Coming from JJTree
------------------

CongoCC's tree building replaces the separate `JJTree <https://javacc.github.io/javacc/documentation/jjtree.html>`_ 
preprocessor from `JavaCC <https://javacc.github.io/javacc/documentation/>`_. There is no
``jjtThis``, no ``SimpleNode``, and no generated visitor interface to implement;
tree building is on by default rather than opt-in, and the node API is the rich
``Node`` interface shown above. The full mapping is in :doc:`appendices/legacy`.
