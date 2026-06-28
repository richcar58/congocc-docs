Code Injection
==============

The classes CongoCC generates — the parser, the lexer, the node types — are
often most useful with some of your own code added to them: a helper method on
a node, a field on the parser, an extra interface. The ``INJECT`` statement adds
that code **from the grammar**, so it lands in the generated files without your
ever editing them by hand and survives every regeneration. 

A first injection
-----------------

This grammar gives its ``KeyValue`` node class a ``getKey()`` method:

.. code-block:: ccc

   Config : ( Pair )+ <EOF> ;
   Pair #KeyValue : <ID> "=" <NUM> ;

   INJECT KeyValue :
   {
       public String getKey() {
           return firstChildOfType(ID.class).getSource();
       }
   }

After generation, ``KeyValue`` has the method, so consuming code can call it
directly:

.. code-block:: java

   for (KeyValue kv : parser.rootNode().descendantsOfType(KeyValue.class))
       System.out.println("key=" + kv.getKey());   // key=x, key=y

Anatomy of an injection
-----------------------

The full form names a target and may supply imports, a superclass, interfaces,
and a body of members:

.. code-block:: ccc

   INJECT TargetName :
       import java.util.List;
       extends SomeBaseClass
       implements SomeInterface
   {
       private List<Foo> foos;
       public List<Foo> getFoos() { return foos; }
   }

Every part is optional. A short injection that only sets a superclass needs no
body at all:

.. code-block:: ccc

   INJECT MyNode : extends AbstractBaseNode

Injection targets
-----------------

The target name is the class to inject into. It may be:

- a **node type** — a production's node name (``KeyValue`` above), to add
  behavior to one kind of tree node;
- the **parser** or **lexer**, referred to by the magic names ``PARSER_CLASS``
  and ``LEXER_CLASS``, which resolve to whatever those classes are actually
  named;
- the **base node or token class**, ``BASE_NODE_CLASS`` and
  ``BASE_TOKEN_CLASS``, to add behavior shared by every node or token;
- the ``Node`` interface, to add default methods to all nodes.

Because ``#abstract`` and ``#interface`` node descriptors (see
:doc:`tree-building`) let you introduce shared supertypes in the tree,
injection into those supertypes is the idiomatic way to give a family of nodes
common behavior.

Hooks
-----

A **hook** is a method with a special name that, when you inject it into the
parser, CongoCC wires into the generated code at the right place. Hooks are how
you run your own logic during lexing and tree building without a configuration
setting. The recognized hooks are:

``TOKEN_HOOK``
   Called for each token as it is produced, receiving the token and returning a
   token — possibly a different or modified one. This is the mechanism behind
   context-sensitive tokenization and synthetic tokens (see
   :doc:`tokenization-advanced`). Its signature takes and returns the base
   token type:

   .. code-block:: ccc

      INJECT PARSER_CLASS :
      {
          BASE_TOKEN_CLASS TOKEN_HOOK(BASE_TOKEN_CLASS t) {
              // inspect or transform t, then ...
              return t;
          }
      }

``OPEN_NODE_HOOK`` / ``CLOSE_NODE_HOOK``
   Called as each tree node is opened and closed, for code that needs to run on
   entry to and exit from a production's node scope.

``RESET_TOKEN_HOOK``
   Called when token processing is reset.

Defining a method with one of these names is all that is required; there is no
separate setting to enable it. (These hooks subsume the legacy
``COMMON_TOKEN_ACTION`` and ``NODE_SCOPE_HOOK`` options; see
:doc:`appendices/legacy`.)

Injected code and target languages
----------------------------------

Injected code is written in the target language, so an injection ties the
grammar to that language. To keep a grammar usable for several targets, guard
language-specific injections with the preprocessor —

.. code-block:: ccc

   #if __java__
   INJECT PARSER_CLASS : { /* Java-specific members */ }
   #endif

— or keep injected code out of the grammar entirely. The
:doc:`Target Language Guide </docs/targets/targets>` covers how injected code
differs across Java, Python, C#, and Rust, including the generated stub files
that mark where handwritten Rust belongs.
