package jagm.classicpipes.util;

import jagm.classicpipes.ClassicPipes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.Comparator;

public enum SortingMode {

    AMOUNT_ASCENDING((byte) 0, MiscUtil.AMOUNT.thenComparing(MiscUtil.NAME), "amount", "ascending"),
    AMOUNT_DESCENDING((byte) 1, MiscUtil.AMOUNT.reversed().thenComparing(MiscUtil.NAME), "amount", "descending"),
    A_TO_Z((byte) 2, MiscUtil.NAME, "name", "az"),
    Z_TO_A((byte) 3, MiscUtil.NAME.reversed(), "name", "za"),
    MOD_A_TO_Z((byte) 4, MiscUtil.MOD.thenComparing(MiscUtil.AMOUNT.reversed().thenComparing(MiscUtil.NAME)), "mod", "az"),
    MOD_Z_TO_A((byte) 5, MiscUtil.MOD.reversed().thenComparing(MiscUtil.AMOUNT.reversed().thenComparing(MiscUtil.NAME.reversed())), "mod", "za"),
    CRAFTABLE_A_TO_Z((byte) 6, MiscUtil.CRAFTABLE.reversed().thenComparing(MiscUtil.NAME), "craftable", "az"),
    CRAFTABLE_Z_TO_A((byte) 7, MiscUtil.CRAFTABLE.reversed().thenComparing(MiscUtil.NAME.reversed()), "craftable", "za");

    private final byte value;
    private final Comparator<Tuple<ItemStack, Boolean>> comparator;
    private final String type;
    private final String direction;

    SortingMode(byte value, Comparator<Tuple<ItemStack, Boolean>> comparator, String type, String direction) {
        this.value = value;
        this.comparator = comparator;
        this.type = type;
        this.direction = direction;
    }

    public Comparator<Tuple<ItemStack, Boolean>> getComparator() {
        return this.comparator;
    }

    public Component getType() {
        return Component.translatable("widget." + ClassicPipes.MOD_ID + "." + this.type);
    }

    public Component getDirection() {
        return Component.translatable("widget." + ClassicPipes.MOD_ID + "." + this.direction);
    }

    public SortingMode nextType() {
        return switch (this) {
            case AMOUNT_ASCENDING -> Z_TO_A;
            case AMOUNT_DESCENDING -> A_TO_Z;
            case A_TO_Z -> MOD_A_TO_Z;
            case Z_TO_A -> MOD_Z_TO_A;
            case MOD_A_TO_Z -> CRAFTABLE_A_TO_Z;
            case MOD_Z_TO_A -> CRAFTABLE_Z_TO_A;
            case CRAFTABLE_A_TO_Z -> AMOUNT_DESCENDING;
            case CRAFTABLE_Z_TO_A -> AMOUNT_ASCENDING;
        };
    }

    public SortingMode prevType() {
        return switch (this) {
            case AMOUNT_ASCENDING -> CRAFTABLE_Z_TO_A;
            case AMOUNT_DESCENDING -> CRAFTABLE_A_TO_Z;
            case A_TO_Z -> AMOUNT_DESCENDING;
            case Z_TO_A -> AMOUNT_ASCENDING;
            case MOD_A_TO_Z -> A_TO_Z;
            case MOD_Z_TO_A -> Z_TO_A;
            case CRAFTABLE_A_TO_Z -> MOD_A_TO_Z;
            case CRAFTABLE_Z_TO_A -> MOD_Z_TO_A;
        };
    }

    public SortingMode otherDirection() {
        return switch (this) {
            case AMOUNT_ASCENDING -> AMOUNT_DESCENDING;
            case AMOUNT_DESCENDING -> AMOUNT_ASCENDING;
            case A_TO_Z -> Z_TO_A;
            case Z_TO_A -> A_TO_Z;
            case MOD_A_TO_Z -> MOD_Z_TO_A;
            case MOD_Z_TO_A -> MOD_A_TO_Z;
            case CRAFTABLE_A_TO_Z -> CRAFTABLE_Z_TO_A;
            case CRAFTABLE_Z_TO_A -> CRAFTABLE_A_TO_Z;
        };
    }

    public byte getValue() {
        return this.value;
    }

    public static SortingMode fromByte(byte value) {
        for (SortingMode sortingMode : SortingMode.values()) {
            if (sortingMode.getValue() == value) {
                return sortingMode;
            }
        }
        return AMOUNT_DESCENDING;
    }

}
