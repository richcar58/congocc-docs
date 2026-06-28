Appendix: Construct Index
=========================

A quick lookup from every keyword, operator, and notation in the grammar
language to the chapter that documents it. Settings have their own complete
listing in :doc:`/docs/reference/settings`.

Lexical
-------

.. list-table::
   :header-rows: 1
   :widths: 50 50

   * - Construct
     - Documented in
   * - ``TOKEN`` / ``REGULAR_TOKEN``, ``SKIP``, ``MORE``, ``UNPARSED`` /
       ``SPECIAL_TOKEN``, ``CONTEXTUAL``
     - :doc:`/docs/reference/lexical`
   * - ``<NAME : … >`` token, ``<NAME>`` reference, ``<EOF>``
     - :doc:`/docs/reference/lexical`
   * - ``<#NAME>`` private regular expression
     - :doc:`/docs/reference/lexical`
   * - ``<?NAME>`` lazy token
     - :doc:`/docs/reference/tokenization-advanced`
   * - character classes ``[ … ]`` and negation ``~[ … ]``
     - :doc:`/docs/reference/lexical`
   * - ``[IGNORE_CASE]`` modifier
     - :doc:`/docs/reference/lexical`
   * - lexical states ``< STATE >`` / ``< * >`` and the ``: NEXT_STATE`` switch
     - :doc:`/docs/reference/lexical`
   * - ``#ClassName`` token node class
     - :doc:`/docs/reference/lexical`

Productions and expansions
--------------------------

.. list-table::
   :header-rows: 1
   :widths: 50 50

   * - Construct
     - Documented in
   * - production ``Name : expansion ;``
     - :doc:`/docs/reference/productions`
   * - terminals (``"literal"``, ``<NAME>``) and non-terminals
     - :doc:`/docs/reference/productions`
   * - choice ``|``, grouping ``( )``, ``( )*``, ``( )+``, ``( )?``, ``[ ]``
     - :doc:`/docs/reference/productions`
   * - embedded code actions ``{ … }``
     - :doc:`/docs/reference/productions`
   * - access modifiers, return types, parameters, ``throws``
     - :doc:`/docs/reference/productions`

Lookahead, predicates, and assertions
--------------------------------------

.. list-table::
   :header-rows: 1
   :widths: 50 50

   * - Construct
     - Documented in
   * - ``SCAN`` and the ``=>`` separator
     - :doc:`/docs/reference/disambiguation`
   * - up-to-here markers ``=>||`` and ``=>|+n``
     - :doc:`/docs/reference/disambiguation`
   * - contextual predicates ``\ … \`` / ``/ … /`` (with ``~``, ``.``, ``...``)
     - :doc:`/docs/reference/disambiguation`
   * - ``ASSERT``, ``ENSURE``
     - :doc:`/docs/reference/disambiguation`
   * - ``FAIL``
     - :doc:`/docs/reference/disambiguation`

Tree building
-------------

.. list-table::
   :header-rows: 1
   :widths: 50 50

   * - Construct
     - Documented in
   * - node descriptors ``#Name``, ``#void``, ``#abstract``, ``#interface``
     - :doc:`/docs/reference/tree-building`
   * - conditional node descriptor ``#Name( … )``
     - :doc:`/docs/reference/tree-building`
   * - cardinality constraint ``& … &``
     - :doc:`/docs/reference/tree-building`

Code injection and composition
------------------------------

.. list-table::
   :header-rows: 1
   :widths: 50 50

   * - Construct
     - Documented in
   * - ``INJECT``
     - :doc:`/docs/reference/injection`
   * - hooks ``TOKEN_HOOK``, ``OPEN_NODE_HOOK``, ``CLOSE_NODE_HOOK``,
       ``RESET_TOKEN_HOOK``
     - :doc:`/docs/reference/injection`
   * - ``INCLUDE``
     - :doc:`/docs/reference/grammar-file`
   * - preprocessor ``#if`` / ``#elif`` / ``#else`` / ``#endif`` / ``#define`` /
       ``#undef``
     - :doc:`/docs/reference/grammar-file`

Advanced tokenization
---------------------

.. list-table::
   :header-rows: 1
   :widths: 50 50

   * - Construct
     - Documented in
   * - ``ACTIVATE_TOKENS``, ``DEACTIVATE_TOKENS``, ``ACTIVE_TOKENS``
     - :doc:`/docs/reference/tokenization-advanced`
   * - ``UNCACHE_TOKENS``
     - :doc:`/docs/reference/tokenization-advanced`

Fault tolerance
---------------

.. list-table::
   :header-rows: 1
   :widths: 50 50

   * - Construct
     - Documented in
   * - tolerant markers ``!`` and ``!->``
     - :doc:`/docs/reference/fault-tolerance`
   * - ``ATTEMPT`` / ``RECOVER``
     - :doc:`/docs/reference/fault-tolerance`
   * - ``RECOVER_TO``
     - :doc:`/docs/reference/fault-tolerance`
