package net.ent.entate.mixin;

import net.ent.entate.component.ModComponents;
import net.ent.entate.trim.CustomTemplate;
import net.ent.entate.trim.CustomTemplateManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public class MixinCustomTemplateName {

    @Inject(method = "getName(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/network/chat/Component;",
            at = @At("HEAD"), cancellable = true)
    private void entate$customTemplateName(ItemStack stack, CallbackInfoReturnable<Component> cir) {
        Identifier pattern = stack.get(ModComponents.TRIM_PATTERN);
        if (pattern == null) {
            return;
        }
        CustomTemplate template = CustomTemplateManager.get(pattern);
        if (template == null) {
            return;
        }
        cir.setReturnValue(entate$name(template.name()));
    }

    @org.spongepowered.asm.mixin.Unique
    private static Component entate$name(String name) {
        boolean looksLikeKey = !name.isEmpty()
                && name.indexOf(' ') < 0
                && name.indexOf('.') >= 0
                && name.equals(name.toLowerCase(java.util.Locale.ROOT));
        return looksLikeKey ? Component.translatable(name) : Component.literal(name);
    }
}
