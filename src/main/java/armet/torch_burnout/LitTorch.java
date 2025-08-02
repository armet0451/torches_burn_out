package armet.torch_burnout;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.redstone.Redstone;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.fluids.FluidType;
import org.checkerframework.checker.units.qual.C;

import javax.annotation.Nullable;


public class LitTorch extends FaceAttachedHorizontalDirectionalBlock {
    public static final BooleanProperty LIT = BooleanProperty.create("lit");
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final EnumProperty<AttachFace> FACE = BlockStateProperties.ATTACH_FACE;
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;



    public int lightLevel = 14;

    public SimpleParticleType flameParticles = ParticleTypes.FLAME;

    public LitTorch(Properties properties, SimpleParticleType particles, int light) {
       super(properties);
       flameParticles = particles;
       lightLevel = light;
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(LIT, true)
            .setValue(WATERLOGGED, false)
            .setValue(FACE, AttachFace.FLOOR)
            .setValue(FACING, Direction.NORTH));
    }


    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LIT);
        builder.add(WATERLOGGED);
        builder.add(FACE);
        builder.add(FACING);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        float x = switch(state.getValue(FACING)){case WEST -> 6.0f; case EAST -> -6.0f; default -> 0.0f;};
        float z = switch(state.getValue(FACING)){case NORTH -> 6.0f; case SOUTH -> -6.0f; default -> 0.0f;};
        float y = 3.0f;

        return switch (state.getValue(FACE)){


            case FLOOR -> Block.box(6.0,0.0,6.0,10.0,10.0,10.0);
            case WALL -> Block.box(6.0 + x,0.0+y,6.0 + z,10.0 + x,10.0+y,10.0 + z);
            default -> Block.box(6.0,8.0,6.0, 10.0,16.0,10.0);
        };


    }




    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        if (state.getValue(LIT)) {
            return lightLevel;
        } else {
            return 0;
        }
    }

    @Override
    protected MapCodec<? extends FaceAttachedHorizontalDirectionalBlock> codec() {
        return null;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {

        BlockState base = super.getStateForPlacement(context);
        if (!(base == null)){

            if (base.getValue(FACE) == AttachFace.CEILING){return null;}

            boolean isWaterlogged = (context.getLevel().getFluidState(context.getClickedPos()).is(Fluids.WATER));
            context.getLevel().scheduleTick(context.getClickedPos(), this, 1);


            return base.setValue(LIT, Config.PLACE_LIT.getAsBoolean()&&!isWaterlogged).setValue(WATERLOGGED, isWaterlogged);


        }

        return super.getStateForPlacement(context);
    }


    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(LIT)){return;}

        Vec3i dir = new Vec3i(0, 0, 0);
        double vertical_offset = 0;
        if (state.getValue(FACE) == AttachFace.WALL){
            dir = state.getValue(FACING).getNormal();
            vertical_offset = 0.2;
        }


        double x = (double)pos.getX() + 0.5 - (dir.getX()*0.3f);
        double y = (double)pos.getY() + 0.7 + vertical_offset;
        double z = (double)pos.getZ() + 0.5 - (dir.getZ()*0.3f);

        level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0, 0.0, 0.0);
        level.addParticle(flameParticles, x, y, z, 0.0, 0.0, 0.0);
    }




    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {

        double chance = Config.BURNOUT_CHANCE.get();



        if (state.getValue(LIT) && level.getRandom().nextDouble() < chance) { // 1% chance to burn out

            if (Config.BURNED_TORCHES.get()){
                level.setBlock(pos, state.setValue(LIT, false), 3);
            }
            else {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(),3 );
            }


            level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS);
        }

        super.randomTick(state, level, pos, random);
    }


    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {

        if (!Config.CAN_RELIGHT.get() || state.getValue(WATERLOGGED)){ return super.useItemOn(stack, state, level, pos, player, hand, hitResult);}

        if (!state.getValue(LIT) && stack.getItem() == Items.FLINT_AND_STEEL) {

            if (!level.isClientSide()) {
                level.setBlock(pos, state.setValue(LIT, true), 3);
                stack.hurtAndBreak(1, player, stack.getEquipmentSlot());
                level.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }


        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }


    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (state.getValue(WATERLOGGED)&& state.getValue(LIT)) {
            level.scheduleTick(pos, this, 1); // Schedule immediate tick
        }

        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }


    @Override
    protected boolean canBeReplaced(BlockState state, Fluid fluid) {

        return true;

    }



    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED) && state.getValue(LIT)) {
            level.scheduleTick(pos, this, 1);
        }

        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(WATERLOGGED)){

            level.setBlock(pos, level.getBlockState(pos).setValue(LIT, false), 3);
            level.playSound(null, pos, SoundEvents.REDSTONE_TORCH_BURNOUT, SoundSource.BLOCKS);
        }

        super.tick(state, level, pos, random);
    }


}