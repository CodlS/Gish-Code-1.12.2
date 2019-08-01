package i.gishreloaded.gishcode.utils;

import java.util.LinkedList;

import i.gishreloaded.gishcode.utils.system.Wrapper;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerDigging.Action;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public final class BlockUtils
{
	private static final Minecraft mc = Wrapper.INSTANCE.mc();
	
	public static IBlockState getState(BlockPos pos)
	{
		return mc.world.getBlockState(pos);
	}
	
	public static Block getBlock(BlockPos pos)
	{
		return getState(pos).getBlock();
	}
	
	public static Material getMaterial(BlockPos pos)
	{
		return getState(pos).getMaterial();
	}
	
	public static boolean canBeClicked(BlockPos pos)
	{
		return getBlock(pos).canCollideCheck(getState(pos), false);
	}
	
	public static float getHardness(BlockPos pos) {
		return getState(pos).getPlayerRelativeBlockHardness(Wrapper.INSTANCE.player(), Wrapper.INSTANCE.world(), pos);
	}
	
	public static boolean placeBlockLegit(BlockPos pos)
	{
		Vec3d eyesPos = new Vec3d(mc.player.posX,
			mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ);
		
		for(EnumFacing side : EnumFacing.values())
		{
			BlockPos neighbor = pos.offset(side);
			EnumFacing side2 = side.getOpposite();
			
			// check if side is visible (facing away from player)
			// TODO: actual line-of-sight check
			if(eyesPos.squareDistanceTo(
				new Vec3d(pos).addVector(0.5, 0.5, 0.5)) >= eyesPos
					.squareDistanceTo(
						new Vec3d(neighbor).addVector(0.5, 0.5, 0.5)))
				continue;
			
			// check if neighbor can be right clicked
			if(!getBlock(neighbor)
				.canCollideCheck(mc.world.getBlockState(neighbor), false))
				continue;
			
			Vec3d hitVec = new Vec3d(neighbor).addVector(0.5, 0.5, 0.5)
				.add(new Vec3d(side2.getDirectionVec()).scale(0.5));
			
			// check if hitVec is within range (4.25 blocks)
			if(eyesPos.squareDistanceTo(hitVec) > 18.0625)
				continue;
			
			// place block
			faceVectorPacket(hitVec);
			//mc.playerController.processRightClickBlock(mc.player, mc.world,
				//neighbor, side2, hitVec, EnumHand.MAIN_HAND);
			//mc.player.swingArm(EnumHand.MAIN_HAND);
			//mc.rightClickDelayTimer = 4;
			
		}
		Wrapper.INSTANCE.sendPacket(new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND));
		mc.player.swingArm(EnumHand.MAIN_HAND);
		
		return true;
	}
	
	public static boolean placeBlockSimple(BlockPos pos)
	{
		Vec3d eyesPos = new Vec3d(mc.player.posX,
			mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ);
		
		for(EnumFacing side : EnumFacing.values())
		{
			BlockPos neighbor = pos.offset(side);
			EnumFacing side2 = side.getOpposite();
			
			// check if neighbor can be right clicked
			if(!getBlock(neighbor)
				.canCollideCheck(mc.world.getBlockState(neighbor), false))
				continue;
			
			Vec3d hitVec = new Vec3d(neighbor).addVector(0.5, 0.5, 0.5)
				.add(new Vec3d(side2.getDirectionVec()).scale(0.5));
			
			// check if hitVec is within range (6 blocks)
			if(eyesPos.squareDistanceTo(hitVec) > 36)
				continue;
			
			// place block
			mc.playerController.processRightClickBlock(mc.player, mc.world,
				neighbor, side2, hitVec, EnumHand.MAIN_HAND);
			
			return true;
		}
		
		return false;
	}
	
	// TODO: RotationUtils class for all the faceSomething() methods
	
	public static void faceVectorPacket(Vec3d vec)
	{
		double diffX = vec.x - mc.player.posX;
		double diffY = vec.y - (mc.player.posY + mc.player.getEyeHeight());
		double diffZ = vec.z - mc.player.posZ;
		
		double dist = MathHelper.sqrt(diffX * diffX + diffZ * diffZ);
		
		float yaw = (float)Math.toDegrees(Math.atan2(diffZ, diffX)) - 90F;
		float pitch = (float)-Math.toDegrees(Math.atan2(diffY, dist));
		
		mc.player.connection.sendPacket(new CPacketPlayer.Rotation(
			mc.player.rotationYaw
				+ MathHelper.wrapDegrees(yaw - mc.player.rotationYaw),
			mc.player.rotationPitch
				+ MathHelper.wrapDegrees(pitch - mc.player.rotationPitch),
			mc.player.onGround));
	}
	
	public static void faceBlockClient(BlockPos blockPos)
	{
		double diffX = blockPos.getX() + 0.5 - mc.player.posX;
		double diffY =      //0.5
			blockPos.getY() + 0.0 - (mc.player.posY + mc.player.getEyeHeight());
		double diffZ = blockPos.getZ() + 0.5 - mc.player.posZ;
		double dist = MathHelper.sqrt(diffX * diffX + diffZ * diffZ);
		float yaw =
			(float)(Math.atan2(diffZ, diffX) * 180.0D / Math.PI) - 90.0F;
		float pitch = (float)-(Math.atan2(diffY, dist) * 180.0D / Math.PI);
		mc.player.rotationYaw = mc.player.rotationYaw
			+ MathHelper.wrapDegrees(yaw - mc.player.rotationYaw);
		mc.player.rotationPitch = mc.player.rotationPitch
			+ MathHelper.wrapDegrees(pitch - mc.player.rotationPitch);
	}
	
	public static void faceBlockPacket(BlockPos blockPos)
	{
		double diffX = blockPos.getX() + 0.5 - mc.player.posX;
		double diffY =      //0.5
			blockPos.getY() + 0.0 - (mc.player.posY + mc.player.getEyeHeight());
		double diffZ = blockPos.getZ() + 0.5 - mc.player.posZ;
		double dist = MathHelper.sqrt(diffX * diffX + diffZ * diffZ);
		float yaw =
			(float)(Math.atan2(diffZ, diffX) * 180.0D / Math.PI) - 90.0F;
		float pitch = (float)-(Math.atan2(diffY, dist) * 180.0D / Math.PI);
		mc.player.connection.sendPacket(new CPacketPlayer.Rotation(
			mc.player.rotationYaw
				+ MathHelper.wrapDegrees(yaw - mc.player.rotationYaw),
			mc.player.rotationPitch
				+ MathHelper.wrapDegrees(pitch - mc.player.rotationPitch),
			mc.player.onGround));
	}
	
	public static void faceBlockClientHorizontally(BlockPos blockPos)
	{
		double diffX = blockPos.getX() + 0.5 - mc.player.posX;
		double diffZ = blockPos.getZ() + 0.5 - mc.player.posZ;
		float yaw =
			(float)(Math.atan2(diffZ, diffX) * 180.0D / Math.PI) - 90.0F;
		mc.player.rotationYaw = mc.player.rotationYaw
			+ MathHelper.wrapDegrees(yaw - mc.player.rotationYaw);
	}
	
	public static float getPlayerBlockDistance(BlockPos blockPos)
	{
		return getPlayerBlockDistance(blockPos.getX(), blockPos.getY(),
			blockPos.getZ());
	}
	
	public static float getPlayerBlockDistance(double posX, double posY,
		double posZ)
	{
		float xDiff = (float)(mc.player.posX - posX);
		float yDiff = (float)(mc.player.posY - posY);
		float zDiff = (float)(mc.player.posZ - posZ);
		return getBlockDistance(xDiff, yDiff, zDiff);
	}
	
	public static float getBlockDistance(float xDiff, float yDiff, float zDiff)
	{
		return MathHelper.sqrt(
			(xDiff - 0.5F) * (xDiff - 0.5F) + (yDiff - 0.5F) * (yDiff - 0.5F)
				+ (zDiff - 0.5F) * (zDiff - 0.5F));
	}
	
	public static float getHorizontalPlayerBlockDistance(BlockPos blockPos)
	{
		float xDiff = (float)(mc.player.posX - blockPos.getX());
		float zDiff = (float)(mc.player.posZ - blockPos.getZ());
		return MathHelper.sqrt(
			(xDiff - 0.5F) * (xDiff - 0.5F) + (zDiff - 0.5F) * (zDiff - 0.5F));
	}
	
	public static boolean breakBlockSimple(BlockPos pos)
	{
		EnumFacing side = null;
		EnumFacing[] sides = EnumFacing.values();
		
		Vec3d eyesPos = Utils.getEyesPos();
		Vec3d relCenter = getState(pos).getBoundingBox(Wrapper.INSTANCE.world(), pos).getCenter();
		Vec3d center = new Vec3d(pos).add(relCenter);
		
		Vec3d[] hitVecs = new Vec3d[sides.length];
		for(int i = 0; i < sides.length; i++)
		{
			Vec3i dirVec = sides[i].getDirectionVec();
			Vec3d relHitVec = new Vec3d(relCenter.x * dirVec.getX(),
					relCenter.y * dirVec.getY(),
					relCenter.z * dirVec.getZ());
			hitVecs[i] = center.add(relHitVec);
		}
		
		for(int i = 0; i < sides.length; i++)
		{
			if(Wrapper.INSTANCE.world().rayTraceBlocks(eyesPos, hitVecs[i], false,
				true, false) != null)
				continue;
			
			side = sides[i];
			break;
		}
		
		if(side == null)
		{
			double distanceSqToCenter = eyesPos.squareDistanceTo(center);
			for(int i = 0; i < sides.length; i++)
			{
				if(eyesPos.squareDistanceTo(hitVecs[i]) >= distanceSqToCenter)
					continue;
				
				side = sides[i];
				break;
			}
		}
		
		if(side == null)
			side = sides[0];
		
		Utils.faceVectorPacket(hitVecs[side.ordinal()]);
		
		if(!mc.playerController.onPlayerDamageBlock(pos, side))
			return false;
		Wrapper.INSTANCE.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
		
		return true;
	}
	
	public static void breakBlocksPacketSpam(Iterable<BlockPos> blocks)
	{
		Vec3d eyesPos = Utils.getEyesPos();
		NetHandlerPlayClient connection = Wrapper.INSTANCE.player().connection;
		
		for(BlockPos pos : blocks)
		{
			Vec3d posVec = new Vec3d(pos).addVector(0.5, 0.5, 0.5);
			double distanceSqPosVec = eyesPos.squareDistanceTo(posVec);
			
			for(EnumFacing side : EnumFacing.values())
			{
				Vec3d hitVec = posVec.add(new Vec3d(side.getDirectionVec()).scale(0.5));
				
				if(eyesPos.squareDistanceTo(hitVec) >= distanceSqPosVec)
					continue;
				
				connection.sendPacket(new CPacketPlayerDigging(
					Action.START_DESTROY_BLOCK, pos, side));
				connection.sendPacket(new CPacketPlayerDigging(
					Action.STOP_DESTROY_BLOCK, pos, side));
				
				break;
			}
		}
	}
	
	public static LinkedList<BlockPos> findBlocksNearEntity(EntityLivingBase entity, int blockId, int blockMeta, int distance) {	
		LinkedList<BlockPos> blocks = new LinkedList<BlockPos>();
		
		for (int x = (int) Wrapper.INSTANCE.player().posX - distance; x <= (int) Wrapper.INSTANCE.player().posX + distance; ++x) {
            for (int z = (int) Wrapper.INSTANCE.player().posZ - distance; z <= (int) Wrapper.INSTANCE.player().posZ + distance; ++z) {
            	
                int height = Wrapper.INSTANCE.world().getHeight(x, z); 
                block: for (int y = 0; y <= height; ++y) {
                	
                	BlockPos blockPos = new BlockPos(x, y, z);
                	IBlockState blockState = Wrapper.INSTANCE.world().getBlockState(blockPos);
                	
                	if(blockId == -1 || blockMeta == -1) {
                		blocks.add(blockPos);
            			continue block;
                	}
                	
                		int id = Block.getIdFromBlock(blockState.getBlock());
                		int meta =  blockState.getBlock().getMetaFromState(blockState);
                		
                		if(id == blockId && meta == blockMeta) {
                			
                			blocks.add(blockPos);
                			continue block;
                		}
                		
                	}
                }
            }
		return blocks;
	}
	
}
