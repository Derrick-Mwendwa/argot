/**
 * Syntax themes built from the site's own tokens.
 *
 * VitePress ships GitHub's themes, whose palette has nothing to do with ours; a code block is the
 * largest coloured object on most pages, so borrowing someone else's palette for it undoes the rest
 * of the design. These carry only the scopes Kotlin, Gradle and console blocks actually use —
 * anything unmatched falls back to the foreground, which is the correct neutral.
 */

interface Scope {
  scope: string[]
  settings: { foreground?: string; fontStyle?: string }
}

function theme(name: string, type: 'light' | 'dark', c: Record<string, string>) {
  const settings: Scope[] = [
    {
      scope: ['comment', 'punctuation.definition.comment', 'string.comment'],
      settings: { foreground: c.comment },
    },
    {
      scope: [
        'string',
        'string.quoted',
        'string.template',
        'constant.character',
        'punctuation.definition.string',
      ],
      settings: { foreground: c.string },
    },
    { scope: ['constant.numeric', 'constant.language'], settings: { foreground: c.number } },
    {
      scope: [
        'keyword',
        'keyword.control',
        'keyword.operator.new',
        'storage',
        'storage.type',
        'storage.modifier',
        'variable.language',
      ],
      settings: { foreground: c.keyword },
    },
    {
      scope: [
        'entity.name.type',
        'entity.name.class',
        'support.type',
        'support.class',
        'meta.annotation',
        'storage.type.annotation',
        'punctuation.definition.annotation',
      ],
      settings: { foreground: c.type },
    },
    {
      scope: ['entity.name.function', 'support.function', 'meta.function-call'],
      settings: { foreground: c.func },
    },
    { scope: ['punctuation', 'meta.brace'], settings: { foreground: c.punctuation } },
    { scope: ['variable', 'variable.other', 'meta.definition.variable'], settings: { foreground: c.fg } },
  ]

  return {
    name,
    type,
    colors: { 'editor.background': c.bg, 'editor.foreground': c.fg },
    settings,
  }
}

/** Light: the card surface, matching the mock's source panel. */
export const argotLight = theme('argot-light', 'light', {
  bg: '#ffffff',
  fg: '#3d3b45',
  comment: '#9a97a3',
  string: '#b4611b',
  number: '#b4611b',
  keyword: '#5b4bea',
  type: '#0f8a6a',
  func: '#0f8a6a',
  punctuation: '#6e6b78',
})

/** Dark: the same ink the terminal panel uses, so the two never read as different surfaces. */
export const argotDark = theme('argot-dark', 'dark', {
  bg: '#0e0e11',
  fg: '#d6d4de',
  comment: '#7c7a86',
  string: '#dc9a5e',
  number: '#dc9a5e',
  keyword: '#a79bff',
  type: '#4cc4a0',
  func: '#4cc4a0',
  punctuation: '#7c7a86',
})
