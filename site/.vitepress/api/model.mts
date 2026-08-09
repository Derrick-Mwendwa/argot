import { existsSync, readdirSync, readFileSync } from 'node:fs'
import { dirname, join, resolve } from 'node:path'
import { fileURLToPath } from 'node:url'
import { titleAnchor } from './anchors.mts'
import { DOCS_VERSION, REPO } from '../shared.mts'

/**
 * Loads the API description emitted by tools/dokka-json and turns it into what the reference pages
 * render.
 *
 * The Kotlin side deliberately emits identifiers rather than URLs, so mapping those onto routes is
 * this file's job — including rewriting the `data-ref` anchors left in the documentation HTML.
 */

const SCHEMA = 1

const here = dirname(fileURLToPath(import.meta.url))
const dokkaOutput = resolve(here, '../../../build/dokka/html')

export interface TypeToken {
  text: string
  ref?: string
  external?: string
  /** Added here: the route [ref] resolves to, when it names something we publish. */
  url?: string
}

export interface Doc {
  html?: string
  summary?: string
  returns?: string
  receiver?: string
  since?: string
  deprecated?: string
  throws?: { type: string; ref?: string; url?: string; html: string }[]
  see?: { name: string; ref?: string; url?: string; html?: string }[]
  samples?: string[]
}

export interface Member {
  kind: 'function' | 'constructor' | 'property'
  id: string
  name: string
  modifiers?: string[]
  typeParameters?: { name: string; bounds?: TypeToken[][] }[]
  receiver?: TypeToken[]
  parameters?: { name: string; type: TypeToken[]; doc?: string }[]
  returnType?: TypeToken[]
  type?: TypeToken[]
  mutable?: boolean
  signature: string
  doc?: Doc
  source?: string
  /** Added here: the route this member lives on, including its anchor. */
  url: string
  anchor: string
  sourceUrl?: string
}

export interface Classlike {
  kind: 'class' | 'interface' | 'object' | 'enum' | 'annotation'
  id: string
  name: string
  packageName: string
  modifiers?: string[]
  typeParameters?: { name: string; bounds?: TypeToken[][] }[]
  supertypes?: { type: TypeToken[]; kind: string }[]
  doc?: Doc
  entries?: { id: string; name: string; doc?: Doc }[]
  constructors?: Member[]
  functions?: Member[]
  properties?: Member[]
  source?: string
  url: string
  sourceUrl?: string
}

export interface ApiPackage {
  name: string
  id: string
  doc?: Doc
  classlikes: Classlike[]
  functions: Member[]
  properties: Member[]
  url: string
}

/** Every module's api.json, or an empty list when Dokka has not been run. */
function readModules(): any[] {
  if (!existsSync(dokkaOutput)) return []
  return readdirSync(dokkaOutput, { withFileTypes: true })
    .filter((e) => e.isDirectory())
    .map((e) => join(dokkaOutput, e.name, 'api.json'))
    .filter(existsSync)
    .map((file) => {
      const parsed = JSON.parse(readFileSync(file, 'utf-8'))
      if (parsed.schema !== SCHEMA) {
        throw new Error(
          `${file} declares schema ${parsed.schema}, this site expects ${SCHEMA}. ` +
            `Rebuild the reference with a matching tools/dokka-json.`,
        )
      }
      return parsed
    })
}

export const REFERENCE_ROOT = '/api/'

/** The package's own directory, which holds its index page and one page per classlike. */
function packageDir(name: string): string {
  return `/docs/${DOCS_VERSION}${REFERENCE_ROOT}${name}`
}

function packageUrl(name: string): string {
  return `${packageDir(name)}/`
}

function classlikeUrl(pkg: string, name: string): string {
  return `${packageDir(pkg)}/${name}`
}

/**
 * An id turned into a link target.
 *
 * Members do not get their own page: they are anchors on the page of whatever declares them, which
 * keeps a class readable as one document instead of scattering it across twenty routes.
 */
function urlForId(id: string): string | undefined {
  const [path, member] = id.split('#')
  const slash = path.indexOf('/')
  if (slash === -1) {
    return member ? `${packageUrl(path)}#${anchorFor(member)}` : packageUrl(path)
  }
  const pkg = path.slice(0, slash)
  const cls = path.slice(slash + 1)
  const base = classlikeUrl(pkg, cls)
  return member ? `${base}#${anchorFor(member)}` : base
}

/** Overload suffixes use `~`, which is legal in a fragment but noisy; `-` reads better. */
function anchorFor(member: string): string {
  return member.replace('~', '-').toLowerCase()
}

/**
 * A repo-relative source path turned into a link.
 *
 * Released trees point at their tag so the line you land on is the line that shipped; `dev` and
 * manual backfills point at `main`, which is the only ref they can honestly claim.
 */
function sourceUrl(path: string | undefined): string | undefined {
  if (!path) return undefined
  const ref = /^\d+\.\d+\.\d+$/.test(DOCS_VERSION) ? `v${DOCS_VERSION}` : 'main'
  return `${REPO}/blob/${ref}/${path}`
}

function load(): { packages: ApiPackage[]; missing: boolean } {
  const modules = readModules()
  if (modules.length === 0) return { packages: [], missing: true }

  const byName = new Map<string, any>()
  for (const mod of modules) {
    for (const pkg of mod.packages ?? []) {
      const existing = byName.get(pkg.name)
      if (!existing) {
        byName.set(pkg.name, {
          ...pkg,
          classlikes: [...(pkg.classlikes ?? [])],
          functions: [...(pkg.functions ?? [])],
          properties: [...(pkg.properties ?? [])],
        })
      } else {
        // argot-core and argot-annotations both contribute to org.draftcode.argot.* namespaces.
        existing.classlikes.push(...(pkg.classlikes ?? []))
        existing.functions.push(...(pkg.functions ?? []))
        existing.properties.push(...(pkg.properties ?? []))
        existing.doc ??= pkg.doc
      }
    }
  }

  const packages: ApiPackage[] = [...byName.values()]
    .map((pkg) => {
      const url = packageUrl(pkg.name)
      const classlikes: Classlike[] = pkg.classlikes
        .sort((a: any, b: any) => a.name.localeCompare(b.name))
        .map((c: any) => {
          const clsUrl = classlikeUrl(pkg.name, c.name)
          return {
            ...c,
            url: clsUrl,
            sourceUrl: sourceUrl(c.source),
            constructors: withUrls(c.constructors, clsUrl),
            functions: withUrls(c.functions, clsUrl),
            properties: withUrls(c.properties, clsUrl),
          }
        })
      return {
        ...pkg,
        url,
        classlikes,
        functions: withUrls(pkg.functions, url),
        properties: withUrls(pkg.properties, url),
      }
    })
    .sort((a, b) => a.name.localeCompare(b.name))

  resolveRefs(packages)
  return { packages, missing: false }
}

function withUrls(members: any[] | undefined, pageUrl: string): Member[] {
  return (members ?? []).map((m) => {
    const raw = m.id.split('#')[1] ?? m.name
    // A constructor is named after its class, so its anchor would collide with the page title's.
    // `#constructor` avoids that and reads better than `#argotconversionexception`.
    const overload = raw.includes('~') ? `-${raw.split('~')[1]}` : ''
    const anchor = m.kind === 'constructor' ? `constructor${overload}` : anchorFor(raw)
    return { ...m, anchor, url: `${pageUrl}#${anchor}`, sourceUrl: sourceUrl(m.source) }
  })
}

/**
 * Rewrites the `data-ref` anchors the Kotlin side left in documentation HTML.
 *
 * A ref that resolves to nothing becomes a `<code>` span rather than a dead link — it names
 * something that is not in the published API, which is worth showing but not worth linking.
 */
function resolveRefs(packages: ApiPackage[]): void {
  const known = new Set<string>()
  for (const pkg of packages) {
    known.add(pkg.id)
    for (const c of pkg.classlikes) {
      known.add(c.id)
      for (const m of [...(c.constructors ?? []), ...(c.functions ?? []), ...(c.properties ?? [])]) {
        known.add(m.id)
      }
    }
    for (const m of [...pkg.functions, ...pkg.properties]) known.add(m.id)
  }

  const rewrite = (html: string): string =>
    html.replace(/<a data-ref="([^"]+)">(.*?)<\/a>/g, (_match, id: string, label: string) => {
      const url = known.has(id) ? urlForId(id) : undefined
      return url ? `<a href="${url}">${label}</a>` : `<code>${label}</code>`
    })

  const visitDoc = (doc: Doc | undefined) => {
    if (!doc) return
    if (doc.html) doc.html = rewrite(doc.html)
    if (doc.returns) doc.returns = rewrite(doc.returns)
    if (doc.receiver) doc.receiver = rewrite(doc.receiver)
    if (doc.deprecated) doc.deprecated = rewrite(doc.deprecated)
    doc.throws?.forEach((t) => {
      t.html = rewrite(t.html)
      if (t.ref && known.has(t.ref)) t.url = urlForId(t.ref)
    })
    doc.see?.forEach((s) => {
      if (s.html) s.html = rewrite(s.html)
      if (s.ref && known.has(s.ref)) s.url = urlForId(s.ref)
    })
  }

  const visitTokens = (tokens: TypeToken[] | undefined) => {
    tokens?.forEach((t) => {
      if (t.ref && known.has(t.ref)) t.url = urlForId(t.ref)
    })
  }

  const visitMember = (m: Member) => {
    visitDoc(m.doc)
    m.parameters?.forEach((p) => {
      if (p.doc) p.doc = rewrite(p.doc)
      visitTokens(p.type)
    })
    visitTokens(m.receiver)
    visitTokens(m.returnType)
    visitTokens(m.type)
    m.typeParameters?.forEach((tp) => tp.bounds?.forEach(visitTokens))
  }

  for (const pkg of packages) {
    visitDoc(pkg.doc)
    for (const c of pkg.classlikes) {
      visitDoc(c.doc)
      c.entries?.forEach((e) => visitDoc(e.doc))
      c.supertypes?.forEach((s) => visitTokens(s.type))
      c.typeParameters?.forEach((tp) => tp.bounds?.forEach(visitTokens))
      ;[...(c.constructors ?? []), ...(c.functions ?? []), ...(c.properties ?? [])].forEach(
        visitMember,
      )
    }
    ;[...pkg.functions, ...pkg.properties].forEach(visitMember)
  }
}

const loaded = load()

/**
 * Missing output means Dokka has not run. That is normal while writing prose with `npm run dev` and
 * a broken build anywhere else, so it warns in the first case and fails in the second.
 */
if (loaded.missing) {
  const message =
    `No api.json under ${dokkaOutput}. Run ./gradlew dokkaGenerate to build the reference.`
  if (process.env.ARGOT_API_OPTIONAL === '1') {
    console.warn(`[argot] ${message} The reference will be empty in this dev session.`)
  } else {
    throw new Error(`[argot] ${message}`)
  }
}

export const packages = loaded.packages

export const allClasslikes: Classlike[] = packages.flatMap((p) => p.classlikes)

export function typeText(tokens: TypeToken[] | undefined): string {
  return (tokens ?? []).map((t) => t.text).join('')
}

function stripTags(html: string | undefined): string {
  return (html ?? '').replace(/<[^>]+>/g, ' ').replace(/\s+/g, ' ').trim()
}

function escapeHtml(text: string): string {
  return text.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
}

/**
 * A heading in the exact shape VitePress's search indexer looks for.
 *
 * It splits a page on `<hN ...>title<a href="#anchor">…</a></hN>` and drops anything that does not
 * match, so a heading without the permalink anchor markdown-it would have added is silently not
 * indexed. The ids match the ones the components render, which is what makes a hit land on the
 * right member rather than the top of the page.
 */
function searchHeading(level: 1 | 2, id: string, title: string): string {
  return `<h${level} id="${id}">${escapeHtml(title)}<a class="header-anchor" href="#${id}">&#8203;</a></h${level}>`
}

/**
 * Indexable HTML for a reference page, or null for any other page.
 *
 * VitePress's local search indexes the HTML a Markdown file renders to, and these pages render a
 * component instead — so without this the entire reference is missing from search.
 */
export function searchHtmlFor(relativePath: string): string | null {
  if (!relativePath.startsWith('api/')) return null
  const path = relativePath.slice('api/'.length).replace(/\.md$/, '')

  const asPackage = path.endsWith('/index') ? path.slice(0, -'/index'.length) : null
  if (asPackage) {
    const pkg = packages.find((p) => p.name === asPackage)
    if (!pkg) return null
    return [
      searchHeading(1, titleAnchor(pkg.name), pkg.name),
      `<p>${escapeHtml(stripTags(pkg.doc?.html))}</p>`,
      ...pkg.classlikes.map(
        (c) => `<p>${escapeHtml(`${c.kind} ${c.name} ${stripTags(c.doc?.summary)}`)}</p>`,
      ),
      ...[...pkg.functions, ...pkg.properties].map((m) => memberHtml(m)),
    ].join('\n')
  }

  const slash = path.lastIndexOf('/')
  if (slash === -1) return null
  const pkg = packages.find((p) => p.name === path.slice(0, slash))
  const cls = pkg?.classlikes.find((c) => c.name === path.slice(slash + 1))
  if (!cls) return null

  return [
    searchHeading(1, titleAnchor(cls.name), cls.name),
    `<p>${escapeHtml(`${cls.kind} in ${cls.packageName}. ${stripTags(cls.doc?.html)}`)}</p>`,
    ...(cls.entries ?? []).map(
      (e) => `<p>${escapeHtml(`${e.name} ${stripTags(e.doc?.html)}`)}</p>`,
    ),
    ...[...(cls.constructors ?? []), ...(cls.properties ?? []), ...(cls.functions ?? [])].map((m) =>
      memberHtml(m),
    ),
  ].join('\n')
}

function memberHtml(member: Member): string {
  return [
    searchHeading(2, member.anchor, member.name),
    `<p>${escapeHtml(`${member.signature} ${stripTags(member.doc?.html)}`)}</p>`,
  ].join('\n')
}

export { urlForId }
