/**
 * Anchor helpers shared by the build-time model and the components that render in the browser.
 *
 * Separate from model.mts because that reads the filesystem: importing it from a component pulls
 * `node:fs` into the client bundle and the build fails.
 */

/** The id the reference components put on a page's title heading. */
export function titleAnchor(name: string): string {
  return name
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/(^-|-$)/g, '')
}
