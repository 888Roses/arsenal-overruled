package net.rose.arsenalOverruled.mixin;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import dev.doctor4t.arsenal.item.ScytheItem;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.Item;
import net.rose.arsenalOverruled.ArsenalOverruled;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(ScytheItem.class)
public class ScytheItemMixin {
    /**
     * @author Rosenoire
     * @reason Changing the attack damage from <b>10 -> 9</b> and attack speed from <b>1.1 -> 1</b>, as well as removing
     * reach.
     */
    @Overwrite
    public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(EquipmentSlot slot) {
        if (slot != EquipmentSlot.MAINHAND) return HashMultimap.create();

        ImmutableMultimap.Builder<EntityAttribute, EntityAttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(EntityAttributes.GENERIC_ATTACK_DAMAGE, new EntityAttributeModifier(Item.ATTACK_DAMAGE_MODIFIER_ID, "Tool modifier", ArsenalOverruled.SCYTHE_ATTACK_DAMAGE - 1, EntityAttributeModifier.Operation.ADDITION));
        builder.put(EntityAttributes.GENERIC_ATTACK_SPEED, new EntityAttributeModifier(Item.ATTACK_SPEED_MODIFIER_ID, "Tool modifier", ArsenalOverruled.SCYTHE_ATTACK_SPEED - 4, EntityAttributeModifier.Operation.ADDITION));
        return builder.build();
    }
}
