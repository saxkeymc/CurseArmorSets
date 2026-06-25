package org.minecurse.armorsets.struct;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ArmorCrystal {
   String name() default "???";

   String[] lore() default {"???"};

   double incoming() default 0.0;

   double outgoing() default 0.0;

   double abilityChance() default 0.0;
}
