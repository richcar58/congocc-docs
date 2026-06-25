How To: Test Grammars and Parsers
=================================

A grammar is software and benefits from tests: a corpus that must keep parsing,
and checks that bad input is rejected. This guide covers the built-in test
harness and writing your own tests.

The built-in corpus harness
----------------------------

Set ``TEST_PRODUCTION`` to a start production and ``TEST_EXTENSION`` to a file
extension, and CongoCC generates a ready-to-run harness that parses every
matching file under the paths you give it:

.. code-block:: text

   TEST_PRODUCTION = Root;
   TEST_EXTENSION  = json;

For the bundled JSON grammar this generates a ``ParseFiles`` class. Point it at
a file or a directory tree (it even descends into ``.zip`` and ``.jar``
archives) and it reports what parsed and what did not:

.. code-block:: text

   The Java impl parsed sample.json.

   Parsed 1 files successfully
   Failed on 0 files

   Duration: 13 milliseconds

This is the quickest way to run a grammar against a large corpus and catch
regressions: keep a directory of known-good inputs and fail the build if any
stops parsing.

Writing your own tests
----------------------

For finer-grained tests, drive the parser directly from a unit test. A positive
test parses an input and asserts something about the resulting tree using the
node API (:doc:`/docs/reference/generated-api`):

.. code-block:: java

   var parser = new CalcParser("2 + 3 * 4");
   assertEquals(14.0, parser.Calc());

A negative test asserts that malformed input is rejected — by default the
parser throws an (unchecked) ``ParseException``:

.. code-block:: java

   assertThrows(ParseException.class, () -> new CalcParser("2 +").Calc());

What to test
------------

- **A corpus** of real-world inputs that must keep parsing — the harness above.
- **Boundary cases**: empty input, the largest constructs you support, deep
  nesting.
- **Negative cases**: inputs that must fail, so a grammar change does not
  silently start accepting nonsense.
- **Tree shape**, where it matters to consumers, so a refactor of the grammar
  does not quietly change the tree your application depends on.
