package org.mirowidgets.value;

import org.immutables.value.Value;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation provides namespace for annotations for immutable value object generation.
 *
 * @see Value
 */
@Retention(RetentionPolicy.SOURCE)
public @interface Immutables {
	/**
	 * Default style
	 * <ul>
	 * <li>Accessors prefixed "is" or "get" will be recognized</li>
	 * <li>Builder initialization methods will be prefixed "set", if the builder is enabled</li>
	 * <li>Immutable implementations won't have the default "Immutable" prefix</li>
	 * <li>Builders will be enabled by default</li>
	 * <li>Copying methods will be enabled by default</li>
	 * <li>Visibility will be public</li>
	 * </ul>
	 * An interface or abstract class utilizing this style must be prefixed "Abstract" or suffixed "Model" and may be
	 * package-private.
	 */
	@Target({ElementType.PACKAGE, ElementType.TYPE})
	@Retention(RetentionPolicy.CLASS)
	@Value.Style(
		get = {"is*", "get*"},
		init = "set*",
		typeAbstract = {"Abstract*", "*Model"},
		typeImmutable = "*",
		typeImmutableEnclosing = "*",
		visibility = Value.Style.ImplementationVisibility.PUBLIC
	)
	@interface DefaultStyle {
	}
}
