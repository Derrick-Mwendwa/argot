# Security Policy

## Supported versions

Argot is pre-1.0. Fixes land on the latest released minor line; older lines are patched only if
someone is genuinely stuck on one.

| Version | Supported |
|---|---|
| 0.1.x | yes |

## Reporting a vulnerability

Please report privately rather than opening a public issue: use
[**Report a vulnerability**](https://github.com/Derrick-Mwendwa/argot/security/advisories/new) on the
Security tab, which opens a private advisory only you and the maintainer can see.

Expect an acknowledgement within a week. If a fix is needed, it ships as a patch release and the
advisory is published once it is available.

## Scope

Argot parses `argv` and has no third-party runtime dependencies, no network access, no file access,
and no runtime reflection, so its attack surface is small. Reports that are in scope include:

- input that crashes the parser in a way a CLI author cannot catch;
- a generated parser that produces values not matching the declared specification;
- anything in `argot-processor` that emits code doing more than parsing arguments.

Argot does not sanitise the *values* it hands back. Validating that a parsed path, URL, or command is
safe to use remains the calling application's responsibility.
