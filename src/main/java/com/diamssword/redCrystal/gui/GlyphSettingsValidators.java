package com.diamssword.redCrystal.gui;

import com.hypixel.hytale.codec.validation.validator.RangeValidator;


public class GlyphSettingsValidators {

	public static class StepRangeValidator<T extends Number & Comparable<T>> extends RangeValidator<T> {
		public final T min;
		public final T max;
		public final T step;

		public StepRangeValidator(T min, T max, T step) {
			super(min, max, true);
			this.min = min;
			this.max = max;
			this.step = step;
		}
	}

	public static class SliderRangeValidator<T extends Number & Comparable<T>> extends RangeValidator<T> {
		public final T min;
		public final T max;
		public final T step;

		public SliderRangeValidator(T min, T max, T step) {
			super(min, max, true);
			this.min = min;
			this.max = max;
			this.step = step;
		}
	}
}