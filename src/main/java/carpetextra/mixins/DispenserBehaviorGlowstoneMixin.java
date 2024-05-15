package carpetextra.mixins;

import carpetextra.utils.PlaceBlockDispenserBehavior;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.block.dispenser.FallibleItemDispenserBehavior;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net/minecraft/block/dispenser/DispenserBehavior$14")
public abstract class DispenserBehaviorGlowstoneMixin extends FallibleItemDispenserBehavior
{
    @SuppressWarnings("UnresolvedMixinReference")
    @Inject(
            method = "dispenseSilently(Lnet/minecraft/util/math/BlockPointer;Lnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;",
            at = @At(value = "INVOKE",
                    //The original method, At.Shift.BEFORE called that⌄ before the DispenserBehavior#dispenseSilently call at the end. Mixin doesn't like that in 1.20.6 though, apparently... I did this instead. Is a bad way, someone please fix.
                    /*if (stack.getItem() instanceof BlockItem && PlaceBlockDispenserBehavior.canPlace(((BlockItem) stack.getItem()).getBlock()))
                     cir.setReturnValue(PlaceBlockDispenserBehavior.getInstance().dispenseSilently(pointer, stack));*/
                    shift = At.Shift.BY, by = 2,
                    target = "Lnet/minecraft/util/math/BlockPointer;world()Lnet/minecraft/server/world/ServerWorld;"),
            cancellable = true
    )
    private void handleBlockPlacing(BlockPointer pointer, ItemStack stack, CallbackInfoReturnable<ItemStack> cir)
    {
        World world = pointer.world();
        BlockState blockState = world.getBlockState(pointer.pos());

        if (blockState.isOf(Blocks.RESPAWN_ANCHOR)) {
            if (blockState.get(RespawnAnchorBlock.CHARGES) != 4) {
                RespawnAnchorBlock.charge(null, world, pointer.pos(), blockState);
                stack.decrement(1);
            } else {
                this.setSuccess(false);
            }

            cir.setReturnValue(stack);
        } else {
            if (stack.getItem() instanceof BlockItem && PlaceBlockDispenserBehavior.canPlace(((BlockItem) stack.getItem()).getBlock()))
                cir.setReturnValue(PlaceBlockDispenserBehavior.getInstance().dispenseSilently(pointer, stack));
            else
                cir.setReturnValue(super.dispenseSilently(pointer, stack));
        }

    }
}
