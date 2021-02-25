#include <unistd.h>
#include "jvmti.h"
#include "header/agent_jvmti_JvmtiUtils.h"
#include "header/util.h"

static jclass getListClass(JNIEnv* env);

static jobject newList(JNIEnv* env, jclass listClass);

static jmethodID getMethodID(JNIEnv *env, jclass clazz, const char *methodName, const char *signature);

static jmethodID getListAdd(JNIEnv *env, jclass clazz);


struct RefCount {
	int maxCount;
	int count;
};


static jint JNICALL
TagObjectByClass(jlong class_tag, jlong size, jlong* tag_ptr, jint length, void* user_data) {
	*tag_ptr = 1;
	struct RefCount* refCount = (struct RefCount*) user_data;
	refCount->count += 1;
	if (refCount->count >= refCount->maxCount) 
		return JVMTI_VISIT_ABORT;
	return JVMTI_VISIT_OBJECTS;
}


JNIEXPORT jobject JNICALL
Java_agent_jvmti_JvmtiUtils_findObjectsByClassHelper(JNIEnv* env, jobject thisObj, jclass targetClass, jint maxCount) {
	JavaVM* vm;
	(*env)->GetJavaVM(env, &vm);

	jvmtiEnv* jvmti;
	(*vm)->GetEnv(vm, (void**) &jvmti, JVMTI_VERSION_1_0);

	jvmtiCapabilities capabilities = {0};
	capabilities.can_tag_objects = 1;
	(*jvmti)->AddCapabilities(jvmti, &capabilities);

	jvmtiHeapCallbacks callbacks = {};
	callbacks.heap_iteration_callback = TagObjectByClass;
	struct RefCount refCount = {maxCount, 0};
	(*jvmti)->IterateThroughHeap(jvmti, JVMTI_HEAP_FILTER_TAGGED, targetClass, &callbacks, &refCount);

	jlong tag = 1;
	jint count;
	jobject* instances;
	(*jvmti)->GetObjectsWithTags(jvmti, 1, &tag, &count, &instances, NULL); 

	if (instances != NULL) {
		jclass listClass = getListClass(env);
		if (listClass != NULL) {
			jobject rsList = newList(env, listClass);
			if (rsList != NULL) {
				jmethodID listAdd = getListAdd(env, listClass);
				if (listAdd != NULL) {
					int i;
					int len = count;
					if (count > maxCount)
						len = maxCount;
					for (i = 0; i < len; ++i) {
						(*env)->CallBooleanMethod(env, rsList, listAdd, *(instances + i));
					}
					(*jvmti)->Deallocate(jvmti, (unsigned char*) instances);
					return rsList;
				}
			}
		}

	}
	return NULL;
}


static 
jclass getListClass(JNIEnv* env) {
	jclass arrayListClass = (*env)->FindClass(env, "java/util/ArrayList");
	if (arrayListClass == NULL)
		return NULL;
	return arrayListClass;
}


static 
jobject newList(JNIEnv* env, jclass listClass) {
	jmethodID arrayListConstructor = (*env)->GetMethodID(env, 
		listClass,
		"<init>",
		"()V"
	);
	if (arrayListConstructor == NULL)
		return NULL;

	jobject rsList = (*env)->NewObject(env, listClass, arrayListConstructor);
	if (rsList == NULL)
		return NULL;
	return rsList;
}


static
jmethodID getMethodID(JNIEnv *env, jclass clazz, const char *methodName, const char *signature) {
	jmethodID methodID = (*env)->GetMethodID(env,
			clazz,
			methodName,
			signature
	);
	if (methodID == NULL)
		return NULL;
	return methodID;
}


static
jmethodID getListAdd(JNIEnv *env, jclass clazz) {
	return getMethodID(env, clazz, "add", "(Ljava/lang/Object;)Z");
}



JNIEXPORT jobject JNICALL 
Java_agent_jvmti_JvmtiUtils_getLoadedClasses(JNIEnv *env, jobject thisObj) {
	JavaVM* vm;
	(*env)->GetJavaVM(env, &vm);

	jvmtiEnv* jvmti;
	(*vm)->GetEnv(vm, (void**) &jvmti, JVMTI_VERSION_1_0);

	jint count;
	jclass* classes_ptr;
	(*jvmti)->GetLoadedClasses(jvmti, &count, &classes_ptr);

	if (classes_ptr != NULL) {
		jclass listClass = getListClass(env);
		if (listClass != NULL) {
			jobject rsList = newList(env, listClass);
			if (rsList != NULL) {
				jmethodID listAdd = getListAdd(env, listClass);
				if (listAdd != NULL) {
					int i;
					for (i = 0; i < count; ++i) {
						(*env)->CallBooleanMethod(env, rsList, listAdd, *(classes_ptr + i));
					}
					(*jvmti)->Deallocate(jvmti, (unsigned char*) classes_ptr);
					return rsList;
				}
			}
		}

	}
	return NULL;
}


JNIEXPORT jboolean JNICALL Java_agent_jvmti_JvmtiUtils_tryToSetEuidAndEgid
  (JNIEnv *env, jobject thisObj, jint pid) {
    return tryToSetEuidAndEgid(pid) == 0? JNI_TRUE : JNI_FALSE;
}


JNIEXPORT jint JNICALL Java_agent_jvmti_JvmtiUtils_getProcId
  (JNIEnv *env, jobject thisObj) {
	return getpid();
}
