package agent.server.transform.impl;

import agent.server.transform.TransformContext;
import agent.server.transform.revision.ClassDataStore;

public class ResetTransformer extends AbstractTransformer {
    private static final String REG_KEY = "@reset";

//    @Override
//    protected void doTransform(Class<?> clazz) throws Exception {
//        int revisionNum = ClassDataStore.REVISION_0;
//        byte[] data = ClassDataStore.load(clazz, revisionNum);
//        if (data == null)
//            throw new RuntimeException("No data of class " + clazz.getName() + " found at revision: " + revisionNum);
//        getClassPool().saveClassData(clazz, data);
//    }

    @Override
    public void transform(TransformContext transformContext) throws Exception {

    }

    @Override
    public String getRegKey() {
        return REG_KEY;
    }
}
