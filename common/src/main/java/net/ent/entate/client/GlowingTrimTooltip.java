package net.ent.entate.client;

import java.util.List;
import net.ent.entate.component.ModComponents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.ArmorTrim;

public final class GlowingTrimTooltip {

    private static final Component GLOWING_TRIM_LINE_KEY =
            Component.translatable("tooltip.entate.glowing_trim");

    public static void append(ItemStack stack, List<Component> lines) {
        if (stack == null || lines == null || lines.isEmpty()) {
            return;
        }
        if (!stack.getOrDefault(ModComponents.GLOWING_TRIM, Boolean.FALSE)) {
            return;
        }
        ArmorTrim trim = stack.get(DataComponents.TRIM);
        if (trim == null) {
            return;
        }

        Component material = trim.material().value().description();
        Style style = material.getStyle();
        Component glowLine = CommonComponents.space()
                .append(GLOWING_TRIM_LINE_KEY.copy().withStyle(style));
        Component materialLine = CommonComponents.space().append(material);

        int insertAt = lines.size();
        for (int i = 0; i < lines.size(); i++) {
            Component line = lines.get(i);
            if (line.equals(materialLine) || line.getSiblings().contains(material)) {
                insertAt = i + 1;
                break;
            }
        }
        lines.add(insertAt, glowLine);
    }

    private GlowingTrimTooltip() {}
}
