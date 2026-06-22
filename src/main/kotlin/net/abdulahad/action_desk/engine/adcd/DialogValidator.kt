package net.abdulahad.action_desk.engine.adcd

object DialogValidator {
	
	fun validate(
		fieldName: String,
		field: DialogFieldSpec,
		rawValue: Any?
	): DialogValidationError? {
		if (field.required && isEmptyValue(rawValue)) {
			return DialogValidationError(
				fieldName = fieldName,
				message = requiredMessage(field, fieldName)
			)
		}
		
		/*
		 * Optional empty value is valid.
		 * Example: optional age field can be blank even if data_type is int.
		 */
		if (!field.required && isEmptyValue(rawValue)) {
			return null
		}
		
		val value = valueToString(rawValue)
		
		validateDataType(field, fieldName, rawValue, value)?.let {
			return it
		}
		
		validateStringLength(field, fieldName, value)?.let {
			return it
		}
		
		validateNumericRange(field, fieldName, value)?.let {
			return it
		}
		
		validateAllowedValues(field, fieldName, value)?.let {
			return it
		}
		
		return null
	}
	
	private fun validateDataType(
		field: DialogFieldSpec,
		fieldName: String,
		rawValue: Any?,
		value: String
	): DialogValidationError? {
		return when (effectiveDataType(field)) {
			"int" -> {
				if (value.trim().toIntOrNull() == null) {
					error(fieldName, field, "must be a whole number.")
				} else {
					null
				}
			}
			
			"float" -> {
				if (value.trim().toDoubleOrNull() == null) {
					error(fieldName, field, "must be a number.")
				} else {
					null
				}
			}
			
			"bool" -> {
				if (rawValue is Boolean || parseBool(value) != null) {
					null
				} else {
					error(fieldName, field, "must be true or false.")
				}
			}
			
			else -> null
		}
	}
	
	private fun validateStringLength(
		field: DialogFieldSpec,
		fieldName: String,
		value: String
	): DialogValidationError? {
		val length = value.length
		
		if (field.minLen != null && length < field.minLen) {
			return error(
				fieldName,
				field,
				"must be at least ${field.minLen} characters."
			)
		}
		
		if (field.maxLen != null && length > field.maxLen) {
			return error(
				fieldName,
				field,
				"must be at most ${field.maxLen} characters."
			)
		}
		
		return null
	}
	
	private fun validateNumericRange(
		field: DialogFieldSpec,
		fieldName: String,
		value: String
	): DialogValidationError? {
		if (field.min == null && field.max == null) {
			return null
		}
		
		val number = value.trim().toDoubleOrNull()
			?: return error(fieldName, field, "must be a number.")
		
		if (field.min != null && number < field.min) {
			return error(fieldName, field, "must be at least ${formatNumber(field.min)}.")
		}
		
		if (field.max != null && number > field.max) {
			return error(fieldName, field, "must be at most ${formatNumber(field.max)}.")
		}
		
		return null
	}
	
	private fun validateAllowedValues(
		field: DialogFieldSpec,
		fieldName: String,
		value: String
	): DialogValidationError? {
		if (field.allowedValues.isEmpty()) {
			return null
		}
		
		val input = value.trim()
		
		val matched = if (field.caseSensitive) {
			field.allowedValues.any { it == input }
		} else {
			field.allowedValues.any { it.equals(input, ignoreCase = true) }
		}
		
		if (matched) {
			return null
		}
		
		return DialogValidationError(
			fieldName = fieldName,
			message = "${fieldTitle(field, fieldName)} must be one of: ${field.allowedValues.joinToString(", ")}."
		)
	}
	
	private fun effectiveDataType(field: DialogFieldSpec): String {
		if (field.dataType != "string") {
			return field.dataType
		}
		
		return if (field.type == "number") {
			"float"
		} else {
			"string"
		}
	}
	
	private fun isEmptyValue(value: Any?): Boolean {
		return when (value) {
			null -> true
			is Boolean -> !value
			else -> value.toString().trim().isBlank()
		}
	}
	
	private fun valueToString(value: Any?): String {
		return value?.toString()?.trim() ?: ""
	}
	
	private fun parseBool(value: String): Boolean? {
		return when (value.trim().lowercase()) {
			"true", "yes", "1" -> true
			"false", "no", "0" -> false
			else -> null
		}
	}
	
	private fun requiredMessage(field: DialogFieldSpec, fieldName: String): String {
		return when (field.type) {
			"checkbox" -> "${fieldTitle(field, fieldName)} must be checked."
			"radio" -> "Please select ${fieldTitle(field, fieldName)}."
			else -> "${fieldTitle(field, fieldName)} is required."
		}
	}
	
	private fun error(
		fieldName: String,
		field: DialogFieldSpec,
		message: String
	): DialogValidationError {
		return DialogValidationError(
			fieldName = fieldName,
			message = "${fieldTitle(field, fieldName)} $message"
		)
	}
	
	private fun fieldTitle(field: DialogFieldSpec, fieldName: String): String {
		return field.label ?: fieldName
	}
	
	private fun formatNumber(value: Double): String {
		return if (value % 1.0 == 0.0) {
			value.toInt().toString()
		} else {
			value.toString()
		}
	}
	
}