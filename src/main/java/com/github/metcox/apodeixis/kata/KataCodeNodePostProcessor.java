package com.github.metcox.apodeixis.kata;

import com.vladsch.flexmark.ast.*;
import com.vladsch.flexmark.parser.block.NodePostProcessor;
import com.vladsch.flexmark.parser.block.NodePostProcessorFactory;
import com.vladsch.flexmark.util.ast.Document;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.ast.NodeTracker;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import org.jetbrains.annotations.NotNull;

public class KataCodeNodePostProcessor extends NodePostProcessor {

    final private KataCodeOptions options;

    public KataCodeNodePostProcessor(DataHolder options) {
        this.options = new KataCodeOptions(options);
    }

    @Override
    public void process(@NotNull NodeTracker state, @NotNull Node node) {

        if (node instanceof Code) {
            Node next = node.getNext();

            if (next instanceof Text) {
                BasedSequence nextChars = next.getChars();
                if (nextChars.startsWith("{{execute}}") && node.getChars().isContinuedBy(nextChars)) {
                    // trim next nextChars to remove '{{execute}}'
                    next.setChars(nextChars.subSequence("{{execute}}".length()));

                    KataCode kataCode = new KataCode((Code) node, KataCodeType.execute);
                    node.unlink();
                    next.insertBefore(kataCode);
                    state.nodeRemoved(node);
                    state.nodeAddedWithChildren(kataCode);
                }
            }
        }

    }

    public static class Factory extends NodePostProcessorFactory {
        public Factory() {
            super(false);

            addNodes(Code.class, FencedCodeBlock.class);
        }

        @NotNull
        @Override
        public NodePostProcessor apply(@NotNull Document document) {
            return new KataCodeNodePostProcessor(document);
        }
    }
}
