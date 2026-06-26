Installation
============

CongoCC is distributed as a single executable JAR file with no other runtime
dependencies. Installing it amounts to downloading that JAR — or building it
from source — and making sure you have a suitable Java runtime.

Requirements
------------

- **Java 17 or later** to *run* CongoCC. The tool ships as Java bytecode no
  matter which language you generate parsers in.
- A toolchain for your **target language**, needed only to compile and run the
  *generated* parser: a JDK for Java (the default), CPython for Python, the
  .NET SDK for C#, or a Rust toolchain for Rust. See the
  :doc:`Target Language Guide </docs/targets/targets>` for the per-language
  details.

.. note::

   You do **not** need the target-language toolchain in order to *generate* a
   parser — only to compile and run it. CongoCC itself only requires Java 17+.

Getting the JAR
---------------

Download the latest build:

.. code-block:: console

   $ curl -L -O https://parsers.org/download/congocc.jar

Verify it works by invoking it with no arguments. CongoCC prints a banner
followed by its usage message:

.. code-block:: console

   $ java -jar congocc.jar

.. code-block:: text

   Usage:
       java -jar congocc.jar grammarfile

   The following command-line flags are available:
    -d <directory>    Specify the directory (absolute or relative to the
                      grammarfile location) to place generated files
    -lang <language>  Specify the language to generate code in (the default is 'java')
                        (valid choices are currently 'java', 'python', 'csharp' and 'rust')
    -n                Suppress the check for a newer version
    -p                Define one or more comma-separated (no spaces) symbols to
                      pass to the preprocessor.
    -q                Quieter output

The first banner line reports the build date of your JAR, which is handy for
checking whether you are running the most recent version:

.. code-block:: text

   CongoCC Parser Generator (congocc.jar built ... on 2026-06-23)

.. tip::

   By default CongoCC checks online for a newer release each time it runs.
   Pass ``-n`` to suppress that check — useful when working offline or in a
   continuous-integration job.

The full command-line reference, including the syntax converter for legacy
JavaCC grammars, is in :doc:`/docs/reference/invocation`.

Building from source
--------------------

If you want the bleeding edge, or intend to work on CongoCC itself, build the
JAR from source. You will need **Git**, a **JDK 17+**, and **Apache Ant**:

.. code-block:: console

   $ git clone https://github.com/congo-cc/congo-parser-generator.git
   $ cd congo-parser-generator
   $ ant

The default Ant target builds the project and produces ``congocc.jar`` in the
repository root.  To build and run the test suite instead, use:

.. code-block:: console

   $ ant test

See the top-level ``build.xml`` file for all possible build targets.

Optionally Building Rust Examples
---------------------------------

Rust examples and test code are not built by default. If the Rust toolchain is available, 
you can build all artifacts from scratch, including Rust artifacts, by using:

.. code-block:: console

   $ ant clean -Drust.enabled=true
   $ ant test -Drust.enabled=true

See :doc:`Rust Target Language Guide </docs/targets/rust>` for more information about Rust support.

Editor support
--------------

A CongoCC grammar is an ordinary UTF-8 text file — conventionally given a
``.ccc`` extension — so any text editor will do. The source repository ships a
``workspace.code-workspace`` file for Visual Studio Code, and the complete
grammars under its ``examples/`` directory (JSON, Lua, Python, Java, C#, and
more) are worth opening alongside your own work as references.

Next steps
----------

With the JAR in hand, continue to
:doc:`/docs/userguide/tutorial/first-grammar` to write, generate, and run your
first parser.
