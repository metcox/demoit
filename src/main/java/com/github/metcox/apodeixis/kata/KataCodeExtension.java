package com.github.metcox.apodeixis.kata;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataHolder;
import org.jetbrains.annotations.NotNull;

public class KataCodeExtension implements Parser.ParserExtension, HtmlRenderer.HtmlRendererExtension {

    private KataCodeExtension() {
    }

    public static KataCodeExtension create() {
        return new KataCodeExtension();
    }

    @Override
    public void rendererOptions(@NotNull MutableDataHolder options) {

    }

    @Override
    public void extend(HtmlRenderer.@NotNull Builder htmlRendererBuilder, @NotNull String rendererType) {
        if (htmlRendererBuilder.isRendererType("HTML")) {
            htmlRendererBuilder.nodeRendererFactory(new KataCodeNodeRenderer.Factory());
        } else if (htmlRendererBuilder.isRendererType("JIRA")) {
        }

    }

    @Override
    public void parserOptions(MutableDataHolder options) {

    }

    @Override
    public void extend(Parser.Builder parserBuilder) {
        parserBuilder.customBlockParserFactory(new FencedKataCodeBlockParser.Factory());
        parserBuilder.postProcessorFactory(new KataCodeNodePostProcessor.Factory());

    }
}
