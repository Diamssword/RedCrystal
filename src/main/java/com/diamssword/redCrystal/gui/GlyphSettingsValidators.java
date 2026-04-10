package com.diamssword.redCrystal.gui;

import com.diamssword.redCrystal.storage.assets.AbstractBehaviorAsset;
import com.diamssword.redCrystal.storage.assets.BehaviorAssetWithSwitchModels;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.validation.ValidationResults;
import com.hypixel.hytale.codec.validation.Validator;
import com.hypixel.hytale.codec.validation.validator.RangeValidator;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;


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

	public static class MapKeySelector<T extends AbstractBehaviorAsset<?>> implements Validator<String> {
		Function<AbstractBehaviorAsset<?>, Set<String>> keysProvider;

		public MapKeySelector(Function<T, Set<String>> keysProvider) {
			this.keysProvider = (Function<AbstractBehaviorAsset<?>, Set<String>>) keysProvider;
		}

		@Override
		public void accept(String var1, ValidationResults var2) {

		}

		@Override
		public void updateSchema(SchemaContext var1, Schema var2) {

		}
	}

	public static class ModelKeySelector implements Validator<String> {
		public final boolean texturePart;

		public ModelKeySelector(boolean texturePart) {
			this.texturePart = texturePart;
		}

		public Set<String> getModels(BehaviorAssetWithSwitchModels asset) {
			return asset.getModelKeys();
		}

		public Set<String> getTexturesKey(BehaviorAssetWithSwitchModels asset, @Nullable String model) {
			return asset.getModelFor(model).getTexturesKey();
		}

		@Override
		public void accept(String var1, ValidationResults var2) {

		}

		@Override
		public void updateSchema(SchemaContext var1, Schema var2) {

		}
	}
}