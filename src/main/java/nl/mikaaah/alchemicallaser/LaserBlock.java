package nl.mikaaah.alchemicallaser;

import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;

public class LaserBlock extends HorizontalKineticBlock implements IBE<LaserBlockEntity> {
    private static final VoxelShape SHAPE = Shapes.or(box(2, 12, 2, 14, 16, 14),
        box(4, 3, 4, 12, 12, 12), box(5, 0, 5, 11, 3, 11));
    public LaserBlock(Properties properties) { super(properties); }
    @Override public Direction.Axis getRotationAxis(BlockState state) { return Direction.Axis.Y; }
    @Override public boolean hasShaftTowards(LevelReader level, BlockPos pos, BlockState state, Direction face) {
        return face == Direction.UP;
    }
    @Override public Class<LaserBlockEntity> getBlockEntityClass() { return LaserBlockEntity.class; }
    @Override public BlockEntityType<? extends LaserBlockEntity> getBlockEntityType() { return AlchemicalLaser.LASER_ENTITY.get(); }
    @Override public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return SHAPE; }
    @Override protected ItemInteractionResult useItemOn(ItemStack held, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        if (!AlchemicalLaser.lensName(held).equals("none")) {
            if (!player.mayBuild()) return ItemInteractionResult.FAIL;
            if (!level.isClientSide && level.getBlockEntity(pos) instanceof LaserBlockEntity be) {
                ItemStack old = be.exchangeLens(held.copyWithCount(1));
                if (!player.isCreative()) held.shrink(1);
                if (!old.isEmpty() && !player.getInventory().add(old)) player.drop(old, false);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (player.isShiftKeyDown() && player.mayBuild() && level.getBlockEntity(pos) instanceof LaserBlockEntity be) {
            if (!level.isClientSide) {
                ItemStack old = be.exchangeLens(ItemStack.EMPTY);
                if (!old.isEmpty() && !player.getInventory().add(old)) player.drop(old, false);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }
    @Override public boolean triggerEvent(BlockState state, Level level, BlockPos pos, int event, int data) {
        return level.getBlockEntity(pos) instanceof LaserBlockEntity be && be.triggerEvent(event,data);
    }
}
