Python
======

CongoCC generates a self-contained Python package from the same grammar used for
any other target, provided the grammar's embedded code (if any) is Python or is
guarded for Python (:doc:`injected-code`).

Output layout
-------------

The output is a Python package whose name is derived from the last component of
``PARSER_PACKAGE`` with ``parser`` appended — ``demo.lang`` becomes
``langparser/``. It contains the expected modules:

.. code-block:: text

   langparser/
   ├── __init__.py     re-exports TokenType, Token, Lexer, Parser, ParseException
   ├── lexer.py
   ├── parser.py
   ├── tokens.py
   └── utils.py

The ``__init__.py`` re-exports the public names, so ``from langparser import
Parser`` is all a caller needs. (The package name can be overridden with the
``py.package`` preprocessor symbol via ``-p``.)

Using the parser
----------------

The API mirrors the language-neutral contract
(:doc:`/docs/reference/generated-api`) with Python naming — production methods
are ``parse_``-prefixed and snake_case, and the root accessor is
``root_node()``:

.. code-block:: python

   from langparser import Parser

   parser = Parser(input)
   parser.parse_NumberList()
   root = parser.root_node()

``TokenType`` is a Python ``enum``, and ``ParseException`` is an ordinary
``Exception`` subclass.

Runtime
-------

Generated parsers run on CPython using only the standard library — there is no
package to ``pip install``. Deploying a parser means shipping the generated
package alongside your code.
