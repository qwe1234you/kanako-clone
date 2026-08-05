import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js/lib/common'
import DOMPurify from 'dompurify'

const md = new MarkdownIt({
  html: false,
  linkify: true,
  breaks: true,
  highlight(str, lang) {
    const code =
      lang && hljs.getLanguage(lang)
        ? hljs.highlight(str, { language: lang, ignoreIllegals: true }).value
        : md.utils.escapeHtml(str)
    const langAttr = lang ? ` data-lang="${md.utils.escapeHtml(lang)}"` : ''
    return `<pre class="hljs"${langAttr}><code>${code}</code></pre>`
  }
})

export function renderMarkdown(src) {
  const tokens = md.parse(src || '', {})
  const toc = []
  let headingIdx = 0
  for (let i = 0; i < tokens.length; i++) {
    const t = tokens[i]
    if (t.type !== 'heading_open') continue
    t.attrSet('id', `sec-${headingIdx}`)
    const level = Number(t.tag.slice(1))
    if (level >= 2 && level <= 3) {
      toc.push({ level, text: tokens[i + 1]?.content || '', id: `sec-${headingIdx}` })
    }
    headingIdx++
  }
  const html = md.renderer.render(tokens, md.options, {})
  return {
    html: DOMPurify.sanitize(html, { ADD_ATTR: ['target', 'loading'] }),
    toc
  }
}

export function countWords(src) {
  return (src || '').replace(/[#>*`_~\-|=]|\s/g, '').length
}
