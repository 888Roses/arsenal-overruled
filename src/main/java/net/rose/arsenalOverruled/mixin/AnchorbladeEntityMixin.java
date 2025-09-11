package net.rose.arsenalOverruled.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.doctor4t.arsenal.entity.AnchorbladeEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AnchorbladeEntity.class)
public class AnchorbladeEntityMixin {
    @ModifyReturnValue(method = "getKnockbackForEntity", at = @At("RETURN"))
    private float getKnockbackForEntity$arsenal_overruled(float original) {
        final var entity = (AnchorbladeEntity) (Object) this;
        return original * (entity.hasReeling() ? 1.5F : 1);
    }
}
