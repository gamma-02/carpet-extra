package carpetextra.dispenser.behaviors;

import carpetextra.dispenser.DispenserBehaviorHelper;
import net.minecraft.block.AbstractCauldronBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BannerPatternsComponent;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.item.BannerItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potions;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.event.GameEvent;

import java.util.Optional;

public class CauldronWaterDispenserBehavior extends DispenserBehaviorHelper {
    @Override
    protected ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
        this.setSuccess(true);
        Item item = stack.getItem();
        ServerWorld world = pointer.world();
        BlockPos frontBlockPos = pointer.pos().offset(pointer.state().get(DispenserBlock.FACING));
        BlockState frontBlockState = world.getBlockState(frontBlockPos);
        Block frontBlock = frontBlockState.getBlock();

        if(frontBlock == Blocks.WATER_CAULDRON) {
            if(item == Items.POTION && stack.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT).potion().equals(Optional.of(Potions.WATER))) {
                // check if cauldron is not full
                if(!((AbstractCauldronBlock) frontBlock).isFull(frontBlockState)) {
                    // increase cauldron level
                    int level = frontBlockState.get(LeveledCauldronBlock.LEVEL);
                    BlockState cauldronState = frontBlockState.with(LeveledCauldronBlock.LEVEL, level + 1);
                    setCauldron(world, frontBlockPos, cauldronState, SoundEvents.ITEM_BOTTLE_EMPTY, GameEvent.FLUID_PLACE);

                    // return glass bottle
                    return this.addOrDispense(pointer, stack, new ItemStack(Items.GLASS_BOTTLE));
                }
            }
            else if(item == Items.GLASS_BOTTLE) {
                // decrease cauldron level
                LeveledCauldronBlock.decrementFluidLevel(frontBlockState, world, frontBlockPos);
                // return water bottle
                var newStack = stack.copy();
                newStack.set(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.createStack(Items.POTION, Potions.WATER).get(DataComponentTypes.POTION_CONTENTS));
                return this.addOrDispense(pointer, stack, newStack);
            }
            else if(Block.getBlockFromItem(item) instanceof ShulkerBoxBlock) {
                // make sure item isn't plain shulker box
                if(item != Items.SHULKER_BOX) {
                    // decrease cauldron level
                    LeveledCauldronBlock.decrementFluidLevel(frontBlockState, world, frontBlockPos);
                    // turn dyed shulker box into undyed shulker box
                    ItemStack undyedShulkerBox = new ItemStack(Items.SHULKER_BOX);
                    stack.applyComponentsFrom(undyedShulkerBox.getComponents());

                    // return undyed shulker box
                    return this.addOrDispense(pointer, stack, undyedShulkerBox);
                }
            }
            if(stack.contains(DataComponentTypes.DYED_COLOR)) {
                DyedColorComponent dyeComponent = stack.get(DataComponentTypes.DYED_COLOR);

                // check if dyeable item has color
                if(dyeComponent.rgb() != DyedColorComponent.DEFAULT_COLOR) {
                    // decrease cauldron level
                    LeveledCauldronBlock.decrementFluidLevel(frontBlockState, world, frontBlockPos);
                    // remove color
                    stack.set(DataComponentTypes.DYED_COLOR, new DyedColorComponent(-6265536, dyeComponent.showInTooltip()));

                    // return undyed item
                    return stack;
                }
            }
            else if(item instanceof BannerItem) {
                // checks if banner has layers
                if((stack.contains(DataComponentTypes.BANNER_PATTERNS))) {
                    // decrease cauldron level
                    LeveledCauldronBlock.decrementFluidLevel(frontBlockState, world, frontBlockPos);
                    // copy banner stack, set to one item
                    ItemStack cleanedBanner = stack.copy();
                    cleanedBanner.setCount(1);


                    // removes layer from banner (yarn name is misleading)
                    BannerPatternsComponent bannerPatternsComponent = stack.get(DataComponentTypes.BANNER_PATTERNS);
                    cleanedBanner.set(DataComponentTypes.BANNER_PATTERNS, bannerPatternsComponent.withoutTopLayer());


                    // return cleaned banner
                    return this.addOrDispense(pointer, stack, cleanedBanner);
                }
            }
        }
        else if(frontBlock == Blocks.CAULDRON && item == Items.POTION && stack.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT).potion().equals(Optional.of(Potions.WATER))) {
            // increase cauldron level
            BlockState cauldronState = Blocks.WATER_CAULDRON.getDefaultState();
            setCauldron(world, frontBlockPos, cauldronState, SoundEvents.ITEM_BOTTLE_EMPTY, GameEvent.FLUID_PLACE);

            // return glass bottle
            return this.addOrDispense(pointer, stack, new ItemStack(Items.GLASS_BOTTLE));
        }

        // fail to dispense
        this.setSuccess(false);
        return stack;
    }

    // set cauldron, play sound, emit game event
    private static void setCauldron(ServerWorld world, BlockPos pos, BlockState state, SoundEvent soundEvent, RegistryEntry<GameEvent> gameEvent) {
        world.setBlockState(pos, state);
        world.playSound(null, pos, soundEvent, SoundCategory.BLOCKS, 1.0F, 1.0F);
        world.emitGameEvent(null, gameEvent, pos);
    }

    public static boolean isWaterCauldronItem(ItemStack stack) {
        Item item = stack.getItem();
        return item == Items.GLASS_BOTTLE ||
            // water bottle
            (item == Items.POTION && stack.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT).potion().equals(Optional.of(Potions.WATER))) ||
            // shulker boxes
            Block.getBlockFromItem(item) instanceof ShulkerBoxBlock ||
            // banners
            item instanceof BannerItem ||
            // dyeable items (leather armor, leather horse armor)
            (stack.getOrDefault(DataComponentTypes.DYED_COLOR, new DyedColorComponent(DyedColorComponent.DEFAULT_COLOR, false)).rgb() != DyedColorComponent.DEFAULT_COLOR);
    }
}
