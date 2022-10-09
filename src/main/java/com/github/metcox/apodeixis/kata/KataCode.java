package com.github.metcox.apodeixis.kata;

import com.vladsch.flexmark.ast.Code;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import org.jetbrains.annotations.NotNull;

public class KataCode extends Node {

    private Code code;
    private KataCodeType type;

    public KataCode(Code code, KataCodeType type) {
        this.code = code;
        this.type = type;
    }

    public Code getCode() {
        return code;
    }

    public KataCodeType getType() {
        return type;
    }

    @Override
    public @NotNull BasedSequence[] getSegments() {
        return new BasedSequence[0];
    }
}
