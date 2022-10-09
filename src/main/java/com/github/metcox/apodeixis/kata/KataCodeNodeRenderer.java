package com.github.metcox.apodeixis.kata;

import com.vladsch.flexmark.html.HtmlRendererOptions;
import com.vladsch.flexmark.html.HtmlWriter;
import com.vladsch.flexmark.html.renderer.*;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class KataCodeNodeRenderer implements NodeRenderer {

    final private boolean codeContentBlock;
    final private boolean codeSoftLineBreaks;

    public KataCodeNodeRenderer(DataHolder options) {
        codeContentBlock = Parser.FENCED_CODE_CONTENT_BLOCK.get(options);
        codeSoftLineBreaks = Parser.CODE_SOFT_LINE_BREAKS.get(options);
    }

    @Override
    public Set<NodeRenderingHandler<?>> getNodeRenderingHandlers() {
        HashSet<NodeRenderingHandler<?>> set = new HashSet<>();
        set.add(new NodeRenderingHandler<>(KataCode.class, this::render));
        set.add(new NodeRenderingHandler<>(FencedKataCodeBlock.class, this::render));
        return set;
    }

    private void render(KataCode node, NodeRendererContext context, HtmlWriter html) {

        if (node.getType() != null) {
            html.attr("class", node.getType().name());
            if (node.getType().getTitle() != null) {
                html.attr("title", node.getType().getTitle());
            }
        }

        context.render(node.getCode());
    }


    private void render(FencedKataCodeBlock node, NodeRendererContext context, HtmlWriter html) {
        html.line();
        html.srcPosWithTrailingEOL(node.getChars()).withAttr().tag("pre").openPre();

        BasedSequence info = node.getInfo();
        HtmlRendererOptions htmlOptions = context.getHtmlOptions();
        List<String> classes = new ArrayList<>();
        if (info.isNotNull() && !info.isBlank()) {
            String language = node.getInfoDelimitedByAny(htmlOptions.languageDelimiterSet).unescape();
            String languageClass = htmlOptions.languageClassMap.getOrDefault(language, htmlOptions.languageClassPrefix + language);
            classes.add(languageClass);
        } else {
            String noLanguageClass = htmlOptions.noLanguageClass.trim();
            if (!noLanguageClass.isEmpty()) {
                classes.add(noLanguageClass);
            }
        }
        if (node.getType() != null) {
            classes.add(node.getType().name());
            if (node.getType().getTitle() != null) {
                html.attr("title", node.getType().getTitle());
            }
        }
        if (classes.size() > 0) {
            html.attr("class", String.join(" ", classes));
        }

        html.srcPosWithEOL(node.getContentChars()).withAttr(CoreNodeRenderer.CODE_CONTENT).tag("code");
        if (codeContentBlock) {
            context.renderChildren(node);
        } else {
            html.text(node.getContentChars().normalizeEOL());
        }
        html.tag("/code");
        html.tag("/pre").closePre();
        html.lineIf(htmlOptions.htmlBlockCloseTagEol);
    }

    public static class Factory implements NodeRendererFactory {
        @NotNull
        @Override
        public NodeRenderer apply(@NotNull DataHolder options) {
            return new KataCodeNodeRenderer(options);
        }
    }
}
