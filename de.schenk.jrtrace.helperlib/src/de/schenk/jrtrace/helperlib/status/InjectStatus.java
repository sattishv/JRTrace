package de.schenk.jrtrace.helperlib.status;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents the status of an entity (JRTrace instance, class or method) as to
 * whether it injects code or not including a description of the reason
 * 
 * @author Christian Schenk
 *
 */

public class InjectStatus implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6454290809499559369L;

	/* Messages */
	public static final String MSG_NO_JRTRACE_SESSION = "There is no active JRTrace session.";
	public static final String MSG_METHOD_MODIFIERS_DONT_MATCH = "The modifiers of the method were not matched.";
	public static final String MSG_PARAMETERS_DONT_MATCH = "The parameters of the method were not matched.";
	public static final String MSG_METHODNAME_DOESNT_MATCH = "The name of the method is not matched.";
	public static final String MSG_NOT_CONNECTED = "Not Connected";
	public static final String MSG_NO_JRTRACE_CLASSES = "No JRTrace classes are currently installed.";
	public static final String MSG_CLASS_NOT_LOADED = "The class is not loaded yet by the Target JVM. ";
	public static final String MSG_SYSTEM_EXCLUDE = "The class is a JRTrace built-in excluded class and cannot be instrumented with JRTrace";
	public static final String MSG_JRTRACE_CLASS_CANNOT_BE_INSTRUMENTED = "The class is one of the classes that have been installed with JRTrace and cannot be instrumented with JRTrace.";
	public static final String MSG_CLASS_NAME_DOESNT_MATCH = "The name of the class wasn't matched.";
	public static final String MSG_METHOD_IS_ABSTRACT = "An interface or abstract method cannot be instrumented.";

	private String msg = "";
	private StatusState injectionState = StatusState.INJECTS;
	private StatusEntityType entityType;
	private Set<InjectStatus> children = new HashSet<InjectStatus>();
	private String entityName = "";

	public InjectStatus(StatusEntityType entityType) {
		this.entityType = entityType;
	}

	public void setEntityName(String name) {
		entityName = name;
	}

	/**
	 * 
	 * @return the injection state of this entity
	 */
	public StatusState getInjectionState() {
		return injectionState;
	}

	/**
	 * 
	 * @return the type of JRTrace entity that this status represents
	 */
	public StatusEntityType getEntityType() {
		return entityType;
	}

	public String getMessage() {
		return msg;
	}

	public void setInjected(StatusState b) {

		injectionState = b;

	}

	public void setMessage(String message) {
		msg = message;

	}

	public void addChildStatus(InjectStatus childStatus) {
		children.add(childStatus);

	}

	public Set<InjectStatus> getChildStatus() {
		return Collections.unmodifiableSet(children);
	}

	public String getEntityName() {
		return entityName;
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();

		b.append(entityType.toString() + ":");

		b.append(injectionState.toString() + " ");
		b.append(" " + entityName);
		b.append(":" + this.msg);
		/*
		 * if (children.size() > 0) { b.append("\nChildren:\n"); for
		 * (InjectStatus s : children) { b.append(s.toString() + "\n"); }
		 * b.append("End of Children\n"); }
		 */
		return b.toString();
	}

	/**
	 * recursively traverses all the children and sets the cumulative status: -
	 * if at least one child injects, the overall status is inject - if at least
	 * one child says: don't know, the overall status is don't know - if all
	 * children say: don't inject: the overall status is don't inject.
	 */
	public void updateStatusFromChildren() {
		if (children.size() == 0)
			return;
		for (InjectStatus s : children) {
			s.updateStatusFromChildren();
		}
		boolean cantCheck = false;
		boolean canInject = false;

		for (InjectStatus s : children) {

			if (s.getInjectionState() == StatusState.CANT_CHECK)
				cantCheck = true;
			if (s.getInjectionState() == StatusState.INJECTS)
				canInject = true;
		}

		if (canInject)
			injectionState = StatusState.INJECTS;
		else {
			if (cantCheck)
				injectionState = StatusState.CANT_CHECK;
			else
				injectionState = StatusState.DOESNT_INJECT;
		}
	}

	public InjectStatus getChildByEntityName(String methodName) {
		for (InjectStatus s : children) {
			String entityName = s.getEntityName();
			if (entityName.contains(methodName))
				return s;
		}
		return null;
	}
}
