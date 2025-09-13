package net.rose.arsenalOverruled;

import com.bawnorton.mixinsquared.api.MixinCanceller;

import java.util.List;

public class ArsenalOverruledMixinCanceller implements MixinCanceller {
    @Override
    public boolean shouldCancel(List<String> targetClassNames, String mixinClassName) {
        return mixinClassName.equals("xyz.amymialee.amarite.mixin.PlayerEntityMixin");
    }
}
