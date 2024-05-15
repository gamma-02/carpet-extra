package carpetextra.mixins;

import net.minecraft.block.dispenser.ItemDispenserBehavior;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import carpetextra.utils.PlaceBlockDispenserBehavior;
import net.minecraft.block.dispenser.FallibleItemDispenserBehavior;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPointer;

@Mixin(targets = "net/minecraft/block/dispenser/DispenserBehavior$13")
public abstract class DispenserBehaviorCarvedPumpkinMixin extends FallibleItemDispenserBehavior {
    @SuppressWarnings("UnresolvedMixinReference")
    @Inject(
            method = "dispenseSilently(Lnet/minecraft/util/math/BlockPointer;Lnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;",
            at = @At("RETURN")
    )
    private void handleBlockPlacing(BlockPointer pointer, ItemStack stack, CallbackInfoReturnable<ItemStack> cir)
    {
        if (!this.isSuccess() && stack.getItem() instanceof BlockItem && PlaceBlockDispenserBehavior.canPlace(((BlockItem) stack.getItem()).getBlock()))
        {
            this.setSuccess(true);
            cir.setReturnValue(PlaceBlockDispenserBehavior.getInstance().dispenseSilently(pointer, stack));
        }
    }
}
