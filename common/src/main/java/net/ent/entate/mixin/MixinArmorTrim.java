package net.ent.entate.mixin;

import java.util.function.Consumer;
import net.ent.entate.component.ModComponents;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ArmorTrim.class)
public class MixinArmorTrim {

    @Inject(method = "addToTooltip", at = @At("TAIL"))
    private void entate$glowingTrimTooltip(Item.TooltipContext context, Consumer<Component> consumer, TooltipFlag flag, DataComponentGetter components, CallbackInfo ci) {
        if (!components.getOrDefault(ModComponents.GLOWING_TRIM, Boolean.FALSE)) {
            return;
        }
        ArmorTrim self = (ArmorTrim) (Object) this;
        Style materialStyle = self.material().value().description().getStyle();
        consumer.accept(CommonComponents.space().append(Component.translatable("tooltip.entate.glowing_trim").withStyle(materialStyle)));
    }
}
