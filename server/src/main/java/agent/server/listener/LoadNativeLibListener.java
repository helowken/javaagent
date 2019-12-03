package agent.server.listener;

import agent.base.utils.FileUtils;
import agent.base.utils.SystemConfig;
import agent.base.utils.Utils;
import agent.jvmti.JvmtiUtils;
import agent.server.ServerListener;

public class LoadNativeLibListener implements ServerListener {
    private static final String KEY_NATIVE_LIB_DIR = "native.lib.dir";

    @Override
    public void onStartup(Object[] args) {
        Utils.wrapToRtError(
                () -> JvmtiUtils.getInstance().load(
                        FileUtils.collectFiles(
                                FileUtils.splitPathStringToPathArray(
                                        SystemConfig.splitToSet(KEY_NATIVE_LIB_DIR),
                                        SystemConfig.getBaseDir()
                                )
                        )
                )
        );
    }

    @Override
    public void onShutdown() {

    }
}
