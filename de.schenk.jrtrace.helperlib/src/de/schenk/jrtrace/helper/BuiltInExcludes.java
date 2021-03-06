/**
 * (c) 2014/2015 by Christian Schenk
 **/
package de.schenk.jrtrace.helper;

import de.schenk.jrtrace.annotations.XClass;
import de.schenk.jrtrace.helperlib.status.InjectStatus;
import de.schenk.jrtrace.helperlib.status.StatusState;

public class BuiltInExcludes {

	/**
	 * All classes that start with the following strings are to be excluded
	 * because they can never be instrumented.
	 * 
	 * Example:
	 * 
	 * - Assume java.lang.String is instrumented. - After byte code generation,
	 * the first time the method is used, the dynamic invoke statement has to be
	 * resolved - Resolving that involves invoking e.g. the DynamicBinder (and a
	 * lot of java-lambda related code) which again requires java.lang.String ->
	 * At that point the instrumentation will start to recurse.
	 */
	private static final String[] excludedPrefixes = { "java.lang.Byte",
			"java.lang.Class", "java.lang.ClassLoader", "java.lang.Integer",
			"java.lang.Math", "java.lang.Object", "java.lang.String",
			"java.lang.System", "java.lang.ref",
			"java.lang.ref.ReferenceQueue", "java.util.Arrays",
			"java.util.HashMap", "java.util.Objects", "java.util.Collections",
			"de.schenk.jrtrace.help", "de.schenk.objectweb",
			"java.lang.invoke", "java.util.concurrent.ConcurrentHashMap",
			"java.lang.reflect", "java.util.ArrayList", "jdk.internal",
			"sun.invoke", "sun.reflect", "java.util.regex.Pattern",
			"java.lang.Enum", "java.lang.ThreadLocal",
			"java.util.AbstractList", "sun.misc.VM", "java.lang.Character",
			"java.lang.BootstrapMethodError", "java.lang.EnumMap",
			"java.util.HashSet", "java.util.AbstractCollection" };

	/**
	 * 
	 * @param classname
	 *            a non-null fully qualified classname
	 * @return true, if the class cannot be converted by JRTrace due to system
	 *         restrictions.
	 */
	static private boolean isBuiltInExclude(String classname) {
		for (String prefix : excludedPrefixes) {
			if (classname.startsWith(prefix))
				return true;
		}
		return false;
	}

	/**
	 * Checks whether a class of name classname is excluded from
	 * instrumentation.
	 * 
	 * This can be because (a) it is excluded via the {@link XClass#exclude}
	 * attribute or (b) because it is a "built-in" exclude that cannot be
	 * instrumented (like the jrtrace code itself.
	 * 
	 *
	 * @param classname
	 *            the class name to check
	 * @param classInjectStatus
	 *            if not null, reports the result on this status if the class is
	 *            indeed excluded.
	 * @return true, if the class is an excluded class.
	 */
	static public boolean isExcludedClassName(String classname,
			InjectStatus classInjectStatus) {

		if (classname == null)
			return true;
		if (isBuiltInExclude(classname)) {
			if (classInjectStatus != null) {
				classInjectStatus.setInjected(StatusState.DOESNT_INJECT);
				classInjectStatus.setMessage(InjectStatus.MSG_SYSTEM_EXCLUDE);

			}
			return true;
		}
		if (JRTraceHelper.isJRTraceClass(classname)) {
			if (classInjectStatus != null) {
				classInjectStatus.setInjected(StatusState.DOESNT_INJECT);
				classInjectStatus
						.setMessage(InjectStatus.MSG_JRTRACE_CLASS_CANNOT_BE_INSTRUMENTED);

			}
			return true;
		}
		return false;
	}
}
