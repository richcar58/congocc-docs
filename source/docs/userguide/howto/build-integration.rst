How To: Integrate the Build
===========================

Generating the parser should be a step in your build, not a manual chore, so
the generated code is always in sync with the grammar. This guide covers the
general approach; per-language packaging is in the
:doc:`Target Language Guide </docs/targets/targets>`.

Make generation a build step
----------------------------

CongoCC is a JAR invoked with a grammar file (:doc:`/docs/reference/invocation`),
so any build tool that can run a Java program can drive it. Generate into a
dedicated output directory with ``-d`` and compile from there.

**Ant** — the bundled examples use Ant; the essential task is:

.. code-block:: xml

   <java jar="congocc.jar" fork="true" failonerror="true">
     <arg line="-n -d ${generated.dir} ${basedir}/MyGrammar.ccc"/>
   </java>

**Maven** — run the JAR from the ``exec-maven-plugin`` (or ``antrun``) in the
``generate-sources`` phase, with ``${project.build.directory}/generated-sources``
as the ``-d`` target so the compiler picks it up automatically.

**Gradle** — a ``JavaExec`` task that runs the JAR, wired as a dependency of
``compileJava``.

Keep regeneration deterministic
-------------------------------

A grammar maps to the same generated code every time, which is what makes
regeneration safe to automate. To keep it that way:

- Pass ``-n`` in automated builds so CongoCC does not try to check for or
  download a newer version.
- Pin the ``congocc.jar`` version your build uses, just as you pin other build
  tools, so output does not change underneath you when a new release appears.
- Generate into a clean directory (or clean before generating) so stale files
  from a previous grammar cannot linger.

CI
--

In continuous integration, treat generation as an ordinary build step and let
the normal compile and test stages run against the freshly generated sources.
Because the output is deterministic, a CI build that regenerates and a
developer build that regenerated earlier produce identical code.
