Templates and Code Generation
=============================

This chapter sketches how CongoCC turns a grammar into source code. It is
mainly of interest if you are curious about the tool's internals or thinking of
contributing to it; you do not need any of it to write grammars.

Template-driven generation
--------------------------

CongoCC parses your grammar into its own internal tree and then renders the
output files from **templates** — one set per target language. The template
files carry a ``.ctl`` extension and are written in a template language derived
from FreeMarker (extended over the years for this project's needs). There is a
template for each kind of generated file: the parser, the lexer, the base node
and token classes, and so on.

Why templates
-------------

Keeping code generation in templates rather than hard-coded string-building is
what makes supporting **four target languages** from one core practical. The
parsing logic, the automaton, and the tree model are computed once in the
language-neutral core; the per-language templates are responsible only for
emitting idiomatic Java, Python, C#, or Rust from that shared analysis. Adding
or adjusting how a language is generated is largely a matter of editing its
templates.

What this means for you
-----------------------

For a grammar author, the template machinery is invisible — you see only the
generated source. It matters in two indirect ways: it is why the same grammar
can target several languages with consistent behavior, and it is the layer a
contributor works in to change or extend the generated output.
