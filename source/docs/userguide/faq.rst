FAQ
===

**Which languages can CongoCC generate?**
   Java, Python, C#, and Rust, from the same grammar, selected with the
   ``-lang`` flag. Java is the default. See the
   :doc:`Target Language Guide </docs/targets/targets>`.

**Do I need JJTree to build a syntax tree?**
   No. Tree building is part of CongoCC and on by default; there is no separate
   preprocessor. See :doc:`/docs/reference/tree-building`.

**How do I get the tree after parsing?**
   Call the start production, then ``parser.rootNode()``. Walk it with the
   ``Node`` API or a visitor; see :doc:`howto/trees`.

**Why didn't a production produce a node?**
   Smart node creation (on by default) omits a node for a production that has
   only one child, to avoid trivial wrappers. Give the production an explicit
   ``#Name``, or turn off ``SMART_NODE_CREATION``, to keep it. See
   :doc:`/docs/reference/tree-building`.

**Do I have to catch the parse exception?**
   Not by default — ``ParseException`` is unchecked. Set
   ``USE_CHECKED_EXCEPTION`` if you want the compiler to enforce handling it.
   See :doc:`/docs/reference/generated-api`.

**How do I make the grammar case-insensitive?**
   Put ``[IGNORE_CASE]`` on a single token production for a few keywords, or set
   the global ``IGNORE_CASE`` for the whole language. See
   :doc:`/docs/reference/lexical`.

**How are comments handled?**
   Declare them ``UNPARSED`` (kept and attached to the next token, so tools can
   recover them) or ``SKIP`` (discarded). Block comments usually want a lazy
   token; see :doc:`howto/tokens`.

**Can a word be a keyword in some places and an identifier in others?**
   Yes — that is a "soft keyword". Use token activation or a contextual token;
   see :doc:`howto/context-sensitive`.

**What Java version do I need?**
   Java 17 or later to run CongoCC. See :doc:`/docs/userguide/installation`.

**How do I move an old JavaCC grammar to CongoCC?**
   Convert its syntax, then build with CongoCC and apply a few fix-ups. See
   :doc:`migration`.

**Is there an editor mode or syntax highlighting for ``.ccc`` files?**
   A grammar is plain UTF-8 text, so any editor works; there is no dedicated
   plugin required. The bundled ``examples/`` grammars are good references to
   open alongside your own.
