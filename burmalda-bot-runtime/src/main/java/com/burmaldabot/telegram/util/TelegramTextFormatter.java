package com.burmaldabot.telegram.util;

import org.commonmark.node.Node;
import org.commonmark.node.Paragraph;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.renderer.html.HtmlNodeRendererContext;
import org.commonmark.renderer.html.HtmlWriter;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;

@Component
public final class TelegramTextFormatter {

    private final Parser parser = Parser.builder().build();

    private final HtmlRenderer renderer = HtmlRenderer.builder()
            .escapeHtml(true)
            .nodeRendererFactory(TelegramParagraphRenderer::new)
            .build();

    public String toTelegramHtml(String markdown) {
        if (markdown == null || markdown.isBlank()) {
            return "";
        }

        return renderer.render(parser.parse(markdown)).strip();
    }

    private static final class TelegramParagraphRenderer
            implements org.commonmark.renderer.NodeRenderer {

        private final HtmlNodeRendererContext context;
        private final HtmlWriter html;

        private TelegramParagraphRenderer(HtmlNodeRendererContext context) {
            this.context = context;
            this.html = context.getWriter();
        }

        @Override
        public Set<Class<? extends Node>> getNodeTypes() {
            return Collections.singleton(Paragraph.class);
        }

        @Override
        public void render(Node node) {
            if (node.getPrevious() != null) {
                html.line();
                html.line();
            }

            Node child = node.getFirstChild();
            while (child != null) {
                context.render(child);
                child = child.getNext();
            }
        }
    }
}