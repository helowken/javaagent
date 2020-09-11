package agent.common.struct.impl;

import agent.common.struct.BBuff;
import agent.common.struct.Struct;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CompoundStruct implements Struct {
    private List<Struct> structList = new ArrayList<>();

    public CompoundStruct(Struct... structs) {
        if (structs == null || structs.length == 0)
            throw new IllegalArgumentException("No struct found.");
        structList.addAll(Arrays.asList(structs));
    }

    @Override
    public void deserialize(BBuff bb) {
        structList.forEach(struct -> struct.deserialize(bb));
    }

    @Override
    public void serialize(BBuff bb) {
        structList.forEach(struct -> struct.serialize(bb));
    }

    @Override
    public int bytesSize() {
        int size = 0;
        for (Struct struct : structList) {
            size += struct.bytesSize();
        }
        return size;
    }
}
