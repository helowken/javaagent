package agent.server.command.executor;

import agent.base.utils.Logger;
import agent.base.utils.ReflectionUtils;
import agent.common.config.SaveClassConfig;
import agent.cmdline.command.Command;
import agent.common.message.result.ExecResult;
import agent.common.message.result.entity.DefaultExecResult;
import agent.server.transform.revision.ClassDataRepository;
import agent.server.transform.revision.ClassDataStore;
import agent.server.transform.search.ClassCache;
import agent.server.transform.search.ClassSearcher;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiPredicate;

class SaveClassCmdExecutor extends AbstractCmdExecutor {
    private static final Logger logger = Logger.getLogger(SaveClassCmdExecutor.class);

    @Override
    ExecResult doExec(Command cmd) throws Exception {
        SaveClassConfig config = cmd.getContent();
        return DefaultExecResult.toSuccess(
                cmd.getType(),
                null,
                saveClasses(
                        config,
                        findClasses(config)
                )
        );
    }

    private Collection<Class<?>> findClasses(SaveClassConfig config) {
        ClassCache classCache = new ClassCache();
        Collection<Class<?>> targetClasses = ClassSearcher.getInstance().search(
                classCache,
                config.getClassFilterConfig()
        );
        Set<Class<?>> rs = new HashSet<>(targetClasses);
        if (config.isWithSubClasses() || config.isWithSubTypes()) {
            Set<Class<?>> restClasses = new HashSet<>(
                    classCache.getLoadedClasses()
            );
            restClasses.removeAll(targetClasses);

            BiPredicate<Class<?>, Class<?>> predicate = config.isWithSubClasses() ?
                    ReflectionUtils::isSubClass :
                    ReflectionUtils::isSubType;
            restClasses.forEach(
                    candidateClass -> targetClasses.forEach(
                            baseClass -> {
                                if (predicate.test(baseClass, candidateClass))
                                    rs.add(candidateClass);
                            }
                    )
            );
        }
        return rs;
    }

    private Collection<String> saveClasses(SaveClassConfig config, Collection<Class<?>> rs) {
        Collection<String> msgs = new TreeSet<>();
        ClassDataStore store = new ClassDataStore(
                config.getOutputPath()
        );
        rs.forEach(
                clazz -> {
                    String msg = clazz.getName() + " (" + System.identityHashCode(
                            clazz.getClassLoader()
                    ) + ")";
                    try {
                        store.save(
                                clazz,
                                ClassDataRepository.getInstance().getCurrentClassData(clazz)
                        );
                    } catch (Throwable e) {
                        logger.error("Save class data failed.", e);
                        msg += " failed by: " + e.getMessage();
                    }
                    msgs.add(msg);
                }
        );
        return msgs;
    }
}
