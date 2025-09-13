package net.rose.arsenalOverruled.mixin;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.jamieswhiteshirt.reachentityattributes.ReachEntityAttributes;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.Item;
import net.rose.arsenalOverruled.ArsenalOverruled;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import xyz.amymialee.amarite.items.AmariteLongswordItem;

import java.util.UUID;

@Mixin(AmariteLongswordItem.class)
public class AmariteLongswordItemMixin {
    /**
     * @author Rosenoire
     * @reason Adjusting stats.
     */
    @Overwrite
    public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(EquipmentSlot slot) {
        if (slot != EquipmentSlot.MAINHAND) return HashMultimap.create();

        final var attackReachModifierID = UUID.fromString("76a8dee3-3e7e-4e11-ba46-a19b0c724567");
        final var blockReachModifierID = UUID.fromString("a31c8afc-a716-425d-89cd-0d373380e6e7");

        ImmutableMultimap.Builder<EntityAttribute, EntityAttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(EntityAttributes.GENERIC_ATTACK_DAMAGE, new EntityAttributeModifier(Item.ATTACK_DAMAGE_MODIFIER_ID, "Tool modifier", ArsenalOverruled.LONGSWORD_ATTACK_DAMAGE - 1, EntityAttributeModifier.Operation.ADDITION));
        builder.put(EntityAttributes.GENERIC_ATTACK_SPEED, new EntityAttributeModifier(Item.ATTACK_SPEED_MODIFIER_ID, "Tool modifier", ArsenalOverruled.LONGSWORD_ATTACK_SPEED - 4, EntityAttributeModifier.Operation.ADDITION));
        builder.put(ReachEntityAttributes.ATTACK_RANGE, new EntityAttributeModifier(attackReachModifierID, "Weapon modifier", ArsenalOverruled.LONGSWORD_ATTACK_REACH, EntityAttributeModifier.Operation.ADDITION));
        builder.put(ReachEntityAttributes.REACH, new EntityAttributeModifier(blockReachModifierID, "Weapon modifier", ArsenalOverruled.LONGSWORD_BLOCK_REACH, EntityAttributeModifier.Operation.ADDITION));
        return builder.build();
    }
}
