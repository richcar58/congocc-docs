# Configuration file for the Sphinx documentation builder.
#
# Full list of built-in configuration values:
# https://www.sphinx-doc.org/en/master/usage/configuration.html

import datetime
import sys
from pathlib import Path

# Make the local extensions under source/_ext importable.
sys.path.insert(0, str(Path(__file__).resolve().parent / '_ext'))

# -- Project information -----------------------------------------------------

project = 'CongoCC'
copyright = f'{datetime.date.today().year}, Jonathan Revusky, Richard Cardone'
author = 'Jonathan Revusky, Richard Cardone'

# CongoCC uses rolling releases rather than numbered versions, so we do not
# advertise a global version/release string. Currency is marked per-page with
# date-keyed versionadded/versionchanged directives (see the relabeling below).
release = ''
version = ''

# -- General configuration ---------------------------------------------------

extensions = [
    'sphinx.ext.todo',             # track drafting gaps with `.. todo::`
    'sphinx.ext.autosectionlabel',  # let `:ref:` target section titles (construct index)
    'ccc_lexer',                    # Pygments lexer for `.. code-block:: ccc`
]

templates_path = ['_templates']
exclude_patterns = []

# Render TODOs while the docs are under construction. Flip to False for a
# published build (or override with `-D todo_include_todos=0`).
todo_include_todos = True

# Prefix autosection labels with the document path so identically named
# sections across the three guides (e.g. several "Overview" pages) don't
# collide and emit duplicate-label warnings.
autosectionlabel_prefix_document = True

# -- Options for HTML output -------------------------------------------------

# Default theme for now; the Read The Docs theme decision is deferred
# (see plan.md). Hosting on RTD does not require any particular theme.
html_theme = 'alabaster'
html_static_path = ['_static']


# -- Date-keyed "version" directives -----------------------------------------
# Because CongoCC has no numbered releases, versionadded/versionchanged/
# deprecated directives are keyed to ISO dates. Re-label the rendered text so a
# `.. versionadded:: 2026-06-19` reads "Added 2026-06-19" rather than the
# default "Added in version 2026-06-19".
#
# Sphinx builds the label as `versionlabels[name] % date` in
# sphinx/domains/changeset.py, reading `versionlabels` as a module global, so
# we mutate that dict in place (not rebind it). Wrapped defensively: if a
# future Sphinx moves this internal API, we silently keep the stock labels
# rather than break the build.
def _relabel_version_directives():
    try:
        from sphinx.domains import changeset
        changeset.versionlabels['versionadded'] = 'Added %s'
        changeset.versionlabels['versionchanged'] = 'Changed %s'
        changeset.versionlabels['deprecated'] = 'Deprecated %s'
        changeset.versionlabels['versionremoved'] = 'Removed %s'
    except Exception:
        pass


_relabel_version_directives()


def setup(app):
    # Reserved hook for future project-specific directives/roles
    # (e.g. a Pygments lexer for `.ccc` grammar snippets).
    return {'parallel_read_safe': True, 'parallel_write_safe': True}
