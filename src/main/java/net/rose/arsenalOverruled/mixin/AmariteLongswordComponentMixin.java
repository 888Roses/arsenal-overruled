package net.rose.arsenalOverruled.mixin;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.rose.arsenalOverruled.ArsenalOverruled;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import xyz.amymialee.amarite.cca.LongswordComponent;
import xyz.amymialee.amarite.items.AmaritePocketMirrorItem;
import xyz.amymialee.amarite.registry.AmariteDamageTypes;
import xyz.amymialee.amarite.registry.AmariteItems;
import xyz.amymialee.amarite.registry.AmariteSoundEvents;

import java.util.ArrayList;

@Mixin(LongswordComponent.class)
public abstract class AmariteLongswordComponentMixin {
    @Shadow(remap = false)
    public abstract boolean isDashing();

    @Shadow(remap = false)
    public abstract boolean isDoubleDashing();

    @Shadow(remap = false)
    @Final
    private PlayerEntity player;

    @Shadow(remap = false)
    @Final
    private IntOpenHashSet slicedEntities;

    @Shadow(remap = false)
    private boolean accumulateActive;

    @Shadow(remap = false)
    public abstract void tick();

    /**
     * @author Rosenoire
     * @reason ughhh
     */
    @Overwrite(remap = false)
    public void serverTick() {
        if (this.isDashing() || this.isDoubleDashing()) {
            this.player.fallDistance = 0.0F;

            if (this.isDashing()) {
                final var damageBox = new Box(
                        this.player.getPos().subtract(this.player.getRotationVector().multiply(2.5F, 2.0F, 2.5F)),
                        this.player.getPos().add(this.player.getRotationVector().multiply(2.5F, 2.0F, 2.5F))
                );

                final var entitiesToSlice = this.player.getWorld().getEntitiesByClass(LivingEntity.class, damageBox, (e) -> true);
                for (var slicedEntity : entitiesToSlice) {
                    if (this.slicedEntities.contains(slicedEntity.getId())) continue;
                    this.slicedEntities.add(slicedEntity.getId());

                    if (slicedEntity.damage(AmariteDamageTypes.create(this.player.getWorld(), AmariteDamageTypes.DASH, this.player), ArsenalOverruled.LONGSWORD_DASH_DAMAGE)) {
                        ItemStack offhand = this.player.getOffHandStack();
                        if (offhand.isOf(AmariteItems.AMARITE_MIRROR)) {
                            StatusEffect primary = AmaritePocketMirrorItem.getPrimary(offhand);
                            StatusEffect secondary = AmaritePocketMirrorItem.getSecondary(offhand);
                            boolean hasOne = primary != null || secondary != null;
                            if (hasOne && (primary == null || !primary.isBeneficial()) && (secondary == null || !secondary.isBeneficial())) {
                                if (primary != null && secondary != null) {
                                    if (primary == secondary) {
                                        slicedEntity.addStatusEffect(new StatusEffectInstance(primary, 300, 1));
                                    } else {
                                        slicedEntity.addStatusEffect(new StatusEffectInstance(primary, 300, 0));
                                        slicedEntity.addStatusEffect(new StatusEffectInstance(secondary, 300, 0));
                                    }
                                } else {
                                    if (primary != null) {
                                        slicedEntity.addStatusEffect(new StatusEffectInstance(primary, 300, 0));
                                    }

                                    if (secondary != null) {
                                        slicedEntity.addStatusEffect(new StatusEffectInstance(secondary, 300, 0));
                                    }
                                }

                                this.player.getItemCooldownManager().set(AmariteItems.AMARITE_MIRROR, AmaritePocketMirrorItem.getCooldown(offhand));
                                this.player.playSound(AmariteSoundEvents.MIRROR_USE, SoundCategory.PLAYERS, 1.0F, 0.75F);
                            }
                        }
                    }
                }
            }
        }

        if (this.accumulateActive) {
            Vec3d cameraTarget = this.player.getEyePos().add(this.player.getRotationVec(1.0F).multiply(4096.0F));
            Box box = this.player.getBoundingBox().stretch(cameraTarget).expand(1.0F, 1.0F, 1.0F);
            ArrayList<Entity> targets = new ArrayList();
            double targetDistance = 0.02;
            Vec3d rotationVec = this.player.getRotationVector();

            for (Entity possibleTarget : this.player.getWorld().getEntitiesByClass(LivingEntity.class, box, (entity) -> true)) {
                if (possibleTarget.getRootVehicle() != this.player.getRootVehicle() && this.player.canSee(possibleTarget)) {
                    Vec3d playerDistance = possibleTarget.getPos().add(0.0F, possibleTarget.getHeight() / 2.0F, 0.0F).subtract(this.player.getEyePos());
                    double distance = (double) 1.0F - playerDistance.normalize().dotProduct(rotationVec);
                    if (!(distance > targetDistance) && !targets.contains(possibleTarget)) {
                        targets.add(possibleTarget);
                    }
                }
            }

            for (Entity target : targets) {
                if (target instanceof LivingEntity living) {
                    if (living.damage(AmariteDamageTypes.create(this.player.getWorld(), AmariteDamageTypes.ACCUMULATE, this.player), 0.06F)) {
                        ItemStack offhand = this.player.getOffHandStack();
                        if (offhand.isOf(AmariteItems.AMARITE_MIRROR)) {
                            StatusEffect primary = AmaritePocketMirrorItem.getPrimary(offhand);
                            StatusEffect secondary = AmaritePocketMirrorItem.getSecondary(offhand);
                            boolean hasOne = primary != null || secondary != null;
                            if (hasOne && (primary == null || !primary.isBeneficial()) && (secondary == null || !secondary.isBeneficial())) {
                                if (primary != null && secondary != null) {
                                    if (primary == secondary) {
                                        living.addStatusEffect(new StatusEffectInstance(primary, 300, 1));
                                    } else {
                                        living.addStatusEffect(new StatusEffectInstance(primary, 300, 0));
                                        living.addStatusEffect(new StatusEffectInstance(secondary, 300, 0));
                                    }
                                } else {
                                    if (primary != null) {
                                        living.addStatusEffect(new StatusEffectInstance(primary, 300, 0));
                                    }

                                    if (secondary != null) {
                                        living.addStatusEffect(new StatusEffectInstance(secondary, 300, 0));
                                    }
                                }

                                this.player.getItemCooldownManager().set(AmariteItems.AMARITE_MIRROR, AmaritePocketMirrorItem.getCooldown(offhand));
                                this.player.playSound(AmariteSoundEvents.MIRROR_USE, SoundCategory.PLAYERS, 1.0F, 0.75F);
                            }
                        }
                    }

                    living.timeUntilRegen = 0;
                    this.player.setHealth(this.player.getHealth() + 0.06F);
                }

                if (this.player.getAbilities().allowModifyWorld) {
                    Vec3d newVelocity = target.getVelocity().multiply(0.4);
                    newVelocity = newVelocity.add(this.player.getPos().subtract(target.getPos()).normalize().multiply(0.15));
                    Vec3d look = this.player.getRotationVec(1.0F);
                    Vec3d targetPos = target.getPos().add(0.0F, target.getHeight() / 2.0F, 0.0F);
                    Vec3d targetToLook = look.subtract(targetPos.subtract(this.player.getEyePos()).normalize().multiply(look.dotProduct(targetPos.subtract(this.player.getEyePos()).normalize())));
                    newVelocity = newVelocity.add(targetToLook.multiply(0.2));
                    if (this.player.getEyeY() > target.getBodyY(0.5F) || target.isOnGround()) {
                        newVelocity = newVelocity.add(0.0F, 0.1, 0.0F);
                    }

                    target.setVelocity(newVelocity);
                }
            }
        }

        this.tick();
    }
}
