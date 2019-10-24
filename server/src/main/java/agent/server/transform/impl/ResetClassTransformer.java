package agent.server.transform.impl;

import agent.server.transform.revision.ClassDataStore;

public class ResetClassTransformer extends AbstractTransformer {

    @Override
    protected void doTransform(Class<?> clazz) throws Exception {
        int revisionNum = ClassDataStore.REVISION_0;
        byte[] data = ClassDataStore.load(clazz, revisionNum);
        if (data == null)
            throw new RuntimeException("No data of class " + clazz.getName() + " found at revision: " + revisionNum);
        getClassPool().saveClassData(clazz, data);
    }
}
