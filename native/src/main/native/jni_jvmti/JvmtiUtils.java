package agent.jvmti;



import java.io.File;
import java.util.List;

public class JvmtiUtils {


	private native List<Object> findObjectsByClassHelper(Class<?> clazz, int maxCount);

}
