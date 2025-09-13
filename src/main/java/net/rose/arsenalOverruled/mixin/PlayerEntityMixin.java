package net.rose.arsenalOverruled.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.amymialee.amarite.cca.BuddingComponent;
import xyz.amymialee.amarite.cca.DiscComponent;
import xyz.amymialee.amarite.cca.LongswordComponent;
import xyz.amymialee.amarite.registry.AmariteSoundEvents;

@Mixin({PlayerEntity.class})
public abstract class PlayerEntityMixin extends LivingEntity {
    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(
            method = {"damage"},
            at = {@At("HEAD")},
            cancellable = true
    )
    private void amarite$noDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        final var player = (PlayerEntity) (Object) this;
        final var longswordComponent = LongswordComponent.get(player);
        if (!longswordComponent.isDashing() && !longswordComponent.isDoubleDashing()) {
            final var discComponent = DiscComponent.get(player);
            discComponent.chargeOrbit(amount);
            return;
        }

        cir.setReturnValue(false);
    }

    @WrapOperation(
            method = {"applyDamage"},
            at = {@At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/PlayerEntity;modifyAppliedDamage(Lnet/minecraft/entity/damage/DamageSource;F)F"
            )}
    )
    private float amarite$swordBlock(PlayerEntity entity, DamageSource source, float amount, Operation<Float> original) {
        final var originalDamageValue = original.call(entity, source, amount);
        final var longswordComponent = LongswordComponent.get(entity);

        if (longswordComponent.isAccumulating()) {
            if (this.getWorld() instanceof ServerWorld serverWorld) {
                serverWorld.playSoundFromEntity(
                        null, this, AmariteSoundEvents.SWORD_BLOCK, SoundCategory.HOSTILE,
                        1.0F, 1.0F + this.getWorld().random.nextFloat() * 0.4F
                );
            }

            return source == this.getWorld().getDamageSources().fall()
                    ? originalDamageValue / 6.0F
                    : originalDamageValue / 2.0F;
        }

        if (longswordComponent.isBlocking()) {
            final var damagePosition = source.getPosition();
            if (damagePosition != null) {
                final var rotationVector = this.getRotationVec(1.0F);
                final var direction = damagePosition.relativize(this.getEyePos()).normalize();
                final var angle = direction.dotProduct(rotationVector);
                if (angle < -1.0 || angle > 1.0) {
                    return originalDamageValue;
                }

                if (angle < -0.35) {
                    if (this.getWorld() instanceof ServerWorld serverWorld) {
                        serverWorld.playSoundFromEntity(
                                null, this, AmariteSoundEvents.SWORD_BLOCK, SoundCategory.HOSTILE,
                                1.0F, 1.0F + this.getWorld().random.nextFloat() * 0.4F
                        );
                    }

                    longswordComponent.absorbDamage(this.getActiveItem(), originalDamageValue);
                    // FIXME: Annoying aahhhh line
                    return originalDamageValue / 2.0F;
                }

                return originalDamageValue;
            }

            if (source == this.getWorld().getDamageSources().fall()) {
                final var rotationVector = this.getRotationVec(1.0F);
                final var upwardsVector = new Vec3d(0.0F, 1.0F, 0.0F);
                double angle = upwardsVector.dotProduct(rotationVector);
                if (angle < -0.35) {
                    if (this.getWorld() instanceof ServerWorld serverWorld) {
                        serverWorld.playSoundFromEntity(
                                null, this, AmariteSoundEvents.SWORD_BLOCK, SoundCategory.HOSTILE,
                                1.0F, 1.0F + this.getWorld().random.nextFloat() * 0.4F
                        );
                    }

                    longswordComponent.absorbDamage(this.getActiveItem(), originalDamageValue);
                    return originalDamageValue / 6F;
                }
            }
        }

        return originalDamageValue;
    }

    @Inject(
            method = "eatFood",
            at = @At("HEAD")
    )
    private void amarite$eatenItAll(World world, ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        final var player = (PlayerEntity) (Object) this;
        final var buddingComponent = BuddingComponent.get(player);

        if (stack.getNbt() != null) {
            if (stack.getNbt().getBoolean("Budded")) buddingComponent.setBuddedTicks(12000);
            if (stack.getNbt().getBoolean("Curative")) buddingComponent.setBuddedTicks(0);
        }

        if (stack.isOf(Items.HONEY_BOTTLE)) buddingComponent.setBuddedTicks(0);
    }

    @Inject(
            method = "attack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/PlayerEntity;onAttacking(Lnet/minecraft/entity/Entity;)V"
            )
    )
    private void amarite$chargeRebound(Entity target, CallbackInfo ci) {
        final var discComponent = DiscComponent.get((PlayerEntity) (Object) this);
        discComponent.chargeRebound(1);
    }
}
