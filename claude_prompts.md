# CongoCC Documentation Project

The goal of this project is to generate the complete, accurate and well-organized documentation for the [CongoCC Paser-Generator](https://github.com/congo-cc/congo-parser-generator).  Besides the documentation in the source code, the majority of [documentation](https://parsers.org/) consists of years of articles and blog posts that cover aspects of what CongoCC does and how to use it.  Unfortunately, to get a full understanding of CongoCC's syntax and semantics, one must also refer to it's two predecessor projects.

## The Historical Problem

CongoCC is based on the original code and documentation from [JavaCC](https://javacc.github.io/javacc/).  JavaCC was developed about 25 years ago and has fairly complete and well-organized documentation.  After a period of stagnation, a new project, [JavaCC21](https://github.com/javacc21/javacc21) revived development of the JavaCC parser-generator, but did not provide much updated documentation.  

CongoCC descended from JavaCC21 as a new project, but the original JavaCC documentation was not carried over and updated.  Instead, nearly 6 years of articles and blog posts have introduced new CongoCC features, deprecated old features and explained how legacy behaviors have changed.  New CongoCC users have to refer to JavaCC documentation to get a basic understanding of how the parser-generator works and how to use it.  They then would read about the changes that JavaCC21 introduced.  Finally, they would have to review 6 years of CongoCC articles to understand the current CongoCC implementation, features and best practices.

## Consolidating CongoCC Documentation

### Phase I - Surveying Existing Documentation

To create CongoCC documentation that is comprehensive, up-to-date, easy to understand and navigate, we need define an approach that would survey and incorporate the following:

1. The JavaCC and JavaCC21 documentation including its features, examples, tutorials, FAQs and [detailed documentation](https://javacc.github.io/javacc/documentation/) for JavaCC, JJTree, and JJDoc.
2. Separately published and accessible articles and books covering JavaCC.
3. The articles and posts on [CongoCC's documentation site](https://parsers.org/).

### Phase II - Determining Currently Valid Information 

To generate the current up-to-date information for CongoCC, we need to:

1. Remove obsolete, out of date or deprecated information.
2. Integrate the latest capabilities into legacy feature documentation.
3. Determine which new features need to be documented. 

### Phase III - Organizing the Documentation for Exposition 

Once we understand the information required to be in the new CongoCC documentation, we have to decide on its organization and formatting.  

1. The primary presentation will use [Sphinx](https://www.sphinx-doc.org/en/master/) served from the [Read The Docs](https://about.readthedocs.com/) site.  Sphinx will also be for local development in this repository (congocc-docs).  Therefore, all content will conform to the [reStructuredText](https://www.sphinx-doc.org/en/master/usage/restructuredtext/index.html) format and reside in .rst files.
2. We need specify the file tree structure for the documentation.
3. We need to specify the main headings for the content in each file.

## Proposal

Please analyze the JavaCC, JavaCC21 and CongoCC documentation and related material and specify what the up-to-date CongoCC documentation should include. Propose for review different ways to organize that informatin in the following documents:

1. Reference Manual - to include complete, detailed explanation of syntax and semantics of CongoCC grammar, command line options, environment variables, target language support, etc.
2. User Guide - to include development/testing strategies, syntactic idioms, best practices, in-depth explanations of features, support for the different target lanaguages, error messages, references to examples, etc.   


# Responses to Claude Questions

Below are answers to the 7 questions.

1. The same grammar definition can essentially be used to generate parsers in any of the 4 target languages, the main difference being the language of any injected code.  For example, Rust parsers include generated FIXME.md and inject.rs files to document where handwritten Rust code needs to be inserted.  Let's go with a third top-level "Target Language Guide" to centralize target language usage in one place.

2. Let's go with Sphinx versionadded/versionchanged directives keyed to dates.

3. Yes, let's handle migration with one appendix (mapping tables + converter workflow) in the Reference Manual plus one User Guide migration chapter.

4. Please document fault-tolerance as a normal chapter flagged experimental.

5. Hand-write to start, but let's generate an extraction tool written in Java for future use.

6. Jonathan Revusky will review the documentation before publication.  This repo's output will mostly likely be published on [Read The Docs](https://about.readthedocs.com/).

7. Rust support is no longer provisional.  We even have a grammar for the Rust language itself (examples/rust/Rust.cc).

When developing the plan for review, note that source code changes to CongoCC have been committed in the past 2 weeks. 

# Examples Matrix

Please create a targets/examples-matrix.rst documentation file that describes each of the examples in CongoCC's examples subtree.  For each example, list the grammar file(s), a short description of the language the grammars define, and the target languages for which parsers are generated.  Integrate the new documentation file into the Target Language Guide just before the build-and-runtime.rst file.  Before generating the documentation, please present a plan for review. 