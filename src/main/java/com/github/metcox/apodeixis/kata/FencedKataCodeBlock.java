package com.github.metcox.apodeixis.kata;

import com.vladsch.flexmark.ast.FencedCodeBlock;

public class FencedKataCodeBlock extends FencedCodeBlock {

    private KataCodeType type;

    public void setType(KataCodeType type) {
        this.type = type;
    }

    public KataCodeType getType() {
        return type;
    }
}
