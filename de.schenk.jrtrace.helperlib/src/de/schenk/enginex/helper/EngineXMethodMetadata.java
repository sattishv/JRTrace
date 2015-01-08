/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.enginex.helper;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.schenk.jrtrace.annotations.XLocation;
import de.schenk.jrtrace.helperlib.JRLog;

public class EngineXMethodMetadata {

	private EngineXMetadata parent;

	/**
	 * The UsedFor annotation values to determine for which target methods this
	 * is called.
	 */
	Set<String> targetMethodNames = new HashSet<String>();

	/**
	 * The name of the method in the enginex class.
	 */
	private String name;
	/**
	 * The descriptor of the method in the enginex class
	 */
	private String parameters;

	public EngineXMethodMetadata(EngineXMetadata md, String name, String desc) {
		this.name = name;
		this.parameters = desc;
		this.parent = md;

	}

	public Set<String> getTargetMethodNames() {
		return Collections.unmodifiableSet(targetMethodNames);
	}

	public String getDescriptor() {
		return parameters;
	}

	public EngineXMetadata getClassMetadata() {
		return parent;
	}

	public void addTargetMethodName(String value) {
		targetMethodNames.add(value);

	}

	public String getMethodName() {
		return name;
	}

	/**
	 * null: no argument list provided = matches any argument list
	 */
	List<String> argumentList = null;

	/**
	 *  a map describing the argument, return and field injections into the instrumented method:
	 *  key: the argument position (0,...)
	 *  value: Integer: 0: this, -1: return value, n>=1: paramter index, String: field injection.
	 */
	private Map<Integer, Injection> injection = new HashMap<Integer, Injection>();


	
	private XLocation injectLocation = XLocation.ENTRY;

  private String invokedethodName="";

  private String invokedClass="";

  private String fieldAccessClass="";
  
  private String fieldAccessName="";

	public void addArgument(String value) {
		if (argumentList == null)
			argumentList = new ArrayList<String>();
		argumentList.add(value);
	}

	public List<String> getArgumentList() {
		return argumentList == null ? null : Collections
				.unmodifiableList(argumentList);
	}

	/**
	 * 
	 * @param methodName
	 * @param desc
	 * @return true, if the methodName and Descriptor passed in is matched by
	 *         this metadata.
	 */
	public boolean mayMatch(String methodName, String desc) {
		if (mayMatchMethodName(methodName)) {
			String[] ps = MethodUtil.getParametersAsString(desc);
			if (mayMatchParameters(ps))
				return true;
		}
		return false;
	}

	/**
	 * 
	 * @param targetposition
	 *            the parameter index to set
	 * @param source
	 *            the source index to take this from (0=this,1=first
	 *            parameter,...) -or- the source field name to inject here
	 */
	public void addInjection(int targetposition, Injection source) {
		injection.put(targetposition, source);

	}

	public Map<Integer, Injection> getInjections() {
		return Collections.unmodifiableMap(injection);
	}

	public Injection getInjection(int i) {
		return injection.get(i);
	}

	public XLocation getInjectLocation() {
		return injectLocation;
	}

	public void setInjectLocation(XLocation valueOf) {
		this.injectLocation = valueOf;

	}

	/**
	 * 
	 * @param methodName
	 * @return true if the method name is matched by this metadata
	 */
	private boolean mayMatchMethodName(String methodName) {
		if (getTargetMethodNames().isEmpty())
			return true;
		if (!parent.getUseRegEx()) {
			for (String targetMethodMatcher : getTargetMethodNames()) {
				if (targetMethodMatcher.equals(methodName))
					return true;

			}
			return false;
		} else {

			for (String targetMethodMatcher : getTargetMethodNames()) {
				if (methodName.matches(targetMethodMatcher))
					return true;
			}
			return false;
		}
	}

	/**
	 * 
	 * @param theclass
	 * @return true, if the theclass contains at least one method that might be
	 *         in the transformation scope of this enginexmethod
	 */
	public boolean mayMatch(Class<?> theclass) {
		Method[] methods = null;
		try {
			methods = theclass.getDeclaredMethods();
		} catch (NoClassDefFoundError e) {
			JRLog.error("getDeclaredMethods failed on Class<?> "
					+ theclass
					+ " with NoClassDefFoundError. The class can anyway probably not load and will therefore be ignored. Known cases when this happens: DynamicImport-Package in Osgi (probably also optional dependencies.)");
			return false;
		}
		for (int i = 0; i < methods.length; i++) {
			String name = methods[i].getName();
			if (mayMatchMethodName(name)) {
				Method theMethod = methods[i];
				String[] ptypesStrings = getParameterList(theMethod);
				if (mayMatchParameters(ptypesStrings))
					return true;
			}
		}
		return false;
	}

	/**
	 * 
	 * @param theMethod
	 * @return a string array containing the parameters of this method
	 */
	private String[] getParameterList(Method theMethod) {
		Class<?>[] ptypes = theMethod.getParameterTypes();
		String[] ptypesStrings = new String[ptypes.length];
		for (int j = 0; j < ptypes.length; j++) {
			ptypesStrings[j] = ptypes[j].getName();
		}
		return ptypesStrings;
	}

	/**
	 * 
	 * @param ptypesStrings
	 *            an array with argument types
	 * @return true, if the list matches the method metadata description
	 */
	private boolean mayMatchParameters(String[] ptypesStrings) {
		if (this.argumentList == null)
			return true;
		if (ptypesStrings.length != argumentList.size())
			return false;
		if (!parent.getUseRegEx()) {

			for (int i = 0; i < ptypesStrings.length; i++) {

				String argType = ptypesStrings[i];
				if (!argType.equals(argumentList.get(i)))
					return false;
			}
			return true;

		} else {
			for (int i = 0; i < ptypesStrings.length; i++) {
				String argType = ptypesStrings[i];
				if (!argType.matches(argumentList.get(i))) {
					return false;
				}
			}
			return true;
		}

	}

	public void setArgumentListEmpty() {
		argumentList = Collections.emptyList();

	}

  /**
   * @return
   */
  public String getInvokedMethodName() {
    
    return invokedethodName;
  }

  /**
   * @param value
   */
  public void setInvokedMethod(String value) {
      invokedethodName=value;
    
  }

  /**
   * @param invokedMethodName
   * @return true: if the passed in method name matches the "invokedMethodName"
   */
  public boolean matchesInvoker(String invokedMethodClass,String invokedMethodName) {
    
    
   return checkStringMatch(invokedMethodName,getInvokedMethodName()) && checkStringMatch(invokedMethodClass.replace('/', '.'),getInvokedMethodClass());
  }

  /**
   * @param invokedMethodName
   * @return
   */
  private boolean checkStringMatch(String state,String match) {
    if(match.isEmpty()) return true;
     if(this.parent.getUseRegEx())
     {
       return state.matches(match);
     } else
     {
       return state.equals(match);
     }
  }

  

  /**
   * @param value
   */
  public void setInvokedClass(String value) {
    this.invokedClass=value;
    
  }

  /**
   * @return
   */
  public String getInvokedMethodClass() {
    return invokedClass;
  }

  /**
   * @return
   */
  public String getFieldAccessClass() {
    
    return fieldAccessClass;
  }

  /**
   * @return
   */
  public String getFieldAccessName() {
   
    return fieldAccessName;
  }

  /**
   * @param value the name of the class to restrict instrumentation of field access (GETFIELD/PUTFIELD)
   */
  public void setFieldAccessClass(String value) {
    fieldAccessClass=value;
    
  }

  /**
   * @param value the name of the field to restrict instrumentation of field access (GETFIELD/PUTFIELD)
   */
  public void setFieldAccessName(String value) {
    this.fieldAccessName=value;
    
  }

}
