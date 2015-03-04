package de.teamlapen.vampirism.tileEntity;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockCompressed;
import net.minecraft.block.BlockStoneBrick;
import net.minecraft.block.material.MapColor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;
import net.minecraftforge.common.MinecraftForge;
import de.teamlapen.vampirism.ModBlocks;
import de.teamlapen.vampirism.VampirismMod;
import de.teamlapen.vampirism.block.BlockBloodAltarTier4;
import de.teamlapen.vampirism.block.BlockBloodAltarTier4Tip;
import de.teamlapen.vampirism.client.gui.VampireHudOverlay;
import de.teamlapen.vampirism.entity.player.VampirePlayer;
import de.teamlapen.vampirism.item.ItemBloodBottle;
import de.teamlapen.vampirism.network.SpawnCustomParticlePacket;
import de.teamlapen.vampirism.util.Logger;

/**
 * Tileentity used for BloodAltarTier4
 * 
 * @author Max
 *
 */
public class TileEntityBloodAltarTier4 extends InventoryTileEntity {

	private final static String TAG = "TEBAltar4";
	private final static int[][][] structure1 = new int[][][] { { { 1, 0, 0, 0, 1 }, { 0, 0, 0, 0, 0 }, { 0, 0, 3, 0, 0 }, { 0, 0, 4, 0, 0 }, { 1, 0, 4, 0, 1 } },
			{ { 1, 0, 0, 0, 1 }, { 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0 }, { 1, 0, 0, 0, 1 } },
			{ { 2, 0, 0, 0, 2 }, { 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0 }, { 2, 0, 0, 0, 2 } } };
	private final static int[][][] structure2 = new int[][][] { { { 0, 1, 0, 0, 0, 1, 0 }, { 0, 0, 0, 0, 0, 0, 0 }, { 1, 0, 0, 3, 0, 0, 1 }, { 0, 0, 0, 4, 0, 0, 0 }, { 0, 1, 0, 4, 0, 1, 0 } },
			{ { 0, 1, 0, 0, 0, 1, 0 }, { 0, 0, 0, 0, 0, 0, 0 }, { 1, 0, 0, 0, 0, 0, 1 }, { 0, 0, 0, 0, 0, 0, 0 }, { 0, 1, 0, 0, 0, 1, 0 } },
			{ { 0, 1, 0, 0, 0, 1, 0 }, { 0, 0, 0, 0, 0, 0, 0 }, { 1, 0, 0, 0, 0, 0, 1 }, { 0, 0, 0, 0, 0, 0, 0 }, { 0, 1, 0, 0, 0, 1, 0 } },
			{ { 0, 2, 0, 0, 0, 2, 0 }, { 0, 0, 0, 0, 0, 0, 0 }, { 2, 0, 0, 0, 0, 0, 2 }, { 0, 0, 0, 0, 0, 0, 0 }, { 0, 2, 0, 0, 0, 2, 0 } } };

	private int runningTick;
	private final int DURATION_TICK=450;
	/**
	 * Only available when running ({@link #runningTick}>0
	 */
	private EntityPlayer player;
	/**
	 * Only available when running ({@link #runningTick}>0
	 */
	private ChunkCoordinates[] tips;
	public TileEntityBloodAltarTier4() {
		super(new Slot[] { new Slot(ItemBloodBottle.class,56, 17), new Slot(Items.diamond,56, 53), new Slot(116, 35)});
	}

	/**
	 * Checks if the blocks in the given box match to the structure description. If it matches it returns the block at the positions marked with 1 in the structure, otherwise its null
	 * 
	 * @param lx
	 *            Lower x
	 * @param ly
	 *            Lower y
	 * @param lz
	 *            Lower z
	 * @param hx
	 *            Higher x
	 * @param hy
	 *            Higher y
	 * @param hz
	 *            Higher z
	 * @param structure
	 * @return
	 */
	private Block checkBlocks(int lx, int ly, int lz, int hx, int hy, int hz, int[][][] structure) {
		Logger.i(TAG, "0.length: " + structure.length + ":" + structure[0].length + ":" + structure[0][0].length);
		Block blocktype = null;
		for (int x = lx; x <= hx; x++) {
			for (int z = lz; z <= hz; z++) {
				for (int y = ly; y <= hy; y++) {
					int type = structure[y - ly][z - lz][x - lx];
					Block b = worldObj.getBlock(x, y, z);
					//Logger.i("Test", "T:" + x + ":" + y + ":" + z + ";" + (x - lx) + ":" + (y - ly) + ":" + (z - lz) + ";" + type + ";" + b.getUnlocalizedName());
					if (type == 0) {
						if (!(b instanceof BlockAir)) {
							Logger.i(TAG, "Expected " + type + " found: " + b.getUnlocalizedName() + " at " + (x - lx) + ":" + (y - ly) + ":" + (z - lz));
							return null;
						}
					}
					if (type == 2) {
						if (!(b instanceof BlockBloodAltarTier4Tip)) {
							Logger.i(TAG, "Expected " + type + " found: " + b.getUnlocalizedName() + " at " + (x - lx) + ":" + (y - ly) + ":" + (z - lz));
							return null;
						}
					}
					if (type == 3) {
						if (!(b instanceof BlockBloodAltarTier4)) {
							Logger.i(TAG, "Expected " + type + " found: " + b.getUnlocalizedName() + " at " + (x - lx) + ":" + (y - ly) + ":" + (z - lz));
							return null;
						}
					}
					if (type == 4) {
						if (!(b instanceof BlockBed)) {
							Logger.i(TAG, "Expected " + type + " found: " + b.getUnlocalizedName() + " at " + (x - lx) + ":" + (y - ly) + ":" + (z - lz));
							return null;
						}
					}
					if (type == 1) {
						if (blocktype != null && !blocktype.equals(b)) {
							Logger.i(TAG, "Expected " + type + " found: " + b.getUnlocalizedName() + " at " + (x - lx) + ":" + (y - ly) + ":" + (z - lz));
							return null;
						}
						blocktype = b;
					}

				}
			}
		}
		return blocktype;
	}

	/**
	 * Determines the level of the structure build around the altar. TODO make it rotatable
	 * 
	 * @return
	 */
	private int determineLevel() {

		int level = 0;
		int x = this.xCoord;
		int y = this.yCoord;
		int z = this.zCoord;
		// setBlocks(x-2,y,z-2,x+2,y+2,z+2,structure1);
		int meta = worldObj.getBlockMetadata(x, y, z);
		Logger.i(TAG, "Meta: " + meta);
		Block type = null;
		Logger.i(TAG, "Testing structure 2");
		type = checkBlocks(x - 3, y, z - 2, x + 3, y + 3, z + 2, structure2);
		if (type != null) {
			if (type instanceof BlockCompressed) {
				if (((BlockCompressed) type).getMapColor(1).equals(MapColor.ironColor)) {
					return 3;
				}
				if (((BlockCompressed) type).getMapColor(1).equals(MapColor.goldColor)) {
					return 4;
				}
			}
		}
		Logger.i(TAG, "Testing structure 1");
		type = checkBlocks(x - 2, y, z - 2, x + 2, y + 2, z + 2, structure1);
		if (type != null) {
			if (type instanceof BlockStoneBrick) {
				return 1;
			}
			if (type instanceof BlockCompressed) {
				if (((BlockCompressed) type).getMapColor(1).equals(MapColor.ironColor)) {
					return 2;
				}
			}
		}
		return 0;

	}

	@Override
	public String getInventoryName() {
		return "Items-test-inventory-name";
	}

	@Override
	public boolean hasCustomInventoryName() {
		return true;
	}

	/**
	 * test method to set the blocks
	 * 
	 * @param lx
	 * @param ly
	 * @param lz
	 * @param hx
	 * @param hy
	 * @param hz
	 * @param structure
	 */
	private void setBlocks(int lx, int ly, int lz, int hx, int hy, int hz, int[][][] structure) {

		Block blocktype = null;
		for (int x = lx; x <= hx; x++) {
			for (int z = lz; z <= hz; z++) {
				for (int y = ly; y < hy; y++) {
					int type = structure[y - ly][z - lz][x - lx];
					if (type == 0) {
						worldObj.setBlock(x, y, z, Blocks.air);
					}
					if (type == 2) {
						worldObj.setBlock(x, y, z, ModBlocks.bloodAltarTier4Tip);
					}
					if (type == 3) {
						worldObj.setBlock(x, y, z, ModBlocks.bloodAltarTier4);
					}
					if (type == 4) {
						worldObj.setBlock(x, y, z, Blocks.bed);
					}
					if (type == 1) {
						worldObj.setBlock(x, y, z, Blocks.stonebrick);
					}

				}
			}
		}
	}
	
	public void onBlockActivated(EntityPlayer player){
		Logger.i("test", "RitualActivated");
		if(runningTick>0){
			Logger.i("test", "still running");
			return;
		}
		int sl=this.determineLevel();
		
		/* TODO enable egain
		LevReq result=checkLevelRequirement(player,sl);
		Logger.i(TAG, "SL: "+sl+" Result: "+result);//TODO user feedback
		if(result!=LevReq.OK)return;
		*/
		runningTick=DURATION_TICK;
		this.player=player;
		tips=getTips(sl);
		for(int i=0;i<tips.length;i++){
			NBTTagCompound data = new NBTTagCompound();
			data.setInteger("destX", tips[i].posX);
			data.setInteger("destY", tips[i].posY);
			data.setInteger("destZ", tips[i].posZ);
			data.setInteger("age", 100);
			VampirismMod.modChannel.sendToAll(new SpawnCustomParticlePacket(1, this.xCoord, this.yCoord, this.zCoord, 5, data));
		}
		
		
		
		
	}
	
	@Override
	public void updateEntity(){
		runningTick--;
		if(runningTick<=0)return;
		if(player.isDead){
			runningTick=1;
		}
		PHASE phase=getPhase();
		if(this.worldObj.isRemote){
			if(phase.equals(PHASE.PARTICLE_SPREAD)){
				if(runningTick%15==0){
					for(int i=0;i<tips.length;i++){
						NBTTagCompound data = new NBTTagCompound();
						data.setInteger("destX", tips[i].posX);
						data.setInteger("destY", tips[i].posY);
						data.setInteger("destZ", tips[i].posZ);
						data.setInteger("age", 60);
						VampirismMod.modChannel.sendToAll(new SpawnCustomParticlePacket(1, this.xCoord, this.yCoord, this.zCoord, 5, data));
					}
				}
			}
		}
		else{
			if(phase.equals(PHASE.BEAM2)){
				VampireHudOverlay.setRenderRed(1F-((float)runningTick-50)/((float)(DURATION_TICK-250))*1F);
			}
			if(phase.equals(PHASE.ENDING)){
				VampireHudOverlay.setRenderRed((float)runningTick/50.0F);
			}
			if(phase.equals(PHASE.CLEAN_UP)){
				VampireHudOverlay.setRenderRed(0F);
			}
		}
		if(phase.equals(PHASE.CLEAN_UP)){
			player=null;
			tips=null;
		}
		if(phase.equals(PHASE.LEVELUP)){
			VampirePlayer.get(player).levelUp();
			if(this.worldObj.isRemote){
				this.worldObj.spawnParticle("hugeexplosion", player.posX, player.posY, player.posZ, 1.0D, 0.0D, 0.0D);
			}
		}
	}
	
	private LevReq checkLevelRequirement(EntityPlayer player,int sl){
		if(sl==0)return LevReq.STRUCTURE_WRONG;
		int slot=1;
		ItemStack stack=this.getStackInSlot(slot);
		if(stack==null)return LevReq.ITEM_MISSING;
		int amt=stack.stackSize;
		int pl=VampirePlayer.get(player).getLevel();
		if(pl<4||pl>10)return LevReq.LEVEL_WRONG;
		if(pl==4){
			if(sl!=1)return LevReq.STRUCTURE_WRONG;
			if(amt<5)return LevReq.ITEM_MISSING;
			amt=5;
		}
		else if(pl==5){
			if(sl!=1)return LevReq.STRUCTURE_WRONG;
			if(amt<10)return LevReq.ITEM_MISSING;
			amt=10;
		}
		else if(pl==6){
			if(sl!=2)return LevReq.STRUCTURE_WRONG;
			if(amt<10)return LevReq.ITEM_MISSING;
			amt=10;
		}
		else if(pl==7){
			if(sl!=2)return LevReq.STRUCTURE_WRONG;
			if(amt<15)return LevReq.ITEM_MISSING;
			amt=15;
		}
		else if(pl==8){
			if(sl!=3)return LevReq.STRUCTURE_WRONG;
			if(amt<15)return LevReq.ITEM_MISSING;
			amt=15;
		}
		else if(pl==9){
			if(sl!=3)return LevReq.STRUCTURE_WRONG;
			if(amt<20)return LevReq.ITEM_MISSING;
			amt=20;
		}
		else if(pl==10){
			if(sl!=4)return LevReq.STRUCTURE_WRONG;
			if(amt<20)return LevReq.ITEM_MISSING;
			amt=20;
		}
		this.decrStackSize(slot, amt);
		return LevReq.OK;

	}
	
	private static enum LevReq{
		OK,STRUCTURE_WRONG,ITEM_MISSING,LEVEL_WRONG;
	}
	
	/**
	 * Searchs for the tips and returns their position
	 * @param level
	 * @return
	 */
	private ChunkCoordinates[] getTips(int level){
		ArrayList<ChunkCoordinates> coord=new ArrayList<ChunkCoordinates>();
		int lx = this.xCoord-3;
		int ly = this.yCoord;
		int lz = this.zCoord-3;
		int hx=lx+6;
		int hy=ly+4;
		int hz=lz+6;
		
		for (int x = lx; x <= hx; x++) {
			for (int z = lz; z <= hz; z++) {
				for (int y = ly; y <= hy; y++) {
					if(worldObj.getBlock(x, y, z).equals(ModBlocks.bloodAltarTier4Tip)){
						coord.add(new ChunkCoordinates(x,y,z));
					}
				}
			}
		}
		Logger.i("test", "found "+coord.size());
		return coord.toArray(new ChunkCoordinates[coord.size()]);
	}
	
	/**
	 * Returns the position of the tips. If the ritual isn't running it returns null
	 * @return
	 */
	public ChunkCoordinates[] getTips(){
		if(this.runningTick<=1)return null;
		return this.tips;
	}
	
	/**
	 * Returns the affected player. If the ritual isn't running it returns null
	 * @return
	 */
	public EntityPlayer getPlayer(){
		if(this.runningTick<=1)return null;
		return this.player;
	}
	
	public PHASE getPhase(){
		if(runningTick<1){
			return PHASE.NOT_RUNNING;
		}
		if(runningTick==1){
			return PHASE.CLEAN_UP;
		}
		if(runningTick>(DURATION_TICK-100)){
			return PHASE.PARTICLE_SPREAD;
		}
		if(runningTick<DURATION_TICK-160&&runningTick>=(DURATION_TICK-200)){
			return PHASE.BEAM1;
		}
		if(runningTick<(DURATION_TICK-200)&&(runningTick>50)){
			return PHASE.BEAM2;
		}
		if(runningTick==50){
			return PHASE.LEVELUP;
		}
		if(runningTick<50){
			return PHASE.ENDING;
		}
		return PHASE.WAITING;
	}
	
	public static enum PHASE{
		NOT_RUNNING,PARTICLE_SPREAD,BEAM1,BEAM2,WAITING,LEVELUP,ENDING,CLEAN_UP;
	}
	
	public int getRunningTick(){
		return runningTick;
	}
	
}