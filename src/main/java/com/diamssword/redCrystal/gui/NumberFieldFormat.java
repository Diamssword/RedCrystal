package com.diamssword.redCrystal.gui;

public class NumberFieldFormat {
	private Integer maxDecimalPlaces;
	private Float step;
	private Float minValue;
	private Float maxValue;

	public NumberFieldFormat() {
	}

	public double parse(double value) {
		if(maxValue != null)
			value = Math.min(maxValue, value);
		if(minValue != null)
			value = Math.max(minValue, value);
		return value;
	}

	public NumberFieldFormat withMaxDecimalPlaces(int maxDecimalPlaces) {
		this.maxDecimalPlaces = maxDecimalPlaces;
		return this;
	}

	public NumberFieldFormat withStep(float step) {
		this.step = step;
		return this;
	}

	public NumberFieldFormat withMinValue(float minValue) {
		this.minValue = minValue;
		return this;
	}

	public NumberFieldFormat withMaxValue(float maxValue) {
		this.maxValue = maxValue;
		return this;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("(");
		if(this.maxDecimalPlaces != null) {
			builder.append("MaxDecimalPlaces:").append(this.maxDecimalPlaces).append(",");
		}

		if(this.step != null) {
			builder.append("Step:").append(this.step.doubleValue()).append(",");
		}

		if(this.minValue != null) {
			builder.append("MinValue:").append(this.minValue.doubleValue()).append(",");
		}

		if(this.maxValue != null) {
			builder.append("MaxValue:").append(this.maxValue.doubleValue()).append(",");
		}
		return builder.append(")").toString();
	}
}
