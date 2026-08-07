import { createContentLoader } from 'vitepress'

/**
 * Builds the news index from the files in news/posts, so adding a post is adding a file. Nothing
 * has to be registered by hand and the index cannot fall out of step with what exists.
 */
export default createContentLoader('news/posts/*.md', {
  excerpt: true,
  transform(raw) {
    return raw
      .map(({ url, frontmatter, excerpt }) => ({
        title: frontmatter.title as string,
        date: frontmatter.date as string,
        tag: (frontmatter.tag as string) ?? null,
        excerpt: excerpt ?? '',
        url,
      }))
      .sort((a, b) => +new Date(b.date) - +new Date(a.date))
  },
})
