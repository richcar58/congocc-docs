# settings-extractor

A small maintenance tool that keeps the **Settings Reference**
(`source/docs/reference/settings.rst`) in sync with the CongoCC source.

CongoCC's recognized settings are declared in `AppSettings.java` as three
comma-delimited constants — `booleanSettings`, `stringSettings`, and
`integerSettings`. The Settings Reference chapter is written by hand, so it can
drift when the source adds, removes, or retypes a setting. This tool reads those
constants as the single source of truth and reports any current setting the
documentation is missing.

## Requirements

A JDK 17 or later. There is no build step — the program is run directly from
source with the single-file launcher.

## Usage

```sh
# List the current settings, grouped by type:
java SettingsExtractor.java /path/to/AppSettings.java

# Emit a reStructuredText list-table fragment:
java SettingsExtractor.java /path/to/AppSettings.java --rst

# Diff against the documentation (non-zero exit if anything is undocumented):
java SettingsExtractor.java /path/to/AppSettings.java \
     --diff /path/to/congocc-docs/source/docs/reference/settings.rst

# Run the built-in self-tests:
java SettingsExtractor.java --self-test
```

`AppSettings.java` lives at `src/java/org/congocc/app/AppSettings.java` in a
CongoCC checkout.

### Exit status

| Status | Meaning |
|--------|---------|
| `0`    | success — and, for `--diff`, every current setting is documented |
| `1`    | extraction failed, an input file was unusable, or `--diff` found undocumented settings |
| `2`    | the command line was malformed |

The non-zero `--diff` status makes the tool usable as a CI check: fail the build
if a current setting is not documented.

## What it checks (and what it does not)

The tool extracts each setting's **name** and **type**, which is what changes
when a setting is added or removed — the drift most likely to make the docs
wrong. It does **not** infer default values: those are encoded in per-setting
getter logic in `AppSettings.java` that is not safe to parse mechanically, so
defaults remain hand-written and human-reviewed.

The `--diff` report has two parts:

- **Undocumented current settings** — current settings absent from the docs.
  These are actionable: add them to the Settings Reference. (This is what the
  exit status reflects.)
- **Documented but not a current setting** — setting-shaped tokens in the docs
  that are not in the registry. This is informational; entries are usually
  deprecated settings (e.g. `JDK_TARGET`), or unrelated upper-case tokens, and
  warrant a glance rather than action.

When scanning the docs, the tool treats an `UPPER_SNAKE_CASE` token in inline
backticks as a setting only if it contains an underscore. Every current setting
does, and this avoids matching unrelated tokens such as `NAME` or `EOF`.

## Maintenance workflow

When the CongoCC source changes, run the `--diff` form against the current
`AppSettings.java`. If it reports undocumented settings, add them to
`settings.rst`; if it lists a documented setting that is no longer current,
confirm whether it became deprecated and update the prose accordingly.
