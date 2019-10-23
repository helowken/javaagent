package agent.server.transform.impl;

import agent.hook.utils.App;
import javassist.CtClass;
import javassist.CtConstructor;

public class HookAppTransformer extends AbstractTransformer {

    @Override
    public void doTransform(Class<?> clazz) throws Exception {
        CtClass ctClass = getClassPool().get(clazz.getName());
        CtConstructor constructor = ctClass.getDeclaredConstructor(new CtClass[0]);
        constructor.insertAfter(App.class.getName() + ".instance = this;");
    }

}
