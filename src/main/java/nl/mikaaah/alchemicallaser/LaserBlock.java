package nl.mikaaah.alchemicallaser;

import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;
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
    private static final VoxelShape LANTERN = Shapes.or(box(4, -3, 4, 12, 7, 12), box(5, -6, 5, 11, -3, 11));
    private static final VoxelShape Z_SHAPE = Shapes.or(LANTERN, box(0, 8, 0, 6, 14, 16), box(10, 8, 0, 16, 14, 16));
    private static final VoxelShape X_SHAPE = Shapes.or(LANTERN, box(0, 8, 0, 16, 14, 6), box(0, 8, 10, 16, 14, 16));
    public LaserBlock(Properties properties) { super(properties); }
    @Override public Direction.Axis getRotationAxis(BlockState state) { return state.getValue(HORIZONTAL_FACING).getAxis(); }
    @Override public boolean hasShaftTowards(LevelReader level, BlockPos pos, BlockState state, Direction face) {
        return false;
    }
    @Override public BlockState getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext context) {
        BlockState above = context.getLevel().getBlockState(context.getClickedPos().above());
        if (ICogWheel.isLargeCog(above)) {
            var axis = above.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.AXIS);
            if (axis != Direction.Axis.Y) return defaultBlockState().setValue(HORIZONTAL_FACING,
                axis == Direction.Axis.X ? Direction.NORTH : Direction.EAST);
        }
        return super.getStateForPlacement(context);
    }
    @Override public void neighborChanged(BlockState state, Level level, BlockPos pos,
            net.minecraft.world.level.block.Block neighbor, BlockPos neighborPos, boolean moved) {
        super.neighborChanged(state, level, pos, neighbor, neighborPos, moved);
        if (!level.isClientSide && neighborPos.equals(pos.above()) && level.getBlockEntity(pos) instanceof LaserBlockEntity be
                && be.hasSource()) {
            var above = level.getBlockEntity(pos.above());
            if (!(above instanceof com.simibubi.create.content.kinetics.base.KineticBlockEntity cog) || !LaserDrive.connects(be, cog)) {
                // A wrench can rotate a cog without replacing its block entity.
                be.detachKinetics(); be.removeSource(); be.attachKinetics(); be.sendData();
            }
        }
    }
    @Override public Class<LaserBlockEntity> getBlockEntityClass() { return LaserBlockEntity.class; }
    @Override public BlockEntityType<? extends LaserBlockEntity> getBlockEntityType() { return AlchemicalLaser.LASER_ENTITY.get(); }
    @Override public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getRotationAxis(state) == Direction.Axis.Z ? Z_SHAPE : X_SHAPE;
    }
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
