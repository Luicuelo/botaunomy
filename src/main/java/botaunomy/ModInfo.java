/*******************************************************************************
 * Copyright (C) 2017 Jeremy Grozavescu <oneandonlyflexo>
 * https://creativecommons.org/licenses/by-nc-sa/4.0/
 *
 * This file is part of Botaunomy, which is open source:
 * https://github.com/oneandonlyflexo/botaunomy
 ******************************************************************************/
package botaunomy;

/**
 * Top level info related to the mod.  Real important stuff here! ...and yes I know constants are typically all
 * upper-cased, but I felt like using lower-case for these.
 *
 * @author "oneandonlyflexo"
 */
public class ModInfo {

	public static final String modid = "botaunomy";
	public static final String name = "Botaunomy";

	/**This gets replaced with a value from build.properties during the gradle build. */
	public static final String version = /*${gradle.mod_version}*/ "0.3.9.4";

	public static final String description = "A Botania addon focused on automation tweaks.";

}
