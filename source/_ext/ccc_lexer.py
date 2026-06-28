"""A lightweight Pygments lexer for CongoCC (``.ccc``) grammar files.

It is registered with Sphinx (this module is a Sphinx extension; see
``conf.py``) so documentation code blocks can use ``.. code-block:: ccc`` to get
syntax-highlighted grammar snippets.

The lexer is deliberately minimal: it highlights the salient parts of a grammar
— keywords, settings and token-type names, ``#`` node annotations, the lookahead
operators, strings, and comments — rather than fully parsing the language.
Embedded target-language code in ``{ … }`` actions is highlighted with the same
coarse rules, which is adequate for documentation snippets.
"""

from pygments.lexer import RegexLexer, words
from pygments.token import (
    Comment, Keyword, Name, Number, Operator, Punctuation, String, Text,
    Whitespace,
)


class CongoCCLexer(RegexLexer):
    """Pygments lexer for the CongoCC grammar language."""

    name = "CongoCC"
    aliases = ["ccc", "congocc"]
    filenames = ["*.ccc"]

    # Grammar-level keywords: production kinds, lexical directives, lookahead,
    # assertions, recovery, and code composition.
    _keywords = (
        "TOKEN", "REGULAR_TOKEN", "SKIP", "MORE", "UNPARSED", "SPECIAL_TOKEN",
        "CONTEXTUAL", "SCAN", "ASSERT", "ENSURE", "FAIL", "ATTEMPT", "RECOVER",
        "RECOVER_TO", "ON_ERROR", "INJECT", "INCLUDE", "IGNORE_CASE",
        "LEXICAL_STATE", "ACTIVATE_TOKENS", "DEACTIVATE_TOKENS", "ACTIVE_TOKENS",
        "UNCACHE_TOKENS", "EOF",
    )
    # Words that commonly appear in embedded code and INJECT headers.
    _code_keywords = (
        "import", "extends", "implements", "public", "private", "protected",
        "throws", "return",
    )

    tokens = {
        "root": [
            (r"\s+", Whitespace),
            (r"//[^\n]*", Comment.Single),
            (r"/\*", Comment.Multiline, "comment"),
            (r'"(\\.|[^"\\\n])*"', String.Double),
            (r"'(\\.|[^'\\\n])*'", String.Single),
            # Lookahead operators: up-to-here (``=>||``, ``=>|+1``) and ``=>``.
            (r"=>\|[|+]?\d*\|?", Operator),
            (r"=>", Operator),
            # Tree-building annotations: ``#void``/``#abstract``/``#interface``
            # and the ``#NodeName`` form.
            (r"#(void|abstract|interface)\b", Keyword.Pseudo),
            (r"#[A-Za-z_]\w*", Name.Decorator),
            (words(_keywords, suffix=r"\b"), Keyword),
            (words(_code_keywords, suffix=r"\b"), Keyword.Reserved),
            # Settings and token-type names are UPPER_SNAKE_CASE.
            (r"[A-Z][A-Z0-9_]+\b", Name.Constant),
            (r"\d+", Number),
            (r"[A-Za-z_]\w*", Name),
            # Contextual-predicate path characters and general operators.
            (r"[~\\/.]", Operator),
            (r"[|*+?:;=&!-]", Operator),
            (r"[(){}\[\]<>,]", Punctuation),
            # Catch-all so the lexer is total and never errors on stray
            # characters (e.g. a `…` placeholder in a snippet).
            (r".", Text),
        ],
        "comment": [
            (r"[^*]+", Comment.Multiline),
            (r"\*/", Comment.Multiline, "#pop"),
            (r"\*", Comment.Multiline),
        ],
    }


def setup(app):
    """Register the lexer so ``.. code-block:: ccc`` works."""
    app.add_lexer("ccc", CongoCCLexer)
    return {"parallel_read_safe": True, "parallel_write_safe": True}
