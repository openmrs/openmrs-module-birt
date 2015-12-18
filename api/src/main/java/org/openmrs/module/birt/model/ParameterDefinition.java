package org.openmrs.module.birt.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.openmrs.Form;
//import org.openmrs.reporting.AbstractReportObject;

/**
 * Report parameter definition.
 * 
 * @author Justin Miranda
 *
 */
public class ParameterDefinition implements Serializable {

	private static final long serialVersionUID = 1L;

	// Parameter value display types
	public static final String TEXT_BOX = "text";
	public static final String LIST_BOX = "select";
	public static final String RADIO_BUTTON = "radio";
	public static final String CHECK_BOX = "checkbox";
	public static final String DATE_CHOOSER = "date";

	// Parameter data types
	public static final String TYPE_BOOLEAN = "boolean";
	public static final String TYPE_DATE = "date";
	public static final String TYPE_DATE_TIME = "dateTime";
	public static final String TYPE_DECIMAL = "decimal";
	public static final String TYPE_FLOAT = "float";
	public static final String TYPE_STRING = "string";
	public static final String TYPE_TIME = "time";

	// Parameter
	private Integer id;
	private String name;
	private String description;
	private String promptText;
	private String helpText;
	private String controlType;
	private String dataType;
	private String parameterType = "simple"; // simple, multi-value
	private Class targetClass;
	private Object defaultValue;
	// private Object value;
	private List<Object> values = new ArrayList<Object>();
	private Map<Object, String> selectionList;
	private String displayFormat;
	private Boolean required = false;
	private Boolean allowNull = false;
	private Boolean allowMultiple = false;
	private Boolean hidden = false;
	private Boolean conceal = false;

	/**
	 * Default public constructor.
	 *
	 */
	public ParameterDefinition() {
	}

	/**
	 * Public constructor
	 * 
	 * @param name
	 * @param value
	 */
	public ParameterDefinition(String name, Object value) {
		this.name = name;
		this.values.add(value);
	}

	/**
	 * Public constructor.
	 */
	public ParameterDefinition(String name, Object[] values) {
		this.name = name;
		this.values = Arrays.asList(values);
	}

	/**
	 * Gets the identifier of the report.
	 * 
	 * @return
	 */
	public Integer getId() {
		return this.id;
	}

	/**
	 * Gets the display name of the report.
	 * 
	 * @return
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Gets the description.
	 * 
	 * @return
	 */
	public String getDescription() {
		return this.description;
	}

	/**
	 * Gets the default prompt text of the report.
	 */
	public String getPromptText() {
		return promptText;
	}

	/**
	 * 
	 * @return
	 */
	public String getHelpText() {
		return this.helpText;
	}

	/**
	 * Gets the prompt text of the report that matches the given locale.
	 */
	public String getPromptText(Locale locale) {
		return promptText;
	}

	/**
	 * 
	 * @return
	 */
	public String getControlType() {
		return this.controlType;
	}

	/**
	 * Gets the data type of the column.
	 */
	public String getDataType() {
		return this.dataType;
	}

	/**
	 * Gets the param type of the column.
	 */
	public String getParameterType() {
		return this.parameterType;
	}

	/**
	 * Gets the class of the value.
	 * 
	 * @return
	 */
	public Class getTargetClass() {
		return this.targetClass;
	}

	/**
	 * Gets the default value.
	 */
	public Object getDefaultValue() {
		return defaultValue;
	}

	/**
	 * Gets the selection list (if one exists)
	 */
	public Map<Object, String> getSelectionList() {
		return selectionList;
	}

	/**
	 * Gets the value, assumes there is only one.
	 */
	public Object getValue() {
		return this.values.get(0);
	}

	/**
	 * Gets all values.
	 * 
	 * @return
	 */
	public Object[] getValues() {
		return values.toArray();
	}

	public boolean getRequired() {
		return required;
	}

	public String getDisplayFormat() {
		return displayFormat;
	}

	public Boolean getAllowNull() {
		return allowNull;
	}

	public Boolean getAllowMultiple() {
		return allowMultiple;
	}

	public Boolean getHidden() {
		return hidden;
	}

	public Boolean getConceal() {
		return conceal;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setControlType(String controlType) {
		this.controlType = controlType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public void setParameterType(String parameterType) {
		this.parameterType = parameterType;
	}

	public void setDefaultValue(Object value) {
		this.defaultValue = value;
	}

	public void setSelectionList(Map<Object, String> values) {
		this.selectionList = values;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setPromptText(String promptText) {
		this.promptText = promptText;
	}

	public void setHelpText(String helpText) {
		this.helpText = helpText;
	}

	public void setTargetClass(Class targetClass) {
		this.targetClass = targetClass;
	}

	public void setValue(Object value) {
		this.values.clear();
		this.values.add(value);
	}

	public void setValues(Object[] values) {
		this.values.clear();
		this.values.addAll(Arrays.asList(values));
	}

	public void setDisplayFormat(String format) {
		this.displayFormat = format;
	}

	/**
	 * @param allowNull
	 *            the allowNull to set
	 */
	public void setAllowNull(Boolean allowNull) {
		this.allowNull = allowNull;
	}

	/**
	 * @param allowMultiple
	 *            the allowMultiple to set
	 */
	public void setAllowMultiple(Boolean allowMultiple) {
		this.allowMultiple = allowMultiple;
	}

	/**
	 * @param conceal
	 *            the conceal to set
	 */
	public void setConceal(Boolean conceal) {
		this.conceal = conceal;
	}

	/**
	 * @param hidden
	 *            the hidden to set
	 */
	public void setHidden(Boolean hidden) {
		this.hidden = hidden;
	}

	/**
	 * @param required
	 *            the required to set
	 */
	public void setRequired(Boolean required) {
		this.required = required;
	}

	/**
	 * Compares two objects for similarity
	 * 
	 * @param obj
	 * @return boolean true/false whether or not they are the same objects
	 */
	public boolean equals(Object obj) {
		if (obj instanceof ParameterDefinition) {
			ParameterDefinition param = (ParameterDefinition) obj;
			if (this.getId() != null && param.getId() != null)
				return this.getId().equals(param.getId());
		}
		return false;
	}

	/**
	 * Returns hash code for object.
	 * 
	 * @return hash code of the object
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		if (this.getName() == null)
			return super.hashCode();
		return this.getName().hashCode();
	}

	/**
	 * Return string representation of object
	 * 
	 * @see org.openmrs.reporting.AbstractReportObject#toString()
	 */
	public String toString() {
		return new StringBuffer().append("[ ").append("name=").append(getName()).append(", dataType=")
				.append(getDataType()).append(", defaultValue=").append(getDefaultValue()).append(", values=")
				.append(getValues()).append(", controlType=").append(getControlType()).append(", required=")
				.append(getRequired()).append(", helpText=").append(getHelpText()).append(", displayFormat=")
				.append(getDisplayFormat()).append(", promptText=").append(getPromptText()).append(", selectionList=")
				.append(getSelectionList()).append("] ").toString();
	}

}
