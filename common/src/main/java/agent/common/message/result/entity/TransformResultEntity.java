package agent.common.message.result.entity;

import agent.base.struct.annotation.PojoProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransformResultEntity {
    public static final int TRANSFORM_ERROR = 0;
    public static final int COMPILE_ERROR = 1;
    public static final int RETRANSFORM_ERROR = 2;
    @PojoProperty(index = 0)
    private Map<Integer, List<ErrorEntity>> typeToErrorList = new HashMap<>();

    public Map<Integer, List<ErrorEntity>> getTypeToErrorList() {
        return typeToErrorList;
    }

    public void setTypeToErrorList(Map<Integer, List<ErrorEntity>> typeToErrorList) {
        this.typeToErrorList = typeToErrorList;
    }

    public boolean hasError() {
        return !typeToErrorList.isEmpty();
    }

    public void addError(int type, ErrorEntity errorEntity) {
        typeToErrorList.computeIfAbsent(
                type,
                key -> new ArrayList<>()
        ).add(errorEntity);
    }

    @Override
    public String toString() {
        return "TransformResultEntity{" +
                "typeToErrorList=" + typeToErrorList +
                '}';
    }
}
