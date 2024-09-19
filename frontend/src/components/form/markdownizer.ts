import TurndownService from "turndown"
import markdownToQuillHtml from "./markdownToQuillHtml"
import quillHtmlToMarkdown from "./quillHtmlToMarkdown"

export const turndownService = new TurndownService({
  br: "<br>",
})

turndownService.addRule("p", {
  filter: "p",
  replacement(_, node: Node) {
    const replacement = (node as HTMLElement).innerHTML
    if (replacement === "<br>") {
      return (node as HTMLElement).outerHTML
    }
    return replacement ? `\n\n${turndownService.turndown(replacement)}\n\n` : ""
  },
})

export default {
  markdownToHtml: markdownToQuillHtml,
  htmlToMarkdown: quillHtmlToMarkdown,
}
